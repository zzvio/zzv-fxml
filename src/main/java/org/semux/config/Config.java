/**
 * Copyright (c) 2017-2020 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.config;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.semux.Network;
import org.semux.core.Amount;
import org.semux.core.Fork;
import org.semux.net.CapabilityTreeSet;
import org.semux.net.NodeManager.Node;
import org.semux.net.msg.MessageCode;

/**
 * Describes the blockchain configurations.
 */
public interface Config {

    /**
     * Returns the chain specification.
     *
     * @return
     */
    ChainSpec spec();

    // =========================
    // General
    // =========================

    /**
     * Returns the data directory.
     *
     * @return
     */
    File rootDir();

    /**
     * Returns the database directory.
     *
     * @return
     */
    File chainDir();

    /**
     * Returns the database directory.
     *
     * @return
     */
    File chainDir(Network network);

    /**
     * Returns the config directory.
     *
     * @return
     */
    File configDir();

    /**
     * Returns the wallet directory.
     *
     * @return
     */
    File walletDir();

    /**
     * Returns the log directory.
     *
     * @return
     */
    File logDir();

    /**
     * Returns the network.
     *
     * @return
     */
    Network network();

    /**
     * Returns the network id.
     *
     * @return
     */
    short networkVersion();

    /**
     * Returns the client id.
     *
     * @return
     */
    String getClientId();

    /**
     * Returns the capabilities of the client.
     * 
     * @return
     */
    CapabilityTreeSet getClientCapabilities();

    // =========================
    // P2P
    // =========================

    /**
     * Returns the declared IP address.
     */
    Optional<String> p2pDeclaredIp();

    /**
     * Returns the p2p listening IP address.
     *
     * @return
     */
    String p2pListenIp();

    /**
     * Returns the p2p listening port.
     *
     * @return
     */
    int p2pListenPort();

    /**
     * Returns a set of seed nodes for P2P.
     *
     * @return
     */
    Set<Node> p2pSeedNodes();

    // =========================
    // Network
    // =========================
    /**
     * Returns the max number of outbound connections.
     *
     * @return
     */
    int netMaxOutboundConnections();

    /**
     * Returns the max number of inbound connections.
     *
     * @return
     */
    int netMaxInboundConnections();

    /**
     * Returns the max number of inbound connections of each unique IP.
     *
     * @return
     */
    int netMaxInboundConnectionsPerIp();

    /**
     * Returns the max message queue size.
     *
     * @return
     */
    int netMaxMessageQueueSize();

    /**
     * Returns the max size of frame body, in bytes.
     *
     * @return
     */
    int netMaxFrameBodySize();

    /**
     * Returns the max size of packet, in bytes.
     *
     * @return
     */
    int netMaxPacketSize();

    /**
     * Returns the message broadcast redundancy.
     *
     * @return
     */
    int netRelayRedundancy();

    /**
     * Returns the handshake expire time in milliseconds.
     *
     * @return
     */
    int netHandshakeExpiry();

    /**
     * Returns the channel idle timeout.
     *
     * @return
     */
    int netChannelIdleTimeout();

    /**
     * Returns a set of prioritized messages.
     *
     * @return
     */
    Set<MessageCode> netPrioritizedMessages();

    /**
     * Returns a list of DNS seeds for main network
     *
     * @return
     */
    List<String> netDnsSeedsMainNet();

    /**
     * Returns a list of DNS seeds for test network
     *
     * @return
     */
    List<String> netDnsSeedsTestNet();

    // =========================
    // Sync
    // =========================

    /**
     * Returns the download timeout in milliseconds
     *
     * @return
     */
    long syncDownloadTimeout();

    /**
     * Returns the max number of queued jobs
     *
     * @return
     */
    int syncMaxQueuedJobs();

    /**
     * Returns the max number of pending jobs
     *
     * @return
     */
    int syncMaxPendingJobs();

    /**
     * Returns the max number of pending blocks
     *
     * @return
     */
    int syncMaxPendingBlocks();

