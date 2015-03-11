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
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.pebble.UnitTest;
import org.pebble.core.PebbleOffsetsStoreWriter;
import org.pebble.core.encoding.EncodingInfo;
import org.pebble.core.encoding.Helper;
import org.pebble.utils.LongListPebbleOffsetsStore;

import java.io.IOException;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.pebble.core.encoding.Helper.getOutput;
import static org.pebble.core.encoding.Helper.toBinaryString;

@Category(UnitTest.class)
public class UUIDIntEncoderTest {

    @Test
    public void encodeItShouldReturnExpectedEncodedValue() throws IOException {
        final EncodingInfo encodingInfo = new EncodingInfo();
        final int referenceWindowSize = 2;
        final Helper.Output out = getOutput();
        final OutputBitStream outputBitStream = new OutputBitStream(out.buffer);
        final PebbleOffsetsStoreWriter offsetsStore = new LongListPebbleOffsetsStore();
        final UUIDIntEncoder encoder = new UUIDIntEncoder(
            referenceWindowSize,
            outputBitStream,
            offsetsStore,
            encodingInfo
        );
        final UUID value = UUID.fromString("bbe6fc81-825e-4899-ba9f-860cecebcbde");
        /**
         * "bbe6fc81-825e-4899-ba9f-860cecebcbde"
         * -4999129671285355554 --> 1011101111100110111111001000000110000010010111100100100010011001
         * -4999129671285355554 --> 1011101010011111100001100000110011101100111010111100101111011110
         */
        final String expectedOutput = (
            "1011101111100110111111001000000110000010010111100100100010011001" +
            "1011101010011111100001100000110011101100111010111100101111011110"
        ).replace(" ", "");
        final int expectedTotalOffset = 128;

        final int totalOffset = encoder.write(value);

        assertEquals(expectedOutput, toBinaryString(out.buffer, totalOffset));
        assertEquals(expectedTotalOffset, totalOffset);
    }

}
