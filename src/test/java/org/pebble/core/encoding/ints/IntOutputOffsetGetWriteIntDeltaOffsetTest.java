package org.pebble.core.encoding.ints;

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
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static junit.framework.TestCase.assertEquals;

@Category(UnitTest.class)
public class IntOutputOffsetGetWriteIntDeltaOffsetTest {

    @Test
    public void whenOffsetIsPreCalculatedItShouldReturnExpectedOffsetSuccessfully() {
        final int value = 4094;
        /**
         * 4094               Decimal representation.
         * 4095               Add 1 to ensure non zeros.
         * 111111111111       Binary representation.
         * 12-11111111111     Decimal Gamma Prefix and Binary Gamma Suffix.
         * 0011-11111111111   Binary Gamma Prefix and Binary Gamma Suffix.
         * 000110011111111111 Delta Encoding.
         */
        final int expectedOffset = 18;

        final int offset = IntOutputOffset.getWriteDeltaOffset(value);

        assertEquals(expectedOffset, offset);
    }

    @Test
    public void whenOffsetIsNotPreCalculatedItShouldReturnExpectedOffsetSuccessfully() {
        final int value = 4095;
        /**
         * 4095                Decimal representation.
         * 4096                Add 1 to ensure non zeros.
         * 0000000000001       Binary representation.
         * 13-000000000000     Decimal Gamma Prefix and Binary Gamma Suffix.
         * 1101-000000000000   Binary Gamma Prefix and Binary Gamma Suffix.
         * 0001110000000000000 Delta Encoding.
         */
        final int expectedOffset = 19;

        final int offset = IntOutputOffset.getWriteDeltaOffset(value);

        assertEquals(expectedOffset, offset);
    }

}
