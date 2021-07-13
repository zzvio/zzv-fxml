/**
 * Copyright (c) 2017-2020 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.net.msg.consensus;

import org.semux.net.msg.Message;
import org.semux.net.msg.MessageCode;
import org.semux.util.SimpleDecoder;
import org.semux.util.SimpleEncoder;

public class GetBlockMessage extends Message {

    private final long number;

    public GetBlockMessage(long number) {
        super(MessageCode.GET_BLOCK, BlockMessage.class);
        this.number = number;

        SimpleEncoder enc = new SimpleEncoder();
        enc.writeLong(number);
        this.body = enc.toBytes();
    }

    public GetBlockMessage(byte[] body) {
        super(MessageCode.GET_BLOCK, BlockMessage.class);

        SimpleDecoder dec = new SimpleDecoder(body);
        this.number = dec.readLong();

        this.body = body;
    }

    public long getNumber() {
        return number;
    }

    @Override
    public String toString() {
        return "GetBlockMessage [number=" + number + "]";
    }
}
