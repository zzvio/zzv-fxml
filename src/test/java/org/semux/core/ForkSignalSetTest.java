/**
 * Copyright (c) 2017-2020 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.core;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.BeforeClass;
import org.junit.Test;
import org.semux.crypto.Hex;

public class ForkSignalSetTest {

    private static final Fork[] eightPendingForks = new Fork[8];

    private static final byte[] eightPendingForksEncoded = Hex.decode("0800010002000300040005000600070008");

    private static final Fork[] onePendingFork = new Fork[1];

    private static final byte[] onePendingForkEncoded = Hex.decode("010001");

    @BeforeClass
    public static void beforeClass() {
        for (short i = 1; i <= 8; i++) {
            Fork a = mock(Fork.class);
            when(a.id()).thenReturn(i);
            eightPendingForks[i - 1] = a;
        }

        onePendingFork[0] = Fork.UNIFORM_DISTRIBUTION;
    }

    @Test
    public void testForkSignalSetCodec_onePendingFork() {
        // test decoding
        ForkSignalSet set = ForkSignalSet.fromBytes(onePendingForkEncoded);
        assertTrue(set.contains(onePendingFork[0]));

        // test encoding
        assertArrayEquals(onePendingForkEncoded, ForkSignalSet.of(onePendingFork).toBytes());
    }

    @Test
    public void testForkSignalSetCodec_eightPendingForks() {
        // test decoding
        ForkSignalSet set = ForkSignalSet.fromBytes(eightPendingForksEncoded);
        for (Fork f : eightPendingForks) {
            set.contains(f);
        }

        // test encoding
        set = ForkSignalSet.of(eightPendingForks);
        assertThat(set.toBytes()).hasSize(ForkSignalSet.MAX_PENDING_FORKS * 2 + 1).isEqualTo(eightPendingForksEncoded);
    }
}
