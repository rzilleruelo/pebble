/**
 *  Copyright 2015 Groupon
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.pebble.core.decoding;

import java.io.Closeable;
import java.io.IOException;

/**
 * Byte array input stream wrapper with useful method to read data at bit level.
 */
public class InputBitStream implements Closeable {

    private long position;
    private final byte[] buffer;

    /**
     *
     * @param buffer input bits.
     */
    public InputBitStream(final byte[] buffer) {
        position = 0L;
        this.buffer = buffer;
    }

    /**
     * Reads positive number from delta coding into a long.
     * (see {@link org.pebble.core.encoding.OutputBitStream#writeDelta(int)}).
     * @return Read number.
     * @throws IOException in case there is an exception reading from input stream.
     */
    public int readDelta() throws IOException {
        final int size = readGamma();
        return ((1 << size) | readInt(size)) - 1;
    }

    /**
     * Reads positive number from delta coding into a long.
     * (see {@link org.pebble.core.encoding.OutputBitStream#writeDelta(long)}).
     * @return Read number.
     * @throws IOException in case there is an exception reading from input stream.
     */
    public long readLongDelta() throws IOException {
        final int size = readGamma();
        return ((1L << size) | readInt(size)) - 1L;
    }

    /**
     * Skips <code>skip</code> delta encoded numbers.
     * @param skip  number of delta encoded elements to skip.
     * @throws IOException in case there is an exception reading from input stream.
     */
    public void skipDeltas(final int skip) throws IOException {
        for (int i = 0; i < skip; i++) {
            position = readGamma() + position;
        }
    }

    /**
     * Reads positive number from gamma coding into an int.
     * (see {@link org.pebble.core.encoding.OutputBitStream#writeGamma(int)}).
     * @return Read number.
     * @throws IOException in case there is an exception reading from input stream.
     */
    public int readGamma() throws IOException {
        final int size = readUnary() - 1;
        return ((1 << size) | readInt(size)) - 1;
    }

    /**
     * Reads positive number from gamma coding into a long.
     * (see {@link org.pebble.core.encoding.OutputBitStream#writeGamma(long)}).
     * @return Read number.
     * @throws IOException in case there is an exception reading from input stream.
     */
    public long readLongGamma() throws IOException {
        final int size = readUnary() - 1;
        return ((1L << size) | readLong(size)) - 1L;
    }

    /**
     * Skips <code>skip</code> gamma encoded numbers.
     * @param skip number of gamma encoded elements to skip.
     * @throws IOException in case there is an exception reading from input stream.
     */
    public void skipGammas(final int skip) throws IOException {
        for (int i = 0; i < skip; i++) {
            position = readUnary() - 1 + position;
        }
    }

    /**
     * Reads <code>size</code> bits into an int.
     * (see {@link org.pebble.core.encoding.OutputBitStream#writeInt(int, int)}).
     * @param size number of bits to be read.
     * @return read value.
     * @throws IOException in case there is an exception reading from input stream.
     */
    public int readInt(final int size) throws IOException {
        int x = 0;
        for (int i = 1; i <= size; i++) {
            x |= (read() << (size - i));
        }
        return x;
    }

    /**
     * Reads <code>size</code> bits into a long.
     * (see {@link org.pebble.core.encoding.OutputBitStream#writeLong(long, int)}).
     * @param size number of bits to be read.
     * @return read value.
     * @throws IOException in case there is an exception reading from input stream.
     */
    public long readLong(final int size) throws IOException {
        long x = 0;
        for (int i = 1; i <= size; i++) {
            x |= ((long) read()) << (size - i);
        }
        return x;
    }

    /**
     * Reads positive number from unary coding into an int.
     * (see {@link org.pebble.core.encoding.OutputBitStream#writeUnary(int)}).
     * @return Read number.
     * @throws IOException in case there is an exception reading from input stream.
     */
    public int readUnary() throws IOException {
        int x = 1;
        while (read() == 0) {
            x++;
        }
        return x;
    }

    /**
     * Reads positive number from unary coding into a long.
     * (see {@link org.pebble.core.encoding.OutputBitStream#writeUnary(long)}).
     * @return Read number.
     * @throws IOException in case there is an exception reading from input stream.
     */
    public long readLongUnary() throws IOException {
        long x = 1;
        while (read() == 0) {
            x++;
        }
        return x;
    }

    /**
     * Read single bit into an int. (see {@link org.pebble.core.encoding.OutputBitStream#writeBit(boolean)}).
     * @return Read bit.
     * @throws IOException in case there is an exception reading from input stream.
     */
    public int readBit() throws IOException {
        return read();
    }

    /**
     * Reads <code>size</code> bits from input stream and sets them into <code>buffer</code>.
     * (see {@link org.pebble.core.encoding.OutputBitStream#write(byte[], long)}).
     * @param buffer where read bits will be stored.
     * @param size number of bits to be read.
     * @throws IOException in case there is an exception reading from input stream.
     */
    public void read(final byte[] buffer, final long size) throws IOException {
        for (int i = 0; i < size; i++) {
            buffer[i / 8] |= read() << (7 - (i % 8));
        }
    }

    /**
     * Returns current cursor position.
     * @return current cursor position in bits.
     */
    public long position() {
        return position;
    }

    /**
     * Sets cursor position.
     * @param position new cursor position in bits.
     * @throws IOException in case there is an exception positioning the cursor.
     */
    public void position(final long position) throws IOException {
        this.position = position;
    }

    /**
     * Nothing to be done in current implementation.
     * @throws IOException in case there is an exception closing stream.
     */
    public void close() throws IOException {
        position = buffer.length;
    }

    private int read() throws IOException {
        try {
            return buffer[(int)(position / 8)] >> (7 - (position++ % 8)) & 0x1;
        } catch (ArrayIndexOutOfBoundsException exception) {
            throw new IOException(exception.getMessage());
        }
    }

}
