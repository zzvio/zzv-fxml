/**
 * Copyright (c) 2017-2020 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.net.msg.p2p.handshake.v2;

import java.util.ArrayList;
import java.util.List;

import org.semux.Network;
import org.semux.config.Config;
import org.semux.crypto.Hex;
import org.semux.crypto.Key;
import org.semux.net.Peer;
import org.semux.net.msg.Message;
import org.semux.net.msg.MessageCode;
import org.semux.util.SimpleDecoder;
import org.semux.util.SimpleEncoder;
import org.semux.util.TimeUtil;

public abstract class HandshakeMessage extends Message {

    protected final Network network;
    protected final short networkVersion;

    protected final String peerId;
    protected final int port;

    protected final String clientId;
    protected final String[] capabilities;

    protected final long latestBlockNumber;

    protected final byte[] secret;
    protected final long timestamp;
    protected final Key.Signature signature;

    public HandshakeMessage(MessageCode code, Class<?> responseMessageClass,
            Network network, short networkVersion, String peerId, int port,
            String clientId, String[] capabilities, long latestBlockNumber,
            byte[] secret, Key coinbase) {
        super(code, responseMessageClass);

        this.network = network;
        this.networkVersion = networkVersion;
        this.peerId = peerId;
        this.port = port;
        this.clientId = clientId;
        this.capabilities = capabilities;
        this.latestBlockNumber = latestBlockNumber;
        this.secret = secret;
        this.timestamp = TimeUtil.currentTimeMillis();

        SimpleEncoder enc = encodeBasicInfo();
        this.signature = coinbase.sign(enc.toBytes());
        enc.writeBytes(signature.toBytes());

        this.body = enc.toBytes();
    }

    public HandshakeMessage(MessageCode code, Class<?> responseMessageClass, byte[] body) {
        super(code, responseMessageClass);

        SimpleDecoder dec = new SimpleDecoder(body);
        this.network = Network.of(dec.readByte());
        this.networkVersion = dec.readShort();
        this.peerId = dec.readString();
        this.port = dec.readInt();
        this.clientId = dec.readString();
        List<String> capabilities = new ArrayList<>();
        for (int i = 0, size = dec.readInt(); i < size; i++) {
            capabilities.add(dec.readString());
        }
        this.capabilities = capabilities.toArray(new String[0]);
        this.latestBlockNumber = dec.readLong();
        this.secret = dec.readBytes();
        this.timestamp = dec.readLong();
        this.signature = Key.Signature.fromBytes(dec.readBytes());

        this.body = body;
    }

    protected SimpleEncoder encodeBasicInfo() {
        SimpleEncoder enc = new SimpleEncoder();

        enc.writeByte(network.id());
        enc.writeShort(networkVersion);
        enc.writeString(peerId);
        enc.writeInt(port);
        enc.writeString(clientId);
        enc.writeInt(capabilities.length);
        for (String capability : capabilities) {
            enc.writeString(capability);
        }
        enc.writeLong(latestBlockNumber);
        enc.writeBytes(secret);
        enc.writeLong(timestamp);

        return enc;
    }

    /**
     * Validates this HELLO message.
     *
     * <p>
     * NOTE: only data format and signature is checked here.
     * </p>
     *
     * @param config
     * @return true if success, otherwise false
     */
    public boolean validate(Config config) {
        if (network == config.network()
                && networkVersion == config.networkVersion()
                && peerId != null && peerId.length() == 40
                && port > 0 && port <= 65535
                && clientId != null && clientId.length() < 128
                && latestBlockNumber >= 0
                && secret != null && secret.length == InitMessage.SECRET_LENGTH
                && Math.abs(TimeUtil.currentTimeMillis() - timestamp) <= config.netHandshakeExpiry()
                && signature != null
                && peerId.equals(Hex.encode(signature.getAddress()))) {

            SimpleEncoder enc = encodeBasicInfo();
            return Key.verify(enc.toBytes(), signature);
        } else {
            return false;
        }
    }

    /**
     * Constructs a Peer object from the handshake info.
     *
     * @param ip
     * @return
     */
    public Peer getPeer(String ip) {
        return new Peer(network, networkVersion, peerId, ip, port, clientId, capabilities, latestBlockNumber);
    }

    /**
     * Returns the secret.
     *
     * @return
     */
    public byte[] getSecret() {
        return secret;
    }
}
