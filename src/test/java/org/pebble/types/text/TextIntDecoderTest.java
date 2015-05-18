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

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.pebble.UnitTest;
import org.pebble.core.PebbleBytesStore;
import org.pebble.core.PebbleOffsetsStore;
import org.pebble.core.decoding.iterators.Helper;
import org.pebble.utils.BytesArrayPebbleBytesStore;
import org.pebble.utils.LongListPebbleOffsetsStore;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.pebble.core.decoding.iterators.Helper.getInput;

@Category(UnitTest.class)
public class TextIntDecoderTest {

    @Test
    public void decodeItShouldReturnExpectedDecodedValue() throws IOException {
        final Helper.Input input = getInput(
            "00100000 01000101 01011000 01000001 01001101 01010000 01001100 01000101"
        );
        final PebbleOffsetsStore offsetsStore = new LongListPebbleOffsetsStore(new long[] {0L});
        final PebbleBytesStore bytesStore = new BytesArrayPebbleBytesStore(input.buffer, offsetsStore);
        final TextIntDecoder encoder = new TextIntDecoder(bytesStore);
        final String expectedValue = "EXAMPLE";

        final String value = encoder.read(input.stream);

        assertEquals(expectedValue, value);
    }

    @Test
    public void whenBufferContainsPreviousDecodedValueDecodeItShouldReturnExpectedDecodedValue() throws IOException {
        final Helper.Input input = getInput(
            "00100000 11111111 11111111 11111111 11111111 11111111 11111111 11111111" +
            "00100000 01000101 01011000 01000001 01001101 01010000 01001100 01000101"
        );
        final PebbleOffsetsStore offsetsStore = new LongListPebbleOffsetsStore(new long[] {0L, 71L});
        final PebbleBytesStore bytesStore = new BytesArrayPebbleBytesStore(input.buffer, offsetsStore);
        final TextIntDecoder encoder = new TextIntDecoder(bytesStore);
        encoder.read(input.stream);
        final String expectedValue = "EXAMPLE";

        final String value = encoder.read(input.stream);

        assertEquals(expectedValue, value);
    }

}
