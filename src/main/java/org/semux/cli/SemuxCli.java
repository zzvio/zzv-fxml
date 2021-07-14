/**
 * Copyright (c) 2017-2020 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.cli;

import java.io.File;
import java.io.IOException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.semux.Kernel;
import org.semux.Launcher;
import org.semux.config.Config;
import org.semux.config.Constants;
import org.semux.config.exception.ConfigException;
import org.semux.core.BlockchainImpl;
import org.semux.core.Genesis;
import org.semux.core.Wallet;
import org.semux.core.exception.WalletLockedException;
import org.semux.crypto.Hex;
import org.semux.crypto.Key;
import org.semux.crypto.bip39.MnemonicGenerator;
import org.semux.db.DatabaseFactory;
import org.semux.db.LeveldbDatabase;
import org.semux.exception.LauncherException;
import org.semux.message.CliMessages;
import org.semux.net.filter.exception.IpFilterJsonParseException;
import org.semux.util.ConsoleUtil;
import org.semux.util.SystemUtil;
import org.semux.util.TimeUtil;

/**
 * Semux command line interface.
 */
public class SemuxCli extends Launcher {

    public static final boolean ENABLE_HD_WALLET_BY_DEFAULT = false;

    private static final Logger logger = Logger.getLogger(SemuxCli.class.getName());

    public static void main(String[] args, SemuxCli cli) {
        try {
//            // check jvm version
//            if (SystemUtil.is32bitJvm()) {
//                logger.severe(CliMessages.get("Jvm32NotSupported"));
//                SystemUtil.exit(SystemUtil.Code.JVM_32_NOT_SUPPORTED);
//            }
//
//            // system system prerequisites
//            checkPrerequisite();

            // start CLI
            cli.parseOptionsAndSetUpLogging(args);
            cli.start(args);

        } catch (LauncherException | ConfigException | IpFilterJsonParseException | IOException exception) {
            logger.severe(exception.getMessage());
        } catch (ParseException exception) {
            logger.severe(CliMessages.get("ParsingFailed", exception.getMessage()));
        }
    }

    public static void main(String[] args) {
        main(args, new SemuxCli());
    }

    /**
     * Creates a new Semux CLI instance.
     */
    public SemuxCli() {
        SystemUtil.setLocale(getConfig().uiLocale());

        Option helpOption = Option.builder()
                .longOpt(SemuxOption.HELP.toString())
                .desc(CliMessages.get("PrintHelp"))
                .build();
        addOption(helpOption);

        Option versionOption = Option.builder()
                .longOpt(SemuxOption.VERSION.toString())
                .desc(CliMessages.get("ShowVersion"))
                .build();
        addOption(versionOption);

        Option accountOption = Option.builder()
                .longOpt(SemuxOption.ACCOUNT.toString())
                .desc(CliMessages.get("ChooseAction"))
                .hasArg(true).numberOfArgs(1).optionalArg(false).argName("action").type(String.class)
                .build();
        addOption(accountOption);

        Option changePasswordOption = Option.builder()
                .longOpt(SemuxOption.CHANGE_PASSWORD.toString()).desc(CliMessages.get("ChangeWalletPassword")).build();
        addOption(changePasswordOption);

        Option dumpPrivateKeyOption = Option.builder()
                .longOpt(SemuxOption.DUMP_PRIVATE_KEY.toString())
                .desc(CliMessages.get("PrintHexKey"))
                .hasArg(true).optionalArg(false).argName("address").type(String.class)
                .build();
        addOption(dumpPrivateKeyOption);

        Option importPrivateKeyOption = Option.builder()
                .longOpt(SemuxOption.IMPORT_PRIVATE_KEY.toString())
                .desc(CliMessages.get("ImportHexKey"))
                .hasArg(true).optionalArg(false).argName("key").type(String.class)
                .build();
        addOption(importPrivateKeyOption);

        Option reindexOption = Option.builder()
                .longOpt(SemuxOption.REINDEX.toString())
                .desc(CliMessages.get("ReindexDescription"))
                .hasArg(true).optionalArg(true).argName("to").type(String.class)
                .build();
        addOption(reindexOption);

    }

