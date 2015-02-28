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
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;
import org.pebble.UnitTest;
import org.pebble.core.exceptions.NotStrictlyIncrementalListException;

import static junit.framework.TestCase.assertEquals;
import static org.pebble.core.encoding.Helper.getOutput;
import static org.pebble.core.encoding.Helper.toBinaryString;

@Category(UnitTest.class)
public class LongsOutputSuccinctStreamWriteDifferenceTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void whenReferenceListHasMatchesItShouldWriteItsSuccinctRepresentationSuccessfully() throws Exception {
        final LongList list = new LongArrayList(new long[] {1L, 2L, 3L, 5L, 6L, 7L, 10L, 11L, 16L, 19L});
        final LongList referenceList = new LongArrayList(new long[] {0L, 2L, 3L, 5L, 9L, 12L, 13L});
        /**
         * {   1, 2, 3, 5, 6, 7,    10, 11,        16, 19} List.
         * {0,    2, 3, 5,       9,         12, 13      }  Reference list.
         *  0    1  1  1         0          0   0          Matches.
         *  3    0 1 3 3                                   Matches blocks representation.
         *  2    0 0 2                                     Matches blocks concise representation.
         *  3    0 1 3                                     Add 1 to ensure non zeros.
         *  11   0 1 11                                    Binary representation.
         *  2-1  0 1 2-1                                   Decimal Gamma Prefix and Binary Gamma Suffix.
         *  01-1 0 1 01-1                                  Binary Gamma Prefix and Binary Gamma Suffix.
         *  0101 0 1 0101                                  Delta Encoding.
         */
        final String expectedOutput = "0101 0 1 0101".replace(" ", "");
        final int expectedOffset = 10;
        final Helper.Output out = getOutput();

        final int offset = out.stream.writeDifference(list, referenceList);
        out.close();

