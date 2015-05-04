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

package org.pebble.core.decoding;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.pebble.UnitTest;
import org.pebble.core.decoding.iterators.Helper;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.pebble.core.decoding.iterators.Helper.getInput;

@Category(UnitTest.class)
public class InputBitStreamReadTest {

    @Test
    public void itShouldReadExpectedValues() throws IOException {
        final String expectedValue = "EXAMPLE";
        final Helper.Input input = getInput("01000101 01011000 01000001 01001101 01010000 01001100 01000101");
        final InputBitStream inputStream = new InputBitStream(input.buffer);
        final int size = 56;
        final byte[] value = new byte[7];

        inputStream.read(value, size);

        assertEquals(expectedValue, new String(value));
    }

}
