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
import org.pebble.core.decoding.iterators.longs.IncrementalListIterator;
import org.pebble.core.encoding.Helper;
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
public class IncrementalLongListsTest {

    @Test
    public void itShouldCompressLists() throws IOException {
        final int storeSize = 3;
        final int maxRecursiveReferences = 1;
        final int minListSize = 3;
        final int valueBitSize = 5;
        final Helper.Output out = getOutput();
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
            new LongArrayList(new long[] {5L, 5L, 5L, 8L, 12L, 12L, 12L, 12L, 12L, 13L})
        };
        /**
         * list=[5, 8, 12, 13]
         * repetitions=[0] reference=[0], intervals=[0], delta=[4, 5, 2, 3, 0]
         * 1               1              1              101   00101 11   100 1
         * 1               1              1              3-01  00101 2-1  3-00  1
         * 1               1              1              11-01 00101 01-1 11-00 1
         * 1               1              1              01101 00101 0101 01100 1
         * list=[5, 5, 5, 8, 12, 12, 12, 12, 12, 13]
         * repetitions=[2, 0, 1, 1, 3] reference=[1, 0, 1], intervals=[0], delta=[0]
         * 11   1 10   10   100        10    1 1            1              1
         * 2-1  1 2-0  2-0  3-00       2-0   1 1            1              1
         * 10-1 1 10-0 10-0 11-00      10-0  1 1            1              1
         * 0101 1 0100 0100 01100      0100  1 1            1              1
         */
        final String expectedOutput = (
            "1 1 1 01101 00101 0101 01100 1" +
            "0101 1 0100 0100 01100 0100 1 1 1 1"
        ).replace(" ", "");
        final int expectedTotalOffset = 49;

        int totalOffset = 0;
        for(int i = 0; i < lists.length; i++) {
            totalOffset += outputSuccinctStream.writeIncrementalList(lists[i], i, valueBitSize, referenceListsStore);
        }

        outputSuccinctStream.flush();
        out.close();
        assertEquals(expectedOutput, toBinaryString(out.buffer, totalOffset));
        assertEquals(expectedTotalOffset, totalOffset);
    }

    @Test
    public void itShouldDecompressLists() throws IOException {
        final Input input = getInput(
            "1 1 1 01101 00101 0101 01100 1" +
            "0101 1 0100 0100 01100 0100 1 1 1 1"
        );
        final long[] offsets = new long[] {0L, 23L};
        final PebbleBytesStore bytesStore = new BytesArrayPebbleBytesStore(input.buffer, offsets);
        final int valueBitSize = 5;
        final LongList[] expectedLists = new LongList[] {
            new LongArrayList(new long[] {5L, 8L, 12L, 13L}),
            new LongArrayList(new long[] {5L, 5L, 5L, 8L, 12L, 12L, 12L, 12L, 12L, 13L})
        };
        final LongList[] lists = new LongList[expectedLists.length];
        LongList list;
        LongIterator iterator;

        for(int i = 0; i < lists.length; i++) {
            iterator = IncrementalListIterator.build(i, valueBitSize, bytesStore);
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
