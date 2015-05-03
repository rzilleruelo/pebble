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

package org.pebble.core.encoding;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Output stream that contains useful method to write data at bit level.
 */
public class OutputBitStream implements Flushable, Closeable {

    private byte free;
    private int buffer;
    private final OutputStream os;

    /**
     * Initializes empty output stream associated to <code>os</code>.
     * @param os where data will be written.
     */
    public OutputBitStream(final OutputStream os) {
        free = 8;
        buffer = 0;
        this.os = new BufferedOutputStream(os);
    }

    /**
     * Initializes empty output stream associated to <code>buffer</code>.
     * @param buffer where data will be written.
     */
    public OutputBitStream(final byte[] buffer) {
        free = 8;
        this.buffer = 0;
        os = new ArrayOutputStream(buffer);
    }

    /**
     * Writes delta coding of value <code>x</code>. For the example value: 5
     * <pre>
     *     6 Increment one.
     *     110 Binary representation.
     *     3-10 Decimal Gamma Prefix and Binary Gamma Suffix.
     *     11-10 Binary Gamma Prefix and Binary Gamma Suffix.
     *     01110 Delta Encoding.
     * </pre>
     *
     * @param x value to be encoded.
     * @return number of written bits.
     * @throws IOException in case there is an exception writing into output stream.
     */
    public int writeDelta(int x) throws IOException {
        x++;
        final int size = 31 - Integer.numberOfLeadingZeros(x);
        return writeGamma(size) + writeInt(x, size);
    }

    /**
     * Writes delta coding of value <code>x</code>. For the example value: 5
     * <pre>
     *     6 Increment one.
     *     110 Binary representation.
     *     3-10 Decimal Gamma Prefix and Binary Gamma Suffix.
     *     11-10 Binary Gamma Prefix and Binary Gamma Suffix.
     *     01110 Delta Encoding.
     * </pre>
     *
     * @param x value to be encoded.
     * @return number of written bits.
     * @throws IOException in case there is an exception writing into output stream.
     */
    public int writeDelta(long x) throws IOException {
        x++;
        final int size = 63 - Long.numberOfLeadingZeros(x);
        return writeGamma(size) + writeLong(x, size);
    }

    /**
     * Writes gamma coding of value <code>x</code>. For the example value: 5
     * <pre>
     *     6     Increment one.
     *     110   Binary representation.
     *     3-10  Decimal Gamma Prefix and Binary Gamma Suffix.
     *     00110 Gamma Encoding.
     * </pre>
     *
     * @param x value to be encoded.
     * @return number of written bits.
     * @throws IOException in case there is an exception writing into output stream.
     */
    public int writeGamma(int x) throws IOException {
        x++;
        final int size = 31 - Integer.numberOfLeadingZeros(x);
        return writeInBuffer(1, size + 1) + writeInt(x, size);
    }

    /**
     * Writes gamma coding of value <code>x</code>. For the example value: 5
     * <pre>
     *     6     Increment one.
     *     110   Binary representation.
     *     3-10  Decimal Gamma Prefix and Binary Gamma Suffix.
     *     00110 Gamma Encoding.
     * </pre>
     *
     * @param x value to be encoded.
     * @return number of written bits.
     * @throws IOException in case there is an exception writing into output stream.
     */
    public int writeGamma(long x) throws IOException {
        x++;
        final int size = 63 - Long.numberOfLeadingZeros(x);
        return writeInBuffer(1, size + 1) + writeLong(x, size);
    }

    /**
     * Writes binary representation of the <code>size</code> least significant bits of value <code>x</code>.
     * For example value <code>1074268235</code> and size <code>20</code>
     * <pre>
     *     10000000100001001011
     * </pre>
     * @param x value to be encoded.
     * @return number of written bits.
     * @throws IOException in case there is an exception writing into output stream.
     */
    public int writeInt(final int x, final int size) throws IOException {
        return writeInBuffer(x & (1 << size) - 1, size);
    }

    /**
     * Writes 32 bits binary representation. For example value <code>1074268235</code>
     * <pre>
     *     01000000000010000000100001001011
     * </pre>
     * @param x value to be encoded.
     * @return number of written bits.
     * @throws IOException in case there is an exception writing into output stream.
     */
    public int writeInt(final int x) throws IOException {
        return writeInBuffer(x, 32);
    }

