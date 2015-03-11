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

import it.unimi.dsi.io.InputBitStream;
import org.pebble.core.PebbleBytesStore;
import org.pebble.types.TypeMapDecoder;

import java.io.IOException;
import java.util.UUID;

/**
 * Class used to decode a list of {@link java.util.UUID} from an encoded list of integers obtained using an
 * instance of {@link org.pebble.types.uuid.UUIDIntEncoder}.
 */
public class UUIDIntDecoder extends TypeMapDecoder<UUID> {

    /**
     *
     * @param bytesStore mapping between list offsets and data bytes arrays and bytes offsets.
     */
    public UUIDIntDecoder(final PebbleBytesStore bytesStore) {
        super(bytesStore);
    }

    /**
     * Decodes uuid by reading the 64 most significant bits and least 64 significant bits stored as longs binary
     * representation.
     * @param inputBitStream input bit stream used to read the encoded value from.
     * @return value of decoded element.
     * @throws IOException in case there is an exception reading the encoded element from <code>inputBitStream</code>.
     */
    @Override
    protected UUID read(final InputBitStream inputBitStream) throws IOException {
        return new UUID(inputBitStream.readLong(64), inputBitStream.readLong(64));
    }

}
