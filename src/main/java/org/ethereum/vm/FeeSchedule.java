/**
 * Copyright (c) [2018] [ The Semux Developers ]
 * Copyright (c) [2016] [ <ether.camp> ]
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.vm;

/**
 * The fundamental network cost unit. Paid for exclusively by Ether, which is
 * converted freely to and from Gas as required. Gas does not exist outside of
 * the internal Ethereum computation engine; its price is set by the Transaction
 * and miners are free to ignore Transactions whose Gas price is too low.
 */
public class FeeSchedule {

    private static final int BALANCE = 20;
    private static final int SHA3 = 30;
    private static final int SHA3_WORD = 6;
    private static final int SLOAD = 50;
    private static final int STOP = 0;
    private static final int SUICIDE = 0;
    private static final int CLEAR_SSTORE = 5000;
    private static final int SET_SSTORE = 20000;
    private static final int RESET_SSTORE = 5000;
    private static final int REFUND_SSTORE = 15000;
    private static final int REUSE_SSTORE = 200;
    private static final int CREATE = 32000;

    private static final int CALL = 40;
    private static final int STIPEND_CALL = 2300;
    private static final int VT_CALL = 9000; // value transfer call
    private static final int NEW_ACCT_CALL = 25000; // new account call
    private static final int MEMORY = 3;
    private static final int SUICIDE_REFUND = 24000;
    private static final int CREATE_DATA = 200;
    private static final int TX_NO_ZERO_DATA = 68;
    private static final int TX_ZERO_DATA = 4;
    private static final int TRANSACTION = 21000;
    private static final int TRANSACTION_CREATE_CONTRACT = 53000;
    private static final int LOG_GAS = 375;
    private static final int LOG_DATA_GAS = 8;
    private static final int LOG_TOPIC_GAS = 375;
    private static final int COPY_GAS = 3;
    private static final int EXP_GAS = 10;
    private static final int EXP_BYTE_GAS = 10;
    private static final int IDENTITY = 15;
    private static final int IDENTITY_WORD = 3;
    private static final int RIPEMD160 = 600;
    private static final int RIPEMD160_WORD = 120;
    private static final int SHA256 = 60;
    private static final int SHA256_WORD = 12;
    private static final int EC_RECOVER = 3000;
    private static final int EXT_CODE_SIZE = 20;
    private static final int EXT_CODE_COPY = 20;
    private static final int EXT_CODE_HASH = 400;
    private static final int NEW_ACCT_SUICIDE = 0;

    public int getBALANCE() {
        return BALANCE;
    }

    public int getSHA3() {
        return SHA3;
    }

    public int getSHA3_WORD() {
        return SHA3_WORD;
    }

    public int getSLOAD() {
        return SLOAD;
    }

    public int getSTOP() {
        return STOP;
    }

    public int getSUICIDE() {
        return SUICIDE;
    }

    public int getCLEAR_SSTORE() {
        return CLEAR_SSTORE;
    }

    public int getSET_SSTORE() {
        return SET_SSTORE;
    }

    public int getRESET_SSTORE() {
        return RESET_SSTORE;
    }

    public int getREFUND_SSTORE() {
        return REFUND_SSTORE;
    }

    public int getREUSE_SSTORE() {
        return REUSE_SSTORE;
    }

    public int getCREATE() {
        return CREATE;
    }

    public int getCALL() {
        return CALL;
    }

    public int getSTIPEND_CALL() {
        return STIPEND_CALL;
    }

    public int getVT_CALL() {
        return VT_CALL;
    }

    public int getNEW_ACCT_CALL() {
        return NEW_ACCT_CALL;
    }

    public int getNEW_ACCT_SUICIDE() {
        return NEW_ACCT_SUICIDE;
    }

    public int getMEMORY() {
        return MEMORY;
    }

    public int getSUICIDE_REFUND() {
        return SUICIDE_REFUND;
    }

    public int getCREATE_DATA() {
        return CREATE_DATA;
    }

    public int getTX_NO_ZERO_DATA() {
        return TX_NO_ZERO_DATA;
    }

    public int getTX_ZERO_DATA() {
        return TX_ZERO_DATA;
    }

    public int getTRANSACTION() {
        return TRANSACTION;
    }

    public int getTRANSACTION_CREATE_CONTRACT() {
        return TRANSACTION_CREATE_CONTRACT;
    }

    public int getLOG_GAS() {
        return LOG_GAS;
    }

    public int getLOG_DATA_GAS() {
        return LOG_DATA_GAS;
    }

    public int getLOG_TOPIC_GAS() {
        return LOG_TOPIC_GAS;
    }

    public int getCOPY_GAS() {
        return COPY_GAS;
    }

    public int getEXP_GAS() {
        return EXP_GAS;
    }

    public int getEXP_BYTE_GAS() {
        return EXP_BYTE_GAS;
    }

    public int getIDENTITY() {
        return IDENTITY;
    }

    public int getIDENTITY_WORD() {
        return IDENTITY_WORD;
    }

    public int getRIPEMD160() {
        return RIPEMD160;
    }

    public int getRIPEMD160_WORD() {
        return RIPEMD160_WORD;
    }

    public int getSHA256() {
        return SHA256;
    }

    public int getSHA256_WORD() {
        return SHA256_WORD;
    }

    public int getEC_RECOVER() {
        return EC_RECOVER;
    }

    public int getEXT_CODE_SIZE() {
        return EXT_CODE_SIZE;
    }

    public int getEXT_CODE_COPY() {
        return EXT_CODE_COPY;
    }

    public int getEXT_CODE_HASH() {
        return EXT_CODE_HASH;
    }
}
