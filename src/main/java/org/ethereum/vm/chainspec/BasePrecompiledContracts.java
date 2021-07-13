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
package org.ethereum.vm.chainspec;

import static org.ethereum.vm.util.BigIntegerUtil.addSafely;
import static org.ethereum.vm.util.BigIntegerUtil.isLessThan;
import static org.ethereum.vm.util.BigIntegerUtil.isZero;
import static org.ethereum.vm.util.ByteArrayUtil.EMPTY_BYTE_ARRAY;
import static org.ethereum.vm.util.ByteArrayUtil.bytesToBigInteger;
import static org.ethereum.vm.util.ByteArrayUtil.numberOfLeadingZeros;
import static org.ethereum.vm.util.ByteArrayUtil.parseBytes;
import static org.ethereum.vm.util.ByteArrayUtil.stripLeadingZeroes;
import static org.ethereum.vm.util.VMUtil.getSizeInWords;

import java.math.BigInteger;

import org.ethereum.vm.DataWord;
import org.ethereum.vm.crypto.ECKey;
import org.ethereum.vm.util.HashUtil;
import org.ethereum.vm.util.Pair;

public class BasePrecompiledContracts implements PrecompiledContracts {

    private static final ECRecover ecRecover = new ECRecover();
    private static final Sha256 sha256 = new Sha256();
    private static final Ripempd160 ripempd160 = new Ripempd160();
    private static final Identity identity = new Identity();
    private static final ModExp modExp = new ModExp();

    private static final DataWord ecRecoverAddr = DataWord.of(1);
    private static final DataWord sha256Addr = DataWord.of(2);
    private static final DataWord ripempd160Addr = DataWord.of(3);
    private static final DataWord identityAddr = DataWord.of(4);
    private static final DataWord modExpAddr = DataWord.of(5);

    @Override
    public PrecompiledContract getContractForAddress(DataWord address) {

        if (address.equals(ecRecoverAddr)) {
            return ecRecover;
        } else if (address.equals(sha256Addr)) {
            return sha256;
        } else if (address.equals(ripempd160Addr)) {
            return ripempd160;
        } else if (address.equals(identityAddr)) {
            return identity;
        } else if (address.equals(modExpAddr)) {
            return modExp;
        }

        return null;
    }

    public static byte[] encodeRes(byte[] w1, byte[] w2) {
        byte[] res = new byte[64];

        w1 = stripLeadingZeroes(w1);
        w2 = stripLeadingZeroes(w2);

        System.arraycopy(w1, 0, res, 32 - w1.length, w1.length);
        System.arraycopy(w2, 0, res, 64 - w2.length, w2.length);

        return res;
    }

    public static class Identity implements PrecompiledContract {

        public Identity() {
        }

        @Override
        public long getGasForData(byte[] data) {
            // gas charge for the execution:
            // minimum 1 and additional 1 for each 32 bytes word (round up)
            if (data == null)
                return 15;
            return 15 + getSizeInWords(data.length) * 3;
        }

        @Override
        public Pair<Boolean, byte[]> execute(PrecompiledContractContext context) {
            return Pair.of(true, context.getInternalTransaction().getData());
        }
    }

    public static class Sha256 implements PrecompiledContract {
        @Override
        public long getGasForData(byte[] data) {
            // gas charge for the execution:
            // minimum 50 and additional 50 for each 32 bytes word (round up)
            if (data == null) {
                return 60;
            }

            return 60 + getSizeInWords(data.length) * 12;
        }

        @Override
        public Pair<Boolean, byte[]> execute(PrecompiledContractContext context) {
            byte[] data = context.getInternalTransaction().getData();
            return Pair.of(true, HashUtil.sha256(data == null ? EMPTY_BYTE_ARRAY : data));
        }
    }

    public static class Ripempd160 implements PrecompiledContract {
        @Override
        public long getGasForData(byte[] data) {
            // gas charge for the execution:
            // minimum 50 and additional 50 for each 32 bytes word (round up)
            if (data == null) {
                return 600;
            }

            return 600 + getSizeInWords(data.length) * 120;
        }

        @Override
        public Pair<Boolean, byte[]> execute(PrecompiledContractContext context) {
            byte[] data = context.getInternalTransaction().getData();
            byte[] result;
            if (data == null) {
                result = HashUtil.ripemd160(EMPTY_BYTE_ARRAY);
            } else {
                result = HashUtil.ripemd160(data);
            }

            return Pair.of(true, DataWord.of(result).getData());
        }
    }

    public static class ECRecover implements PrecompiledContract {

        @Override
        public long getGasForData(byte[] data) {
            return 3000;
        }

