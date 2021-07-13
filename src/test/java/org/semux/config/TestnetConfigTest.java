/**
 * Copyright (c) 2017-2020 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.config;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.semux.Network;

public class TestnetConfigTest {

    @Test
    public void testNetworkId() {
        Config config = new TestnetConfig(Constants.DEFAULT_ROOT_DIR);
        assertEquals(Network.TESTNET, config.network());
    }

}
