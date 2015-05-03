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

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.pebble.UnitTest;
import org.pebble.core.decoding.iterators.Helper;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.pebble.core.decoding.iterators.Helper.getInput;

@Category(UnitTest.class)
public class InputBitStreamReadDeltaTest {

    @Test
    public void readDeltaItShouldReadExpectedValues() throws IOException {
        final IntList expectedValues = new IntArrayList(new int[] {17, 4, 6});
        /**
         * 18        5     7     Increment one.
         * 10010     101   111   Binary representation.
         * 5-0010    3-01  3-11  Decimal Gamma Prefix and Binary Gamma Suffix.
         * 101-0010  11-01 11-11 Binary Gamma Prefix and Binary Gamma Suffix.
         * 001010010 01101 01111 Delta Encoding.
         */
        final Helper.Input input = getInput("001010010 01101 01111");
        final InputBitStream inputStream = new InputBitStream(input.buffer);
        final IntList values = new IntArrayList();

        for (int i = 0; i < expectedValues.size(); i++) {
            values.add(inputStream.readDelta());
        }

        assertEquals(expectedValues, values);
    }

    @Test
    public void readLongDeltaItShouldReadExpectedValues() throws IOException {
        final LongList expectedValues = new LongArrayList(new long[] {17L, 4L, 6L});
        /**
         * 18        5     7     Increment one.
         * 10010     101   111   Binary representation.
         * 5-0010    3-01  3-11  Decimal Gamma Prefix and Binary Gamma Suffix.
         * 101-0010  11-01 11-11 Binary Gamma Prefix and Binary Gamma Suffix.
         * 001010010 01101 01111 Delta Encoding.
         */
        final Helper.Input input = getInput("001010010 01101 01111");
        final InputBitStream inputStream = new InputBitStream(input.buffer);
        final LongList values = new LongArrayList();

        for (int i = 0; i < expectedValues.size(); i++) {
            values.add(inputStream.readLongDelta());
        }

        assertEquals(expectedValues, values);
    }

}