    public void start(String[] args) throws ParseException, IOException {
        // parse common options
        CommandLine cmd = parseOptions(args);

        // parse remaining options
        if (cmd.hasOption(SemuxOption.HELP.toString())) {
            printHelp();

        } else if (cmd.hasOption(SemuxOption.VERSION.toString())) {
            printVersion();

        } else if (cmd.hasOption(SemuxOption.ACCOUNT.toString())) {
            String action = cmd.getOptionValue(SemuxOption.ACCOUNT.toString()).trim();
            if ("create".equals(action)) {
                createAccount();
            } else if ("list".equals(action)) {
                listAccounts();
            }

        } else if (cmd.hasOption(SemuxOption.CHANGE_PASSWORD.toString())) {
            changePassword();

        } else if (cmd.hasOption(SemuxOption.DUMP_PRIVATE_KEY.toString())) {
            dumpPrivateKey(cmd.getOptionValue(SemuxOption.DUMP_PRIVATE_KEY.toString()).trim());

        } else if (cmd.hasOption(SemuxOption.IMPORT_PRIVATE_KEY.toString())) {
            importPrivateKey(cmd.getOptionValue(SemuxOption.IMPORT_PRIVATE_KEY.toString()).trim());

        } else if (cmd.hasOption(SemuxOption.REINDEX.toString())) {
            reindex(cmd.getOptionValue(SemuxOption.REINDEX.toString()));

        } else {
            start();
        }
    }

