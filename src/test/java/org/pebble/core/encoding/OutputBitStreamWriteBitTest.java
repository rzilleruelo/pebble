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
public class OutputBitStreamWriteBitTest {

    @Test
    public void whenWritingOnBitItShouldGenerateExpectedOutput() throws IOException {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final OutputBitStream outputBitStream = new OutputBitStream(outputStream);
        final boolean bit = true;
        final int expectedWrittenBits = 1;
        final String expectedOutput = "1";

        final int writtenBits = outputBitStream.writeBit(bit);
        outputBitStream.close();

        assertEquals(expectedOutput, toBinaryString(outputStream.toByteArray(), writtenBits));
        assertEquals(expectedWrittenBits, writtenBits);
    }

    @Test
    public void whenWritingOffBitItShouldGenerateExpectedOutput() throws IOException {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final OutputBitStream outputBitStream = new OutputBitStream(outputStream);
        final boolean bit = false;
        final int expectedWrittenBits = 1;
        final String expectedOutput = "0";

        final int writtenBits = outputBitStream.writeBit(bit);
        outputBitStream.close();

        assertEquals(expectedOutput, toBinaryString(outputStream.toByteArray(), writtenBits));
        assertEquals(expectedWrittenBits, writtenBits);
    }

}
