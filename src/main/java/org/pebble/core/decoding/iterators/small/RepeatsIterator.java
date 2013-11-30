package org.pebble.core.decoding.iterators.small;

/*
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

import java.io.IOException;

/**
 * Implements the iterator of the repetitions of an incremental list. See
 * {@link org.pebble.core.encoding.OutputSuccinctStream#writeRepetitions(it.unimi.dsi.fastutil.ints.IntList) writeRepetitions}
 * for details about the encoding.
 */
class RepeatsIterator extends CompressionIterator {

    private int currentIndex;
    private int repetitions;

    /**
     * @param inputBitStream input bit stream used to read the compressed list representation.
     * @throws IOException when there is an exception reading from <code>inputBitStream</code>.
     */
    public RepeatsIterator(final InputBitStream inputBitStream) throws IOException {
        super(inputBitStream);
        if (remainingElements > 0) {
            currentValue = inputBitStream.readDelta();
            repetitions = inputBitStream.readDelta() + 1;
            remainingElements--;
            recordOffset();
        } else {
            currentValue = -1;
        }
        currentIndex = 0;
    }

    /**
     * @return the number remaining elements of current iteration.
     */
    public int getRemainingElements() {
        return remainingElements;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int next() throws IOException {
        if (currentIndex == currentValue && repetitions > 0) {
            repetitions--;
            return 1;
        } else if (currentIndex >= currentValue && remainingElements > 0) {
            seek();
            currentValue = inputBitStream.readDelta() + currentValue + 1;
            repetitions = inputBitStream.readDelta() + 1;
            remainingElements--;
            recordOffset();
        }
        currentIndex++;
        return 0;
    }
}
