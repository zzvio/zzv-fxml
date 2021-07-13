/**
 * Copyright (c) 2017-2020 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.cli;

import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;

import org.fusesource.leveldbjni.JniDBFactory;
import org.iq80.leveldb.CompressionType;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import org.semux.crypto.Hex;

public class DatabaseScanner {
    public static void main(String[] args) throws IOException {
        Options options = new Options();
        options.createIfMissing(false);
        options.cacheSize(128L * 1024L * 1024L);
        options.compressionType(CompressionType.NONE);

        File f = new File("database/testnet/index");

        try (DB db = JniDBFactory.factory.open(f, options)) {
            DBIterator itr = db.iterator();
            itr.seekToFirst();
            while (itr.hasNext()) {
                Entry<byte[], byte[]> entry = itr.next();
                System.out.println(Hex.encode(entry.getKey()) + " = " + Hex.encode(entry.getValue()));
            }
            itr.close();
        }
    }
}
