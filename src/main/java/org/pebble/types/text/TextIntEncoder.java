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

package org.pebble.types.text;

import it.unimi.dsi.io.OutputBitStream;
import org.pebble.core.PebbleOffsetsStoreWriter;
import org.pebble.core.encoding.EncodingInfo;
import org.pebble.types.TypeMapEncoder;

import java.io.IOException;

/**
 * Class used to encode a list of {@link java.lang.String} types into a list of integers that can be compressed by
 * the core pebble functionality.
 */
public class TextIntEncoder extends TypeMapEncoder<String> {

    /**
     * Maximum supported length of text to be decoded.
     */
    public static final int MAX_TEXT_LENGTH = 1024;

    /**
     *
     * @param referenceWindowSize size of the maximum size of the list index between current list and its encoded value
     *                            list index.
     * @param outputBitStream output stream where a new encoded element will be written.
     * @param offsetsStore stores the current offset after each element is written.
     * @param encodingInfo encodingInfo used to keep track of relevant encoding information.
     */
    public TextIntEncoder(
        final int referenceWindowSize,
        final OutputBitStream outputBitStream,
        final PebbleOffsetsStoreWriter offsetsStore,
        final EncodingInfo encodingInfo
    ) {
        super(referenceWindowSize, outputBitStream, offsetsStore, encodingInfo);
    }

    /**
     * Store the length of the text stored in <code>element</code> using delta encoding and then stores
     * the bytes of the string characters.
     * @param element to be written.
     * @return numbers of bits used to write the encoded <code>element</code>.
     * @throws IOException when there is an exception writing <code>element</code>.
     * @throws IllegalArgumentException when the length of <code>element</code> is bigger than
     * {@link TextIntEncoder#MAX_TEXT_LENGTH}.
     */
    @Override
    protected int write(final String element) throws IOException {
        final int size = element.length();
        if (size > TextIntEncoder.MAX_TEXT_LENGTH) {
            throw new IllegalArgumentException(String.format(
                "element \"%s\" of length: %d, cannot be bigger than %d",
                element,
                element.length(),
                MAX_TEXT_LENGTH
            ));
        }
        final int offset = outputBitStream.writeDelta(size);
        return offset + (int) outputBitStream.write(element.getBytes(), size * 8);
    }

}
