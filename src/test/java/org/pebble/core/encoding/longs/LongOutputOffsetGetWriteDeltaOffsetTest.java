package org.pebble.core.encoding.longs;

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

@Category(UnitTest.class)
public class LongOutputOffsetGetWriteDeltaOffsetTest {

    @Test
    public void itShouldReturnExpectedOffsetSuccessfully() {
        final int valueBitSize = 1;
        final LongList list = new LongArrayList(new long[] {1L, 2L, 3L, 5L, 7L, 10L});
        /**
         * 6     1 0 0 1    1    2    Delta list.
         * 7     1 1 1 2    2    3    Add 1 to ensure non zeros.
         * 111   1 1 1 01   01   11   Binary representation.
         * 3-11  1 1 1 2-0  2-0  2-1  Decimal Gamma Prefix and Binary Gamma Suffix.
         * 11-11 1 1 1 01-0 01-0 01-1 Binary Gamma Prefix and Binary Gamma Suffix.
         * 01111 1 1 1 0100 0100 0101 Delta Encoding.
         */
        final int expectedOffset = 20;
        LongOutputOffset outputOffset = new LongOutputOffset();

        final int offset = outputOffset.getWriteDeltaOffset(list, valueBitSize);

        assertEquals(expectedOffset, offset);
    }

    @Test
    public void whenListIsEmptyItShouldGetExpectedOffsetSuccessfully() {
        final int valueBitSize = 1;
        final LongList list = new LongArrayList(new long[] {});
        final int expectedOffset = 1;
        LongOutputOffset outputOffset = new LongOutputOffset();

        final int offset = outputOffset.getWriteDeltaOffset(list, valueBitSize);

        assertEquals(expectedOffset, offset);
    }

}