        @Override
        public Pair<Boolean, byte[]> execute(PrecompiledContractContext context) {
            byte[] data = context.getInternalTransaction().getData();

            byte[] h = new byte[32];
            byte[] v = new byte[32];
            byte[] r = new byte[32];
            byte[] s = new byte[32];

            DataWord out = null;

            try {
                System.arraycopy(data, 0, h, 0, 32);
                System.arraycopy(data, 32, v, 0, 32);
                System.arraycopy(data, 64, r, 0, 32);

                int sLength = data.length < 128 ? data.length - 96 : 32;
                System.arraycopy(data, 96, s, 0, sLength);

                ECKey.ECDSASignature signature = ECKey.ECDSASignature.fromComponents(r, s, v[31]);
                if (validateV(v) && signature.validateComponents()) {
                    out = DataWord.of(ECKey.signatureToAddress(h, signature));
                }
            } catch (Throwable any) {
            }

            return Pair.of(true, out == null ? EMPTY_BYTE_ARRAY : out.getData());
        }

        private static boolean validateV(byte[] v) {
            for (int i = 0; i < v.length - 1; i++) {
                if (v[i] != 0) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Computes modular exponentiation on big numbers
     *
     * format of data[] array: [length_of_BASE] [length_of_EXPONENT]
     * [length_of_MODULUS] [BASE] [EXPONENT] [MODULUS] where every length is a
     * 32-byte left-padded integer representing the number of bytes. Call data is
     * assumed to be infinitely right-padded with zero bytes.
     *
     * Returns an output as a byte array with the same length as the modulus
     */
    public static class ModExp implements PrecompiledContract {

        private static final BigInteger GQUAD_DIVISOR = BigInteger.valueOf(20);

        private static final int ARGS_OFFSET = 32 * 3; // addresses length part

        @Override
        public long getGasForData(byte[] data) {
            if (data == null) {
                data = EMPTY_BYTE_ARRAY;
            }

            int baseLen = parseLen(data, 0);
            int expLen = parseLen(data, 1);
            int modLen = parseLen(data, 2);

            byte[] expHighBytes = parseBytes(data, addSafely(ARGS_OFFSET, baseLen), Math.min(expLen, 32));

            long multComplexity = getMultComplexity(Math.max(baseLen, modLen));
            long adjExpLen = getAdjustedExponentLength(expHighBytes, expLen);

            // use big numbers to stay safe in case of overflow
            BigInteger gas = BigInteger.valueOf(multComplexity)
                    .multiply(BigInteger.valueOf(Math.max(adjExpLen, 1)))
                    .divide(GQUAD_DIVISOR);

            return isLessThan(gas, BigInteger.valueOf(Long.MAX_VALUE)) ? gas.longValue() : Long.MAX_VALUE;
        }

        @Override
        public Pair<Boolean, byte[]> execute(PrecompiledContractContext context) {
            byte[] data = context.getInternalTransaction().getData();
            if (data == null) {
                return Pair.of(true, EMPTY_BYTE_ARRAY);
            }

            int baseLen = parseLen(data, 0);
            int expLen = parseLen(data, 1);
            int modLen = parseLen(data, 2);

            BigInteger base = parseArg(data, ARGS_OFFSET, baseLen);
            BigInteger exp = parseArg(data, addSafely(ARGS_OFFSET, baseLen), expLen);
            BigInteger mod = parseArg(data, addSafely(addSafely(ARGS_OFFSET, baseLen), expLen), modLen);

            // check if modulus is zero
            if (isZero(mod))
                return Pair.of(true, new byte[modLen]); // should keep length of the result

            byte[] res = stripLeadingZeroes(base.modPow(exp, mod).toByteArray());

            // adjust result to the same length as the modulus has
            if (res.length < modLen) {

                byte[] adjRes = new byte[modLen];
                System.arraycopy(res, 0, adjRes, modLen - res.length, res.length);

                return Pair.of(true, adjRes);

            } else {
                return Pair.of(true, res);
            }
        }

        private long getMultComplexity(long x) {
            long x2 = x * x;

            if (x <= 64)
                return x2;
            if (x <= 1024)
                return x2 / 4 + 96 * x - 3072;

            return x2 / 16 + 480 * x - 199680;
        }

        private long getAdjustedExponentLength(byte[] expHighBytes, long expLen) {
            int leadingZeros = numberOfLeadingZeros(expHighBytes);
            int highestBit = 8 * expHighBytes.length - leadingZeros;

            // set index basement to zero
            if (highestBit > 0)
                highestBit--;

            if (expLen <= 32) {
                return highestBit;
            } else {
                return 8 * (expLen - 32) + highestBit;
            }
        }

        private int parseLen(byte[] data, int idx) {
            byte[] bytes = parseBytes(data, 32 * idx, 32);
            return DataWord.of(bytes).intValueSafe();
        }

        private BigInteger parseArg(byte[] data, int offset, int len) {
            byte[] bytes = parseBytes(data, offset, len);
            return bytesToBigInteger(bytes);
        }
    }
}
