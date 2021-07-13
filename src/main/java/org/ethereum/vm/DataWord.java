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
package org.ethereum.vm;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.ethereum.vm.util.HexUtil;

/**
 * DataWord is the 32-byte array representation of a 256-bit number.
 *
 * @ImplNote DataWord objects are immutable.
 */
public class DataWord implements Comparable<DataWord> {

    public static final BigInteger TWO_POW_256 = BigInteger.valueOf(2).pow(256);
    public static final BigInteger MAX_VALUE = TWO_POW_256.subtract(BigInteger.ONE);

    public static final DataWord ZERO = of(0);
    public static final DataWord ONE = of(1);

    public static final int SIZE = 32;

    private final byte[] data;

    public static DataWord of(byte num) {
        byte[] bb = new byte[32];
        bb[31] = num;
        return new DataWord(bb, false);
    }

    public static DataWord of(int num) {
        return new DataWord(ByteBuffer.allocate(Integer.BYTES).putInt(num).array(), false);
    }

    public static DataWord of(long num) {
        return new DataWord(ByteBuffer.allocate(Long.BYTES).putLong(num).array(), false);
    }

    public static DataWord of(BigInteger num) {
        if (num.signum() < 0 || num.compareTo(MAX_VALUE) > 0) {
            throw new IllegalArgumentException("Input BigInt can't be negative or larger than MAX_VALUE");
        }

        // NOTE: a 33 bytes array may be produced
        byte[] bytes = num.toByteArray();
        int copyOffset = Math.max(bytes.length - SIZE, 0);
        int copyLength = bytes.length - copyOffset;

        byte[] data = new byte[SIZE];
        System.arraycopy(bytes, copyOffset, data, SIZE - copyLength, copyLength);

        return new DataWord(data, false);
    }

    public static DataWord of(String hex) {
        return new DataWord(HexUtil.fromHexString(hex), false);
    }

    public static DataWord of(byte[] data) {
        return new DataWord(data, true);
    }

    /**
     * Creates a DataWord instance from byte array.
     *
     * @param data
     *            an byte array
     * @param unsafe
     *            whether the data is safe to refer
     */
    protected DataWord(byte[] data, boolean unsafe) {
        if (data == null || data.length > SIZE) {
            throw new IllegalArgumentException("Input data can't be NULL or exceed " + SIZE + " bytes");
        }

        if (data.length == SIZE) {
            this.data = unsafe ? data.clone() : data;
        } else {
            this.data = new byte[SIZE];
            System.arraycopy(data, 0, this.data, SIZE - data.length, data.length);
        }
    }

    /**
     * Returns a clone of the underlying byte array.
     *
     * @return a byte array
     */
    public byte[] getData() {
        return data.clone();
    }

    /**
     * Returns the last 20 bytes.
     *
     * @return
     */
    public byte[] getLast20Bytes() {
        return Arrays.copyOfRange(data, SIZE - 20, SIZE);
    }

    /**
     * Returns the n-th byte.
     *
     * @param index
     * @return
     */
    public byte getByte(int index) {
        return data[index];
    }

    public BigInteger value() {
        return new BigInteger(1, data);
    }

    public BigInteger sValue() {
        return new BigInteger(data);
    }

    /**
     * Converts this DataWord to an integer, checking for lost information. If this
     * DataWord is out of the possible range, then an ArithmeticException is thrown.
     *
     * @return an integer
     * @throws ArithmeticException
     *             if this value is larger than {@link Integer#MAX_VALUE}
     */
    public int intValue() throws ArithmeticException {
        return intValue(false);
    }

    /**
     * Returns {@link Integer#MAX_VALUE} in case of overflow
     */
    public int intValueSafe() {
        return intValue(true);
    }

    /**
     * Converts this DataWord to a long integer, checking for lost information. If
     * this DataWord is out of the possible range, then an ArithmeticException is
     * thrown.
     *
     * @return a long integer
     * @throws ArithmeticException
     *             if this value is larger than {@link Long#MAX_VALUE}
     */
    public long longValue() {
        return longValue(false);
    }

    /**
     * Returns {@link Long#MAX_VALUE} in case of overflow
     */
    public long longValueSafe() {
        return longValue(true);
    }

    public boolean isZero() {
        for (byte b : data) {
            if (b != 0) {
                return false;
            }
        }
        return true;
    }

    public boolean isNegative() {
        return (data[0] & 0x80) != 0;
    }

    public DataWord and(DataWord w2) {
        byte[] buffer = new byte[SIZE];
        for (int i = 0; i < this.data.length; ++i) {
            buffer[i] = (byte) (this.data[i] & w2.data[i]);
        }
        return new DataWord(buffer, false);
    }

    public DataWord or(DataWord w2) {
        byte[] buffer = new byte[SIZE];
        for (int i = 0; i < this.data.length; ++i) {
            buffer[i] = (byte) (this.data[i] | w2.data[i]);
        }
        return new DataWord(buffer, false);
    }

    public DataWord xor(DataWord w2) {
        byte[] buffer = new byte[SIZE];
        for (int i = 0; i < this.data.length; ++i) {
            buffer[i] = (byte) (this.data[i] ^ w2.data[i]);
        }
        return new DataWord(buffer, false);
    }

    public DataWord negate() {
        return isZero() ? ZERO : bnot().add(DataWord.ONE);
    }

    // bitwise not
    public DataWord bnot() {
        byte[] buffer = new byte[SIZE];
        for (int i = 0; i < this.data.length; ++i) {
            buffer[i] = (byte) (~this.data[i]);
        }
        return new DataWord(buffer, false);
    }

