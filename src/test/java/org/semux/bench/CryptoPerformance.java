/**
 * Copyright (c) 2017-2020 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.bench;

import java.util.logging.Logger;

import org.semux.crypto.Hash;
import org.semux.crypto.Key;

public class CryptoPerformance {
    private static final Logger logger = Logger.getLogger(CryptoPerformance.class.getName());

    private static int[] DATA_SIZES = { 1024, 1024 * 1024 };
    private static int REPEAT = 1000;

    public static void testH256() {
        for (int size : DATA_SIZES) {
            byte[] data = new byte[size];

            long t1 = System.nanoTime();
            for (int i = 0; i < REPEAT; i++) {
                Hash.h256(data);
            }
            long t2 = System.nanoTime();

            logger.info(String.format("Perf_h256_%sk: %s μs/time", size / 1024, (t2 - t1) / 1_000 / REPEAT));
        }
    }

    public static void testH160() {
        for (int size : DATA_SIZES) {
            byte[] data = new byte[size];

            long t1 = System.nanoTime();
            for (int i = 0; i < REPEAT; i++) {
                Hash.h160(data);
            }
            long t2 = System.nanoTime();

            logger.info(String.format("Perf_h160_%sk: %s μs/time", size / 1024, (t2 - t1) / 1_000 / REPEAT));
        }
    }

    public static void testSign() {
        for (int size : DATA_SIZES) {
            Key eckey = new Key();
            byte[] data = new byte[size];
            byte[] hash = Hash.h256(data);

            long t1 = System.nanoTime();
            for (int i = 0; i < REPEAT; i++) {
                eckey.sign(hash);
            }
            long t2 = System.nanoTime();

            logger.info(String.format("Perf_sign_%sk: %s μs/time", size / 1024, (t2 - t1) / 1_000 / REPEAT));
        }
    }

    public static void testVerify() {
        for (int size : DATA_SIZES) {
            Key eckey = new Key();
            byte[] data = new byte[size];
            byte[] hash = Hash.h256(data);
            byte[] sig = eckey.sign(hash).toBytes();

            long t1 = System.nanoTime();
            for (int i = 0; i < REPEAT; i++) {
                Key.verify(hash, sig);
            }
            long t2 = System.nanoTime();

            logger.info(String.format("Perf_verify_%sk: %s μs/time", size / 1024, (t2 - t1) / 1_000 / REPEAT));
        }
    }

    public static void main(String[] args) throws Exception {
        testH256();
        testH160();
        testSign();
        testVerify();
    }
}