    /**
     * Returns whether to disconnect the peer is an invalid block is received.
     *
     * @return
     */
    boolean syncDisconnectOnInvalidBlock();

    /**
     * Returns whether to use FAST_SYNC. When enabled, votes are downloaded for
     * pivot blocks only.
     *
     * @return
     */
    boolean syncFastSync();

    // =========================
    // API
    // =========================

    /**
     * Returns whether API is enabled.
     *
     * @return
     */
    boolean apiEnabled();

    /**
     * Returns the API listening IP address.
     *
     * @return
     */
    String apiListenIp();

    /**
     * Returns the API listening port.
     *
     * @return
     */
    int apiListenPort();

    /**
     * Returns the user name for API basic authentication.
     *
     * @return
     */
    String apiUsername();

    /**
     * Returns the password for API basic authentication.
     *
     * @return
     */
    String apiPassword();

    /**
     * Returns enabled public API services.
     *
     * @return
     */
    String[] apiPublicServices();

    /**
     * Returns enabled private API services.
     *
     * @return
     */
    String[] apiPrivateServices();

    // =========================
    // BFT consensus
    // =========================

    /**
     * Returns the duration of NEW_HEIGHT state. This allows validators to catch up.
     *
     * @return
     */
    long bftNewHeightTimeout();

    /**
     * Returns the duration of PROPOSE state.
     *
     * @return
     */
    long bftProposeTimeout();

    /**
     * Returns the duration of VALIDATE state.
     *
     * @return
     */
    long bftValidateTimeout();

    /**
     * Return the duration of PRE_COMMIT state.
     *
     * @return
     */
    long bftPreCommitTimeout();

    /**
     * Return the duration of COMMIT state. May be skipped after +2/3 commit votes.
     *
     * @return
     */
    long bftCommitTimeout();

    /**
     * Return the duration of FINALIZE state. This allows validators to persist
     * block.
     *
     * @return
     */
    long bftFinalizeTimeout();

    /**
     * Returns the maximum time drift of a block time in the future.
     *
     * @return
     */
    long bftMaxBlockTimeDrift();

    // =========================
    // Transaction pool
    // =========================

    /**
     * Returns the maximum gas limit for a block proposal, local setting
     *
     * @return
     */
    int poolMaxTotalGasConsumed();

    /**
     * Returns the max gas limit for any transaction proposed, local setting
     *
     * @return
     */
    long poolMaxTxGasLimit();

    /**
     * Returns the minimum gas price for any transaction proposed, local setting
     * 
     * @return
     */
    Amount poolMinGasPrice();

    /**
     * Returns the maximum allowed time drift between transaction timestamp and
     * local clock.
     *
     * @return
     */
    long poolMaxTxTimeDrift();

    // =========================
    // UI
    // =========================

    /**
     * Returns the localization of UI
     *
     * @return
     */
    Locale uiLocale();

    /**
     * Returns the unit of displayed values.
     *
     * @return
     */
    String uiUnit();

    /**
     * Returns the fraction digits of displayed values.
     *
     * @return
     */
    int uiFractionDigits();

    // =========================
    // Forks
    // =========================

    /**
     * Returns whether UNIFORM_DISTRIBUTION fork is enabled.
     *
     * @return
     */
    boolean forkUniformDistributionEnabled();

    /**
     * Returns whether VIRTUAL_MACHINE fork is enabled.
     *
     * @return
     */
    boolean forkVirtualMachineEnabled();

    /**
     * Returns whether VOTING_PRECOMPILED_UPGRADE fork is enabled.
     *
     * @return
     */
    boolean forkVotingPrecompiledUpgradeEnabled();

    // =========================
    // Checkpoints
    // =========================

    /**
     * Get checkpoints.
     *
     * @return a map of blockchain checkpoints [block height] => [block hash]
     */
    Map<Long, byte[]> checkpoints();

    /**
     * Returns manually activated forks.
     *
     * @return a map of Validator-Activated fork activation checkpoints [fork] =>
     *         [block height]
     */
    Map<Fork, Long> manuallyActivatedForks();
}
