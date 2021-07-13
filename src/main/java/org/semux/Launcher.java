/**
 * Copyright (c) 2017-2020 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux;

import static org.semux.Network.MAINNET;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.tuple.Pair;
import org.semux.cli.SemuxOption;
import org.semux.config.Config;
import org.semux.config.Constants;
import org.semux.config.DevnetConfig;
import org.semux.config.MainnetConfig;
import org.semux.config.TestnetConfig;
import org.semux.event.PubSubFactory;
import org.semux.exception.LauncherException;
import org.semux.log.LoggerConfigurator;
import org.semux.message.CliMessages;
import org.semux.util.SystemUtil;
import org.semux.util.exception.UnreachableException;

public abstract class Launcher {

    private static final Logger logger = Logger.getLogger(Launcher.class.getName());

    private static final String ENV_SEMUX_WALLET_PASSWORD = "SEMUX_WALLET_PASSWORD";

    /**
     * Here we make sure that all shutdown hooks will be executed in the order of
     * registration. This is necessary to be manually maintained because
     * ${@link Runtime#addShutdownHook(Thread)} starts shutdown hooks concurrently
     * in unspecified order.
     */
    private static final List<Pair<String, Runnable>> shutdownHooks = Collections.synchronizedList(new ArrayList<>());

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(Launcher::shutdownHook, "shutdown-hook"));
    }

    private final Options options = new Options();

    private String rootDir = Constants.DEFAULT_ROOT_DIR;
    private Network network = MAINNET;

    private Integer coinbase = null;
    private String password = null;

    private Boolean hdWalletEnabled = null;

    public Launcher() {
        Option dataDirOption = Option.builder()
                .longOpt(SemuxOption.DATA_DIR.toString())
                .desc(CliMessages.get("SpecifyDataDir"))
                .hasArg(true).numberOfArgs(1).optionalArg(false).argName("path").type(String.class)
                .build();
        addOption(dataDirOption);

        Option networkOption = Option.builder()
                .longOpt(SemuxOption.NETWORK.toString())
                .desc(CliMessages.get("SpecifyNetwork"))
                .hasArg(true).numberOfArgs(1).optionalArg(false).argName("name").type(String.class)
                .build();
        addOption(networkOption);

        Option logOption = Option.builder()
                .longOpt(SemuxOption.LOG.toString())
                .desc("Log Level = default OFF")
                .hasArg(true).numberOfArgs(1).optionalArg(false).argName("name").type(String.class)
                .build();
        addOption(logOption);

        Option coinbaseOption = Option.builder()
                .longOpt(SemuxOption.COINBASE.toString())
                .desc(CliMessages.get("SpecifyCoinbase"))
                .hasArg(true).numberOfArgs(1).optionalArg(false).argName("index").type(Number.class)
                .build();
        addOption(coinbaseOption);

        Option passwordOption = Option.builder()
                .longOpt(SemuxOption.PASSWORD.toString())
                .desc(CliMessages.get("WalletPassword"))
                .hasArg(true).numberOfArgs(1).optionalArg(false).argName("password").type(String.class)
                .build();
        addOption(passwordOption);

        Option hdOption = Option.builder()
                .longOpt(SemuxOption.HD_WALLET.toString())
                .desc(CliMessages.get("SpecifyHDWallet"))
                .hasArg(true).numberOfArgs(1).optionalArg(false).argName("hd").type(Boolean.class)
                .build();
        addOption(hdOption);
    }

    /**
     * Creates an instance of {@link Config} based on the given `--network` option.
     * <p>
     * Defaults to MainNet.
     *
     * @return the configuration
     */
    public Config getConfig() {
        switch (getNetwork()) {
        case MAINNET:
            return new MainnetConfig(rootDir);
        case TESTNET:
            return new TestnetConfig(rootDir);
        case DEVNET:
            return new DevnetConfig(rootDir);
        default:
            throw new UnreachableException();
        }
    }

    /**
     * Returns the network.
     *
     * @return
     */
    public Network getNetwork() {
        return network;
    }

    /**
     * Returns the coinbase.
     *
     * @return The specified coinbase, or NULL
     */
    public Integer getCoinbase() {
        return coinbase;
    }

    /**
     * Returns the provided password if any.
     *
     * @return The specified password, or NULL
     */
    public String getPassword() {
        return password;
    }

    /**
     * Parses options from the given arguments.
     *
     * @param args
     * @return
     * @throws ParseException
     */
    protected CommandLine parseOptions(String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(getOptions(), args);

        if (cmd.hasOption(SemuxOption.DATA_DIR.toString())) {
            setRootDir(cmd.getOptionValue(SemuxOption.DATA_DIR.toString()));
        }

        if (cmd.hasOption(SemuxOption.NETWORK.toString())) {
            String option = cmd.getOptionValue(SemuxOption.NETWORK.toString());
            Network net = Network.of(option);
            if (net == null) {
                logger.severe(String.format("Invalid network label: %s", option));
                SystemUtil.exit(SystemUtil.Code.INVALID_NETWORK_LABEL);
            } else {
                setNetwork(net);
            }
        }

        if ( cmd.hasOption(SemuxOption.LOG.toString())){
            String option = cmd.getOptionValue(SemuxOption.LOG.toString());
            Logger rootLogger = LogManager.getLogManager().getLogger("");
            Level level = Level.parse(option);
            rootLogger.setLevel(level);
            for (Handler h : rootLogger.getHandlers()) {
                h.setLevel(level);
            }
        }

        if (cmd.hasOption(SemuxOption.COINBASE.toString())) {
            setCoinbase(((Number) cmd.getParsedOptionValue(SemuxOption.COINBASE.toString())).intValue());
        }

        // Priority: arguments => system property => console input
        if (cmd.hasOption(SemuxOption.PASSWORD.toString())) {
            setPassword(cmd.getOptionValue(SemuxOption.PASSWORD.toString()));
        } else if (System.getenv(ENV_SEMUX_WALLET_PASSWORD) != null) {
            setPassword(System.getenv(ENV_SEMUX_WALLET_PASSWORD));
        }

        if (cmd.hasOption(SemuxOption.HD_WALLET.toString())) {
            setHdWalletEnabled(Boolean.parseBoolean(cmd.getOptionValue(SemuxOption.HD_WALLET.toString())));
        }

        return cmd;
    }

    /**
     * Set up customized logger configuration.
     *
     * @param args
     * @throws ParseException
     */
    protected void parseOptionsAndSetUpLogging(String[] args) throws ParseException {
        // parse options
        parseOptions(args);

        LoggerConfigurator.configure(getConfig());
    }

    /**
     * Set up pubsub service.
     */
    protected void setupPubSub() {
        PubSubFactory.getDefault().start();
        registerShutdownHook("pubsub-default", () -> PubSubFactory.getDefault().stop());
    }

    /**
     * Returns all supported options.
     *
     * @return
     */
    protected Options getOptions() {
        return options;
    }

    /**
     * Adds a supported option.
     *
     * @param option
     */
    protected void addOption(Option option) {
        options.addOption(option);
    }

    /**
     * Sets the network.
     *
     * @param network
     */
    protected void setNetwork(Network network) {
        this.network = network;
    }

    /**
     * Sets the data directory.
     *
     * @param rootDir
     */
    protected void setRootDir(String rootDir) {
        this.rootDir = rootDir;
    }

    /**
     * Sets the coinbase.
     *
     * @param coinbase
     */
    protected void setCoinbase(int coinbase) {
        this.coinbase = coinbase;
    }

    /**
     * Sets the password.
     *
     * @param password
     */
    protected void setPassword(String password) {
        this.password = password;
    }

    public Optional<Boolean> isHdWalletEnabled() {
        return Optional.ofNullable(hdWalletEnabled);
    }

    public void setHdWalletEnabled(Boolean hdWalletEnabled) {
        this.hdWalletEnabled = hdWalletEnabled;
    }

    /**
     * Check runtime prerequisite.
     */
    protected static void checkPrerequisite() {
        switch (SystemUtil.getOsName()) {
        case WINDOWS:
            if (!SystemUtil.isWindowsVCRedist2012Installed()) {
                throw new LauncherException(
                        "Microsoft Visual C++ 2012 Redistributable Package is not installed. Please visit: https://www.microsoft.com/en-us/download/details.aspx?id=30679");
            }
            break;
        default:
        }
    }

    /**
     * Registers a shutdown hook which will be executed in the order of
     * registration.
     *
     * @param name
     * @param runnable
     */
    public static synchronized void registerShutdownHook(String name, Runnable runnable) {
        shutdownHooks.add(Pair.of(name, runnable));
    }

    /**
     * Call registered shutdown hooks in the order of registration.
     */
    private static synchronized void shutdownHook() {
        // shutdown hooks
        for (Pair<String, Runnable> r : shutdownHooks) {
            try {
                logger.info(String.format("Shutting down %s", r.getLeft()));
                r.getRight().run();
            } catch (Exception e) {
                logger.info(String.format("Failed to shutdown %s", r.getLeft(), e));
            }
        }

        // flush log4j async loggers
        //LogManager.shutdown();
    }
}
