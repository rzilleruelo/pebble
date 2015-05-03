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
public class OutputBitStreamWriteLongInBufferTest {

    @Test
    public void whenBufferIsEmptyAndValueFitInAByteItShouldGenerateExpectedOutput() throws IOException {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final OutputBitStream outputBitStream = new OutputBitStream(outputStream);
        final long x = 165L;
        final int expectedWrittenBits = 8;
        final String expectedOutput = "10100101";

        final int writtenBits = outputBitStream.writeInBuffer(x, expectedWrittenBits);
        outputBitStream.close();

        assertEquals(expectedOutput, toBinaryString(outputStream.toByteArray(), writtenBits));
        assertEquals(expectedWrittenBits, writtenBits);
    }

    @Test
    public void whenBufferIsNotEmptyAndValueFitsInRemainingBufferItShouldGenerateExpectedOutput() throws IOException {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final OutputBitStream outputBitStream = new OutputBitStream(outputStream);
        final long[] values = {2L, 51L};
        final int[] sizes = {2, 6};
        final int expectedWrittenBits = 8;
        final String expectedOutput = "10 110011".replace(" ", "");
        int writtenBits = 0;

        for (int i = 0; i < values.length; i++) {
            writtenBits += outputBitStream.writeInBuffer(values[i], sizes[i]);
        }
        outputBitStream.close();

        assertEquals(expectedOutput, toBinaryString(outputStream.toByteArray(), writtenBits));
        assertEquals(expectedWrittenBits, writtenBits);
    }

    @Test
    public void whenBufferIsEmptyAndValueDoesNotFitInAByteItShouldGenerateExpectedOutput() throws IOException {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final OutputBitStream outputBitStream = new OutputBitStream(outputStream);
        final long x = 10249L;
        final int expectedWrittenBits = 14;
        final String expectedOutput = "10100000001001";

        final int writtenBits = outputBitStream.writeInBuffer(x, expectedWrittenBits);
        outputBitStream.close();

        assertEquals(expectedOutput, toBinaryString(outputStream.toByteArray(), writtenBits));
        assertEquals(expectedWrittenBits, writtenBits);
    }

    @Test
    public void whenBufferIsNotEmptyAndValueDoesNotFitInAByteItShouldGenerateExpectedOutput() throws IOException {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final OutputBitStream outputBitStream = new OutputBitStream(outputStream);
        final long[] values = {2L, 10249L};
        final int[] sizes = {2, 14};
        final int expectedWrittenBits = 16;
        final String expectedOutput = "10 10100000001001".replace(" ", "");;
        int writtenBits = 0;

        for (int i = 0; i < values.length; i++) {
            writtenBits += outputBitStream.writeInBuffer(values[i], sizes[i]);
        }
        outputBitStream.close();

        assertEquals(expectedOutput, toBinaryString(outputStream.toByteArray(), writtenBits));
        assertEquals(expectedWrittenBits, writtenBits);
    }

    @Test
    public void whenBufferIsFullAndThereIsANewValueItShouldGenerateExpectedOutput() throws IOException {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final OutputBitStream outputBitStream = new OutputBitStream(outputStream);
        final long[] values = {148L, 51L};
        final int[] sizes = {8, 6};
        final int expectedWrittenBits = 14;
        final String expectedOutput = "10010100 110011".replace(" ", "");;
        int writtenBits = 0;

        for (int i = 0; i < values.length; i++) {
            writtenBits += outputBitStream.writeInBuffer(values[i], sizes[i]);
        }
        outputBitStream.close();

        assertEquals(expectedOutput, toBinaryString(outputStream.toByteArray(), writtenBits));
        assertEquals(expectedWrittenBits, writtenBits);
    }

}
