/**
 * Copyright (c) 2017-2020 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.consensus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.semux.util.SimpleDecoder;
import org.semux.util.SimpleEncoder;

public class Proof {
    private final long height;
    private final int view;
    private final List<Vote> votes;

    public Proof(long height, int view, List<Vote> votes) {
        this.height = height;
        this.view = view;
        this.votes = votes;
    }

    public Proof(long height, int view) {
        this(height, view, Collections.emptyList());
    }

    public long getHeight() {
        return height;
    }

    public int getView() {
        return view;
    }

    public List<Vote> getVotes() {
        return votes;
    }

    public byte[] toBytes() {
        SimpleEncoder enc = new SimpleEncoder();
        enc.writeLong(height);
        enc.writeInt(view);
        enc.writeInt(votes.size());
        for (Vote v : votes) {
            enc.writeBytes(v.toBytes());
        }
        return enc.toBytes();
    }

    public static Proof fromBytes(byte[] bytes) {
        SimpleDecoder dec = new SimpleDecoder(bytes);
        long height = dec.readLong();
        int view = dec.readInt();
        List<Vote> votes = new ArrayList<>();
        int n = dec.readInt();
        for (int i = 0; i < n; i++) {
            votes.add(Vote.fromBytes(dec.readBytes()));
        }

        return new Proof(height, view, votes);
    }

    @Override
    public String toString() {
        return "Proof [height=" + height + ", view=" + view + ", # votes=" + votes.size() + "]";
    }
}