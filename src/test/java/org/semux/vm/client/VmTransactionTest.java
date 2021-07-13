/**
 * Copyright (c) 2017-2020 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.vm.client;

import static org.ethereum.vm.util.BytecodeCompiler.compile;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.semux.core.Amount.ZERO;
import static org.semux.core.Unit.SEM;

import java.util.logging.Logger;

import org.ethereum.vm.util.HashUtil;
import org.ethereum.vm.util.HexUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
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

public class VmTransactionTest {

    private Logger logger = Logger.getLogger(VmTransactionTest.class.getName());

    @Rule
    public TemporaryDatabaseRule temporaryDBFactory = new TemporaryDatabaseRule();

    private Config config;
    private Blockchain chain;
    private AccountState as;
    private DelegateState ds;
    private Network network;

    private SemuxBlock block;

    @Before
    public void prepare() {
        config = new UnitTestnetConfig(Constants.DEFAULT_ROOT_DIR);
        chain = Mockito.spy(new BlockchainImpl(config, temporaryDBFactory));

        as = chain.getAccountState();
        ds = chain.getDelegateState();
        network = config.network();

        block = new SemuxBlock(
                new BlockHeader(1, Bytes.random(20), Bytes.random(32), TimeUtil.currentTimeMillis(),
                        Bytes.random(20), Bytes.random(20), Bytes.random(20), Bytes.random(20)),
                config.spec().maxBlockGasLimit());
    }

    /**
     * Just a basic test to check wiring so far
     */
    @Test
    public void testCall() {
        TransactionExecutor exec = new TransactionExecutor(config, new SemuxBlockStore(chain), true, true);
        Key key = new Key();

        TransactionType type = TransactionType.CALL;
        byte[] from = key.toAddress();
        byte[] to = Bytes.random(20);
        Amount value = Amount.of(5);
        long nonce = as.getAccount(from).getNonce();
        long timestamp = TimeUtil.currentTimeMillis();

        // set the contract to a simple program
        byte[] contract = compile("PUSH2 0x1234 PUSH1 0x00 MSTORE PUSH1 0x20 PUSH1 0x00 RETURN");
        logger.info(Hex.encode0x(contract));
        logger.info(
                Hex.encode0x(HashUtil.calcNewAddress(Hex.decode0x("0x23a6049381fd2cfb0661d9de206613b83d53d7df"), 17)));
        as.setCode(to, contract);

        byte[] data = Bytes.random(16);
        long gas = 30000;
        Amount gasPrice = Amount.of(1);

        Transaction tx = new Transaction(network, type, to, value, Amount.ZERO, nonce, timestamp, data, gas, gasPrice);
        tx.sign(key);
        assertTrue(tx.validate(network));

        // insufficient available
        TransactionResult result = exec.execute(tx, as.track(), ds.track(), block, 0);
        assertFalse(result.getCode().isSuccess());

        Amount available = Amount.of(1000, SEM);
        as.adjustAvailable(key.toAddress(), available);

        // execute but not commit
        result = exec.execute(tx, as.track(), ds.track(), block, 0);
        assertTrue(result.getCode().isSuccess());
        assertEquals(available, as.getAccount(key.toAddress()).getAvailable());
        assertEquals(ZERO, as.getAccount(to).getAvailable());

        // execute and commit
        result = exec.execute(tx, as, ds, block, 0);
        assertTrue(result.getCode().isSuccess());

        // miner're reward is not yet given
        assertEquals(ZERO, as.getAccount(block.getCoinbase()).getAvailable());
    }

    /**
     * Just a basic test to check wiring so far
     */
    @Test
    public void testCreate() {
        TransactionExecutor exec = new TransactionExecutor(config, new SemuxBlockStore(chain), true, true);
        Key key = new Key();

        TransactionType type = TransactionType.CREATE;
        byte[] from = key.toAddress();
        byte[] to = Bytes.EMPTY_ADDRESS;
        Amount value = Amount.of(0);
        long nonce = as.getAccount(from).getNonce();
        long timestamp = TimeUtil.currentTimeMillis();

        // set the contract to a simple program
        String code = "608060405234801561001057600080fd5b506040516020806100e7833981018060405281019080805190602001909291905050508060008190555050609e806100496000396000f300608060405260043610603f576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff1680632e52d606146044575b600080fd5b348015604f57600080fd5b506056606c565b6040518082815260200191505060405180910390f35b600054815600a165627a7a72305820efb6a6369e3c5d7fe9b3274b20753bb0fe188b763fc2adee86cd844de935c8220029";
        byte[] create = HexUtil.fromHexString(code);
        byte[] data = HexUtil.fromHexString(code.substring(code.indexOf("60806040", 1)));

        long gas = 1000000;
        Amount gasPrice = Amount.of(1);

        Transaction tx = new Transaction(network, type, to, value, Amount.ZERO, nonce, timestamp, create, gas,
                gasPrice);
        tx.sign(key);
        assertTrue(tx.validate(network));

        // insufficient available
        TransactionResult result = exec.execute(tx, as.track(), ds.track(), block, 0);
        assertFalse(result.getCode().isSuccess());

        Amount available = Amount.of(1000, SEM);
        as.adjustAvailable(key.toAddress(), available);

        // execute but not commit
        result = exec.execute(tx, as.track(), ds.track(), block, 0);
        assertTrue(result.getCode().isSuccess());
        assertEquals(available, as.getAccount(key.toAddress()).getAvailable());
        assertEquals(ZERO, as.getAccount(to).getAvailable());

        // execute and commit
        result = exec.execute(tx, as, ds, block, 0);
        assertTrue(result.getCode().isSuccess());

        byte[] newContractAddress = HashUtil.calcNewAddress(tx.getFrom(), tx.getNonce());

        byte[] contract = as.getCode(newContractAddress);
        assertArrayEquals(data, contract);

        // miner're reward is not yet given
        assertEquals(ZERO, as.getAccount(block.getCoinbase()).getAvailable());
    }

    // pragma solidity ^0.5.7;
    //
    // contract SimpleStorage {
    // uint storedData;
    // function set(uint x) public {
    // storedData = x;
    // }
    // function get() public view returns (uint retVal) {
    // return storedData;
    // }
    // }
    @Test
    public void testCreateAndCall() {
        TransactionExecutor exec = new TransactionExecutor(config, new SemuxBlockStore(chain), true, true);
        Key key = new Key();

        TransactionType type = TransactionType.CREATE;
        byte[] from = key.toAddress();
        byte[] to = Bytes.EMPTY_ADDRESS;
        Amount value = Amount.of(0);
        long nonce = as.getAccount(from).getNonce();
        long timestamp = TimeUtil.currentTimeMillis();

        // set the contract to a simple program
        String code = "0x608060405234801561001057600080fd5b5060c68061001f6000396000f3fe6080604052348015600f57600080fd5b506004361060325760003560e01c806360fe47b11460375780636d4ce63c146062575b600080fd5b606060048036036020811015604b57600080fd5b8101908080359060200190929190505050607e565b005b60686088565b6040518082815260200191505060405180910390f35b8060008190555050565b6000805490509056fea265627a7a7230582040d9036c93b76f14f0505e24ea49d3ca539f2d82eb0acc12f279fd28094a429364736f6c63430005090032";
        byte[] data = HexUtil.fromHexString(code);

        long gas = 1000000;
        Amount gasPrice = Amount.of(1);

        Amount available = Amount.of(1000, SEM);
        as.adjustAvailable(key.toAddress(), available);

        Transaction tx = new Transaction(network, type, to, value, Amount.ZERO, nonce, timestamp, data, gas, gasPrice);
        tx.sign(key);
        assertTrue(tx.validate(network));

        TransactionResult result = exec.execute(tx, as, ds, block, 0);
        assertTrue(result.getCode().isSuccess());

        byte[] newContractAddress = HashUtil.calcNewAddress(tx.getFrom(), tx.getNonce());

        type = TransactionType.CALL;
        to = newContractAddress;
        nonce += 1;
        data = Hex.decode0x("0x60fe47b10000000000000000000000000000000000000000000000000000000000000009");
        tx = new Transaction(network, type, to, value, Amount.ZERO, nonce, timestamp, data, gas, gasPrice);
        tx.sign(key);
        assertTrue(tx.validate(network));

        result = exec.execute(tx, as, ds, block, 0);
        assertTrue(result.getCode().isSuccess());
    }

    // ISSUE-229
    //
    // pragma solidity >=0.4.24 <0.7.0;
    // contract Transfer {
    // function send(address payable receiver) public payable {
    // receiver.transfer(msg.value);
    // }
    // }
    @Test
    public void testInternalTransferNotEnoughGas() {
        TransactionExecutor exec = new TransactionExecutor(config, new SemuxBlockStore(chain), true, true);
        Key key = new Key();

        TransactionType type = TransactionType.CREATE;
        byte[] from = key.toAddress();
        byte[] to = Bytes.EMPTY_ADDRESS;
        Amount value = Amount.of(0);
        long nonce = as.getAccount(from).getNonce();
        long timestamp = TimeUtil.currentTimeMillis();

        // set the contract to a simple program
        String code = "0x608060405234801561001057600080fd5b506101ff806100206000396000f3fe60806040526004361061001e5760003560e01c80633e58c58c1461012e575b60146000369050146000369091610096576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040180806020018281038252848482818152602001925080828437600081840152601f19601f820116905080830192505050935050505060405180910390fd5b50506100e66000368080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f82011690508083019250505050505050610172565b73ffffffffffffffffffffffffffffffffffffffff166108fc349081150290604051600060405180830381858888f1935050505015801561012b573d6000803e3d6000fd5b50005b6101706004803603602081101561014457600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff169060200190929190505050610180565b005b600060148201519050919050565b8073ffffffffffffffffffffffffffffffffffffffff166108fc349081150290604051600060405180830381858888f193505050501580156101c6573d6000803e3d6000fd5b505056fea265627a7a723058205cbe214ea9276e4fc90bc9fd6028f0422b5d0c1402b3a0fc16f3f748a722c15464736f6c634300050a0032";
        byte[] data = HexUtil.fromHexString(code);

        long gas = 100000;
        Amount gasPrice = Amount.of(1);

        Amount available = Amount.of(1000, SEM);
        as.adjustAvailable(key.toAddress(), available);

        Transaction tx = new Transaction(network, type, to, value, Amount.ZERO, nonce, timestamp, data, gas, gasPrice);
        tx.sign(key);
        assertTrue(tx.validate(network));

        TransactionResult result = exec.execute(tx, as, ds, block, 0);
        assertFalse(result.getCode().isSuccess());
    }

    @Test
    public void testInternalTransfer() {
        TransactionExecutor exec = new TransactionExecutor(config, new SemuxBlockStore(chain), true, true);
        Key key = new Key();

        TransactionType type = TransactionType.CREATE;
        byte[] from = key.toAddress();
        byte[] to = Bytes.EMPTY_ADDRESS;
        Amount value = Amount.of(0);
        long nonce = as.getAccount(from).getNonce();
        long timestamp = TimeUtil.currentTimeMillis();

        // set the contract to a simple program
        String code = "0x608060405234801561001057600080fd5b506101ff806100206000396000f3fe60806040526004361061001e5760003560e01c80633e58c58c1461012e575b60146000369050146000369091610096576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040180806020018281038252848482818152602001925080828437600081840152601f19601f820116905080830192505050935050505060405180910390fd5b50506100e66000368080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f82011690508083019250505050505050610172565b73ffffffffffffffffffffffffffffffffffffffff166108fc349081150290604051600060405180830381858888f1935050505015801561012b573d6000803e3d6000fd5b50005b6101706004803603602081101561014457600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff169060200190929190505050610180565b005b600060148201519050919050565b8073ffffffffffffffffffffffffffffffffffffffff166108fc349081150290604051600060405180830381858888f193505050501580156101c6573d6000803e3d6000fd5b505056fea265627a7a723058205cbe214ea9276e4fc90bc9fd6028f0422b5d0c1402b3a0fc16f3f748a722c15464736f6c634300050a0032";
        byte[] data = HexUtil.fromHexString(code);

        long gas = 1000000;
        Amount gasPrice = Amount.of(1);

        Amount available = Amount.of(1000, SEM);
        as.adjustAvailable(key.toAddress(), available);

        Transaction tx = new Transaction(network, type, to, value, Amount.ZERO, nonce, timestamp, data, gas, gasPrice);
        tx.sign(key);
        assertTrue(tx.validate(network));

        TransactionResult result = exec.execute(tx, as, ds, block, 0);
        assertTrue(result.getCode().isSuccess());

        byte[] newContractAddress = HashUtil.calcNewAddress(tx.getFrom(), tx.getNonce());

        type = TransactionType.CALL;
        to = newContractAddress;
        nonce += 1;
        data = Hex.decode0x("0x3e58c58c000000000000000000000000791f1c3f06b19f1b3a4c7774675df9933a091d10");
        value = Amount.of(1, SEM);
        tx = new Transaction(network, type, to, value, Amount.ZERO, nonce, timestamp, data, gas, gasPrice);
        tx.sign(key);
        assertTrue(tx.validate(network));

        assertEquals(ZERO, as.getAccount(newContractAddress).getAvailable());
        assertEquals(ZERO, as.getAccount(Hex.decode0x("791f1c3f06b19f1b3a4c7774675df9933a091d10")).getAvailable());

        result = exec.execute(tx, as, ds, block, 0);

        assertTrue(result.getCode().isSuccess());
        assertEquals(ZERO, as.getAccount(newContractAddress).getAvailable());
        assertEquals(value, as.getAccount(Hex.decode0x("791f1c3f06b19f1b3a4c7774675df9933a091d10")).getAvailable());

        assertEquals(1, result.getInternalTransactions().size());
        logger.info(String.format("Result: %s", result));
    }

    @Test
    public void testTransferToContract() {
        TransactionExecutor exec = new TransactionExecutor(config, new SemuxBlockStore(chain), true, true);
        Key key = new Key();

        TransactionType type = TransactionType.CALL;
        byte[] from = key.toAddress();
        byte[] to = Bytes.random(20);
        Amount value = Amount.of(50);
        long nonce = as.getAccount(from).getNonce();
        long timestamp = TimeUtil.currentTimeMillis();

        byte[] contract = Hex.decode("60006000");
        as.setCode(to, contract);
        as.adjustAvailable(from, Amount.of(1000, SEM));

        long gas = 100000;
        Amount gasPrice = Amount.of(1);

        Transaction tx = new Transaction(network, type, to, value, Amount.ZERO, nonce, timestamp, contract, gas,
                gasPrice);
        tx.sign(key);

        TransactionResult result = exec.execute(tx, as, ds, block, 0);
        assertTrue(result.getCode().isSuccess());
        assertEquals(Amount.of(1000, SEM)
                .subtract(value)
                .subtract(tx.getGasPrice().multiply(result.getGasUsed())),
                as.getAccount(from).getAvailable());
        assertEquals(value, as.getAccount(to).getAvailable());

        // miner're reward is not yet given
        assertEquals(ZERO, as.getAccount(block.getCoinbase()).getAvailable());
    }

    @Test
    public void testTransferToContractOutOfGas() {
        TransactionExecutor exec = new TransactionExecutor(config, new SemuxBlockStore(chain), true, true);
        Key key = new Key();

        TransactionType type = TransactionType.CALL;
        byte[] from = key.toAddress();
        byte[] to = Bytes.random(20);
        Amount value = Amount.of(50);
        long nonce = as.getAccount(from).getNonce();
        long timestamp = TimeUtil.currentTimeMillis();

        byte[] contract = Hex.decode("6000");
        as.setCode(to, contract);
        as.adjustAvailable(from, Amount.of(1000, SEM));

        long gas = 21073;
        Amount gasPrice = Amount.of(1);

        Transaction tx = new Transaction(network, type, to, value, Amount.ZERO, nonce, timestamp, contract, gas,
                gasPrice);
        tx.sign(key);

        TransactionResult result = exec.execute(tx, as, ds, block, 0);
        assertTrue(result.getCode().isFailure());
        // value transfer reverted
        assertEquals(Amount.of(1000, SEM).subtract(tx.getGasPrice().multiply(result.getGasUsed())),
                as.getAccount(from).getAvailable());
        assertEquals(ZERO, as.getAccount(to).getAvailable());

        // miner're reward is not yet given
        assertEquals(ZERO, as.getAccount(block.getCoinbase()).getAvailable());
    }

    // tx: 0x31d6c6c1c5e82b286b8179f4368543fb4595beb57ee1fd2a01dbbb22f6cca9f1
    @Test
    public void testCallFailureRevertBeforeFork() {
        TransactionExecutor exec = new TransactionExecutor(config, new SemuxBlockStore(chain), true, false);
        Key sender = new Key();

        TransactionType type = TransactionType.CREATE;
        byte[] to = Bytes.EMPTY_ADDRESS;
        Amount value = Amount.of(0);
        long nonce = as.getAccount(sender.toAddress()).getNonce();
        long timestamp = TimeUtil.currentTimeMillis();
        byte[] data = HexUtil.fromHexString(
                "0x608060405234801561001057600080fd5b50610165806100206000396000f300608060405260043610610041576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff1680633bc5de3014610046575b600080fd5b34801561005257600080fd5b5061005b6100d6565b6040518080602001828103825283818151815260200191508051906020019080838360005b8381101561009b578082015181840152602081019050610080565b50505050905090810190601f1680156100c85780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b60608060405190810160405280602281526020017f466972737420636f6e7472616374212053656d757820746f20746865204d6f6f81526020017f6e210000000000000000000000000000000000000000000000000000000000008152509050905600a165627a7a72305820934582d75405e634939862f4188ebbb6c2765362add401961e8f44aa91b91f040029");
        long gas = 1000000;
        Amount gasPrice = Amount.of(1);
        Transaction tx = new Transaction(network, type, to, value, Amount.ZERO, nonce, timestamp, data, gas, gasPrice)
                .sign(sender);

        // credit the sender some balance
        Amount available = Amount.of(1000, SEM);
        as.adjustAvailable(sender.toAddress(), available);

        // deploy the contract
        TransactionResult result = exec.execute(tx, as, ds, block, 0);
        available = available.subtract(result.getGasPrice().multiply(result.getGasUsed()));
        assertTrue(result.getCode().isSuccess());
        byte[] newContractAddress = HashUtil.calcNewAddress(tx.getFrom(), tx.getNonce());

        type = TransactionType.CALL;
        to = newContractAddress;
        nonce = as.getAccount(sender.toAddress()).getNonce();
        data = Hex.decode0x("0x3bc5de30");
        value = Amount.of(1, SEM);
        tx = new Transaction(network, type, to, value, Amount.ZERO, nonce, timestamp, data, gas, gasPrice).sign(sender);

        // call the contract
        result = exec.execute(tx, as, ds, block, 0);
        available = available.subtract(result.getGasPrice().multiply(result.getGasUsed()));
        logger.info(String.format("Result: %s", result));
        assertFalse(result.getCode().isSuccess());
        assertEquals(available, as.getAccount(sender.toAddress()).getAvailable());

        assertTrue(tx.getGas() == result.getGasUsed());
    }

    @Test
    public void testCallFailureRevertAfterFork() {
        TransactionExecutor exec = new TransactionExecutor(config, new SemuxBlockStore(chain), true, true);
        Key sender = new Key();

        TransactionType type = TransactionType.CREATE;
        byte[] to = Bytes.EMPTY_ADDRESS;
        Amount value = Amount.of(0);
        long nonce = as.getAccount(sender.toAddress()).getNonce();
        long timestamp = TimeUtil.currentTimeMillis();
        byte[] data = HexUtil.fromHexString(
                "0x608060405234801561001057600080fd5b50610165806100206000396000f300608060405260043610610041576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff1680633bc5de3014610046575b600080fd5b34801561005257600080fd5b5061005b6100d6565b6040518080602001828103825283818151815260200191508051906020019080838360005b8381101561009b578082015181840152602081019050610080565b50505050905090810190601f1680156100c85780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b60608060405190810160405280602281526020017f466972737420636f6e7472616374212053656d757820746f20746865204d6f6f81526020017f6e210000000000000000000000000000000000000000000000000000000000008152509050905600a165627a7a72305820934582d75405e634939862f4188ebbb6c2765362add401961e8f44aa91b91f040029");
        long gas = 1000000;
        Amount gasPrice = Amount.of(1);
        Transaction tx = new Transaction(network, type, to, value, Amount.ZERO, nonce, timestamp, data, gas, gasPrice)
                .sign(sender);

        // credit the sender some balance
        Amount available = Amount.of(1000, SEM);
        as.adjustAvailable(sender.toAddress(), available);

        // deploy the contract
        TransactionResult result = exec.execute(tx, as, ds, block, 0);
        available = available.subtract(result.getGasPrice().multiply(result.getGasUsed()));
        assertTrue(result.getCode().isSuccess());
        byte[] newContractAddress = HashUtil.calcNewAddress(tx.getFrom(), tx.getNonce());

        type = TransactionType.CALL;
        to = newContractAddress;
        nonce = as.getAccount(sender.toAddress()).getNonce();
        data = Hex.decode0x("0x3bc5de30");
        value = Amount.of(1, SEM);
        tx = new Transaction(network, type, to, value, Amount.ZERO, nonce, timestamp, data, gas, gasPrice).sign(sender);

        // call the contract
        result = exec.execute(tx, as, ds, block, 0);
        available = available.subtract(result.getGasPrice().multiply(result.getGasUsed()));
        logger.info(String.format("Result: %s", result));
        assertFalse(result.getCode().isSuccess());
        assertEquals(available, as.getAccount(sender.toAddress()).getAvailable());

        assertTrue(tx.getGas() > result.getGasUsed());
    }

    // tx: 0x64fa2479faaeca0dcefbb57c2fc96f785336663f27726e8e7225e7dce3096452
    @Test
    public void testCreateFailure() {
        TransactionExecutor exec = new TransactionExecutor(config, new SemuxBlockStore(chain), true, true);
        Key sender = new Key();

        TransactionType type = TransactionType.CREATE;
        byte[] to = Bytes.EMPTY_ADDRESS;
        Amount value = Amount.of(0);
        long nonce = as.getAccount(sender.toAddress()).getNonce();
        long timestamp = TimeUtil.currentTimeMillis();
        byte[] data = HexUtil.fromHexString(
                "0x608060405234801561001057600080fd5b50610165806100206000396000f300608060405260043610610041576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff1680633bc5de3014610046575b600080fd5b34801561005257600080fd5b5061005b6100d6565b6040518080602001828103825283818151815260200191508051906020019080838360005b8381101561009b578082015181840152602081019050610080565b50505050905090810190601f1680156100c85780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b60608060405190810160405280602281526020017f466972737420636f6e7472616374212053656d757820746f20746865204d6f6f81526020017f6e210000000000000000000000000000000000000000000000000000000000008152509050905600a165627a7a72305820934582d75405e634939862f4188ebbb6c2765362add401961e8f44aa91b91f040029");
        long gas = 121000;
        Amount gasPrice = Amount.of(200);
        Transaction tx = new Transaction(network, type, to, value, Amount.ZERO, nonce, timestamp, data, gas, gasPrice)
                .sign(sender);

        // credit the sender some balance
        Amount available = Amount.of(1000, SEM);
        as.adjustAvailable(sender.toAddress(), available);

        // deploy the contract
        TransactionResult result = exec.execute(tx, as, ds, block, 0);
        assertFalse(result.getCode().isSuccess());
        byte[] newContractAddress = HashUtil.calcNewAddress(tx.getFrom(), tx.getNonce());
        assertFalse(as.exists(newContractAddress));
    }
}