    /**
     * Writes binary representation of the <code>size</code> least significant bits of value <code>x</code>.
     * For example value <code>4620693221977622603L</code> and size <code>54</code>
     * <pre>
     *     100000000000000000000100000000000010000000100001001011
     * </pre>
     * @param x value to be encoded.
     * @return number of written bits.
     * @throws IOException in case there is an exception writing into output stream.
     */
    public int writeLong(final long x, final int size) throws IOException {
        return writeInBuffer(x & (1L << size) - 1L, size);
    }

    /**
     * Writes 64 bits binary representation. For example value <code>4620693221977622603L</code>
     * <pre>
     *     0100000000100000000000000000000100000000000010000000100001001011
     * </pre>
     * @param x value to be encoded.
     * @return number of written bits.
     * @throws IOException in case there is an exception writing into output stream.
     */
    public int writeLong(final long x) throws IOException {
        return writeInBuffer(x, 64);
    }

    /**
     * Writes unary coding of value <code>x</code>. For the example value: <code>17</code>.
     * <pre>
     *     00000000000000001
     * </pre>
     *
     * @param x value to be encoded.
     * @return number of written bits.
     * @throws IOException in case there is an exception writing into output stream.
     */
    public int writeUnary(final int x) throws IOException {
        return writeInBuffer(1, x);
    }

    /**
     * Writes unary encoding of value <code>x</code>. For the example value: <code>17</code>
     * <pre>
     *     00000000000000001
     * </pre>
     *
     * @param x value to be encoded.
     * @return number of written bits.
     * @throws IOException in case there is an exception writing into output stream.
     */
    public int writeUnary(final long x) throws IOException {
        return writeInBuffer(1, (int) x);
    }

    /**
     * Writes 1 when <code>x</code> is true and 0 when <code>x</code> is false.
     * @param x bit to be written.
     * @return number of written bit.
     * @throws IOException in case there is an exception writing into output stream.
     */
    public int writeBit(final boolean x) throws IOException {
        return writeInBuffer(x ? 1 : 0, 1);
    }

    /**
     * Writes <code>size</code> bits from <code>bytes</code> into output stream.
     * @param bytes buffer containing bits to be written.
     * @param size number of bits to be written.
     * @return number of written bits.
     * @throws IOException in case there is an exception writing into output stream.
     */
    public long write(final byte[] bytes, final long size) throws IOException {
        int writtenBits = 0;
        for (int i = 0, b = 8; b < size; i++, b += 8) {
            writeInBuffer(bytes[i], 8);
            writtenBits += 8;
        }
        writeInBuffer(bytes[bytes.length - 1], (int) (size - writtenBits));
        return size;
    }

    /**
     * Writes the <code>size</code> least significant bits from value <code>x</code>.
     * @param x value to be encoded.
     * @param size number of least significant bits to be encoded.
     * @return number of written bits.
     * @throws IOException in case there is an exception writing into output stream.
     */
    public int writeInBuffer(int x, int size) throws IOException {
        final int writtenBits = size;
        if (free == 0) {
            os.write(buffer);
            buffer = 0;
            free = 8;
        }
        while (size > free) {
            buffer |= (x >> (size - free));
            size -= free;
            x &= ~(0xFFFFFFFF << size);
            os.write(buffer);
            buffer = 0;
            free = 8;
        }
        buffer |= x << (32 - size + free);
        free -= size;
        return writtenBits;
    }

    /**
     * Writes the <code>size</code> least significant bits from value <code>x</code>.
     * @param x value to be encoded.
     * @param size number of least significant bits to be encoded.
     * @return number of written bits.
     * @throws IOException in case there is an exception writing into output stream.
     */
    public int writeInBuffer(long x, int size) throws IOException {
        final int writtenBits = size;
        if (free == 0) {
            os.write(buffer);
            buffer = 0;
            free = 8;
        }
        while (size > free) {
            buffer |= (int)(x >> (size - free));
            size -= free;
            x &= ~(0xFFFFFFFFFFFFFFFFL << size);
            os.write(buffer);
            buffer = 0;
            free = 8;
        }
        buffer |= (int) (x << (64 - size + free));
        free -= size;
        return writtenBits;
    }

    /**
     * Flushes buffer content into output stream and closes it.
     * @throws IOException in case there is an exception closing output stream.
     */
    public void close() throws IOException {
        flush();
        os.close();
    }

    /**
     * Flushes buffer content into output stream and clears buffer content.
     * @throws IOException in case there is an exception writing into output stream.
     */
    public void flush() throws IOException {
        if (free < 8) {
            os.write(buffer);
            buffer = 0;
            free = 8;
        }
        os.flush();
    }

}
