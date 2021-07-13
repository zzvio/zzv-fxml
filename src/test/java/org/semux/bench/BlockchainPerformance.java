/**
 * Copyright (c) 2017-2020 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.bench;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.semux.Network;
import org.semux.TestUtils;
import org.semux.config.Config;
import org.semux.config.Constants;
import org.semux.config.UnitTestnetConfig;
import org.semux.core.Amount;
import org.semux.core.Block;
import org.semux.core.BlockHeader;
import org.semux.core.Blockchain;
import org.semux.core.BlockchainImpl;
import org.semux.core.Genesis;
import org.semux.core.Transaction;
import org.semux.core.TransactionResult;
import org.semux.core.TransactionType;
import org.semux.crypto.Key;
import org.semux.crypto.Key.Signature;
import org.semux.rules.TemporaryDatabaseRule;
import org.semux.util.Bytes;
import org.semux.util.MerkleUtil;
import org.semux.util.TimeUtil;

public class BlockchainPerformance {
    private static final Logger logger = Logger.getLogger(BlockchainPerformance.class.getName());

    private static Config config = new UnitTestnetConfig(Constants.DEFAULT_ROOT_DIR);
    private static Key key = new Key();

    public static Block testBlockCreation() {
        long t1 = System.nanoTime();

        List<Transaction> txs = new ArrayList<>();
        List<TransactionResult> res = new ArrayList<>();

        long remainingBlockGas = config.spec().maxBlockGasLimit();
        for (int i = 0; remainingBlockGas >= config.spec().nonVMTransactionGasCost(); i++) {
            Network network = config.network();
            TransactionType type = TransactionType.TRANSFER;
            byte[] to = Bytes.random(20);
            Amount value = Amount.of(1);
            Amount fee = config.spec().minTransactionFee();
            long nonce = 1 + i;
            long timestamp = TimeUtil.currentTimeMillis();
            byte[] data = Bytes.EMPTY_BYTES;
            Transaction tx = new Transaction(network, type, to, value, fee, nonce, timestamp, data).sign(key);
            txs.add(tx);
            res.add(new TransactionResult());

            remainingBlockGas -= config.spec().nonVMTransactionGasCost();
        }

        long number = 1;
        byte[] coinbase = key.toAddress();
        byte[] prevHash = Bytes.random(32);
        long timestamp = TimeUtil.currentTimeMillis();
        byte[] transactionsRoot = MerkleUtil.computeTransactionsRoot(txs);
        byte[] resultsRoot = MerkleUtil.computeResultsRoot(res);
        byte[] stateRoot = Bytes.EMPTY_HASH;
        byte[] data = {};

        BlockHeader header = new BlockHeader(number, coinbase, prevHash, timestamp, transactionsRoot, resultsRoot,
                stateRoot, data);
        Block block = new Block(header, txs, res);

        List<Signature> votes = new ArrayList<>();
        for (int i = 0; i < config.spec().getNumberOfValidators(1000000L); i++) {
            votes.add(new Key().sign(Bytes.EMPTY_BYTES));
        }
        block.setView(1);
        block.setVotes(votes);

        long t2 = System.nanoTime();
        logger.info(String.format("block # of txs: %s", block.getTransactions().size()));
        logger.info(String.format("block header size: %s B", block.getEncodedHeader().length));
        logger.info(String.format("block transaction size: %s KB", block.getEncodedTransactions().length / 1024));
        logger.info(String.format("block results size: %s KB", block.getEncodedResults().length / 1024));
        logger.info(String.format("block votes size: %s KB", block.getEncodedVotes().length / 1024));
        logger.info(String.format("block total size: %s KB", block.size() / 1024));
        logger.info(String.format("Perf_block_creation: %s ms", (t2 - t1) / 1_000_000));
        return block;
    }

    public static void testBlockValidation(Block block) {
        Genesis genesis = Genesis.load(Network.DEVNET);

        long t1 = System.nanoTime();
        block.validateHeader(block.getHeader(), genesis.getHeader());
        block.validateTransactions(block.getHeader(), block.getTransactions(), config.network());
        block.validateResults(block.getHeader(), block.getResults());
        // block votes validation skipped
        long t2 = System.nanoTime();

        logger.info(String.format("Perf_block_validation: %s ms", (t2 - t1) / 1_000_000));
    }

    public static void testTransactionValidation() {
        Key key = new Key();

        Network network = config.network();
        TransactionType type = TransactionType.TRANSFER;
        byte[] to = Bytes.random(20);
        Amount value = Amount.of(1);
        Amount fee = config.spec().minTransactionFee();
        long nonce = 1;
        long timestamp = TimeUtil.currentTimeMillis();
        byte[] data = {};
        Transaction tx = new Transaction(network, type, to, value, fee, nonce, timestamp, data);
        tx.sign(key);

        int repeat = 1000;
        long t1 = System.nanoTime();
        for (int i = 0; i < repeat; i++) {
            tx.validate(network);
        }
        long t2 = System.nanoTime();
        logger.info(String.format("Perf_transaction_size: %s B", tx.toBytes().length));
        logger.info(String.format("Perf_transaction_validation: %s μs/time", (t2 - t1) / repeat / 1_000));
    }

    public static void testAddBlock() throws Throwable {
        final int repeat = 10000;
        Block[] blocks = new Block[repeat];
        for (int i = 0; i < repeat; i++) {
            blocks[i] = TestUtils.createEmptyBlock(i);
        }

        TemporaryDatabaseRule temporaryDbRule = new TemporaryDatabaseRule();
        temporaryDbRule.before();
        Blockchain blockchain = new BlockchainImpl(config, temporaryDbRule);
        long t1 = TimeUtil.currentTimeMillis();
        for (int i = 0; i < repeat; i++) {
            blockchain.addBlock(blocks[i]);
        }
        long t2 = TimeUtil.currentTimeMillis();
        temporaryDbRule.after();
        logger.info(String.format("Perf_addBlock: %s ms / %s blocks", t2 - t1, repeat));
    }

    public static void main(String[] args) throws Throwable {
        Block block = testBlockCreation();
        testBlockValidation(block);
        testTransactionValidation();
        testAddBlock();

        System.exit(0);
    }
}
