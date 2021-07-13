/**
 * Copyright (c) 2017-2020 The Semux Developers
 *
 * <p>Distributed under the MIT software license, see the accompanying file LICENSE or
 * https://opensource.org/licenses/mit-license.php
 */
package org.semux.vm.client;

import static org.semux.vm.client.Conversion.amountToWei;
import static org.semux.vm.client.Conversion.weiToAmount;

import java.math.BigInteger;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.ethereum.vm.DataWord;
import org.ethereum.vm.chainspec.ConstantinoplePrecompiledContracts;
import org.ethereum.vm.chainspec.PrecompiledContract;
import org.ethereum.vm.chainspec.PrecompiledContractContext;
import org.ethereum.vm.client.Repository;
import org.ethereum.vm.program.InternalTransaction;
import org.ethereum.vm.program.ProgramResult;
import org.ethereum.vm.util.Pair;
import org.semux.core.Amount;
import org.semux.core.state.AccountState;
import org.semux.core.state.Delegate;
import org.semux.core.state.DelegateState;
import org.semux.util.Bytes;

public class SemuxPrecompiledContracts extends ConstantinoplePrecompiledContracts {

  private static final Vote vote = new Vote();
  private static final Unvote unvote = new Unvote();
  private static final GetVotes getVotes = new GetVotes();
  private static final GetVote getVote = new GetVote();

  private static final DataWord voteAddr = DataWord.of(100);
  private static final DataWord unvoteAddr = DataWord.of(101);
  private static final DataWord getVotesAddr = DataWord.of(102);
  private static final DataWord getVoteAddr = DataWord.of(103);

  private static final Pair<Boolean, byte[]> success =
      new Pair<>(true, ArrayUtils.EMPTY_BYTE_ARRAY);
  private static final Pair<Boolean, byte[]> failure =
      new Pair<>(false, ArrayUtils.EMPTY_BYTE_ARRAY);

  private static InternalTransaction addInternalTx(
      ProgramResult result,
      InternalTransaction internalTx,
      String type,
      byte[] from,
      byte[] to,
      long nonce,
      BigInteger value,
      byte[] data,
      long gas) {

    int depth = internalTx.getDepth() + 1;
    int index = result.getInternalTransactions().size();

    InternalTransaction tx =
        new InternalTransaction(
            depth, index, type, from, to, nonce, value, data, gas, internalTx.getGasPrice());
    result.addInternalTransaction(tx);

    return tx;
  }

  @Override
  public PrecompiledContract getContractForAddress(DataWord address) {

    if (address.equals(voteAddr)) {
      return vote;
    } else if (address.equals(unvoteAddr)) {
      return unvote;
    } else if (address.equals(getVotesAddr)) {
      return getVotes;
    } else if (address.equals(getVoteAddr)) {
      return getVote;
    }
    return super.getContractForAddress(address);
  }

  public static class Vote implements PrecompiledContract {
    @Override
    public long getGasForData(byte[] data) {
      return 21000;
    }

    @Override
    public Pair<Boolean, byte[]> execute(PrecompiledContractContext context) {
      byte[] data = context.getInternalTransaction().getData();

      if (data == null || data.length != 32 + 32) {
        return failure;
      }

      Repository track = context.getTrack();
      if (track instanceof SemuxRepository) {
        SemuxRepository semuxTrack = (SemuxRepository) track;
        AccountState as = semuxTrack.getAccountState();
        DelegateState ds = semuxTrack.getDelegateState();
        byte[] from = context.getInternalTransaction().getFrom();
        byte[] to = Arrays.copyOfRange(data, 12, 32);
        Amount value = weiToAmount(new BigInteger(1, Arrays.copyOfRange(data, 32, 64)));

        if (as.getAccount(from).getAvailable().greaterThanOrEqual(value)
            && ds.vote(from, to, value)) {
          as.adjustAvailable(from, value.negate());
          as.adjustLocked(from, value);

          addInternalTx(
              context.getResult(),
              context.getInternalTransaction(),
              "VOTE",
              from,
              to,
              context.getTrack().getNonce(from),
              amountToWei(value),
              Bytes.EMPTY_BYTES,
              0);
          return success;
        }
      }

      return failure;
    }
  }

  public static class Unvote implements PrecompiledContract {
    @Override
    public long getGasForData(byte[] data) {
      return 21000;
    }

    @Override
    public Pair<Boolean, byte[]> execute(PrecompiledContractContext context) {
      byte[] data = context.getInternalTransaction().getData();

      if (data == null || data.length != 32 + 32) {
        return failure;
      }

      Repository track = context.getTrack();
      if (track instanceof SemuxRepository) {
        SemuxRepository semuxTrack = (SemuxRepository) track;
        AccountState as = semuxTrack.getAccountState();
        DelegateState ds = semuxTrack.getDelegateState();
        byte[] from = context.getInternalTransaction().getFrom();
        byte[] to = Arrays.copyOfRange(data, 12, 32);
        Amount value = weiToAmount(new BigInteger(1, Arrays.copyOfRange(data, 32, 64)));

        if (as.getAccount(from).getLocked().greaterThanOrEqual(value)
            && ds.unvote(from, to, value)) {
          as.adjustAvailable(from, value);
          as.adjustLocked(from, value.negate());

          addInternalTx(
              context.getResult(),
              context.getInternalTransaction(),
              "UNVOTE",
              from,
              to,
              context.getTrack().getNonce(from),
              amountToWei(value),
              Bytes.EMPTY_BYTES,
              0);
          return success;
        }
      }

      return failure;
    }
  }

  public static class GetVotes implements PrecompiledContract {

    @Override
    public long getGasForData(byte[] bytes) {
      return 500L;
    }

    @Override
    public Pair<Boolean, byte[]> execute(PrecompiledContractContext context) {
      byte[] data = context.getInternalTransaction().getData();

      if (data == null || data.length != 32) {
        return failure;
      }
      Repository track = context.getTrack();
      if (track instanceof SemuxRepository) {
        SemuxRepository semuxTrack = (SemuxRepository) track;
        DelegateState ds = semuxTrack.getDelegateState();
        byte[] delegateAddress = Arrays.copyOfRange(data, 12, 32);

        Delegate delegate = ds.getDelegateByAddress(delegateAddress);
        Amount votes = delegate == null ? Amount.ZERO : delegate.getVotes();
        return Pair.of(true, DataWord.of(amountToWei(votes)).getData());
      }

      return failure;
    }
  }

  public static class GetVote implements PrecompiledContract {

    @Override
    public long getGasForData(byte[] bytes) {
      return 500L;
    }

    @Override
    public Pair<Boolean, byte[]> execute(PrecompiledContractContext context) {
      byte[] data = context.getInternalTransaction().getData();

      if (data == null || data.length != 32 + 32) {
        return failure;
      }
      Repository track = context.getTrack();
      if (track instanceof SemuxRepository) {
        SemuxRepository semuxTrack = (SemuxRepository) track;
        DelegateState ds = semuxTrack.getDelegateState();
        byte[] voterAddress = Arrays.copyOfRange(data, 12, 32);
        byte[] delegateAddress = Arrays.copyOfRange(data, 44, 64);

        Amount vote = ds.getVote(voterAddress, delegateAddress);
        return Pair.of(true, DataWord.of(amountToWei(vote)).getData());
      }

      return failure;
    }
  }
}
