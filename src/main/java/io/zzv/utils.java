package io.zzv;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class utils {
    public static long toLong(byte[] b) {
        ByteBuffer buffer = ByteBuffer.wrap(b);
        return buffer.getLong();
    }

    public static int toInt(byte[] b) {
        ByteBuffer buffer = ByteBuffer.wrap(b);
        return buffer.getInt();
    }

    public static byte[] intToByteArray(int i) {
        return ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(i).array();
    }

    public static byte[] longToByteArray(long i) {
        return ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN).putLong(i).array();
    }

    public static byte[] doubleToByteArray(double value) {
        byte[] bytes = new byte[8];
        ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).putDouble(value);
        return bytes;
    }

    public static boolean byteToBoolean(byte b) {
        return b != (byte) 0;
    }

    public static byte[] append(byte[] a, byte[] b) {
        byte[] arr = new byte[a.length + b.length];
        System.arraycopy(a, 0, arr, 0, a.length);
        System.arraycopy(b, 0, arr, a.length, b.length);
        return arr;
    }
}
