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

import org.pebble.core.PebbleBytesStore;
import org.pebble.core.decoding.InputBitStream;
import org.pebble.types.TypeMapDecoder;

import java.io.IOException;

/**
 * Class used to decode a list of {@link java.lang.String} from an encoded list of integers obtained using an
 * instance of {@link org.pebble.types.text.TextIntEncoder}.
 */
public class TextIntDecoder extends TypeMapDecoder<String> {

    private final byte[] buffer;

    /**
     *
     * @param bytesStore mapping between list offsets and data bytes arrays and bytes offsets.
     */
    public TextIntDecoder(final PebbleBytesStore bytesStore) {
        super(bytesStore);
        this.buffer = new byte[TextIntEncoder.MAX_TEXT_LENGTH];
    }

    /**
     * Reads encoded string by reading its size delta encoded and then reading its bytes representation.
     * @param inputBitStream input bit stream used to read the encoded value from.
     * @return value of decoded element.
     * @throws IOException in case there is an exception reading the encoded element from <code>inputBitStream</code>.
     */
    @Override
    protected String read(final InputBitStream inputBitStream) throws IOException {
        final int size = inputBitStream.readDelta();
        inputBitStream.read(buffer, size * 8);
        return new String(buffer, 0, size);
    }

}
