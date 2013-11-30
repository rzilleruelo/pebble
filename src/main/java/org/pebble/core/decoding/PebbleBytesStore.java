package org.pebble.core.decoding;

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
 * Abstract class of Pebble's compressed data store. Abstracts the specifics on how the compressed data is
 * stored and retrieved, adding flexibility in the way the compressed data is handled.
 */
public abstract class PebbleBytesStore {

    /**
     * Returns bits input stream that contains the compressed list of the given index <code>listIndex</code> with the
     * cursor positioned on the beginning of the representation.
     * @param listIndex index of list.
     * @return bits input stream that contains the compressed list of the given index <code>listIndex</code> with the
     * cursor positioned on the beginning of the representation.
     * @throws IOException in case there is an exception positioning the cursor in the beginning of the representation.
     */
    public InputBitStream getInputBitStream(final int listIndex) throws IOException {
        final InputBitStream inputBitStream = new InputBitStream(get(listIndex));
        inputBitStream.position(offset(listIndex));
        return inputBitStream;
    }

    /**
     * Gets the bytes array which contains the data of the compressed list associated with <code>listIndex</code>.
     * @param listIndex index of list.
     * @return byte array which contains the data of the compressed list associated with <code>listIndex</code>.
     */
    protected abstract byte[] get(int listIndex);

    /**
     * Gets the offset in bits where the compressed list associated with <code>listIndex</code> starts.
     * @param listIndex index of list.
     * @return the offset in bits where the compressed list associated with <code>listIndex</code> starts.
     */
    protected abstract long offset(int listIndex);

}
