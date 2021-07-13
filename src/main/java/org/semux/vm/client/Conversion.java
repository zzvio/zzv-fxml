/**
 * Copyright (c) 2017-2020 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.vm.client;

import java.math.BigInteger;

import org.semux.core.Amount;
import org.semux.core.Unit;

/**
 * Conversion between ETH and SEM. The idea is to make 1 SEM = 1 ETH from a
 * smart contract viewpoint.
 */
public class Conversion {

    private static final BigInteger TEN_POW_NINE = BigInteger.TEN.pow(9);

    public static Amount weiToAmount(BigInteger value) {
        BigInteger nanoSEM = value.divide(TEN_POW_NINE);
        return Amount.of(nanoSEM.longValue(), Unit.NANO_SEM);
    }

    public static BigInteger amountToWei(Amount value) {
        return value.toBigInteger().multiply(TEN_POW_NINE);
    }

    public static BigInteger amountToWei(long nanoSEM) {
        return BigInteger.valueOf(nanoSEM).multiply(TEN_POW_NINE);
    }
}
