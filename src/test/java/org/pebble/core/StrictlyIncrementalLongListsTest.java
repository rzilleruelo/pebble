package org.pebble.core;

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
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongList;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.pebble.FastIntegrationTest;
import org.pebble.core.decoding.PebbleBytesStore;
import org.pebble.core.decoding.iterators.Helper.Input;
import org.pebble.core.decoding.iterators.longs.StrictlyIncrementalListIterator;
import org.pebble.core.encoding.Helper;
import org.pebble.core.encoding.Helper.Output;
import org.pebble.core.encoding.OutputSuccinctStream;
import org.pebble.core.encoding.longs.datastructures.InvertedListLongReferenceListsIndex;
import org.pebble.core.encoding.longs.datastructures.LongReferenceListsIndex;
import org.pebble.core.encoding.longs.datastructures.LongReferenceListsStore;
import org.pebble.utils.decoding.BytesArrayPebbleBytesStore;

import java.io.IOException;

import static junit.framework.TestCase.assertEquals;
import static org.pebble.core.decoding.iterators.Helper.getInput;
import static org.pebble.core.encoding.Helper.getOutput;
import static org.pebble.core.encoding.Helper.toBinaryString;

@Category(FastIntegrationTest.class)
public class StrictlyIncrementalLongListsTest {

    @Test
    public void itShouldCompressLists() throws IOException {
        final int storeSize = 3;
        final int maxRecursiveReferences = 1;
        final int minListSize = 3;
        final int valueBitSize = 5;
        final Output out = getOutput();
        final LongReferenceListsIndex referenceListsIndex = new InvertedListLongReferenceListsIndex();
        final LongReferenceListsStore referenceListsStore = new LongReferenceListsStore(
            storeSize,
            maxRecursiveReferences,
            minListSize,
            referenceListsIndex
        );
        final OutputSuccinctStream outputSuccinctStream = new OutputSuccinctStream(out.buffer);
        final LongList[] lists = new LongList[] {
            new LongArrayList(new long[] {5L, 8L, 12L, 13L}),
            new LongArrayList(new long[] {1L, 2L, 3L, 5L, 8L, 12L, 13L, 14L}),
            new LongArrayList(new long[] {5L, 8L, 12L, 13L}),
            new LongArrayList(new long[] {1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L, 14L}),
            new LongArrayList(new long[] {5L, 8L, 12L, 13L}),
            new LongArrayList(new long[] {5L, 8L, 12L, 13L})
        };
        /**
         * list=[5, 8, 12, 13]
         * reference=[0], intervals=[0], delta=[4, 5, 2, 3, 0]
         * 1              1              101   00101 11   100 1
         * 1              1              3-01  00101 2-1  3-00  1
         * 1              1              11-01 00101 01-1 11-00 1
         * 1              1              01101 00101 0101 01100 1
         * list=[1, 2, 3, 5, 8, 12, 13, 14]
         * reference=[1, 0, 1], intervals=[0], delta=[4, 1, 0, 0, 10]
         * 10   1 1             1              101   1     1 1 1011
         * 2-0  1 1             1              3-01  00001 1 1 4-011
         * 01-0 1 1             1              11-01 00001 1 1 100-011
         * 0100 1 1             1              01101 00001 1 1 00100011
         * list=[5, 8, 12, 13]
         * reference=[2, 0, 1], intervals=[0], delta=[0]
         * 11   1 1             1              1
         * 2-1  1 1             1              1
         * 10-1 1 1             1              1
         * 0101 1 1             1              1
         * list=[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14]
         * reference=[0], intervals=[1, 1, 10],   delta=[0]
         * 1              10   00001 1011         1
         * 1              2-0  00001 4-011        1
         * 1              10-0 00001 100-011      1
         * 1              0100 00001 00100011     1
         * list=[5, 8, 12, 13]
         * reference=[2, 0, 1], intervals=[0],   delta=[0]
         * 11  1 1              1                1
         * 2-1  1 1             1                1
         * 10-1 1 1             1                1
         * 0101 1 1             1                1
         * list=[5, 8, 12, 13]
         * reference=[4, 2, 0, 2, 3], intervals=[0],   delta=[0]
         * 101   11   0 11   100      1                1
         * 3-01  2-1  0 2-1  3-00     1                1
         * 11-01 10-1 0 10-1 11-00    1                1
         * 01101 0101 0 0101 01100    1                1
         */
        final String expectedOutput = (
            "1 1 01101 00101 0101 01100 1" +
            "0100 1 1 1 01101 00001 1 1 00100011" +
            "0101 1 1 1 1" +
            "1 0100 00001 001000111" +
            "0101 1 1 1 1" +
            "01101 0101 0 0101 01100 1 1"
        ).replace(" ", "");
        final int expectedTotalOffset = 105;

        int totalOffset = 0;
        for(int i = 0; i < lists.length; i++) {
            totalOffset += outputSuccinctStream.writeStrictlyIncrementalList(lists[i], i, valueBitSize, referenceListsStore);
        }

        outputSuccinctStream.flush();
        out.close();
        assertEquals(expectedOutput, toBinaryString(out.buffer, totalOffset));
        assertEquals(expectedTotalOffset, totalOffset);
    }

    @Test
    public void itShouldDecompressLists() throws IOException {
        final Input input = getInput(
            "1 1 01101 00101 0101 01100 1" +
            "0100 1 1 1 01101 00001 1 1 00100011" +
            "0101 1 1 1 1" +
            "1 0100 00001 001000111" +
            "0101 1 1 1 1" +
            "01101 0101 0 0101 01100 1 1"
        );
        final long[] offsets = new long[] {0L, 22L, 49L, 57L, 76L, 84L};
        final PebbleBytesStore bytesStore = new BytesArrayPebbleBytesStore(input.buffer, offsets);
        final int valueBitSize = 5;
        final LongList[] expectedLists = new LongList[] {
            new LongArrayList(new long[] {5L, 8L, 12L, 13L}),
            new LongArrayList(new long[] {1L, 2L, 3L, 5L, 8L, 12L, 13L, 14L}),
            new LongArrayList(new long[] {5L, 8L, 12L, 13L}),
            new LongArrayList(new long[] {1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L, 14L}),
            new LongArrayList(new long[] {5L, 8L, 12L, 13L}),
            new LongArrayList(new long[] {5L, 8L, 12L, 13L})
        };
        final LongList[] lists = new LongList[expectedLists.length];
        LongList list;
        LongIterator iterator;

        for(int i = 0; i < lists.length; i++) {
            iterator = StrictlyIncrementalListIterator.build(i, valueBitSize, bytesStore);
            lists[i] = list = new LongArrayList();
            while(iterator.hasNext()) {
                list.add(iterator.nextLong());
            }
        }

        assertEquals(
            Helper.<Long, LongList>translateToUtilsCollection(expectedLists),
            Helper.<Long, LongList>translateToUtilsCollection(lists)
        );
    }
}
