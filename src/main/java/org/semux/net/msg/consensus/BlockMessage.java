/**
 * Copyright (c) 2017-2020 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.net.msg.consensus;

import org.semux.core.Block;
import org.semux.net.msg.Message;
import org.semux.net.msg.MessageCode;

public class BlockMessage extends Message {

    private final Block block;

    public BlockMessage(Block block) {
        super(MessageCode.BLOCK, null);

        this.block = block;

        this.body = block.toBytes();
    }

    public BlockMessage(byte[] body) {
        super(MessageCode.BLOCK, null);

        this.block = Block.fromBytes(body);

        this.body = body;
    }

    public Block getBlock() {
        return block;
    }

    @Override
    public String toString() {
        return "BlockMessage [block=" + block + "]";
    }
}
