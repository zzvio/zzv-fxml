/**
 * Copyright (c) 2017-2020 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.semux.Network;
import org.semux.crypto.Aes;
import org.semux.crypto.Hash;
import org.semux.crypto.Key;
import org.semux.util.Bytes;
import org.semux.util.IOUtil;
import org.semux.util.SimpleEncoder;

/**
 * Tests ability to read old wallet versions
 */
public class WalletVersionTest {

    @Test
    public void testVersion1Wallet() throws IOException {
        File file = File.createTempFile("wallet", ".data");
        List<Key> accounts = Collections.singletonList(new Key());

        writeVersion1Wallet(accounts, file, "password!");

        // read it as current version
        Wallet wallet = new Wallet(file, Network.DEVNET);
        wallet.unlock("password!");
        List<Key> readAccounts = wallet.getAccounts();

        assertEquals(accounts, readAccounts);

        // verify that it has 'name' set to default
        Optional<String> name = wallet.getAddressAlias(accounts.get(0).getPublicKey());
        assertFalse(name.isPresent());
    }

    private void writeVersion1Wallet(List<Key> accounts, File file, String password) throws IOException {
        byte[] key = Hash.h256(Bytes.of(password));

        // write a version 1 wallet
        SimpleEncoder enc = new SimpleEncoder();
        enc.writeInt(1);
        enc.writeInt(accounts.size());

        for (Key a : accounts) {
            byte[] iv = Bytes.random(16);

            enc.writeBytes(iv, false);
            enc.writeBytes(a.getPublicKey(), false);
            enc.writeBytes(Aes.encrypt(a.getPrivateKey(), key, iv), false);
        }

        IOUtil.writeToFile(enc.toBytes(), file);
    }
}
