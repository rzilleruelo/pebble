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

package org.pebble.types;

import it.unimi.dsi.fastutil.ints.IntIterator;
import org.pebble.core.PebbleBytesStore;
import org.pebble.core.decoding.InputBitStream;
import org.pebble.core.decoding.iterators.ints.ListIterator;

import java.io.IOException;
import java.util.Iterator;

/**
 * Abstract class used to recover list of original complex types from an encoded list of integers obtained using an
 * extending instance of {@link org.pebble.types.TypeMapEncoder}. This class takes the list index stored
 * on the integer list to lookup the offset of the encoded original complex type and decodes the stored value.
 * @param <T> type supported by the encoder.
 */
public abstract class TypeMapDecoder<T> extends TypeIntDecoder<T> {

    /**
     *
     * @param bytesStore mapping between list offsets and data bytes arrays and bytes offsets.
     */
    public TypeMapDecoder(final PebbleBytesStore bytesStore) {
        super(bytesStore);
    }

    /**
     * {@inheritDoc}
     */
    public Iterator<T> read(final int listIndex, final int valueBitSize) throws IOException {
        return new MappedTypeIterator(ListIterator.build(listIndex, valueBitSize, bytesStore));
    }

    /**
     * Reads encoded element.
     * @param inputBitStream input bit stream used to read the encoded value from.
     * @return value of decoded element.
     * @throws IOException in case there is an exception reading the encoded element from <code>inputBitStream</code>.
     */
    protected abstract T read(final InputBitStream inputBitStream) throws IOException;

    /**
     * Iterator over decoded complex types <code>T</code>.
     */
    protected class MappedTypeIterator extends TypeIterator {

        /**
         *
         * @param iterator over the list contained the integers resulting from the encoding of complex types.
         */
        public MappedTypeIterator(IntIterator iterator) {
            super(iterator);
        }

        /**
         * Decodes current element by using the index list stored in current element value, positions the cursor
         * where the encoding starts for the encoded element and decode its value by calling
         * {@link org.pebble.types.TypeMapDecoder#read(org.pebble.core.decoding.InputBitStream)}.
         * @return current decoded element.
         * @throws java.lang.IllegalStateException in case there is an exception reading encoded element.
         */
        @Override
        public T next() {
            try {
                return read(bytesStore.getInputBitStream(iterator.nextInt()));
            } catch (IOException e) {
                throw new IllegalStateException(e.getMessage());
            }
        }

    }

}
