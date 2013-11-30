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
 * Has base logic used to implement the iterators for the elements of the compressed representation of a list.
 */
abstract class CompressionIterator {

    /**
     * stream to read from.
     */
    protected final InputBitStream inputBitStream;

    /**
     * current value of the iteration.
     */
    protected int currentValue;

    /**
     * auxiliary variable used to keep the current value of the iteration when reading next one.
     */
    protected int value;

    /**
     * number of the remaining elements in the iteration.
     */
    protected int remainingElements;

    /**
     * current offset of the cursor in the input stream.
     */
    protected long offset;

    /**
     * @param inputBitStream stream to read from.
     * @throws IOException when there is an exception reading from <code>inputBitStream</code> the number of
     *                     elements in the iteration.
     */
    public CompressionIterator(final InputBitStream inputBitStream) throws IOException {
        this.inputBitStream = inputBitStream;
        this.remainingElements = inputBitStream.readDelta();
    }

    /**
     * records internally the current position of the cursor at the <code>inputBitStream</code>. This is useful
     * to come back to the current position to continue reading after reading other sections of
     * <code>inputBitStream</code>.
     */
    protected void recordOffset() {
        offset = inputBitStream.position();
    }

    /**
     * Sets the cursor of <code>inputBitStream</code> in the latest recorded offset.
     * @throws IOException when there is an exception moving the cursor of <code>inputBitStream</code>.
     */
    public void seek() throws IOException {
        inputBitStream.position(offset);
    }

    /**
     * Method used to determine of the iteration has remaining elements.
     * @return true in case of the current iteration has remaining elements and false we does not.
     */
    public boolean hasNext() {
        return currentValue != -1;
    }

    /**
     * Retrieves next element in the iteration.
     * @return next element in the iteration.
     * @throws IOException when there is an exception reading the next value from <code>inputBitStream</code>.
     */
    public abstract int next() throws IOException;

}
