/**
 * Copyright (c) 2017-2020 The Semux Developers
 * <p>
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

import org.semux.config.Config;
import org.semux.consensus.SemuxBft;
import org.semux.consensus.SemuxSync;
import org.semux.core.*;
import org.semux.crypto.Key;
import org.semux.db.DatabaseFactory;
import org.semux.db.DatabaseName;
import org.semux.db.LeveldbDatabase.LeveldbFactory;
import org.semux.event.KernelBootingEvent;
import org.semux.event.PubSub;
import org.semux.event.PubSubFactory;
import org.semux.net.ChannelManager;
import org.semux.net.NodeManager;
import org.semux.net.PeerClient;
import org.semux.net.PeerServer;
import org.semux.util.Bytes;
import org.semux.util.TimeUtil;
import org.semux.vm.client.SemuxBlock;

import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.OperatingSystem;

/**
 * Kernel holds references to each individual components.
 */
public class Kernel {

    // Fix JNA issue: There is an incompatible JNA native library installed
    // Fix JNA issue: There is an incompatible JNA native library installed
//    static {
//        System.setProperty("jna.nosys", "true");
//        //ours  System.setProperty("jna.noclasspath","true");
//    }

    private static final Logger logger = Logger.getLogger(Kernel.class.getName());

    private static final PubSub pubSub = PubSubFactory.getDefault();

    public enum State {
        STOPPED, BOOTING, RUNNING, STOPPING
    }

    protected State state = State.STOPPED;

    protected Config config;
    protected Genesis genesis;

    protected Wallet wallet;
    protected Key coinbase;

    protected DatabaseFactory dbFactory;
    protected Blockchain chain;
    protected PeerClient client;

    protected ChannelManager channelMgr;
    protected PendingManager pendingMgr;
    protected NodeManager nodeMgr;

    protected PeerServer p2p;

    protected Thread consThread;
    protected SemuxSync sync;
    protected SemuxBft bft;

    private final byte[] DUMMY_ADDRESS = new Key().toAddress();

    private static Kernel instance = null;

    public static Kernel getInstance() throws RuntimeException{
        if ( instance != null ){
            return instance;
        }
        throw new RuntimeException("Kernel is not initialized!");
    }
    /**
     * Creates a kernel instance and initializes it.
     *
     * @param config
     *            the config instance
     * @prarm genesis the genesis instance
     * @param wallet
     *            the wallet instance
     * @param coinbase
     *            the coinbase key
     */
    public Kernel(Config config, Genesis genesis, Wallet wallet, Key coinbase) {
        this.config = config;
        this.genesis = genesis;
        this.wallet = wallet;
        this.coinbase = coinbase;
    }

    /**
     * Start the kernel.
     */
    public synchronized void start() {
        if (state != State.STOPPED) {
            return;
        } else {
            state = State.BOOTING;
            pubSub.publish(new KernelBootingEvent());
        }

        // ====================================
        // print system info
        // ====================================
        logger.info(config.getClientId());
        logger.info(String.format("System booting up: network = %s, networkVersion = %s, coinbase = %s", config.network(),
                config.networkVersion(),
                coinbase));
        printSystemInfo();
        TimeUtil.startNtpProcess();

        // ====================================
        // initialize blockchain database
        // ====================================
        dbFactory = new LeveldbFactory(config.chainDir());
        chain = new BlockchainImpl(config, genesis, dbFactory);
        long number = chain.getLatestBlockNumber();
        logger.info(String.format("Latest block number = %s", number));

        // ====================================
        // set up client
        // ====================================
        client = new PeerClient(config, coinbase);

        // ====================================
        // start channel/pending/node manager
        // ====================================
        channelMgr = new ChannelManager(this);
        pendingMgr = new PendingManager(this);
        nodeMgr = new NodeManager(this);

        pendingMgr.start();
        nodeMgr.start();

        // ====================================
        // start p2p module
        // ====================================
        p2p = new PeerServer(this);
        p2p.start();

        // ====================================
        // start sync/consensus
        // ====================================
        sync = new SemuxSync(this);
        bft = new SemuxBft(this);

        consThread = new Thread(bft::start, "consensus");
        consThread.start();

        // ====================================
        // add port forwarding
        // ====================================
        new Thread(this::setupUpnp, "upnp").start();

        // ====================================
        // register shutdown hook
        // ====================================
        Launcher.registerShutdownHook("kernel", this::stop);

        state = State.RUNNING;
    }

    /**
     * Moves database to another directory.
     *
     * @param srcDir
     * @param dstDir
     */
    private void moveDatabase(File srcDir, File dstDir) {
        // store the sub-folders
        File[] files = srcDir.listFiles();

        // create the destination folder
        dstDir.mkdirs();

        // move to destination
        for (File f : Objects.requireNonNull(files)) {
            f.renameTo(new File(dstDir, f.getName()));
        }
    }

