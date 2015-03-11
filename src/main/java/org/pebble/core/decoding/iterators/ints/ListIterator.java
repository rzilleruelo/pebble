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

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.io.InputBitStream;
import org.pebble.core.PebbleBytesStore;

import java.io.IOException;

import static org.pebble.core.encoding.DefaultParametersValues.DEFAULT_MIN_INTERVAL_SIZE;

/**
 * Iterator over a compressed list of <code>int</code>s. See
 * {@link org.pebble.core.encoding.OutputSuccinctStream#writeList(it.unimi.dsi.fastutil.ints.IntList, int, int, org.pebble.core.encoding.ints.datastructures.IntReferenceListsStore) writeList}
 * for details regarding the compressed representation.
 */
public class ListIterator extends StrictlyIncrementalListIterator {

    private int index;
    private int remainingElements;
    private int lastIndex;
    private final IntList valuesMap;

    private ListIterator(
        final int listIndex,
        final int valueBitSize,
        final int minIntervalSize,
        final InputBitStream inputBitStream,
        final PebbleBytesStore bytesStore
    ) throws IOException {
        super(listIndex, valueBitSize, minIntervalSize, inputBitStream, bytesStore);
        final long offset = inputBitStream.position();
        valuesMap = new IntArrayList();
        int value;
        while ((value = super.nextInt()) != -1) {
            valuesMap.add(value);
        }
        inputBitStream.position(offset);
        remainingElements = inputBitStream.readDelta() + valuesMap.size();
        lastIndex = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int nextInt() {
        try {
            if (hasNext()) {
                remainingElements--;
                index = inputBitStream.readDelta();
                if ((index & 1) == 0) {
                    lastIndex = index / 2 + lastIndex;
                } else {
                    lastIndex = lastIndex - (index + 1) / 2;
                }
                return valuesMap.get(lastIndex);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {
        return remainingElements > 0;
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
    public static ListIterator build(
        final int listIndex,
        final int valueBitSize,
        final PebbleBytesStore bytesStore
    ) throws IOException {
        return build(listIndex, valueBitSize, bytesStore, bytesStore.getInputBitStream(listIndex));
    }

    /**
     * Instance builder.
     * @param listIndex index of the current list.
     * @param valueBitSize fixed number of bits used to represent value in list to be encoded. It can be any value
     *                     between 1bit and 31 bits.
     * @param inputBitStream input bit stream used to read the compressed lists representations. The cursor must be
     *                       positioned at the beginning of encoding.
     * @param bytesStore mapping between list offsets and data bytes arrays and bytes offsets.
     * @return built instance.
     * @throws IOException when there is an exception reading from <code>inputBitStream</code>.
     */
    public static ListIterator build(
        final int listIndex,
        final int valueBitSize,
        final PebbleBytesStore bytesStore,
        final InputBitStream inputBitStream
    ) throws IOException {
        return new ListIterator(listIndex, valueBitSize, DEFAULT_MIN_INTERVAL_SIZE, inputBitStream, bytesStore);
    }

}
