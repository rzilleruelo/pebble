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

package org.pebble.types;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.pebble.core.PebbleOffsetsStoreWriter;
import org.pebble.core.encoding.EncodingInfo;
import org.pebble.core.encoding.OutputBitStream;

import java.io.IOException;

/**
 * Abstract class used to encode a list of a complex types into a list of integers that can be compressed by the core
 * pebble functionality. This encoder writes into the output stream a generic encoding of the values that needs to be
 * implemented on subclasses and then replaces its value by the list index of the encoding. This enable the compression
 * of data types that cannot be transformed into integers and recovered back from its value or in cases where its
 * transformation do not have good compression properties. This class defines the base for such kind of encoders,
 * expanding the capabilities of pebble to compress complex data types.
 * @param <T> type supported by the encoder.
 */
public abstract class TypeMapEncoder<T> extends TypeIntEncoder<T> {

    /**
     * Output stream where a new encoded element will be written.
     */
    protected final OutputBitStream outputBitStream;

    /**
     * Stores the current offset after each encoded element is stored.
     */
    protected final PebbleOffsetsStoreWriter offsetsStore;

    /**
     * Used to keep track of relevant encoding information.
     */
    protected final EncodingInfo encodingInfo;

    private static final int MISSING_MAP_VALUE = -1;

    private final int referenceWindowSize;
    private final T[] buffer;
    private final Object2IntOpenHashMap<T> map;

    private int bufferIndex;

    /**
     *
     * @param referenceWindowSize size of the maximum size of the list index between current list and its encoded value
     *                            list index.
     * @param outputBitStream output stream where a new encoded element will be written.
     * @param offsetsStore stores the current offset after each element is written.
     * @param encodingInfo encodingInfo used to keep track of relevant encoding information.
     */
    @SuppressWarnings({"unchecked"})
    public TypeMapEncoder(
        final int referenceWindowSize,
        final OutputBitStream outputBitStream,
        final PebbleOffsetsStoreWriter offsetsStore,
        final EncodingInfo encodingInfo
    ) {
        this.referenceWindowSize = referenceWindowSize;
        this.outputBitStream = outputBitStream;
        this.offsetsStore = offsetsStore;
        this.encodingInfo = encodingInfo;
        this.buffer = (T[]) new Object[referenceWindowSize];
        this.bufferIndex = 0;
        this.map = new Object2IntOpenHashMap<T>();
        this.map.defaultReturnValue(MISSING_MAP_VALUE);
    }

    /**
     * This methods keeps a map in memory of all elements that has been already encoded. The maximum number of elements
     * the map can hold is <code>referenceWindowSize</code>. This allows the control of the memory used by the encoder
     * and also limits the length of the references used in the mapping. When the number of encoded elements is bigger
     * than <code>referenceWindowSize</code>, elements with longest references in the map will by elements with shorter
     * references.
     * @param element to be encoded to an integer.
     * @return <code>int</code> value of encoded <code>element</code>.
     * @throws IOException in case there is an exception storing the encoded element.
     */
    @Override
    protected int encode(T element) throws IOException {
        int index = map.getInt(element);
        if (index == MISSING_MAP_VALUE || (encodingInfo.getCurrentIndex() - index) > referenceWindowSize) {
            offsetsStore.append(encodingInfo.incrementOffset(write(element)));
            int deleteObjectIndex;
            if (
                buffer[bufferIndex] != null &&
                (deleteObjectIndex = map.getInt(buffer[bufferIndex])) != MISSING_MAP_VALUE &&
                (encodingInfo.getCurrentIndex() - deleteObjectIndex) >= referenceWindowSize
            ) {
                map.remove(buffer[bufferIndex]);
            }
            buffer[bufferIndex] = element;
            bufferIndex = (bufferIndex + 1) % referenceWindowSize;
            index = encodingInfo.incrementCurrentIndex();
            map.put(element, index);
        }
        return index;
    }

    /**
     * Writes encoded value of <code>element</code>.
     * @param element to be written.
     * @return numbers of bits used to write the encoded <code>element</code>.
     * @throws IOException when there is an exception writing <code>element</code>.
     */
    protected abstract int write(final T element) throws IOException;

}
