package io.zzv.plugins.LimitOrderBook;

import io.zzv.views.LimitOrderBookPresenter;

import java.util.Arrays;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ConcurrentHashMap;

public class Loader implements  Runnable{

    static native long getGasForData(byte[] arr);

    static native byte[] run(byte[] arr);

    static void callback(int secret) {
        System.out.println("Secret: " + secret);
        callRun();
    }

    public static ConcurrentHashMap<Integer, Integer> orderIDMap = new ConcurrentHashMap<>();

    @Override
    public void run() {
        System.load(System.getProperty("user.home") + "/.java/packages/lib/" + System.mapLibraryName("golob"));
        System.out.printf("Gas required: %d\n", getGasForData("Test".getBytes()));

        startPlugin();
        // startSession(clientID);
        // startBook(tokenID, benchmarkID, clientID);
        // getPriceLevelOrders(tokenID, benchmarkID, clientID, bookType, (byte) 0, 101,
        // 0);
    }

    public static long toLong(byte[] b) {
        ByteBuffer buffer = ByteBuffer.wrap(b);
        return buffer.getLong();
    }

    private static int toInt(byte[] b) {
        ByteBuffer buffer = ByteBuffer.wrap(b);
        return buffer.getInt();
    }

    private static byte[] intToByteArray(int i) {
        return ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(i).array();
    }