        assertEquals(expectedOutput, toBinaryString(out.buffer, offset));
        assertEquals(expectedOffset, offset);
    }

    @Test
    public void whenReferenceStartWithMatchesItShouldWriteItsSuccinctRepresentationSuccessfully() throws Exception {
        final LongList list = new LongArrayList(new long[] {1L, 2L, 3L, 5L, 6L, 7L, 10L, 11L, 16L, 19L});
        final LongList referenceList = new LongArrayList(new long[] {1L, 2L, 3L, 5L, 9L, 12L, 13L});
        /**
         * {1, 2, 3, 5, 6, 7,    10, 11,         16, 19} List.
         * {1, 2, 3, 5,       9,         12, 13        } Reference list.
         *  1  1  1  1        0          0   0           Matches.
         *  2    1 4 3                                   Matches blocks representation.
         *  1    1 3                                     Matches blocks concise representation.
         *  2    1 4                                     Add 1 to ensure non zeros.
         *  01   1 001                                   Binary representation.
         *  2-0  1 3-00                                  Decimal Gamma Prefix and Binary Gamma Suffix.
         *  01-0 1 11-00                                 Binary Gamma Prefix and Binary Gamma Suffix.
         *  0100 1 01100                                 Delta Encoding.
         */
        final String expectedOutput = "0100 1 01100".replace(" ", "");
        final int expectedOffset = 10;
        final Helper.Output out = getOutput();

        final int offset = out.stream.writeDifference(list, referenceList);
        out.close();

        assertEquals(expectedOutput, toBinaryString(out.buffer, offset));
        assertEquals(expectedOffset, offset);
    }

    @Test
    public void whenReferenceListHasMatchesAtTheEndOfListItShouldWriteItsSuccinctRepresentationSuccessfully()
        throws Exception
    {
        final LongList list = new LongArrayList(new long[] {1L, 2L, 3L, 5L, 6L, 7L, 10L, 11L, 12L, 13L});
        final LongList referenceList = new LongArrayList(new long[] {0L, 2L, 3L, 5L, 9L, 12L, 13L, 14L});
        /**
         * {   1, 2, 3, 5, 6, 7,    10, 11, 12, 13    } List.
         * {0,    2, 3, 5,       9,         12, 13, 14} Reference list.
         *  0     1  1  1        0          1   1   0   Matches.
         *  5     0 1 3    1 2 1                        Matches blocks representation.
         *  4     0 0 2    0 1                          Matches blocks concise representation.
         *  5     0 1 3    1 2                          Add 1 to ensure non zeros.
         *  101   0 1 11   1 01                         Binary representation.
         *  3-01  0 1 2-1  1 2-0                        Decimal Gamma Prefix and Binary Gamma Suffix.
         *  11-01 0 1 01-1 1 01-0                       Binary Gamma Prefix and Binary Gamma Suffix.
         *  01101 0 1 0101 1 0100                       Delta Encoding.
         */
        final String expectedOutput = "01101 0 1 0101 1 0100".replace(" ", "");
        final int expectedOffset = 16;
        final Helper.Output out = getOutput();

        final int offset = out.stream.writeDifference(list, referenceList);
        out.close();

        assertEquals(expectedOutput, toBinaryString(out.buffer, offset));
        assertEquals(expectedOffset, offset);
    }

    @Test
    public void whenReferenceListHasMatchesAtTheEndOfReferenceListItShouldWriteItsSuccinctRepresentationSuccessfully()
        throws Exception
    {
        final LongList list = new LongArrayList(new long[] {1L, 2L, 3L, 5L, 6L, 7L, 10L, 11L, 12L, 13L, 14L});
        final LongList referenceList = new LongArrayList(new long[] {0L, 2L, 3L, 5L, 9L, 12L, 13L});
        /**
         * {   1, 2, 3, 5, 6, 7,    10, 11, 12, 13  14} List.
         * {0,    2, 3, 5,       9,         12, 13,   } Reference list.
         *  0     1  1  1        0          1   1       Matches.
         *  4     0 1 3    1 2                          Matches blocks representation.
         *  3     0 0 2    0                            Matches blocks concise representation.
         *  4     0 1 3    1                            Add 1 to ensure non zeros.
         *  001   0 1 11   1                            Binary representation.
         *  3-00  0 1 2-1  1                            Decimal Gamma Prefix and Binary Gamma Suffix.
         *  11-00 0 1 01-1 1                            Binary Gamma Prefix and Binary Gamma Suffix.
         *  01100 0 1 0101 1                            Delta Encoding.
         */
        final String expectedOutput = "01100 0 1 0101 1".replace(" ", "");
        final int expectedOffset = 12;
        final Helper.Output out = getOutput();

        final int offset = out.stream.writeDifference(list, referenceList);
        out.close();

        assertEquals(expectedOutput, toBinaryString(out.buffer, offset));
        assertEquals(expectedOffset, offset);
    }

    @Test
    public void whenReferenceListHasMatchesAndNoMatchAtEndItShouldWriteItsSuccinctRepresentationSuccessfully()
        throws Exception
    {
        final LongList list = new LongArrayList(new long[] {1L, 2L, 3L, 5L, 6L, 7L, 10L, 11L});
        final LongList referenceList = new LongArrayList(new long[] {0L, 2L, 3L, 5L, 9L, 12L, 13L});
        /**
         * {   1, 2, 3, 5, 6, 7,    10, 11,       } List.
         * {0,    2, 3, 5,       9,         12, 13} Reference list.
         *  0     1  1  1        0          0   0   Matches.
         *  3    0 1 3 3                            Matches blocks representation.
         *  2    0 0 2                              Matches blocks concise representation.
         *  3    0 1 3                              Add 1 to ensure non zeros.
         *  11   0 1 11                             Binary representation.
         *  2-1  0 1 2-1                            Decimal Gamma Prefix and Binary Gamma Suffix.
         *  01-1 0 1 01-                            Binary Gamma Prefix and Binary Gamma Suffix.
         *  0101 0 1 0101                           Delta Encoding.
         */
        final String expectedOutput = "0101 0 1 0101".replace(" ", "");
        final int expectedOffset = 10;
        final Helper.Output out = getOutput();

        final int offset = out.stream.writeDifference(list, referenceList);
        out.close();

        assertEquals(expectedOutput, toBinaryString(out.buffer, offset));
        assertEquals(expectedOffset, offset);
    }

    @Test
    public void whenReferenceListHasMatchesAndListGetsIteratedFirstItShouldWriteItsSuccinctRepresentationSuccessfully()
        throws Exception
    {
        final LongList list = new LongArrayList(new long[] {1L, 2L, 3L, 5L, 6L, 7L, 10L, 11L, 12L});
        final LongList referenceList = new LongArrayList(new long[] {0L, 2L, 3L, 5L, 9L, 11L, 13L});
        /**
         * {   1, 2, 3, 5, 6, 7,    10, 11, 12   } List.
         * {0,    2, 3, 5,       9,     11,    13} Reference list.
         *  0     1  1  1        0      1      0   Matches.
         *  5     0 1 3    1 11                    Matches blocks representation.
         *  4     0 0 2    0 0                     Matches blocks concise representation.
         *  5     0 1 3    1 1                     Add 1 to ensure non zeros.
         *  101   0 1 11   1 1                     Binary representation.
         *  3-01  0 1 2-1  1 1                     Decimal Gamma Prefix and Binary Gamma Suffix.
         *  11-01 0 1 01-1 1 1                     Binary Gamma Prefix and Binary Gamma Suffix.
         *  01101 0 1 0101 1 1                     Delta Encoding.
         */
        final String expectedOutput = "01101 0 1 0101 1 1".replace(" ", "");
        final int expectedOffset = 13;
        final Helper.Output out = getOutput();

        final int offset = out.stream.writeDifference(list, referenceList);
        out.close();

        assertEquals(expectedOutput, toBinaryString(out.buffer, offset));
        assertEquals(expectedOffset, offset);
    }

    @Test
    public void whenReferenceListHasMatchesAndStartWithBiggerNumberThanListItShouldWriteItsSuccinctRepresentationSuccessfully()
        throws Exception
    {
        final LongList list = new LongArrayList(new long[] {0L, 2L, 3L, 5L, 6L, 7L, 10L, 11L, 16L, 19L});
        final LongList referenceList = new LongArrayList(new long[] {1L, 2L, 3L, 5L, 9L, 12L, 13L});
        /**
         * {0,    2, 3, 5, 6, 7,    10, 11,        16, 19} List.
         * {   1, 2, 3, 5,       9,         12, 13       } Reference list.
         *     0  1  1  1        0          0   0          Matches.
         *  3    0 1 3 3                                   Matches blocks representation.
         *  2    0 0 2                                     Matches blocks concise representation.
         *  3    0 1 3                                     Add 1 to ensure non zeros.
         *  11   0 1 11                                    Binary representation.
         *  2-1  0 1 2-1                                   Decimal Gamma Prefix and Binary Gamma Suffix.
         *  01-1 0 1 01-1                                  Binary Gamma Prefix and Binary Gamma Suffix.
         *  0101 0 1 0101                                  Delta Encoding.
         */
        final String expectedOutput = "0101 0 1 0101".replace(" ", "");
        final int expectedOffset = 10;
        final Helper.Output out = getOutput();

        final int offset = out.stream.writeDifference(list, referenceList);
        out.close();

        assertEquals(expectedOutput, toBinaryString(out.buffer, offset));
        assertEquals(expectedOffset, offset);
    }

    @Test
    public void whenReferenceListFullyMatchesItShouldWriteItsSuccinctRepresentationSuccessfully() throws Exception {
        final LongList list = new LongArrayList(new long[] {1L, 2L, 3L, 5L, 6L, 7L, 10L, 11L, 16L, 19L});
        final LongList referenceList = new LongArrayList(new long[] {1L, 2L, 3L, 7L, 11L, 19L});
        /**
         * {1, 2, 3, 5, 6, 7, 10, 11, 16, 19} List.
         * {1, 2, 3,       7,     11,     19} Reference list.
         *  1  1  1        1      1       1   Matches.
         *  1 1 6                             Matches blocks representation.
         *  0 1                               Matches blocks concise representation.
         *  1 1                               Add 1 to ensure non zeros.
         *  1 1                               Binary representation.
         *  1 1                               Decimal Gamma Prefix and Binary Gamma Suffix.
         *  1 1                               Binary Gamma Prefix and Binary Gamma Suffix.
         *  1 1                               Delta Encoding.
         */
        final String expectedOutput = "1 1".replace(" ", "");
        final int expectedOffset = 2;
        final Helper.Output out = getOutput();

        final int offset = out.stream.writeDifference(list, referenceList);
        out.close();

        assertEquals(expectedOutput, toBinaryString(out.buffer, offset));
        assertEquals(expectedOffset, offset);
    }

    @Test
    public void itShouldThrowAnExceptionWhenNonStrictlyIncrementalListOnMatch() throws Exception {
        final LongList list = new LongArrayList(new long[] {1L, 2L, 3L, 5L, 0L, 7L, 10L, 11L, 16L, 19L});
        final LongList referenceList = new LongArrayList(new long[] {0L, 2L, 3L, 5L, 9L, 12L, 13L});
        final Helper.Output out = getOutput();
        expectedException.expect(NotStrictlyIncrementalListException.class);
        expectedException.expectMessage("List is not strictly incremental, found 0 after 5");

        out.stream.writeDifference(list, referenceList);
    }

    @Test
    public void itShouldThrowAnExceptionWhenNonStrictlyIncrementalListOnNoMatch() throws Exception {
        final LongList list = new LongArrayList(new long[] {1L, 2L, 3L, 6L, 5L, 7L, 10L, 11L, 16L, 19L});
        final LongList referenceList = new LongArrayList(new long[] {0L, 2L, 3L, 5L, 9L, 12L, 13L});
        final Helper.Output out = getOutput();
        expectedException.expect(NotStrictlyIncrementalListException.class);
        expectedException.expectMessage("List is not strictly incremental, found 5 after 6");

        out.stream.writeDifference(list, referenceList);
    }

}