    /**
     * Prints system info.
     */
    protected void printSystemInfo() {
        if (true)
            return;

        try {
            SystemInfo si = new SystemInfo();
            HardwareAbstractionLayer hal = si.getHardware();

            // computer system
            ComputerSystem cs = hal.getComputerSystem();
            logger.info(String.format("Computer: manufacturer = %s, model = %s", cs.getManufacturer(), cs.getModel()));

            // operating system
            OperatingSystem os = si.getOperatingSystem();
            logger.info(String.format("OS: name = %s", os));

            // cpu
            CentralProcessor cp = hal.getProcessor();
            logger.info(String.format("CPU: processor = %s, cores = %s / %s", cp, cp.getPhysicalProcessorCount(),
                    cp.getLogicalProcessorCount()));

            // memory
            GlobalMemory m = hal.getMemory();
            long mb = 1024L * 1024L;
            logger.info(String.format("Memory: total = %s MB, available = %s MB", m.getTotal() / mb, m.getAvailable() / mb));

            // disk
            for (HWDiskStore disk : hal.getDiskStores()) {
                logger.info(String.format("Disk: name = %s, size = %s MB", disk.getName(), disk.getSize() / mb));
            }

            // network
            for (NetworkIF net : hal.getNetworkIFs()) {
                logger.info(String.format("Network: name = %s, ip = [%s]", net.getDisplayName(), String.join(",", net.getIPv4addr())));
            }

            // java version
            logger.info(String.format("Java: version = %s, xmx = %s MB", System.getProperty("java.version"),
                    Runtime.getRuntime().maxMemory() / mb));
        } catch (RuntimeException e) {
            logger.severe(String.format("Unable to retrieve System information.", e));
        }
    }

    /**
     * Sets up uPnP port mapping.
     */
    protected void setupUpnp() {
        return;
//        try {
//            GatewayDiscover discover = new GatewayDiscover();
//            Map<InetAddress, GatewayDevice> devices = discover.discover();
//            for (Map.Entry<InetAddress, GatewayDevice> entry : devices.entrySet()) {
//                GatewayDevice gw = entry.getValue();
//                logger.info(String.format("Found a gateway device: local address = %s, external address = %s",
//                        gw.getLocalAddress().getHostAddress(), gw.getExternalIPAddress()));
//
//                gw.deletePortMapping(config.p2pListenPort(), "TCP");
//                gw.addPortMapping(config.p2pListenPort(), config.p2pListenPort(), gw.getLocalAddress().getHostAddress(),
//                        "TCP", "Semux P2P network");
//            }
//        } catch (IOException | SAXException | ParserConfigurationException e) {
//            logger.info(String.format("Failed to add port mapping", e));
//        }
    }

    /**
     * Stops the kernel.
     */
    public synchronized void stop() {
        if (state != State.RUNNING) {
            return;
        } else {
            state = State.STOPPING;
        }

        // stop consensus
        try {
            sync.stop();
            bft.stop();

            // make sure consensus thread is fully stopped
            consThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.severe("Failed to stop sync/bft manager properly");
        }

        // stop p2p
        p2p.stop();

        // stop pending manager and node manager
        pendingMgr.stop();
        nodeMgr.stop();

        // close client
        client.close();

        // make sure no thread is reading/writing the state
        ReentrantReadWriteLock.WriteLock lock = chain.getStateLock().writeLock();
        lock.lock();
        try {
            for (DatabaseName name : DatabaseName.values()) {
                dbFactory.getDB(name).close();
            }
        } finally {
            lock.unlock();
        }

        state = State.STOPPED;
    }

    /**
     * Returns the kernel state.
     *
     * @return
     */
    public State state() {
        return state;
    }

    /**
     * Returns the wallet.
     *
     * @return
     */
    public Wallet getWallet() {
        return wallet;
    }

    /**
     * Returns the coinbase.
     *
     * @return
     */
    public Key getCoinbase() {
        return coinbase;
    }

    /**
     * Returns the blockchain.
     *
     * @return
     */
    public Blockchain getBlockchain() {
        return chain;
    }

    /**
     * Returns the peer client.
     *
     * @return
     */
    public PeerClient getClient() {
        return client;
    }

    /**
     * Returns the pending manager.
     *
     * @return
     */
    public PendingManager getPendingManager() {
        return pendingMgr;
    }

    /**
     * Returns the channel manager.
     *
     * @return
     */
    public ChannelManager getChannelManager() {
        return channelMgr;
    }

    /**
     * Returns the node manager.
     *
     * @return
     */
    public NodeManager getNodeManager() {
        return nodeMgr;
    }

    /**
     * Returns the config.
     *
     * @return
     */
    public Config getConfig() {
        return config;
    }

    /**
     * Returns the syncing manager.
     *
     * @return
     */
    public SyncManager getSyncManager() {
        return sync;
    }

    /**
     * Returns the BFT manager.
     *
     * @return
     */
    public BftManager getBftManager() {
        return bft;
    }

    /**
     * Returns the p2p server instance.
     *
     * @return a {@link PeerServer} instance or null
     */
    public PeerServer getP2p() {
        return p2p;
    }

    /**
     * Returns the database factory.
     *
     * @return
     */
    public DatabaseFactory getDbFactory() {
        return dbFactory;
    }

    /**
     * Create an empty block.
     *
     * @return
     */
    public SemuxBlock createEmptyBlock() {
        Block prevBlock = getBlockchain().getLatestBlock();
        BlockHeader blockHeader = new BlockHeader(
                prevBlock.getNumber() + 1,
                DUMMY_ADDRESS,
                prevBlock.getHash(),
                TimeUtil.currentTimeMillis(),
                Bytes.EMPTY_HASH,
                Bytes.EMPTY_HASH,
                Bytes.EMPTY_HASH,
                Bytes.EMPTY_BYTES);
        return new SemuxBlock(blockHeader, getConfig().spec().maxBlockGasLimit());
    }
}
