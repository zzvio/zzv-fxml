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
package org.ethereum.vm.program.exception;

import java.math.BigInteger;

import org.ethereum.vm.OpCode;
import org.ethereum.vm.program.Program;
import org.ethereum.vm.util.HexUtil;

public class ExceptionFactory {

    public static OutOfGasException notEnoughOpGas(OpCode op, long opGas, long programGas) {
        return new OutOfGasException("Not enough gas for '%s' operation executing: opGas[%d], programGas[%d];", op,
                opGas, programGas);
    }

    public static OutOfGasException notEnoughSpendingGas(String cause, long gasValue, Program program) {
        return new OutOfGasException("Not enough gas for '%s' cause spending: gas[%d], usedGas[%d];",
                cause, gasValue, program.getResult().getGasUsed());
    }

    public static OutOfGasException gasOverflow(BigInteger actual, BigInteger limit) {
        return new OutOfGasException("Gas value overflow: actual[%d], limit[%d];", actual.longValue(),
                limit.longValue());
    }

    public static IllegalOperationException invalidOpCode(byte opCode) {
        return new IllegalOperationException("Invalid operation code: opCode[%s];",
                HexUtil.toHexString(new byte[] { opCode }));
    }

    public static BadJumpDestinationException badJumpDestination(int pc) {
        return new BadJumpDestinationException("Operation with pc isn't 'JUMPDEST': PC[%d];", pc);
    }

    public static StackUnderflowException tooSmallStack(int expectedSize, int actualSize) {
        return new StackUnderflowException("Expected stack size %d but actual %d;", expectedSize, actualSize);
    }

    public static StackOverflowException tooLargeStack(int expectedSize, int maxSize) {
        return new StackOverflowException("Expected stack size %d exceeds stack limit %d", expectedSize, maxSize);
    }
}
