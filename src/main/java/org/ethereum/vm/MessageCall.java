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
 * A wrapper for a message call from a contract to another account.
 */
public class MessageCall {

    /**
     * Type of internal call. Either CALL, CALLCODE or POST
     */
    private final OpCode type;

    /**
     * gas to pay for the call, remaining gas will be refunded to the caller
     */
    private final long gas;

    /**
     * address of account which code to call
     */
    private final DataWord codeAddress;

    /**
     * the value that can be transfer along with the code execution
     */
    private final DataWord endowment;

    /**
     * start of memory to be input data to the call
     */
    private final DataWord inDataOffs;

    /**
     * size of memory to be input data to the call
     */
    private final DataWord inDataSize;

    /**
     * start of memory to be output of the call
     */
    private DataWord outDataOffs;

    /**
     * size of memory to be output data to the call
     */
    private DataWord outDataSize;

    public MessageCall(OpCode type, long gas, DataWord codeAddress,
            DataWord endowment, DataWord inDataOffs, DataWord inDataSize,
            DataWord outDataOffs, DataWord outDataSize) {
        this.type = type;
        this.gas = gas;
        this.codeAddress = codeAddress;
        this.endowment = endowment;
        this.inDataOffs = inDataOffs;
        this.inDataSize = inDataSize;
        this.outDataOffs = outDataOffs;
        this.outDataSize = outDataSize;
    }

    public OpCode getType() {
        return type;
    }

    public long getGas() {
        return gas;
    }

    public DataWord getCodeAddress() {
        return codeAddress;
    }

    public DataWord getEndowment() {
        return endowment;
    }

    public DataWord getInDataOffs() {
        return inDataOffs;
    }

    public DataWord getInDataSize() {
        return inDataSize;
    }

    public DataWord getOutDataOffs() {
        return outDataOffs;
    }

    public DataWord getOutDataSize() {
        return outDataSize;
    }
}
