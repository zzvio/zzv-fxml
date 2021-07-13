/**
 * Copyright (c) 2017-2020 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.core.state;

import java.util.Arrays;

import org.semux.core.Amount;
import org.semux.crypto.Hex;
import org.semux.util.Bytes;
import org.semux.util.SimpleDecoder;
import org.semux.util.SimpleEncoder;

public class Delegate {
    protected final byte[] address;
    protected final byte[] name;
    protected final long registeredAt;

    protected Amount votes;

    /**
     * Create a delegate instance.
     * 
     * @param address
     * @param name
     * @param registeredAt
     * @param votes
     */
    public Delegate(byte[] address, byte[] name, long registeredAt, Amount votes) {
        this.address = address;
        this.name = name;
        this.registeredAt = registeredAt;
        this.votes = votes;
    }

    public byte[] getAddress() {
        return address;
    }

    public String getAddressString() {
        return Hex.encode(getAddress());
    }

    public byte[] getName() {
        return name;
    }

    public String getNameString() {
        return Bytes.toString(name);
    }

    public long getRegisteredAt() {
        return registeredAt;
    }

    public Amount getVotes() {
        return votes;
    }

    void setVotes(Amount votes) {
        this.votes = votes;
    }

    /**
     * Serializes this delegate object into byte array.
     * 
     * @return
     */
    public byte[] toBytes() {
        SimpleEncoder enc = new SimpleEncoder();
        enc.writeBytes(name);
        enc.writeLong(registeredAt);
        enc.writeAmount(votes);

        return enc.toBytes();
    }

    /**
     * Parses a delegate from a byte array.
     * 
     * @param address
     * @param bytes
     * @return
     */
    public static Delegate fromBytes(byte[] address, byte[] bytes) {
        SimpleDecoder dec = new SimpleDecoder(bytes);
        byte[] name = dec.readBytes();
        long registeredAt = dec.readLong();
        Amount votes = dec.readAmount();

        return new Delegate(address, name, registeredAt, votes);
    }

    @Override
    public String toString() {
        return "Delegate [address=" + Hex.encode(address) + ", name=" + Arrays.toString(name) + ", registeredAt="
                + registeredAt + ", votes=" + votes + "]";
    }

}