    // Credit -> http://stackoverflow.com/a/24023466/459349
    public DataWord add(DataWord word) {
        byte[] buffer = new byte[SIZE];
        for (int i = 31, overflow = 0; i >= 0; i--) {
            int v = (this.data[i] & 0xff) + (word.data[i] & 0xff) + overflow;
            buffer[i] = (byte) v;
            overflow = v >>> 8;
        }
        return new DataWord(buffer, false);
    }

    public DataWord mul(DataWord word) {
        BigInteger result = value().multiply(word.value());

        return of(result.and(MAX_VALUE));
    }

    public DataWord div(DataWord word) {
        if (word.isZero()) {
            return ZERO;
        } else {
            BigInteger result = value().divide(word.value());
            return of(result.and(MAX_VALUE));
        }
    }

    public DataWord sDiv(DataWord word) {
        if (word.isZero()) {
            return ZERO;
        } else {
            BigInteger result = sValue().divide(word.sValue());
            return of(result.and(MAX_VALUE));
        }
    }

    public DataWord sub(DataWord word) {
        BigInteger result = value().subtract(word.value());
        return of(result.and(MAX_VALUE));
    }

    public DataWord exp(DataWord word) {
        BigInteger result = value().modPow(word.value(), TWO_POW_256);
        return of(result);
    }

    public DataWord mod(DataWord word) {
        if (word.isZero()) {
            return ZERO;
        } else {
            BigInteger result = value().mod(word.value());
            return of(result.and(MAX_VALUE));
        }
    }

    public DataWord sMod(DataWord word) {
        if (word.isZero()) {
            return ZERO;
        } else {
            BigInteger result = sValue().abs().mod(word.sValue().abs());
            result = (sValue().signum() == -1) ? result.negate() : result;

            return of(result.and(MAX_VALUE));
        }
    }

    public DataWord addmod(DataWord word1, DataWord word2) {
        if (word2.isZero()) {
            return ZERO;
        } else {
            BigInteger result = value().add(word1.value()).mod(word2.value());
            return of(result.and(MAX_VALUE));
        }
    }

    public DataWord mulmod(DataWord word1, DataWord word2) {
        if (this.isZero() || word1.isZero() || word2.isZero()) {
            return ZERO;
        } else {
            BigInteger result = value().multiply(word1.value()).mod(word2.value());
            return of(result.and(MAX_VALUE));
        }
    }

    public DataWord signExtend(byte k) {
        if (0 > k || k > 31) {
            throw new IndexOutOfBoundsException();
        }

        byte[] buffer = data.clone();
        byte mask = this.sValue().testBit((k * 8) + 7) ? (byte) 0xff : 0;
        for (int i = 31; i > k; i--) {
            buffer[31 - i] = mask;
        }

        return new DataWord(buffer, false);
    }

    public int bytesOccupied() {
        for (int i = 0; i < SIZE; i++) {
            if (data[i] != 0) {
                return SIZE - i;
            }
        }

        return 0;
    }

    /**
     * Shift left, both this and input arg are treated as unsigned
     *
     * @param arg
     * @return this << arg
     */
    public DataWord shiftLeft(DataWord arg) {
        if (arg.value().compareTo(BigInteger.valueOf(SIZE * 8)) > 0) {
            return ZERO;
        } else {
            return DataWord.of(value().shiftLeft(arg.intValue()).and(MAX_VALUE));
        }
    }

    /**
     * Shift right, both this and input arg are treated as unsigned
     *
     * @param arg
     * @return this >>> arg
     */
    public DataWord shiftRight(DataWord arg) {
        if (arg.value().compareTo(BigInteger.valueOf(SIZE * 8)) > 0) {
            return ZERO;
        } else {
            return DataWord.of(value().shiftRight(arg.intValue()).and(MAX_VALUE));
        }
    }

    /**
     * Shift right, this is signed, while input arg is treated as unsigned
     *
     * @param arg
     * @return this >> arg
     */
    public DataWord shiftRightSigned(DataWord arg) {
        if (arg.value().compareTo(BigInteger.valueOf(SIZE * 8)) > 0) {
            if (this.isNegative()) {
                return DataWord.ONE.negate();
            } else {
                return DataWord.ZERO;
            }
        } else {
            return DataWord.of(sValue().shiftRight(arg.intValue()).and(MAX_VALUE));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        return Arrays.equals(data, ((DataWord) o).data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }

    @Override
    public int compareTo(DataWord o) {
        return org.bouncycastle.util.Arrays.compareUnsigned(this.data, o.data);
    }

    @Override
    public String toString() {
        return HexUtil.toHexString(data);
    }

    private int intValue(boolean safe) {
        if (bytesOccupied() > 4 || (data[SIZE - 4] & 0x80) != 0) {
            if (safe) {
                return Integer.MAX_VALUE;
            } else {
                throw new ArithmeticException();
            }
        }

        int value = 0;
        for (int i = 0; i < 4; i++) {
            value = (value << 8) + (0xff & data[SIZE - 4 + i]);
        }

        return value;
    }

    private long longValue(boolean safe) {
        if (bytesOccupied() > 8 || (data[SIZE - 8] & 0x80) != 0) {
            if (safe) {
                return Long.MAX_VALUE;
            } else {
                throw new ArithmeticException();
            }
        }

        long value = 0;
        for (int i = 0; i < 8; i++) {
            value = (value << 8) + (0xff & data[SIZE - 8 + i]);
        }

        return value;
    }
}
