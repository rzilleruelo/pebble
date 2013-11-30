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
import org.pebble.core.exceptions.NotStrictlyIncrementalListException;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;

import static org.pebble.core.encoding.Helper.getOutput;
import static org.pebble.core.encoding.Helper.toBinaryString;
import static junit.framework.TestCase.assertEquals;

@Category(UnitTest.class)
public class OutputSuccinctStreamWriteDeltaTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void itShouldWriteDeltaSuccinctRepresentationSuccessfully() throws Exception {
        final int valueBitSize = 1;
        final IntList list = new IntArrayList(new int[] {1, 2, 3, 5, 7, 10});
        /**
         * 6     1 0 0 1    1    2    Delta list.
         * 7     1 1 1 2    2    3    Add 1 to ensure non zeros.
         * 111   1 1 1 01   01   11   Binary representation.
         * 3-11  1 1 1 2-0  2-0  2-1  Decimal Gamma Prefix and Binary Gamma Suffix.
         * 11-11 1 1 1 01-0 01-0 01-1 Binary Gamma Prefix and Binary Gamma Suffix.
         * 01111 1 1 1 0100 0100 0101 Delta Encoding.
         */
        final String expectedOutput = "01111 1 1 1 0100 0100 0101".replace(" ", "");
        final int expectedOffset = 20;
        final Helper.Output out = getOutput();

        final int offset = out.stream.writeDelta(list, valueBitSize);
        out.close();

        assertEquals(expectedOutput, toBinaryString(out.buffer, offset));
        assertEquals(expectedOffset, offset);
    }

    @Test
    public void whenListIsEmptyItShouldWriteDeltaSuccinctRepresentationSuccessfully() throws Exception {
        final int valueBitSize = 1;
        final IntList list = new IntArrayList(new int[] {});
        final String expectedOutput = "1".replace(" ", "");
        final int expectedOffset = 1;
        final Helper.Output out = getOutput();

        final int offset = out.stream.writeDelta(list, valueBitSize);
        out.close();

        assertEquals(expectedOutput, toBinaryString(out.buffer, offset));
        assertEquals(expectedOffset, offset);
    }

    @Test
    public void itShouldThrowAnExceptionWhenListIsNonStrictlyIncremental() throws Exception {
        final int valueBitSize = 1;
        final IntList list = new IntArrayList(new int[] {1, 2, 3, 5, 0, 7, 10, 11, 16, 19});
        final Helper.Output out = getOutput();
        expectedException.expect(NotStrictlyIncrementalListException.class);
        expectedException.expectMessage("List is not strictly incremental, found 0 after 5");

        out.stream.writeDelta(list, valueBitSize);
    }

}