    protected void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(200);
        formatter.printHelp("./semux-cli.sh [options]", getOptions());
    }

    protected void printVersion() {
        System.out.println(Constants.CLIENT_VERSION);
    }

    protected void reindex(String to) {
        Config config = getConfig();
        DatabaseFactory dbFactory = new LeveldbDatabase.LeveldbFactory(config.chainDir());
        BlockchainImpl.upgrade(config, dbFactory, to == null ? Long.MAX_VALUE : Long.parseLong(to));
    }

    private Kernel kernel;
    protected void start() throws IOException {
        // create/unlock wallet
        Wallet wallet = loadWallet().exists() ? loadAndUnlockWallet() : createNewWallet();
        if (wallet == null) {
            return;
        }

        // check file permissions
        if (SystemUtil.isPosix()) {
            if (!wallet.isPosixPermissionSecured()) {
                logger.warning(CliMessages.get("WarningWalletPosixPermission"));
            }
        }

        // check time drift
        long timeDrift = TimeUtil.getTimeOffsetFromNtp();
        if (Math.abs(timeDrift) > 5000L) {
            logger.warning(CliMessages.get("SystemTimeDrift"));
        }

        // in case HD wallet is enabled, make sure the seed is properly initialized.
        if (isHdWalletEnabled().orElse(ENABLE_HD_WALLET_BY_DEFAULT)) {
            if (!wallet.isHdWalletInitialized()) {
                initializedHdSeed(wallet);
            }
        }

        // create a new account if the wallet is empty
        List<Key> accounts = wallet.getAccounts();
        if (accounts.isEmpty()) {
            Key key;
            if (isHdWalletEnabled().orElse(ENABLE_HD_WALLET_BY_DEFAULT)) {
                key = wallet.addAccountWithNextHdKey();
            } else {
                key = wallet.addAccountRandom();
            }
            wallet.flush();

            accounts = wallet.getAccounts();
            logger.info(CliMessages.get("NewAccountCreatedForAddress", key.toAddressString()));
        }

        // check coinbase if the user specifies one
        int coinbase = getCoinbase() == null ? 0 : getCoinbase();
        if (coinbase < 0 || coinbase >= accounts.size()) {
            logger.warning(CliMessages.get("CoinbaseDoesNotExist"));
            exit(SystemUtil.Code.ACCOUNT_NOT_EXIST);
            return;
        }

        // start kernel
        try {
            kernel = startKernel(getConfig(), wallet, wallet.getAccount(coinbase));
        } catch (Exception e) {
            logger.warning(String.format("Uncaught exception during kernel startup.", e));
            exit(SystemUtil.Code.FAILED_TO_LAUNCH_KERNEL);
        }
    }

    public synchronized void stop(){
    if (kernel != null) {
        kernel.stop();
        }
    }
    /**
     * Starts the kernel.
     */
    protected Kernel startKernel(Config config, Wallet wallet, Key coinbase) {
        Kernel kernel = new Kernel(config, Genesis.load(config.network()), wallet, coinbase);
        kernel.start();

        return kernel;
    }

    protected void createAccount() {
        Wallet wallet = loadAndUnlockWallet();

        Key key;
        if (isHdWalletEnabled().orElse(ENABLE_HD_WALLET_BY_DEFAULT)) {
            key = wallet.addAccountWithNextHdKey();
        } else {
            key = wallet.addAccountRandom();
        }

        if (wallet.flush()) {
            logger.info(CliMessages.get("NewAccountCreatedForAddress", key.toAddressString()));
            logger.info(CliMessages.get("PublicKey", Hex.encode(key.getPublicKey())));
        }
    }

    protected void listAccounts() {
        Wallet wallet = loadAndUnlockWallet();

        List<Key> accounts = wallet.getAccounts();

        if (accounts.isEmpty()) {
            logger.info(CliMessages.get("AccountMissing"));
        } else {
            for (int i = 0; i < accounts.size(); i++) {
                logger.info(CliMessages.get("ListAccountItem", i, accounts.get(i).toString()));
            }
        }
    }

    protected void changePassword() {
        Wallet wallet = loadAndUnlockWallet();

        try {
            String newPassword = readNewPassword("EnterNewPassword", "ReEnterNewPassword");
            if (newPassword == null) {
                return;
            }

            wallet.changePassword(newPassword);
            boolean isFlushed = wallet.flush();
            if (!isFlushed) {
                logger.warning(CliMessages.get("WalletFileCannotBeUpdated"));
                exit(SystemUtil.Code.FAILED_TO_WRITE_WALLET_FILE);
                return;
            }

            logger.info(CliMessages.get("PasswordChangedSuccessfully"));
        } catch (WalletLockedException exception) {
            logger.warning(exception.getMessage());
        }
    }

    protected void exit(int code) {
        SystemUtil.exit(code);
    }

    protected String readPassword() {
        return ConsoleUtil.readPassword();
    }

    protected String readPassword(String prompt) {
        return ConsoleUtil.readPassword(prompt);
    }

    /**
     * Read a new password from input and require confirmation
     *
     * @return new password, or null if the confirmation failed
     */
    protected String readNewPassword(String newPasswordMessageKey, String reEnterNewPasswordMessageKey) {
        String newPassword = readPassword(CliMessages.get(newPasswordMessageKey));
        String newPasswordRe = readPassword(CliMessages.get(reEnterNewPasswordMessageKey));

        if (!newPassword.equals(newPasswordRe)) {
            logger.severe(CliMessages.get("ReEnterNewPasswordIncorrect"));
            exit(SystemUtil.Code.PASSWORD_REPEAT_NOT_MATCH);
            return null;
        }

        return newPassword;
    }

    protected void dumpPrivateKey(String address) {
        Wallet wallet = loadAndUnlockWallet();

        byte[] addressBytes = Hex.decode0x(address);
        Key account = wallet.getAccount(addressBytes);
        if (account == null) {
            logger.severe(CliMessages.get("AddressNotInWallet"));
            exit(SystemUtil.Code.ACCOUNT_NOT_EXIST);
        } else {
            System.out.println(CliMessages.get("PrivateKeyIs", Hex.encode(account.getPrivateKey())));
        }
    }

    protected void importPrivateKey(String key) {
        try {
            Wallet wallet = loadAndUnlockWallet();
            byte[] keyBytes = Hex.decode0x(key);
            Key account = new Key(keyBytes);

            boolean accountAdded = wallet.addAccount(account);
            if (!accountAdded) {
                logger.severe(CliMessages.get("PrivateKeyAlreadyInWallet"));
                exit(SystemUtil.Code.ACCOUNT_ALREADY_EXISTS);
                return;
            }

            boolean walletFlushed = wallet.flush();
            if (!walletFlushed) {
                logger.severe(CliMessages.get("WalletFileCannotBeUpdated"));
                exit(SystemUtil.Code.FAILED_TO_WRITE_WALLET_FILE);
                return;
            }

            logger.info(CliMessages.get("PrivateKeyImportedSuccessfully"));
            logger.info(CliMessages.get("Address", account.toAddressString()));
            logger.info(CliMessages.get("PublicKey", Hex.encode(account.getPublicKey())));
        } catch (InvalidKeySpecException exception) {
            logger.severe(CliMessages.get("PrivateKeyCannotBeDecoded", exception.getMessage()));
            exit(SystemUtil.Code.INVALID_PRIVATE_KEY);
        } catch (WalletLockedException exception) {
            logger.severe(exception.getMessage());
            exit(SystemUtil.Code.WALLET_LOCKED);
        }
    }

    protected Wallet loadAndUnlockWallet() {
        Wallet wallet = loadWallet();
        if (getPassword() == null) {
            if (wallet.unlock("")) {
                setPassword("");
            } else {
                setPassword(readPassword());
            }
        }

        if (!wallet.unlock(getPassword())) {
            logger.severe("Invalid password");
            exit(SystemUtil.Code.FAILED_TO_UNLOCK_WALLET);
        }

        return wallet;
    }

    /**
     * Create a new wallet with a new password from input and save the wallet file
     * to disk
     *
     * @return created new wallet, or null if it failed to create the wallet
     */
    protected Wallet createNewWallet() {
        String newPassword = readNewPassword("EnterNewPassword", "ReEnterNewPassword");
        if (newPassword == null) {
            return null;
        }

        setPassword(newPassword);
        Wallet wallet = loadWallet();
        if (!wallet.unlock(newPassword) || !wallet.flush()) {
            logger.severe("CreateNewWalletError");
            exit(SystemUtil.Code.FAILED_TO_WRITE_WALLET_FILE);
            return null;
        }

        return wallet;
    }

    protected Wallet loadWallet() {
        File file = new File(getConfig().walletDir(), Constants.WALLET_FILE);
        return new Wallet(file, getConfig().network());
    }

    protected void initializedHdSeed(Wallet wallet) {
        if (wallet.isUnlocked() && !wallet.isHdWalletInitialized()) {
            // HD Mnemonic
            System.out.println(CliMessages.get("HdWalletInitialize"));
            MnemonicGenerator generator = new MnemonicGenerator();
            String phrase = generator.getWordlist(Wallet.MNEMONIC_ENTROPY_LENGTH, Wallet.MNEMONIC_LANGUAGE);
            System.out.println(CliMessages.get("HdWalletMnemonic", phrase));

            String repeat = ConsoleUtil.readLine(CliMessages.get("HdWalletMnemonicRepeat"));
            repeat = String.join(" ", repeat.trim().split("\\s+"));

            if (!repeat.equals(phrase)) {
                logger.severe(CliMessages.get("HdWalletInitializationFailure"));
                SystemUtil.exit(SystemUtil.Code.FAILED_TO_INIT_HD_WALLET);
                return;
            }

            wallet.initializeHdWallet(phrase);
            wallet.flush();
            logger.info(CliMessages.get("HdWalletInitializationSuccess"));
        }
    }
}
