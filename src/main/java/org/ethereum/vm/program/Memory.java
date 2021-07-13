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
package org.ethereum.vm.program;

import static java.lang.Math.ceil;
import static java.lang.Math.min;

import static org.ethereum.vm.util.ByteArrayUtil.EMPTY_BYTE_ARRAY;

import java.util.LinkedList;
import java.util.List;

import org.ethereum.vm.DataWord;

public class Memory {

    private static final int CHUNK_SIZE = 1024;
    private static final int WORD_SIZE = 32;

    private List<byte[]> chunks = new LinkedList<>();
    private int softSize;

    public byte[] read(int address, int size) {
        if (size <= 0) {
            return EMPTY_BYTE_ARRAY;
        }

        extend(address, size);
        byte[] data = new byte[size];

        int chunkIndex = address / CHUNK_SIZE;
        int chunkOffset = address % CHUNK_SIZE;

        int toGrab = data.length;
        int start = 0;

        while (toGrab > 0) {
            int copied = grabMax(chunkIndex, chunkOffset, toGrab, data, start);

            // read next chunk from the start
            ++chunkIndex;
            chunkOffset = 0;

            // mark remind
            toGrab -= copied;
            start += copied;
        }

        return data;
    }

    public void write(int address, byte[] data, int dataSize, boolean limited) {
        if (dataSize <= 0) {
            return;
        }

        if (data.length < dataSize) {
            dataSize = data.length;
        }

        if (!limited) {
            extend(address, dataSize);
        }

        int chunkIndex = address / CHUNK_SIZE;
        int chunkOffset = address % CHUNK_SIZE;

        int toCapture;
        if (limited) {
            toCapture = (address + dataSize > softSize) ? softSize - address : dataSize;
        } else {
            toCapture = dataSize;
        }

        int start = 0;
        while (toCapture > 0) {
            int captured = captureMax(chunkIndex, chunkOffset, toCapture, data, start);

            // capture next chunk
            ++chunkIndex;
            chunkOffset = 0;

            // mark remind
            toCapture -= captured;
            start += captured;
        }
    }

    public void extendAndWrite(int address, int allocSize, byte[] data) {
        extend(address, allocSize);
        write(address, data, allocSize, false);
    }

    public void extend(int address, int size) {
        if (size <= 0) {
            return;
        }

        final int newSize = address + size;

        int toAllocate = newSize - internalSize();
        if (toAllocate > 0) {
            addChunks((int) ceil((double) toAllocate / CHUNK_SIZE));
        }

        toAllocate = newSize - softSize;
        if (toAllocate > 0) {
            toAllocate = (int) ceil((double) toAllocate / WORD_SIZE) * WORD_SIZE;
            softSize += toAllocate;
        }
    }

    public DataWord readWord(int address) {
        return DataWord.of(read(address, 32));
    }

    // just access expecting all data valid
    public byte readByte(int address) {

        int chunkIndex = address / CHUNK_SIZE;
        int chunkOffset = address % CHUNK_SIZE;

        byte[] chunk = chunks.get(chunkIndex);

        return chunk[chunkOffset];
    }

    public int size() {
        return softSize;
    }

    public int internalSize() {
        return chunks.size() * CHUNK_SIZE;
    }

    public List<byte[]> getChunks() {
        return new LinkedList<>(chunks);
    }

    private int captureMax(int chunkIndex, int chunkOffset, int size, byte[] src, int srcPos) {

        byte[] chunk = chunks.get(chunkIndex);
        int toCapture = min(size, chunk.length - chunkOffset);

        System.arraycopy(src, srcPos, chunk, chunkOffset, toCapture);
        return toCapture;
    }

    private int grabMax(int chunkIndex, int chunkOffset, int size, byte[] dest, int destPos) {

        byte[] chunk = chunks.get(chunkIndex);
        int toGrab = min(size, chunk.length - chunkOffset);

        System.arraycopy(chunk, chunkOffset, dest, destPos, toGrab);

        return toGrab;
    }

    private void addChunks(int num) {
        for (int i = 0; i < num; ++i) {
            chunks.add(new byte[CHUNK_SIZE]);
        }
    }
}
