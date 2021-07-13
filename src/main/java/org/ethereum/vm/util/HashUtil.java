/**
 * Copyright (c) [2018] [ The Semux Developers ]
 * Copyright (c) [2016] [ <ether.camp> ]
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.vm.util;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.bouncycastle.jcajce.provider.digest.Keccak;

public class HashUtil {

    /**
     * Computes the Keccak-256 hash digest.
     *
     * @param input
     *            the input data
     * @return a 32 bytes digest
     */
    public static byte[] keccak256(byte[] input) {
        Keccak.Digest256 digest = new Keccak.Digest256();
        digest.update(input);
        return digest.digest();
    }

    /**
     * Calculates RIGTMOST160(KECCAK256(input)). This is used in address
     * calculations. *
     *
     * @param input
     *            data
     * @return 20 right bytes of the hash keccak of the data
     */
    public static byte[] sha3omit12(byte[] input) {
        byte[] hash = keccak256(input);
        return Arrays.copyOfRange(hash, 12, hash.length);
    }

    /**
     * Computes the address of a deployed contract.
     *
     * @param address
     *            the sender's address
     * @param nonce
     *            the sender's nonce
     * @return an 20 bytes array
     * @implNote the implementation is slightly different from Ethereum's specs;
     *           we're not using RLP codec to encode the address and nonce.
     */
    public static byte[] calcNewAddress(byte[] address, long nonce) {
        ByteBuffer buffer = ByteBuffer.allocate(20 + 8);
        buffer.put(address).putLong(nonce);
        byte[] keccak256 = keccak256(buffer.array());

        return Arrays.copyOfRange(keccak256, 12, 32);
    }

    /**
     * The way to calculate new address inside ethereum for
     * {@link org.ethereum.vm.OpCode#CREATE2} sha3(0xff ++ msg.sender ++ salt ++
     * sha3(init_code)))[12:]
     *
     * @param senderAddr
     *            - creating address
     * @param initCode
     *            - contract init code
     * @param salt
     *            - salt to make different result addresses
     * @return new address
     */
    public static byte[] calcSaltAddress(byte[] senderAddr, byte[] initCode, byte[] salt) {
        // 1 - 0xff length, 32 bytes - keccak-256
        byte[] data = new byte[1 + senderAddr.length + salt.length + 32];
        data[0] = (byte) 0xff;
        int currentOffset = 1;
        System.arraycopy(senderAddr, 0, data, currentOffset, senderAddr.length);
        currentOffset += senderAddr.length;
        System.arraycopy(salt, 0, data, currentOffset, salt.length);
        currentOffset += salt.length;
        byte[] sha3InitCode = keccak256(initCode);
        System.arraycopy(sha3InitCode, 0, data, currentOffset, sha3InitCode.length);
        return sha3omit12(data);
    }

    public static byte[] sha256(byte[] input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] ripemd160(byte[] data) {
        Digest digest = new RIPEMD160Digest();
        byte[] buffer = new byte[digest.getDigestSize()];
        digest.update(data, 0, data.length);
        digest.doFinal(buffer, 0);
        return buffer;
    }
}
