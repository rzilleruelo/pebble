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

import it.unimi.dsi.fastutil.ints.IntIterator;
import org.pebble.core.PebbleBytesStore;
import org.pebble.core.decoding.InputBitStream;

import java.io.IOException;

/**
 * Base class for implementing Pebble's compressed list iterator.
 */
abstract class BaseListIterator implements IntIterator {

    /**
     * Fixed number of bits used to represent value in list to be encoded. It can be any value between 1bit and 31 bits.
     */
    protected final int valueBitSize;

    /**
     * Min interval size to be encoded as interval.
     */
    protected final int minIntervalSize;

    /**
     * Input bit stream used to read the compressed list representation.
     */
    protected final InputBitStream inputBitStream;

    /**
     * Mapping between list offsets and data bytes arrays and bytes offsets.
     */
    protected final PebbleBytesStore bytesStore;

    private final CompressionIterator referenceIt;
    private final CompressionIterator intervalIt;
    private final CompressionIterator deltaIt;

    /**
     * Initializes the iterators of each piece of the compressed representation of a list.
     * <ul>
     *     <li>Reference iterator.</li>
     *     <li>Intervals iterator.</li>
     *     <li>Delta iterator.</li>
     * </ul>
     * @param listIndex offset of the current list that is described in terms of reference.
     * @param valueBitSize fixed number of bits used to represent value in list to be encoded. It can be any value
     *                     between 1bit and 31 bits.
     * @param minIntervalSize min interval size to be encoded as interval.
     * @param inputBitStream input bit stream used to read the compressed list representation.
     * @param bytesStore mapping between list offsets and data bytes arrays and bytes offsets.
     * @throws IOException when there is an exception reading from <code>inputBitStream</code>
     */
    public BaseListIterator(
        final int listIndex,
        final int valueBitSize,
        final int minIntervalSize,
        final InputBitStream inputBitStream,
        final PebbleBytesStore bytesStore
    ) throws IOException {
        this.valueBitSize = valueBitSize;
        this.minIntervalSize = minIntervalSize;
        this.inputBitStream = inputBitStream;
        this.bytesStore = bytesStore;
        referenceIt = initializeReferenceIterator(listIndex, inputBitStream);
        inputBitStream.skipDeltas(referenceIt.remainingElements);
        intervalIt = new IntervalIterator(valueBitSize, minIntervalSize, inputBitStream);
        inputBitStream.skipDeltas(intervalIt.remainingElements * 2);
        deltaIt = new DeltaIterator(valueBitSize, inputBitStream);
        inputBitStream.skipDeltas(deltaIt.remainingElements);
    }

    /**
     * Returns the next <code>int</code> in the iteration. When there is no more elements returns -1
     * @return the next <code>int</code> in the iteration
     */
    @Override
    public int nextInt() {
        try {
            if (
                referenceIt.currentValue != -1 &&
                (intervalIt.currentValue == -1 || referenceIt.currentValue < intervalIt.currentValue) &&
                (deltaIt.currentValue == -1 || referenceIt.currentValue < deltaIt.currentValue)
            ) {
                return referenceIt.next();
            }
            if (
                intervalIt.currentValue != -1 &&
                (deltaIt.currentValue == -1 || intervalIt.currentValue < deltaIt.currentValue)
            ) {
                return intervalIt.next();
            }
            return deltaIt.next();
        } catch (IOException exception) {
            throw new IllegalStateException(exception.getMessage());
        }
    }

    /**
     * checks whether the iteration has remaining elements or not.
     * @return true if there is remaining elements in the iteration and false whether not.
     */
    @Override
    public boolean hasNext() {
        return referenceIt.hasNext() || intervalIt.hasNext() || deltaIt.hasNext();
    }

    /**
     * Returns the next {@link java.lang.Integer} in the iteration. When there is no more elements returns null. This
     * method wrap into an {@link java.lang.Integer} the result from
     * {@link BaseListIterator#nextInt()} method.
     * @return the next {@link java.lang.Integer} in the iteration.
     */
    @Override
    public Integer next() {
        final int value = nextInt();
        return value == -1 ? null : value;
    }

    /**
     * This method skips <code>i</code> elements from current element on iteration.
     * @param i number of elements from current iteration position to be skipped.
     * @return the actual number of skipped elements. When the remaining elements in the iterator is smaller than
     * <code>i</code> only the remaining elements will be skipped.
     */
    @Override
    public int skip(final int i) {
        int n = 0;
        while (hasNext() && n < i) {
            nextInt();
            n++;
        }
        return n;
    }

    /**
     * The iterator is from an immutable list, therefore elements can't be removed. If this method is invoked a
     * {@link java.lang.UnsupportedOperationException} will be thrown.
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("The list is immutable");
    }

    /**
     * Initialize reference iterator.
     * @param listIndex index of the current list that is described in terms of reference.
     * @param inputBitStream input bit stream used to read the compressed list representation.
     * @return initialized reference iterator
     * @throws IOException when the initialization of reference iterator raises the exception
     */
    protected abstract ReferenceIterator initializeReferenceIterator(
        final int listIndex,
        final InputBitStream inputBitStream
    ) throws IOException;
}
