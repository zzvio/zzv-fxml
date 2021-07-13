/**
 * Copyright (c) 2017-2020 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.net;

import java.net.InetSocketAddress;
import java.util.logging.Logger;

import org.semux.Kernel;
import org.semux.net.NodeManager.Node;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.socket.nio.NioSocketChannel;

public class SemuxChannelInitializer extends ChannelInitializer<NioSocketChannel> {

    private static final Logger logger = Logger.getLogger(SemuxChannelInitializer.class.getName());

    private final Kernel kernel;
    private final ChannelManager channelMgr;

    private final Node remoteNode;
    private final boolean discoveryMode;

    /**
     * Create an instance of SemuxChannelInitializer.
     * 
     * @param kernel
     *            the kernel instance
     * @param remoteNode
     *            the remote node to connect, or null if in server mode
     * @param discoveryMode
     *            whether in discovery mode or not
     */
    public SemuxChannelInitializer(Kernel kernel, Node remoteNode, boolean discoveryMode) {
        this.kernel = kernel;
        this.channelMgr = kernel.getChannelManager();

        this.remoteNode = remoteNode;
        this.discoveryMode = discoveryMode;
    }

    public SemuxChannelInitializer(Kernel kernel, Node remoteNode) {
        this(kernel, remoteNode, false);
    }

    @Override
    public void initChannel(NioSocketChannel ch) throws Exception {
        try {
            InetSocketAddress address = isServerMode() ? ch.remoteAddress() : remoteNode.toAddress();
            logger.finest(String.format("New %s channel: remoteAddress = %s:%s", isServerMode() ? "inbound" : "outbound",
                    address.getAddress().getHostAddress(), address.getPort()));

            if (!channelMgr.isAcceptable(address)) {
                logger.finest(String.format("Disallowed %s connection: %s", isServerMode() ? "inbound" : "outbound",
                        address.toString()));
                ch.disconnect();
                return;
            }

            Channel channel = new Channel(ch);
            channel.init(ch.pipeline(), isServerMode(), address, kernel);
            if (!isDiscoveryMode()) {
                channelMgr.add(channel);
            }

            // limit the size of receiving buffer
            int bufferSize = Frame.HEADER_SIZE + kernel.getConfig().netMaxFrameBodySize();
            ch.config().setRecvByteBufAllocator(new FixedRecvByteBufAllocator(bufferSize));
            ch.config().setOption(ChannelOption.SO_RCVBUF, bufferSize);
            ch.config().setOption(ChannelOption.SO_BACKLOG, 1024);

            // notify disconnection to channel manager
            ch.closeFuture().addListener(future -> {
                if (!isDiscoveryMode()) {
                    channelMgr.remove(channel);
                }
            });
        } catch (Exception e) {
            logger.severe(String.format("Exception in channel initializer", e));
        }
    }

    /**
     * Returns whether is in server mode.
     * 
     * @return
     */
    public boolean isServerMode() {
        return remoteNode == null;
    }

    /**
     * Returns whether is in discovery mode.
     * 
     * @return
     */
    public boolean isDiscoveryMode() {
        return discoveryMode;
    }
}
