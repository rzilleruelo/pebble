package org.pebble.core.decoding.iterators.ints;

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

import it.unimi.dsi.io.InputBitStream;
import org.pebble.core.decoding.PebbleBytesStore;
import org.pebble.core.encoding.DefaultParametersValues;

import java.io.IOException;

/**
 * Iterator over a compressed strictly incremental list of <code>int</code>s. See
 * {@link org.pebble.core.encoding.OutputSuccinctStream#writeStrictlyIncrementalList(it.unimi.dsi.fastutil.ints.IntList, int, int, org.pebble.core.encoding.ints.datastructures.IntReferenceListsStore) writeStrictlyIncrementalList}
 * for details regarding the compressed representation.
 */
public class StrictlyIncrementalListIterator extends BaseListIterator {

    /**
     * Creates an iterator over the compressed representation of an strictly incremental list of <code>int</code>s.
     * @param listIndex index of the current list.
     * @param valueBitSize fixed number of bits used to represent value in list to be encoded. It can be any value
     *                     between 1bit and 31 bits.
     * @param inputBitStream input bit stream used to read the compressed lists representations.
     * @param bytesStore mapping between list offsets and data bytes arrays and bytes offsets.
     * @throws IOException when there is an exception reading from <code>inputBitStream</code>.
     */
    protected StrictlyIncrementalListIterator(
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
    protected ReferenceIterator initializeReferenceIterator(final int listIndex, final InputBitStream inputBitStream) throws IOException {
        return new StrictlyIncrementalReferenceIterator(listIndex, valueBitSize, minIntervalSize, inputBitStream, bytesStore);
    }

    /**
     * Instance builder.
     * @param listIndex index of the current list.
     * @param valueBitSize fixed number of bits used to represent value in list to be encoded. It can be any value
     *                     between 1bit and 31 bits.
     * @param bytesStore mapping between list offsets and data bytes arrays and bytes offsets.
     * @return built instance.
     * @throws IOException when there is an exception reading from <code>inputBitStream</code>.
     */
    public static StrictlyIncrementalListIterator build(
        final int listIndex,
        final int valueBitSize,
        final PebbleBytesStore bytesStore
    ) throws IOException {
        return new StrictlyIncrementalListIterator(
            listIndex,
            valueBitSize,
            DefaultParametersValues.DEFAULT_MIN_INTERVAL_SIZE,
            bytesStore.getInputBitStream(listIndex),
            bytesStore
        );
    }

}
