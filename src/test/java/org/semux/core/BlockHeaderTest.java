/**
 * Copyright (c) 2017-2020 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.core;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.logging.Logger;

import org.junit.Test;
import org.semux.config.Constants;
import org.semux.util.Bytes;
import org.semux.util.TimeUtil;

public class BlockHeaderTest {
    private static final Logger logger = Logger.getLogger(BlockHeaderTest.class.getName());

    private long number = 1;
    private byte[] coinbase = Bytes.random(20);
    private byte[] prevHash = Bytes.random(32);
    private long timestamp = TimeUtil.currentTimeMillis();
    private byte[] transactionsRoot = Bytes.random(32);
    private byte[] resultsRoot = Bytes.random(32);
    private byte[] stateRoot = Bytes.random(32);
    private byte[] data = Bytes.of("data");

    private byte[] hash;

    @Test
    public void testNew() {
        BlockHeader header = new BlockHeader(number, coinbase, prevHash, timestamp, transactionsRoot, resultsRoot,
                stateRoot, data);
        hash = header.getHash();

        testFields(header);
    }

    @Test
    public void testSerialization() {
        BlockHeader header = new BlockHeader(number, coinbase, prevHash, timestamp, transactionsRoot, resultsRoot,
                stateRoot, data);
        hash = header.getHash();

        testFields(BlockHeader.fromBytes(header.toBytes()));
    }

    @Test
    public void testBlockHeaderSize() {
        BlockHeader header = new BlockHeader(number, coinbase, prevHash, timestamp, transactionsRoot, resultsRoot,
                stateRoot, data);
        byte[] bytes = header.toBytes();

        logger.info(String.format("block header size: %s", bytes.length));
        logger.info(String.format("block header size (1y): %s GB",
                1.0 * bytes.length * Constants.BLOCKS_PER_YEAR / 1024 / 1024 / 1024));
    }

    private void testFields(BlockHeader header) {
        assertArrayEquals(hash, header.getHash());
        assertEquals(number, header.getNumber());
        assertArrayEquals(coinbase, header.getCoinbase());
        assertArrayEquals(prevHash, header.getParentHash());
        assertEquals(timestamp, header.getTimestamp());
        assertArrayEquals(transactionsRoot, header.getTransactionsRoot());
        assertArrayEquals(resultsRoot, header.getResultsRoot());
        assertArrayEquals(stateRoot, header.getStateRoot());
        assertArrayEquals(data, header.getData());
    }
}
