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

package org.pebble.core.decoding.iterators.ints;

import org.pebble.core.PebbleBytesStore;
import org.pebble.core.decoding.InputBitStream;

import java.io.IOException;

/**
 * This class implements and iterator over a compressed incremental list of <code>int</code>s which removes all
 * duplicating elements. This allows to iterate over an incremental list as if it were an strictly incremental list.
 */
class IncrementalListUniqueIterator extends BaseListIterator {

    /**
     * Creates an unique iterator over the compressed representation of an incremental list of <code>int</code>s.
     * @param listIndex index of the current list.
     * @param valueBitSize fixed number of bits used to represent value in list to be encoded. It can be any value.
     *                     between 1bit and 31 bits.
     * @param minIntervalSize min size of intervals used to encode the compressed list.
     * @param inputBitStream input bit stream used to read the compressed lists representations.
     * @param bytesStore mapping between list offsets and data bytes arrays and bytes offsets.
     * @throws IOException when there is an exception reading from <code>inputBitStream</code>.
     */
    protected IncrementalListUniqueIterator(
        final int listIndex,
        final int valueBitSize,
        final int minIntervalSize,
        final InputBitStream inputBitStream,
        final PebbleBytesStore bytesStore
    ) throws IOException {
        super(listIndex, valueBitSize, minIntervalSize, inputBitStream, bytesStore);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ReferenceIterator initializeReferenceIterator(
        final int listIndex,
        final InputBitStream inputBitStream
    ) throws IOException {
        return new IncrementalReferenceUniqueIterator(listIndex, valueBitSize, minIntervalSize, inputBitStream, bytesStore);
    }

    /**
     * Instance builder.
     * @param listIndex index of the current list.
     * @param valueBitSize fixed number of bits used to represent value in list to be encoded. It can be any value
     *                     between 1bit and 31 bits.
     * @param minIntervalSize min interval size to be encoded as interval.
     * @param inputBitStream input bit stream used to read the compressed lists representations.
     * @param bytesStore mapping between list offsets and data bytes arrays and bytes offsets.
     * @return built instance.
     * @throws IOException when there is an exception reading from <code>inputBitStream</code>.
     */
    public static IncrementalListUniqueIterator build(
        final int listIndex,
        final int valueBitSize,
        final int minIntervalSize,
        final InputBitStream inputBitStream,
        final PebbleBytesStore bytesStore
    ) throws IOException {
        final RepeatsIterator repeatsIterator = new RepeatsIterator(inputBitStream);
        inputBitStream.skipDeltas(repeatsIterator.getRemainingElements() * 2);
        return new IncrementalListUniqueIterator(listIndex, valueBitSize, minIntervalSize, inputBitStream, bytesStore);
    }
}
