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

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;
import org.pebble.UnitTest;
import org.pebble.core.PebbleOffsetsStoreWriter;
import org.pebble.core.encoding.EncodingInfo;
import org.pebble.core.encoding.Helper;
import org.pebble.core.encoding.OutputBitStream;
import org.pebble.utils.LongListPebbleOffsetsStore;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.pebble.core.encoding.Helper.getOutput;
import static org.pebble.core.encoding.Helper.toBinaryString;

@Category(UnitTest.class)
public class TextIntEncoderTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void encodeItShouldReturnExpectedEncodedValue() throws IOException {
        final EncodingInfo encodingInfo = new EncodingInfo();
        final int referenceWindowSize = 2;
        final Helper.Output out = getOutput();
        final OutputBitStream outputBitStream = new OutputBitStream(out.buffer);
        final PebbleOffsetsStoreWriter offsetsStore = new LongListPebbleOffsetsStore();
        final TextIntEncoder encoder = new TextIntEncoder(
            referenceWindowSize,
            outputBitStream,
            offsetsStore,
            encodingInfo
        );
        final String value = "EXAMPLE";
        /**
         * 7        E         X       A        M        P        L        E
         * 1000     01000101 01011000 01000001 01001101 01010000 01001100 01000101
         * 100-000  01000101 01011000 01000001 01001101 01010000 01001100 01000101
         * 00100000 01000101 01011000 01000001 01001101 01010000 01001100 01000101
         */
        final String expectedOutput = (
            "00100000 01000101 01011000 01000001 01001101 01010000 01001100 01000101"
        ).replace(" ", "");
        final int expectedTotalOffset = 64;

        final int totalOffset = encoder.write(value);
        outputBitStream.close();

        assertEquals(expectedOutput, toBinaryString(out.buffer, totalOffset));
        assertEquals(expectedTotalOffset, totalOffset);
    }

    @Test
    public void encodeItShouldThrowIllegalArgumentExceptionExceptionWhenEncodingTextBiggerThanMaxLength()
        throws IOException
    {
        final EncodingInfo encodingInfo = new EncodingInfo();
        final int referenceWindowSize = 2;
        final Helper.Output out = getOutput();
        final OutputBitStream outputBitStream = new OutputBitStream(out.buffer);
        final PebbleOffsetsStoreWriter offsetsStore = new LongListPebbleOffsetsStore();
        final TextIntEncoder encoder = new TextIntEncoder(
            referenceWindowSize,
            outputBitStream,
            offsetsStore,
            encodingInfo
        );
        final StringBuilder value = new StringBuilder();
        for (int i = 0; i <= TextIntEncoder.MAX_TEXT_LENGTH; i++) {
            value.append('a');
        }
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(String.format(
            "element \"%s\" of length: %d, cannot be bigger than %d",
            value,
            value.length(),
            TextIntEncoder.MAX_TEXT_LENGTH
        ));

        encoder.write(value.toString());
    }

}
