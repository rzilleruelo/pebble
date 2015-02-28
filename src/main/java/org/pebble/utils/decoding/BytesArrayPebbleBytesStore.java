package org.pebble.utils.decoding;

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

import org.pebble.core.decoding.PebbleBytesStore;

/**
 * Wrapper of a single byte array which implements the {@link org.pebble.core.decoding.PebbleBytesStore}
 * interface. This implementation loads into main memory the full compressed data stored at <code>store</code> byte
 * array. Given the maximum number of an array 2^31-1 in java, this implementation is limited to compressed data sets
 * that fit in a single array. Approximately not bigger than 1.9[Gb].
 */
public class BytesArrayPebbleBytesStore extends PebbleBytesStore {

    private final byte[] store;
    private final long[] offsets;

    /**
     * Initialize a pebble byte store containing the compressed lists stored on <code>store</code> and its respective
     * offsets contained at <code>offsets</code>.
     * @param store byte array containing the bits of the compressed lists.
     * @param offsets offsets indicating the start in bits of each compressed list representation stored in
     *                <code>store</code>.
     */
    public BytesArrayPebbleBytesStore(byte[] store, long[] offsets) {
       this.store = store;
        this.offsets = offsets;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] get(int listIndex) {
        return store;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long offset(int listIndex) {
        return this.offsets[listIndex];
    }
}
