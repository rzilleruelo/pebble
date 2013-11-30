package org.pebble.core.encoding;

/*
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

import org.pebble.UnitTest;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.pebble.core.encoding.Helper.getOutput;
import static org.pebble.core.encoding.Helper.toBinaryString;
import static junit.framework.TestCase.assertEquals;

@Category(UnitTest.class)
public class OutputSuccinctStreamWriteRepetitionsTest {

    @Test
    public void whenRepetitionsItShouldWriteItsSuccinctRepresentationSuccessfully() throws Exception {
        final IntList list = new IntArrayList(new int[] {1, 1, 2, 3, 3, 3, 5, 6, 6, 7, 10, 11, 11, 16, 19, 19});
        /**
         * {(0, 2), (2, 3), (4, 2), (7, 2), (9, 2)}    Repetitions Intervals.
         * 5      0  0  1    1    1    0 2    0 1    0 Delta Representation.
         * 6      1  1  2    2    2    1 3    1 2    1 Add 1 to ensure non zeros.
         * 011    1  1  10   10   10   1 11   1 10   1 Binary representation.
         * 3-10   1  1  2-0  2-0  2-0  1 2-1  1 2-0  1 Decimal Gamma Prefix and Binary Gamma Suffix
         * 11-10  1  1  10-0 10-0 10-0 1 10-1 1 10-0 1 Binary Gamma Prefix and Binary Gamma Suffix
         * 01110  1  1  0100 0100 0100 1 0101 1 0100 1 Delta Encoding
         */
        final String expectedOutput = "01110 1 1 0100 0100 0100 1 0101 1 0100 1".replace(" ", "");
        final int expectedOffset = 30;
        final Helper.Output out = getOutput();

        final int offset = out.stream.writeRepetitions(list);
        out.close();

        assertEquals(expectedOutput, toBinaryString(out.buffer, offset));
        assertEquals(expectedOffset, offset);
    }

    @Test
    public void whenNoRepetitionsItShouldWriteItsSuccinctRepresentationSuccessfully() throws Exception {
        final IntList list = new IntArrayList(new int[] {1, 2, 3, 5, 6, 7, 10, 11, 16, 19});
        final String expectedOutput = "1".replace(" ", "");
        final int expectedOffset = 1;
        final Helper.Output out = getOutput();

        final int offset = out.stream.writeRepetitions(list);
        out.close();

        assertEquals(expectedOutput, toBinaryString(out.buffer, offset));
        assertEquals(expectedOffset, offset);
    }

}
