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

package org.pebble.core.encoding;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.pebble.UnitTest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.pebble.core.encoding.Helper.toBinaryString;

@Category(UnitTest.class)
public class OutputBitStreamWriteIntTest {

    @Test
    public void whenWritingMaxValueWithAllSignificantBitsItShouldGenerateExpectedOutput() throws IOException {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final OutputBitStream outputBitStream = new OutputBitStream(outputStream);
        final int x = Integer.MAX_VALUE;
        final int expectedWrittenBits = 31;
        final String expectedOutput = "1111111111111111111111111111111";

        final int writtenBits = outputBitStream.writeInt(x, expectedWrittenBits);
        outputBitStream.close();

        assertEquals(expectedOutput, toBinaryString(outputStream.toByteArray(), writtenBits));
        assertEquals(expectedWrittenBits, writtenBits);
    }

    @Test
    public void whenWritingMinValueWithAllSignificantBitsItShouldGenerateExpectedOutput() throws IOException {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final OutputBitStream outputBitStream = new OutputBitStream(outputStream);
        final int x = 0;
        final int expectedWrittenBits = 31;
        final String expectedOutput = "0000000000000000000000000000000";

        final int writtenBits = outputBitStream.writeInt(x, expectedWrittenBits);
        outputBitStream.close();

        assertEquals(expectedOutput, toBinaryString(outputStream.toByteArray(), writtenBits));
        assertEquals(expectedWrittenBits, writtenBits);
    }

    @Test
    public void whenWritingValueWithNotAllSignificantBitsItShouldGenerateExpectedOutput() throws IOException {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final OutputBitStream outputBitStream = new OutputBitStream(outputStream);
        final int x = 1074268235;
        final int expectedWrittenBits = 20;
            final String expectedOutput = "10000000100001001011";

        final int writtenBits = outputBitStream.writeInt(x, expectedWrittenBits);
        outputBitStream.close();

        assertEquals(expectedOutput, toBinaryString(outputStream.toByteArray(), writtenBits));
        assertEquals(expectedWrittenBits, writtenBits);
    }

    @Test
    public void whenWritingValueWith32BitsItShouldGenerateExpectedOutput() throws IOException {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final OutputBitStream outputBitStream = new OutputBitStream(outputStream);
        final int x = -1;
        final int expectedWrittenBits = 32;
        final String expectedOutput = "11111111111111111111111111111111";

        final int writtenBits = outputBitStream.writeInt(x);
        outputBitStream.close();

        assertEquals(expectedOutput, toBinaryString(outputStream.toByteArray(), writtenBits));
        assertEquals(expectedWrittenBits, writtenBits);
    }

}
