/**
 * Copyright (c) 2017-2020 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.vm.client;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.semux.core.Unit.SEM;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.semux.Network;
import org.semux.config.Config;
import org.semux.config.Constants;
import org.semux.config.UnitTestnetConfig;
import org.semux.core.Amount;
import org.semux.core.BlockHeader;
import org.semux.core.Blockchain;
import org.semux.core.BlockchainImpl;
import org.semux.core.Transaction;
import org.semux.core.TransactionExecutor;
import org.semux.core.TransactionResult;
import org.semux.core.TransactionType;
import org.semux.core.state.AccountState;
import org.semux.core.state.DelegateState;
import org.semux.crypto.Hex;
import org.semux.crypto.Key;
import org.semux.rules.TemporaryDatabaseRule;
import org.semux.util.Bytes;
import org.semux.util.TimeUtil;

public class InternalTransactionTest {

    @Rule
    public TemporaryDatabaseRule temporaryDBFactory = new TemporaryDatabaseRule();

    private Config config;
    private Blockchain chain;
    private AccountState as;
    private DelegateState ds;
    private Network network;

    @Before
    public void prepare() {
        config = new UnitTestnetConfig(Constants.DEFAULT_ROOT_DIR);
        chain = spy(new BlockchainImpl(config, temporaryDBFactory));
        doReturn(true).when(chain).isForkActivated(any());

        as = chain.getAccountState();
        ds = chain.getDelegateState();
        network = config.network();
    }

    // pragma solidity ^0.5.0;
    //
    // contract InternalTransfer {
    // constructor() public {
    // 0x0000000000000000000000000000000012345678.transfer(5 ether);
    // }
    // }
    @Test
    public void testInternalTransfer() {
        TransactionExecutor exec = new TransactionExecutor(config, new SemuxBlockStore(chain), true, true);
        Key key = new Key();

        TransactionType type = TransactionType.CALL;
        byte[] from = key.toAddress();
        byte[] to = Bytes.EMPTY_ADDRESS;
        Amount value = Amount.of(0);
        long nonce = as.getAccount(from).getNonce();
        long timestamp = TimeUtil.currentTimeMillis();

        byte[] contract = Hex
                .decode("6080604052348015600f57600080fd5b50631234567873ffffffffffffffffffffffffffffffffffffffff166108fc674563918244f400009081150290604051600060405180830381858888f193505050501580156061573d6000803e3d6000fd5b50603580606f6000396000f3fe6080604052600080fdfea165627a7a723058207a87d470374147eafafb919d03f46646ca4e91b3cc5be1d7c4b152eb692f63e50029");
        as.setCode(to, contract);

        SemuxBlock bh = new SemuxBlock(
                new BlockHeader(123, Bytes.random(20), Bytes.random(20), TimeUtil.currentTimeMillis(),
                        Bytes.random(20), Bytes.random(20), Bytes.random(20), Bytes.random(20)),
                config.spec().maxBlockGasLimit());
        as.adjustAvailable(from, Amount.of(1000, SEM));
        as.adjustAvailable(to, Amount.of(1000, SEM));

        long gas = 100000;
        Amount gasPrice = Amount.of(1);

        Transaction tx = new Transaction(network, type, to, value, Amount.ZERO, nonce, timestamp, contract, gas,
                gasPrice);
        tx.sign(key);

        TransactionResult result = exec.execute(tx, as, ds, bh, 0);
        assertTrue(result.getCode().isSuccess());
        assertEquals(Amount.of(1000, SEM).subtract(Amount.of(5, SEM)),
                as.getAccount(to).getAvailable());
        assertEquals(Amount.of(5, SEM),
                as.getAccount(Hex.decode0x("0x0000000000000000000000000000000012345678")).getAvailable());

        assertFalse(result.getInternalTransactions().isEmpty());
        SemuxInternalTransaction it = result.getInternalTransactions().get(0);
        assertArrayEquals(to, it.getFrom());
        assertArrayEquals(Hex.decode0x("0x0000000000000000000000000000000012345678"), it.getTo());
        assertEquals(Amount.of(5, SEM), it.getValue());
    }
}
