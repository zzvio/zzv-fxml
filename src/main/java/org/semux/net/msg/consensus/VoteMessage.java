/**
 * Copyright (c) 2017-2020 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.net.msg.consensus;

import org.semux.consensus.Vote;
import org.semux.net.msg.Message;
import org.semux.net.msg.MessageCode;

public class VoteMessage extends Message {

    private final Vote vote;

    public VoteMessage(Vote vote) {
        super(MessageCode.BFT_VOTE, null);

        this.vote = vote;

        // TODO: consider wrapping by simple codec
        this.body = vote.toBytes();
    }

    public VoteMessage(byte[] body) {
        super(MessageCode.BFT_VOTE, null);

        this.vote = Vote.fromBytes(body);

        this.body = body;
    }

    public Vote getVote() {
        return vote;
    }

    @Override
    public String toString() {
        return "BFTVoteMessage: " + vote;
    }
}
