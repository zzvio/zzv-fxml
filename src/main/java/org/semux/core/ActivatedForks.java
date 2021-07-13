/**
 * Copyright (c) 2017-2020 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.core;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.semux.config.Config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * An in-memory structure holding all the activated forks.
 */
public class ActivatedForks {

    private static final Logger logger = Logger.getLogger(ActivatedForks.class.getName());

    private Blockchain chain;
    private Config config;

    /**
     * Activated forks at current height.
     */
    private Map<Fork, Fork.Activation> activatedForks;

    /**
     * Cache of <code>(fork, height) -> activated blocks</code>.
     */
    private Cache<ImmutablePair<Fork, Long>, ForkActivationMemory> cache = Caffeine
            .newBuilder()
            .maximumSize(1024)
            .build();

    /**
     * Creates a activated fork set.
     *
     * @param chain
     * @param config
     * @param activatedForks
     */
    public ActivatedForks(Blockchain chain, Config config, Map<Fork, Fork.Activation> activatedForks) {
        this.chain = chain;
        this.config = config;
        this.activatedForks = new ConcurrentHashMap<>(activatedForks);
    }

    /**
     * Tries to activate a fork.
     *
     * @param fork
     */
    public boolean activateFork(Fork fork) {
        long[] period = config.spec().getForkSignalingPeriod(fork);

        long number = chain.getLatestBlockNumber();
        if (number >= period[0]
                && number <= period[1]
                && !isActivated(fork, number)
                && isActivated(fork, number + 1)) {
            activatedForks.put(fork, new Fork.Activation(fork, number + 1));
            logger.info(String.format("Fork %s has been activated and will be effective from #%s", fork, number + 1));
            return true;
        }

        return false;
    }

    /**
     * Checks if a fork is activated at a certain height of this blockchain.
     *
     * @param fork
     *            An instance of ${@link Fork} to check.
     * @param height
     *            A blockchain height to check.
     * @return
     */
    public boolean isActivated(Fork fork, final long height) {
        assert (fork.blocksRequired() > 0);
        assert (fork.blocksToCheck() > 0);

        // checks whether the fork has been activated and recorded in database
        if (activatedForks.containsKey(fork)) {
            return height >= activatedForks.get(fork).effectiveFrom;
        }

        // checks whether the local blockchain has reached the fork activation
        // checkpoint
        if (config.manuallyActivatedForks().containsKey(fork)) {
            return height >= config.manuallyActivatedForks().get(fork);
        }

        // do not search if it's not within the range
        long[] period = config.spec().getForkSignalingPeriod(fork);
        if (height - 1 < period[0] || height - 1 > period[1]) {
            return false;
        }

        // returns memoized result of fork activation lookup at current height
        ForkActivationMemory current = cache.getIfPresent(ImmutablePair.of(fork, height));
        if (current != null) {
            return current.activatedBlocks >= fork.blocksRequired();
        }

        // block range to search:
        // from (number - 1)
        // to (number - fork.blocksToCheck)
        long higherBound = Math.max(0, height - 1);
        long lowerBound = Math.max(0, height - fork.blocksToCheck());
        long activatedBlocks = 0;

        ForkActivationMemory previous = cache.getIfPresent(ImmutablePair.of(fork, height - 1));
        if (previous != null) {
            // O(1) dynamic-programming lookup
            activatedBlocks = previous.activatedBlocks
                    - (lowerBound > 0 && previous.lowerBoundActivated ? 1 : 0)
                    + (chain.getBlockHeader(higherBound).getDecodedData().parseForkSignals().contains(fork) ? 1 : 0);
        } else {
            // O(m) traversal lookup
            for (long i = higherBound; i >= lowerBound; i--) {
                activatedBlocks += chain.getBlockHeader(i).getDecodedData().parseForkSignals().contains(fork) ? 1 : 0;
            }
        }

        logger.fine(String.format("number = %s, higher bound = %s, lower bound = %s", height, higherBound, lowerBound));

        // memorizes
        cache.put(ImmutablePair.of(fork, height),
                new ForkActivationMemory(
                        chain.getBlockHeader(lowerBound).getDecodedData().parseForkSignals().contains(fork),
                        activatedBlocks));

        // returns
        boolean activated = activatedBlocks >= fork.blocksRequired();
        if (activatedBlocks > 0) {
            logger.finest(String.format("Fork: name = %s, requirement = %s / %s, progress = %s",
                    fork.name(), fork.blocksRequired(), fork.blocksToCheck(), activatedBlocks));
        }

        return activated;
    }

    /**
     * Returns all the activate forks.
     *
     * @return
     */
    public Map<Fork, Fork.Activation> getActivatedForks() {
        return new HashMap<>(activatedForks);
    }

    /**
     * <code>
     * ForkActivationMemory[height].lowerBoundActivated =
     * forkActivated(height - ${@link Fork#blocksToCheck()})
     *
     * ForkActivationMemory[height].activatedBlocks =
     * ForkActivationMemory[height - 1].activatedBlocks -
     * ForkActivationMemory[height - 1].lowerBoundActivated ? 1 : 0 +
     * forkActivated(height - 1) ? 1 : 0
     * </code>
     */
    private static class ForkActivationMemory {

        /**
         * Whether the fork is activated at height
         * <code>(current height -{@link Fork#blocksToCheck()})</code>.
         */
        public final boolean lowerBoundActivated;

        /**
         * The number of activated blocks at the memorized height.
         */
        public final long activatedBlocks;

        public ForkActivationMemory(boolean lowerBoundActivated, long activatedBlocks) {
            this.lowerBoundActivated = lowerBoundActivated;
            this.activatedBlocks = activatedBlocks;
        }
    }
}
