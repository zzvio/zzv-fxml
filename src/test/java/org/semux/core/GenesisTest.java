/**
 * Copyright (c) 2017-2020 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.core;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.semux.Network;
import org.semux.core.Genesis.Premine;
import org.semux.crypto.Hex;
import org.semux.util.ByteArray;
import org.semux.util.Bytes;

public class GenesisTest {

    private static final byte[] ZERO_ADDRESS = Hex.decode0x("0000000000000000000000000000000000000000");
    private static final byte[] ZERO_HASH = Bytes.EMPTY_HASH;

    Genesis genesis;

    @Before
    public void setUp() {
        genesis = Genesis.load(Network.MAINNET);
    }

    @Test
    public void testIsGenesis() {
        assertEquals(0, genesis.getNumber());
    }

    @Test
    public void testBlock() {
        assertEquals(0, genesis.getNumber());
        assertArrayEquals(ZERO_ADDRESS, genesis.getCoinbase());
        assertArrayEquals(ZERO_HASH, genesis.getParentHash());
        assertTrue(genesis.getTimestamp() > 0);
        assertFalse(Arrays.equals(ZERO_ADDRESS, genesis.getHash()));
    }

    @Test
    public void testPremines() {
        Map<ByteArray, Premine> premine = genesis.getPremines();

        assertFalse(premine.isEmpty());
        for (Premine p : premine.values()) {
            assertTrue(p.getAmount().isPositive());
        }
    }

    @Test
    public void testDelegates() {
        Map<String, byte[]> delegates = genesis.getDelegates();

        assertFalse(delegates.isEmpty());
        for (byte[] address : delegates.values()) {
            assertEquals(20, address.length);
        }
    }

    @Test
    public void testConfig() {
        assertTrue(genesis.getConfig().isEmpty());
    }
}
