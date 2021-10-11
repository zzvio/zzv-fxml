package io.zzv.plugins.LimitOrderBook;

import io.zzv.utils;

public class Order {
    public int tokenID, benchmarkID, clientID, orderQty, orderQtyDecimal, orderPrice, orderPriceDecimal;
    public byte msgType, msgSubtype, bookType, orderSide;

    public Order(int tokenID, int benchmarkID, int clientID, byte msgType, byte msgSubtype,
                      byte bookType, byte orderSide, int orderQty, int orderQtyDecimal, int orderPrice, int orderPriceDecimal) {
        this.tokenID = tokenID;
        this.benchmarkID = benchmarkID;
        this.clientID = clientID;
        this.msgType = msgType;
        this.msgSubtype = msgSubtype;
        this.bookType = bookType;
        this.orderSide = orderSide;
        this.orderQty = orderQty;
        this.orderQtyDecimal = orderQtyDecimal;
        this.orderPrice = orderPrice;
        this.orderPriceDecimal = orderPriceDecimal;
    }
    public static byte[] orderToBytes(Order order){
        byte[] header = { 3, 1 }; // RoutingInfo
        header = utils.append(header, utils.longToByteArray(0)); // NodeTime
        header = utils.append(header, utils.intToByteArray(0)); // Owner: 0

        byte[] arr = { order.msgType, order.msgSubtype };
        arr = utils.append(arr, utils.longToByteArray(0)); // OrderTime
        arr = utils.append(arr, utils.intToByteArray(order.clientID));
        byte aon = (byte) 0;
        arr = utils.append(arr, new byte[] { order.bookType, order.orderSide, aon });
        arr = utils.append(arr, utils.intToByteArray(order.tokenID));
        arr = utils.append(arr, utils.intToByteArray(order.benchmarkID));

        arr = utils.append(arr, utils.intToByteArray(order.orderQty));
        arr = utils.append(arr, utils.intToByteArray(order.orderQtyDecimal));
        arr = utils.append(arr, utils.intToByteArray(order.orderPrice));
        arr = utils.append(arr, utils.intToByteArray(order.orderPriceDecimal));

        header = utils.append(header, utils.intToByteArray(arr.length));
        arr = utils.append(header, arr);
        return arr;
    }

}
