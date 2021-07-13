/**
 * Copyright (c) 2017-2020 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.core;

import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.semux.core.BlockchainImpl.ValidatorStats;
import org.semux.core.state.AccountState;
import org.semux.core.state.DelegateState;
import org.semux.vm.client.SemuxInternalTransaction;

public interface Blockchain {

    /**
     * Returns the latest block.
     * 
     * @return
     */
    Block getLatestBlock();

    /**
     * Returns the hash of the latest block.
     * 
     * @return
     */
    byte[] getLatestBlockHash();

    /**
     * Returns the number of the latest block.
     * 
     * @return
     */
    long getLatestBlockNumber();

    /**
     * Returns block number by hash.
     * 
     * @param hash
     * @return
     */
    long getBlockNumber(byte[] hash);

    /**
     * Returns genesis block.
     * 
     * @return
     */
    Genesis getGenesis();

    /**
     * Returns block by number.
     * 
     * @param number
     * @return
     */
    Block getBlock(long number);

    /**
     * Returns block by its hash.
     * 
     * @param hash
     * @return
     */
    Block getBlock(byte[] hash);

    /**
     * Returns block header by block number.
     * 
     * @param number
     * @return
     */
    BlockHeader getBlockHeader(long number);

    /**
     * Returns block header by block hash.
     * 
     * @param hash
     * @return
     */
    BlockHeader getBlockHeader(byte[] hash);

    /**
     * Returns whether the block is existing.
     *
     * @param number
     * @return
     */
    boolean hasBlock(long number);

    /**
     * Returns transaction by its hash.
     * 
     * @param hash
     * @return
     */
    Transaction getTransaction(byte[] hash);

    /**
     * Returns coinbase transaction of the block number. This method is required as
     * Semux doesn't store coinbase transaction in blocks.
     *
     * @param blockNumber
     *            the block number
     * @return the coinbase transaction
     */
    Transaction getCoinbaseTransaction(long blockNumber);

    /**
     * Returns whether the transaction is in the blockchain.
     *
     * @param hash
     * @return
     */
    boolean hasTransaction(byte[] hash);

    /**
     * Returns transaction result.
     * 
     * @param hash
     * @return
     */
    TransactionResult getTransactionResult(byte[] hash);

    /**
     * Returns the block number of the given transaction.
     * 
     * @param hash
     * @return
     */
    long getTransactionBlockNumber(byte[] hash);

    /**
     * Returns the total number of transactions from/to the given address.
     * 
     * @param address
     *            account address
     * @return
     */
    int getTransactionCount(byte[] address);

    /**
     * Returns transactions from/to an address.
     * 
     * @param address
     *            account address
     * @param from
     *            transaction index from
     * @param to
     *            transaction index to
     * @return
     */
    List<Transaction> getTransactions(byte[] address, int from, int to);

    /**
     * Returns the total number of internal transactions from/to the given address.
     *
     * @param address
     *            account address
     * @return
     */
    int getInternalTransactionCount(byte[] address);

    /**
     * Returns internal transactions from/to an address.
     *
     * @param address
     *            account address
     * @param from
     *            transaction index from
     * @param to
     *            transaction index to
     * @return
     */
    List<SemuxInternalTransaction> getInternalTransactions(byte[] address, int from, int to);

    /**
     * Add a block to the chain.
     * 
     * @param block
     */
    void addBlock(Block block);

    /**
     * Returns account state.
     * 
     * @return
     */
    AccountState getAccountState();

    /**
     * Returns delegate state.
     * 
     * @return
     */
    DelegateState getDelegateState();

    /**
     * Returns the validator set based on current state.
     * 
     * @return the peerIds of validators
     */
    List<String> getValidators();

    /**
     * Returns the statistics of a validator.
     * 
     * @param address
     * @return
     */
    ValidatorStats getValidatorStats(byte[] address);

    /**
     * Register a blockchain listener.
     * 
     * @param listener
     */
    void addListener(BlockchainListener listener);

    /**
     * Returns whether a fork has been activated.
     *
     * @param fork
     */
    boolean isForkActivated(Fork fork, long number);

    /**
     * Returns whether a fork has been activated.
     *
     * @param fork
     */
    boolean isForkActivated(Fork fork);

    /**
     * Returns the date field for the next block, based on fork configuration.
     * 
     * @return
     */
    byte[] constructBlockHeaderDataField();

    /**
     * Returns the state lock.
     *
     * @return
     */
    ReentrantReadWriteLock getStateLock();

    /**
     * Imports a new block.
     *
     * @param block
     *            the block to import
     * @param validateVotes
     *            whether to validate the block votes
     * @return true if the block is successfully imported; otherwise, false
     */
    boolean importBlock(Block block, boolean validateVotes);

    /**
     * Validate the block votes only.
     *
     * @return true if the votes are valid, otherwise false
     */
    boolean validateBlockVotes(Block block);

    boolean isVMEnabled();

    boolean isVotingPrecompiledUpgraded();
}
