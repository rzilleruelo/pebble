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

import java.io.IOException;
import java.util.Iterator;

/**
 * Abstract class used to decode a list of complex types from an encoded list of integers obtained using an
 * extending instance of {@link org.pebble.types.TypeIntEncoder}. This class defines the base for such kind of decoders,
 * expanding pebble capabilities to decompress complex data types.
 * @param <T> type supported by the decoder.
 */
public abstract class TypeIntDecoder<T> {

    /**
     * Mapping between list offsets and data bytes arrays and bytes offsets.
     */
    protected final PebbleBytesStore bytesStore;

    /**
     *
     * @param bytesStore mapping between list offsets and data bytes arrays and bytes offsets.
     */
    public TypeIntDecoder(final PebbleBytesStore bytesStore) {
        this.bytesStore = bytesStore;
    }

    /**
     * Builds iterator over a list of complex types <code>T</code>.
     * @param listIndex index of the current list.
     * @param valueBitSize fixed number of bits used to represent the values in the encoded list. It can be any value
     *                     between 1bit and 31 bits.
     * @return iterator of decoded list.
     * @throws IOException in case there is an exception reading the information required to build iterator.
     */
    public abstract Iterator<T> read(final int listIndex, final int valueBitSize) throws IOException;

    /**
     * Base class used to define complex types <code>T</code> iterator.
     */
    protected abstract class TypeIterator implements Iterator<T> {

        /**
         * Iterator over the list containing the integers resulting from the encoding of complex types.
         */
        protected final IntIterator iterator;

        /**
         *
         * @param iterator over the list containing the integers resulting from the encoding of complex types.
         */
        public TypeIterator(final IntIterator iterator) {
            this.iterator = iterator;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        /**
         * Unsupported method. The list cannot be modified.
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException("List is immutable");
        }

    }

}
