package org.pebble.core.encoding;

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

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.pebble.UnitTest;

import static junit.framework.TestCase.assertEquals;
import static org.pebble.core.encoding.Helper.getOutput;
import static org.pebble.core.encoding.Helper.toBinaryString;

@Category(UnitTest.class)
public class LongsOutputSuccinctStreamWriteRepetitionsTest {

    @Test
    public void whenRepetitionsItShouldWriteItsSuccinctRepresentationSuccessfully() throws Exception {
        final LongList list = new LongArrayList(
            new long[] {1L, 1L, 2L, 3L, 3L, 3L, 5L, 6L, 6L, 7L, 10L, 11L, 11L, 16L, 19L, 19L}
        );
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
        final LongList list = new LongArrayList(new long[] {1L, 2L, 3L, 5L, 6L, 7L, 10L, 11L, 16L, 19L});
        final String expectedOutput = "1".replace(" ", "");
        final int expectedOffset = 1;
        final Helper.Output out = getOutput();

        final int offset = out.stream.writeRepetitions(list);
        out.close();

        assertEquals(expectedOutput, toBinaryString(out.buffer, offset));
        assertEquals(expectedOffset, offset);
    }

}
