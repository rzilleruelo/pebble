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

package org.pebble.core.decoding.iterators.longs;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.io.InputBitStream;
import org.pebble.core.PebbleBytesStore;

import java.io.IOException;

/**
 * Implements the iterator of an strictly incremental lists.
 */
class StrictlyIncrementalReferenceIterator extends ReferenceIterator {

    /**
     * @param listIndex offset of the current list that is described in terms of reference.
     * @param valueBitSize fixed number of bits used to represent value in list to be encoded. It can be any value
     *                     between 1bit and 63 bits.
     * @param minIntervalSize min size of intervals used to encode the compressed list.
     * @param inputBitStream input bit stream used to read the compressed lists representations.
     * @param bytesStore mapping between list offsets and data bytes arrays and bytes offsets.
     * @throws java.io.IOException when there is an exception reading from <code>inputBitStream</code>.
     */
    public StrictlyIncrementalReferenceIterator(
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
    public LongIterator getReferenceListIterator(final int listIndex, final InputBitStream inputBitStream) throws IOException {
        return new StrictlyIncrementalListIterator(listIndex, valueBitSize, minIntervalSize, inputBitStream, bytesStore);
    }

}
