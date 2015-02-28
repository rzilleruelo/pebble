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
import org.pebble.core.decoding.iterators.longs.ListIterator;
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
public class LongListsTest {

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
            new LongArrayList(new long[] {12L, 8L, 5L, 12L, 13L, 5L, 13L, 8L}),
            new LongArrayList(new long[] {13L, 13L, 5L, 8L, 5L, 8L, 12L, 13L, 12L, 12L})
        };
        /**
         * list=[12, 8, 5, 12, 13, 5, 13, 8, 5, 12, 8]
         * values=[5, 8, 12, 13] indexes=[2, 1, 0, 2, 3, 0, 3, 1, 0, 2, 1]
         * reference=[0], intervals=[0], delta=[4, 5, 2, 3, 0],   indexes=[4, 4, 1, 1, 4, 2, 5, 6, 3]
         * 1              1              101   00101 11   100   1 101   101   10   10   101   11   110   111   100
         * 1              1              3-01  00101 2-1  3-00  1 3-01  3-01  2-0  2-0  3-01  2-1  3-10  3-11  3-00
         * 1              1              11-01 00101 01-1 11-00 1 11-01 11-01 10-0 10-0 11-01 10-1 11-10 11-11 11-00
         * 1              1              01101 00101 0101 01100 1 01101 01101 0100 0100 01101 0101 01110 01111 01100
         * list=[13, 13, 5, 8, 5, 8, 12, 13, 12, 12]
         * values=[5, 8, 12, 13] indexes=[3, 3, 0, 1, 0, 1, 2, 3, 2, 2]
         * reference=[1, 0, 1], intervals=[0], delta=[0], indexes=[6, 6, 0, 5, 2, 1, 2, 2, 2, 1, 0]
         * 10    1 1            1              1          111   111   1 110   11   10   11   11   11   10   1
         * 2-0   1 1            1              1          3-11  3-11  1 3-10  2-1  2-0  2-1  2-1  2-1  2-0  1
         * 10-0  1 1            1              1          11-11 11-11 1 11-10 10-1 10-0 10-1 10-1 10-1 10-0 1
         * 0100  1 1            1              1          01111 01111 1 01110 0101 0100 0101 0101 0101 0100 1
         */
        final String expectedOutput = (
            "1 1 01101 00101 0101 01100 1 01101 01101 0100 0100 01101 0101 01110 01111 01100" +
            "0100 1 1 1 1 01111 01111 1 01110 0101 0100 0101 0101 0101 0100 1"
        ).replace(" ", "");
        final int expectedTotalOffset = 113;

        int totalOffset = 0;
        for(int i = 0; i < lists.length; i++) {
            totalOffset += outputSuccinctStream.writeList(lists[i], i, valueBitSize, referenceListsStore);
        }

        outputSuccinctStream.flush();
        out.close();
        assertEquals(expectedOutput, toBinaryString(out.buffer, totalOffset));
        assertEquals(expectedTotalOffset, totalOffset);
    }

    @Test
    public void itShouldDecompressLists() throws IOException {
        final Input input = getInput(
            "1 1 01101 00101 0101 01100 1 01101 01101 0100 0100 01101 0101 01110 01111 01100" +
            "0100 1 1 1 1 01111 01111 1 01110 0101 0100 0101 0101 0101 0100 1"
        );
        final long[] offsets = new long[] {0L, 64L};
        final PebbleBytesStore bytesStore = new BytesArrayPebbleBytesStore(input.buffer, offsets);
        final int valueBitSize = 5;
        final LongList[] expectedLists = new LongList[] {
            new LongArrayList(new long[] {12L, 8L, 5L, 12L, 13L, 5L, 13L, 8L}),
            new LongArrayList(new long[] {13L, 13L, 5L, 8L, 5L, 8L, 12L, 13L, 12L, 12L})
        };
        final LongList[] lists = new LongList[expectedLists.length];
        LongList list;
        LongIterator iterator;

        for(int i = 0; i < lists.length; i++) {
            iterator = ListIterator.build(i, valueBitSize, bytesStore);
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
