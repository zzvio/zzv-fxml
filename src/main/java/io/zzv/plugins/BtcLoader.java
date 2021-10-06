package io.zzv.plugins;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicBoolean;

public class BtcLoader implements  Runnable {
    static native byte[] call(byte[] arr);

    static void callback(int token) {
        System.out.println("Token: " + token);
    }

    @Override
    public void run() {
        String fileName = System.getProperty("user.home") + "/.java/packages/lib/" + System.mapLibraryName("gobtc");
        System.out.println("Loading BTC plugin " + fileName);
        System.load(fileName);
    }

    public void start(){
        // Commandline arguments passed to GoPlugin to start Ethereum node with these
        // arguments.
        String goArgs = "--testnet ";

        startPlugin(goArgs);
        System.out.println("BTC plugin loaded ");
    }

    private static void startPlugin(String args) {
        // StartPlugin Message
        byte[] arr = { (byte) 0, (byte) 0 }; // MsgTypeStartPlugin
        int token = 123456; // Mock token, actual implementation in client code
        arr = append(arr, intToByteArray(token));
        arr = append(arr, args.getBytes());
        call(arr);
    }

    public void stopPlugin() {
        // StopPlugin Message
        byte[] arr = { (byte) 2, (byte) 2 }; // MsgTypeStopPlugin
        int token = 123456; // Mock token, actual implementation in client code
        arr = append(arr, intToByteArray(token));
        call(arr);
        System.out.println("BTC plugin stopped ");
    }

    private static byte[] append(byte[] a, byte[] b) {
        byte[] arr = new byte[a.length + b.length];
        System.arraycopy(a, 0, arr, 0, a.length);
        System.arraycopy(b, 0, arr, a.length, b.length);
        return arr;
    }

    private static byte[] intToByteArray(int i) {
        return ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(i).array();
    }

}
