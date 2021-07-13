/**
 * Copyright (c) 2017-2020 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.net;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.semux.Kernel;
import org.semux.net.filter.SemuxIpFilter;

/**
 * Channel Manager.
 * 
 */
public class ChannelManager {

    private static final Logger logger = Logger.getLogger(ChannelManager.class.getName());

    /**
     * All channels, indexed by the <code>remoteAddress (ip + port)</code>, not
     * necessarily the listening address.
     */
    protected ConcurrentHashMap<InetSocketAddress, Channel> channels = new ConcurrentHashMap<>();
    protected ConcurrentHashMap<String, Channel> activeChannels = new ConcurrentHashMap<>();

    protected final SemuxIpFilter ipFilter;

    public ChannelManager(Kernel kernel) {
        ipFilter = new SemuxIpFilter.Loader()
                .load(new File(kernel.getConfig().configDir(), SemuxIpFilter.CONFIG_FILE).toPath());
    }

    /**
     * Returns the IP filter if enabled.
     * 
     * @return
     */
    public SemuxIpFilter getIpFilter() {
        return ipFilter;
    }

    /**
     * Returns whether a connection from the given address is acceptable or not.
     * 
     * @param address
     * @return
     */
    public boolean isAcceptable(InetSocketAddress address) {
        return ipFilter == null || ipFilter.isAcceptable(address);
    }

    /**
     * Returns whether a socket address is connected.
     * 
     * @param address
     * @return
     */
    public boolean isConnected(InetSocketAddress address) {
        return channels.containsKey(address);
    }

    /**
     * Returns whether the specified IP is connected.
     * 
     * @param ip
     * @return
     */
    public boolean isActiveIP(String ip) {
        for (Channel c : activeChannels.values()) {
            if (c.getRemoteIp().equals(ip)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns whether the specified peer is connected.
     * 
     * @param peerId
     * @return
     */
    public boolean isActivePeer(String peerId) {
        return activeChannels.containsKey(peerId);
    }

    /**
     * Returns the number of channels.
     * 
     * @return
     */
    public int size() {
        return channels.size();
    }

    /**
     * Adds a new channel to this manager.
     * 
     * @param ch
     *            channel instance
     */
    public void add(Channel ch) {
        logger.finest(String.format("Channel added: remoteAddress = %s:%s", ch.getRemoteIp(), ch.getRemotePort()));

        channels.put(ch.getRemoteAddress(), ch);
    }

    /**
     * Removes a disconnected channel from this manager.
     * 
     * @param ch
     *            channel instance
     */
    public void remove(Channel ch) {
        logger.finest(String.format("Channel removed: remoteAddress = %s:%s", ch.getRemoteIp(), ch.getRemotePort()));

        channels.remove(ch.getRemoteAddress());
        if (ch.isActive()) {
            activeChannels.remove(ch.getRemotePeer().getPeerId());
            ch.setInactive();
        }
    }

    /**
     * Closes all blacklisted channels.
     */
    public void closeBlacklistedChannels() {
        for (Map.Entry<InetSocketAddress, Channel> entry : channels.entrySet()) {
            Channel channel = entry.getValue();
            if (!isAcceptable(channel.getRemoteAddress())) {
                remove(channel);
                channel.close();
            }
        }
    }

    /**
     * When a channel becomes active.
     * 
     * @param channel
     * @param peer
     */
    public void onChannelActive(Channel channel, Peer peer) {
        channel.setActive(peer);
        activeChannels.put(peer.getPeerId(), channel);
    }

    /**
     * Returns a copy of the active peers.
     * 
     * @return
     */
    public List<Peer> getActivePeers() {
        List<Peer> list = new ArrayList<>();

        for (Channel c : activeChannels.values()) {
            list.add(c.getRemotePeer());
        }

        return list;
    }

    /**
     * Returns the listening IP addresses of active peers.
     * 
     * @return
     */
    public Set<InetSocketAddress> getActiveAddresses() {
        Set<InetSocketAddress> set = new HashSet<>();

        for (Channel c : activeChannels.values()) {
            Peer p = c.getRemotePeer();
            set.add(new InetSocketAddress(p.getIp(), p.getPort()));
        }

        return set;
    }

    /**
     * Returns the active channels.
     * 
     * @return
     */
    public List<Channel> getActiveChannels() {

        return new ArrayList<>(activeChannels.values());
    }

    /**
     * Returns the active channels, filtered by peerId.
     * 
     * @param peerIds
     *            peerId filter
     * @return
     */
    public List<Channel> getActiveChannels(List<String> peerIds) {
        List<Channel> list = new ArrayList<>();

        for (String peerId : peerIds) {
            if (activeChannels.containsKey(peerId)) {
                list.add(activeChannels.get(peerId));
            }
        }

        return list;
    }

    /**
     * Returns the active channels, whose message queue is idle.
     * 
     * @return
     */
    public List<Channel> getIdleChannels() {
        List<Channel> list = new ArrayList<>();

        for (Channel c : activeChannels.values()) {
            if (c.getMessageQueue().isIdle()) {
                list.add(c);
            }
        }

        return list;
    }
}