    private static byte[] longToByteArray(long i) {
        return ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN).putLong(i).array();
    }

    public static byte[] doubleToByteArray(double value) {
        byte[] bytes = new byte[8];
        ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).putDouble(value);
        return bytes;
    }

    private static boolean byteToBoolean(byte b) {
        return b != (byte) 0;
    }

    private static byte[] append(byte[] a, byte[] b) {
        byte[] arr = new byte[a.length + b.length];
        System.arraycopy(a, 0, arr, 0, a.length);
        System.arraycopy(b, 0, arr, a.length, b.length);
        return arr;
    }

    private static void startSession(int clientID) {
        byte[] header = { 3, 1 }; // RoutingInfo
        header = append(header, longToByteArray(0)); // NodeTime
        header = append(header, intToByteArray(0)); // Owner: 0

        byte[] arr = { (byte) 140, (byte) 150 }; // MsgTypeTrade, SessionStarted
        arr = append(arr, intToByteArray(clientID));

        header = append(header, intToByteArray(arr.length));
        arr = append(header, arr);
        run(arr);
    }

    // private static void getPriceLevelOrders(int tokenID, int benchmarkID, int
    // clientID, byte bookType, byte orderSide,
    // int price, int priceDecimal) {
    // byte[] header = { 3, 1 }; // RoutingInfo
    // header = append(header, longToByteArray(0)); // NodeTime
    // header = append(header, intToByteArray(0)); // Owner: 0

    // byte[] arr = new byte[] { 0, (byte) 151, 10 }; // MsgTypeQuote,
    // PriceLevelOrders
    // arr = append(header, intToByteArray(clientID));
    // arr = append(arr, new byte[] { bookType, orderSide });
    // arr = append(arr, intToByteArray(tokenID)); // TokenID: IEO
    // arr = append(arr, intToByteArray(benchmarkID)); // BenchmarkID: IEO
    // arr = append(arr, intToByteArray(price));
    // arr = append(arr, intToByteArray(priceDecimal));

    // header = append(header, intToByteArray(arr.length));
    // arr = append(header, arr);
    // run(arr);
    // }

    private static void startPlugin() {
        // StartPlugin Message
        byte[] arr = { (byte) 0, (byte) 0 }; // MsgTypeStartPlugin
        int secret = 123456; // Mock secret, actual implementation in blockchain code
        arr = append(arr, intToByteArray(secret));
        run(arr);
    }

    private static void startBook(int tokenID, int benchmarkID, int clientID) {
        byte[] header = { 3, 1 }; // RoutingInfo
        header = append(header, longToByteArray(0)); // NodeTime
        header = append(header, intToByteArray(0)); // Owner: 0

        byte[] arr = { (byte) 140, (byte) 153 }; // MsgTypeTrade, BookStarted
        arr = append(arr, intToByteArray(clientID));
        arr = append(arr, intToByteArray(tokenID));
        arr = append(arr, intToByteArray(benchmarkID));

        header = append(header, intToByteArray(arr.length));
        arr = append(header, arr);
        run(arr);
    }

    public static void run (Order order) {
        run(Order.orderToBytes(order));
    }


    public static void callRun() {
        byte[] arr = { (byte) 255 }; // MsgTypeReceive
        byte[] rarr = run(arr);
        callProcessMsg(rarr);
    }

    private static void callProcessMsg(byte[] rarr) {
        processMsg(rarr);
    }

    public static void processMsg(byte[] rarr) {
        // TODO: Optimize method of receiving and sending msgs by using Unsafe class application memory pointer
        ByteBuffer buffer = ByteBuffer.wrap(Arrays.copyOfRange(rarr, 10, 14));
        int count = buffer.getInt();
        int pos = 14;
        byte[] inArr = Arrays.copyOfRange(rarr, 0, pos);
        for (int i = 0; i < count; i++) {
            int msgLen = toInt(Arrays.copyOfRange(rarr, pos + 0, pos + 4));
            Byte msgType = rarr[pos + 4];
            Byte msgSubType = rarr[pos + 5];
            long reportTime = toLong(Arrays.copyOfRange(rarr, pos + 6, pos + 14));
            int orderID = toInt(Arrays.copyOfRange(rarr, pos + 14, pos + 18));

            if (msgType == (byte) 1 && msgSubType == (byte) 1) { // MsgTypeGetOrderID
                int clientID = toInt(Arrays.copyOfRange(rarr, pos + 18, pos + 22));
                orderIDMap.put(clientID, orderID);
            } else {
                if (msgType == -116) { // msgType = MsgTypeTrade(140 = -116)
                    if (msgSubType >= 0 && msgSubType <= (byte) 100) { // Execution reports
                        byte bookType = rarr[pos + 18];
                        byte orderSide = rarr[pos + 19];
                        byte aon = rarr[pos + 20];
                        byte status = rarr[pos + 21];
                        byte liquidity = rarr[pos + 22];
                        int numPos = pos + 23;
                        int tokenID = toInt(Arrays.copyOfRange(rarr, numPos, numPos + 4));
                        numPos += 4;
                        int benchmarkID = toInt(Arrays.copyOfRange(rarr, numPos, numPos + 4));
                        numPos += 4;
                        int orderQty = toInt(Arrays.copyOfRange(rarr, numPos, numPos + 4));
                        numPos += 4;
                        int orderQtyDecimal = toInt(Arrays.copyOfRange(rarr, numPos, numPos + 4));
                        numPos += 4;

                        int filledQty = toInt(Arrays.copyOfRange(rarr, numPos, numPos + 4));
                        numPos += 4;
                        int filledQtyDecimal = toInt(Arrays.copyOfRange(rarr, numPos, numPos + 4));
                        numPos += 4;

                        int orderPrice = toInt(Arrays.copyOfRange(rarr, numPos, numPos + 4));
                        numPos += 4;
                        int orderPriceDecimal = toInt(Arrays.copyOfRange(rarr, numPos, numPos + 4));
                        numPos += 4;

                        int filledPrice = toInt(Arrays.copyOfRange(rarr, numPos, numPos + 4));
                        numPos += 4;
                        int filledPriceDecimal = toInt(Arrays.copyOfRange(rarr, numPos, numPos + 4));
                        String eventType = "";
                        switch (msgSubType){
                            case 20:
                                eventType = "OrderNew";
                                break;
                            case 21:
                                eventType = "OrderAccepted";
                                break;
                            case 22:
                                eventType = "OrderRejected";
                                break;
                            case 23:
                                eventType = "OrderOnMarket";
                                break;
                            case 24:
                                eventType = "OrderOffMarket";
                                break;
                            case 25:
                                eventType = "OrderCancelRejected";
                                break;
                            case 26:
                                eventType = "OrderCancelled";
                                break;
                            case 27:
                                eventType = "OrderReplaceRejected";
                                break;
                            case 28:
                                eventType = "OrderReplaced";
                                break;
                            case 29:
                                eventType = "OrderFilled";
                                break;
                            case 30:
                                eventType = "OrderDone";
                                break;                        }
                        LimitOrderBookPresenter.Response(String.format(
                                " %s\n MsgSubType:%d\n OrderID:%d\n bookType:%x\n liquidity:%x\n orderSide:%x\n tokenID:%d\n orderQty:%d.%d\n filledQty:%d.%d\n orderPrice:%d.%d\n filledPrice:%d.%d\n",
                                eventType, msgSubType, orderID, bookType, liquidity, orderSide, tokenID, orderQty, orderQtyDecimal,
                                filledQty, filledQtyDecimal, orderPrice, orderPriceDecimal, filledPrice,
                                filledPriceDecimal));
                    } else { // Admin msgs results
                        System.out.printf("MsgTypeTrade MsgSubType:%d\n", msgSubType, orderID);
                    }
                } else if (msgType == -105) { // MsgTypeQuote
                    switch (msgSubType) {
                        case 0: { // BestQuote
                            int numPos = pos + 18;
                            int tokenID = toInt(Arrays.copyOfRange(rarr, numPos, numPos + 4));
                            numPos += 4;
                            int bidPrice = toInt(Arrays.copyOfRange(rarr, numPos, numPos + 4));
                            numPos += 4;
                            int bidPriceDecimal = toInt(Arrays.copyOfRange(rarr, numPos, numPos + 4));
                            numPos += 4;

                            int bidQty = toInt(Arrays.copyOfRange(rarr, numPos, numPos + 4));
                            numPos += 4;
                            int bidQtyDecimal = toInt(Arrays.copyOfRange(rarr, numPos, numPos + 4));
                            numPos += 4;

                            int askPrice = toInt(Arrays.copyOfRange(rarr, numPos, numPos + 4));
                            numPos += 4;
                            int askPriceDecimal = toInt(Arrays.copyOfRange(rarr, numPos, numPos + 4));
                            numPos += 4;

                            int askQty = toInt(Arrays.copyOfRange(rarr, numPos, numPos + 4));
                            numPos += 4;
                            int askQtyDecimal = toInt(Arrays.copyOfRange(rarr, numPos, numPos + 4));
                            System.out.printf(
                                    "BestQuote OrderID:%d id:%x subID:%x tokenID:%d BidPrice:%d.%d bidQty:%d.%d askPrice:%d.%d askQty:%d.%d\n",
                                    orderID, msgType, msgSubType, tokenID, bidPrice, bidPriceDecimal, bidQty,
                                    bidQtyDecimal, askPrice, askPriceDecimal, askQty, askQtyDecimal);
                        }
                        break;
                        case 1: // BestBid
                        case 2: {// Best Ask
                            int numPos = pos + 18;
                            int tokenID = toInt(Arrays.copyOfRange(rarr, numPos, numPos + 4));
                            numPos += 4;
                            int price = toInt(Arrays.copyOfRange(rarr, numPos, numPos + 4));
                            numPos += 4;
                            int priceDecimal = toInt(Arrays.copyOfRange(rarr, numPos, numPos + 4));
                            numPos += 4;

                            int qty = toInt(Arrays.copyOfRange(rarr, numPos, numPos + 4));
                            numPos += 4;
                            int qtyDecimal = toInt(Arrays.copyOfRange(rarr, numPos, numPos + 4));

                            System.out.printf(
                                    "BestBid/Ask OrderID:%d id:%x subID:%x tokenID:%d Price:%d.%d Qty:%d.%d\n", orderID,
                                    msgType, msgSubType, tokenID, price, priceDecimal, qty, qtyDecimal);
                        }
                        break;
                        case 9: {// PriceBookSnapshot
                            int numPos = pos + 19;
                            int tokenID = toInt(Arrays.copyOfRange(rarr, numPos, numPos + 4));
                            numPos += 8; // skipping BenchmarkID
                            int groupCount = toInt(Arrays.copyOfRange(rarr, numPos, numPos + 4));
                            System.out.printf("PriceBookSnapshot SeqID:%d id:%x subID:%x tokenID:%d groupCount:%d\n",
                                    orderID, msgType, msgSubType, tokenID, groupCount);
                        }
                        break;
                        case 10: { // PriceLevelOrders
                            int numPos = pos + 10;
                            int tokenID = toInt(Arrays.copyOfRange(rarr, numPos, numPos + 4));
                            numPos += 4;
                            int groupCount = toInt(Arrays.copyOfRange(rarr, numPos, numPos + 4));

                            System.out.printf("PriceLevelOrders SeqID:%d id:%x subID:%x tokenID:%d groupCount:%d\n",
                                    orderID, msgType, msgSubType, tokenID, groupCount);
                        }
                        break;
                        case 12:
                        case 13:
                        case 14: { // BookAdded, deleted and updated
                            byte bookType = rarr[pos + 18];
                            int numPos = pos + 19;
                            int tokenID = toInt(Arrays.copyOfRange(rarr, numPos, numPos + 4));
                            numPos += 4;
                            int benchmarkID = toInt(Arrays.copyOfRange(rarr, numPos, numPos + 4));
                            numPos += 4;
                            byte bookSide = rarr[numPos];
                            numPos += 1;
                            int price = toInt(Arrays.copyOfRange(rarr, numPos, numPos + 4));
                            numPos += 4;
                            int priceDecimal = toInt(Arrays.copyOfRange(rarr, numPos, numPos + 4));

                            System.out.printf(
                                    "BookUpdated SeqID:%d id:%x subID:%x tokenID:%d benchmarkID:%d BookType:%x BookSide:%x price:%d.%d\n",
                                    orderID, msgType, msgSubType, tokenID, benchmarkID, bookType, bookSide, price,
                                    priceDecimal);
                        }
                        break;
                        default:

                    }
                }
            }
            pos += msgLen + 4;
        }
    }

}
