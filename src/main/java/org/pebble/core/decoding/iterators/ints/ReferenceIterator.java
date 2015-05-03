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

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.io.InputBitStream;
import org.pebble.core.PebbleBytesStore;

import java.io.IOException;

/**
 * Has base logic used to implement a reference iterator of the compressed representation of a list.
 */
abstract class ReferenceIterator extends CompressionIterator {

    private final IntIterator referenceListIterator;
    private boolean currentBit;
    private int remainingBlockElements;

    /**
     * Fixed number of bits used to represent the values without compression in the compressed list.
     */
    protected final int valueBitSize;

    /**
     * Min size of intervals used to encode the compressed list.
     */
    protected final int minIntervalSize;

    /**
     * Mapping between list offsets and data bytes arrays and bytes offsets.
     */
    protected final PebbleBytesStore bytesStore;

    /**
     * @param listIndex offset of the current list that is described in terms of reference.
     * @param valueBitSize fixed number of bits used to represent value in list to be encoded. It can be any value
     *                     between 1bit and 31 bits.
     * @param minIntervalSize min size of intervals used to encode the compressed list.
     * @param inputBitStream input bit stream used to read the compressed lists representations.
     * @param bytesStore mapping between list offsets and data bytes arrays and bytes offsets.
     * @throws IOException when there is an exception reading from <code>inputBitStream</code>.
     */
    public ReferenceIterator(
        final int listIndex,
        final int valueBitSize,
        final int minIntervalSize,
        final InputBitStream inputBitStream,
        final PebbleBytesStore bytesStore
    ) throws IOException {
        super(inputBitStream);
        this.valueBitSize = valueBitSize;
        this.minIntervalSize = minIntervalSize;
        this.bytesStore = bytesStore;
        final int index = remainingElements;
        if (index > 0) {
            remainingElements = inputBitStream.readDelta();
            final int i = listIndex - index;
            referenceListIterator = getReferenceListIterator(i, bytesStore.getInputBitStream(i));
            currentBit = inputBitStream.readBit() == 0;
            remainingBlockElements = 0;
            recordOffset();
            currentValue = getNextReferenceListMatchingElement();
        } else {
            currentValue = -1;
            remainingElements = 0;
            referenceListIterator = null;
            currentBit = false;
            remainingBlockElements = 0;
            recordOffset();
        }
    }

    /**
     * Initializes the specific reference iterator.
     * @return iterator of reference list.
     * @throws IOException in case there is an exception reading from the input stream when initializing the
     *                     specific reference iterator.
     */
    public abstract IntIterator getReferenceListIterator(
        final int listIndex,
        final InputBitStream inputBitStream
    ) throws IOException;

    /**
     * {@inheritDoc}
     */
    @Override
    public int next() throws IOException {
        value = currentValue;
        currentValue = getNextReferenceListMatchingElement();
        return value;
    }

    private int getNextReferenceListMatchingElement() throws IOException {
        if (remainingBlockElements > 0) {
            if (currentBit) {
                remainingBlockElements--;
                return referenceListIterator.nextInt();
            }
            referenceListIterator.skip(remainingBlockElements);
            remainingBlockElements = 0;
            return getNextReferenceListMatchingElement();
        }
        if (remainingElements > 0) {
            currentBit = !currentBit;
            seek();
            remainingBlockElements = inputBitStream.readDelta() + 1;
            remainingElements--;
            recordOffset();
            return getNextReferenceListMatchingElement();
        }
        if (!currentBit && referenceListIterator.hasNext()) {
            return referenceListIterator.nextInt();
        }
        return -1;
    }
}
