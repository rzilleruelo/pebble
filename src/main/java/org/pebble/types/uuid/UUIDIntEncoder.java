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

package org.pebble.types.uuid;

import it.unimi.dsi.io.OutputBitStream;
import org.pebble.core.PebbleOffsetsStoreWriter;
import org.pebble.core.encoding.EncodingInfo;
import org.pebble.types.TypeMapEncoder;

import java.io.IOException;
import java.util.UUID;

/**
 * Class used to encode a list of {@link java.util.UUID} types into a list of integers that can be compressed by
 * the core pebble functionality.
 */
public class UUIDIntEncoder extends TypeMapEncoder<UUID> {

    /**
     *
     * @param referenceWindowSize size of the maximum size of the list index between current list and its encoded value
     *                            list index.
     * @param outputBitStream output stream where a new encoded element will be written.
     * @param offsetsStore stores the current offset after each element is written.
     * @param encodingInfo encodingInfo used to keep track of relevant encoding information.
     */
    public UUIDIntEncoder(
        final int referenceWindowSize,
        final OutputBitStream outputBitStream,
        final PebbleOffsetsStoreWriter offsetsStore,
        final EncodingInfo encodingInfo
    ) {
        super(referenceWindowSize, outputBitStream, offsetsStore, encodingInfo);
    }

    /**
     * Store the 64 most significant bits and the 64 least significant bits as longs binary representation.
     * @param element to be written.
     * @return numbers of bits used to write the encoded <code>element</code>.
     * @throws IOException when there is an exception writing <code>element</code>.
     */
    @Override
    protected int write(final UUID element) throws IOException {
        outputBitStream.writeLong(element.getMostSignificantBits(), 64);
        outputBitStream.writeLong(element.getLeastSignificantBits(), 64);
        return 128;
    }

}
