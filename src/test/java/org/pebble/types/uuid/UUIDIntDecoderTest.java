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

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.pebble.UnitTest;
import org.pebble.core.PebbleBytesStore;
import org.pebble.core.PebbleOffsetsStore;
import org.pebble.core.decoding.iterators.Helper;
import org.pebble.utils.BytesArrayPebbleBytesStore;
import org.pebble.utils.LongListPebbleOffsetsStore;

import java.io.IOException;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.pebble.core.decoding.iterators.Helper.getInput;

@Category(UnitTest.class)
public class UUIDIntDecoderTest {

    @Test
    public void encodeItShouldReturnExpectedDecodedValue() throws IOException {
        final Helper.Input input = getInput(
            "1011101111100110111111001000000110000010010111100100100010011001" +
            "1011101010011111100001100000110011101100111010111100101111011110"
        );
        final PebbleOffsetsStore offsetsStore = new LongListPebbleOffsetsStore(new long[] {0L});
        final PebbleBytesStore bytesStore = new BytesArrayPebbleBytesStore(input.buffer, offsetsStore);
        final UUIDIntDecoder encoder = new UUIDIntDecoder(bytesStore);
        final UUID expectedValue = UUID.fromString("bbe6fc81-825e-4899-ba9f-860cecebcbde");

        final UUID value = encoder.read(input.stream);

        assertEquals(expectedValue, value);
    }

}
