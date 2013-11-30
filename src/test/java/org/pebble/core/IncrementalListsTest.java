package org.pebble.core;

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

import org.pebble.FastIntegrationTest;
import org.pebble.core.decoding.PebbleBytesStore;
import org.pebble.core.encoding.OutputSuccinctStream;
import org.pebble.core.encoding.small.datastructures.IntReferenceListsIndex;
import org.pebble.core.encoding.small.datastructures.IntReferenceListsStore;
import org.pebble.core.encoding.small.datastructures.InvertedListIntReferenceListsIndex;
import org.pebble.core.encoding.Helper;
import org.pebble.core.decoding.iterators.small.Helper.Input;
import org.pebble.core.decoding.iterators.small.IncrementalListIterator;
import org.pebble.utils.decoding.BytesArrayPebbleBytesStore;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;

import static org.pebble.core.encoding.small.datastructures.Helper.translateToUtilsCollection;
import static org.pebble.core.encoding.Helper.getOutput;
import static org.pebble.core.encoding.Helper.toBinaryString;
import static org.pebble.core.decoding.iterators.small.Helper.getInput;
import static junit.framework.TestCase.assertEquals;

@Category(FastIntegrationTest.class)
public class IncrementalListsTest {

    @Test
    public void itShouldCompressLists() throws IOException {
        final int storeSize = 3;
        final int maxRecursiveReferences = 1;
        final int minListSize = 3;
        final int valueBitSize = 5;
        final Helper.Output out = getOutput();
        final IntReferenceListsIndex referenceListsIndex = new InvertedListIntReferenceListsIndex();
        final IntReferenceListsStore referenceListsStore = new IntReferenceListsStore(
            storeSize,
            maxRecursiveReferences,
            minListSize,
            referenceListsIndex
        );
        final OutputSuccinctStream outputSuccinctStream = new OutputSuccinctStream(out.buffer);
        final IntList[] lists = new IntList[] {
            new IntArrayList(new int[] {5, 8, 12, 13}),
            new IntArrayList(new int[] {5, 5, 5, 8, 12, 12, 12, 12, 12, 13})
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
        final long[] offsets = new long[] {0l, 23l};
        final PebbleBytesStore bytesStore = new BytesArrayPebbleBytesStore(input.buffer, offsets);
        final int valueBitSize = 5;
        final IntList[] expectedLists = new IntList[] {
            new IntArrayList(new int[] {5, 8, 12, 13}),
            new IntArrayList(new int[] {5, 5, 5, 8, 12, 12, 12, 12, 12, 13})
        };
        final IntList[] lists = new IntList[expectedLists.length];
        IntList list;
        IntIterator iterator;

        for(int i = 0; i < lists.length; i++) {
            iterator = IncrementalListIterator.build(i, valueBitSize, bytesStore);
            lists[i] = list = new IntArrayList();
            while(iterator.hasNext()) {
                list.add(iterator.nextInt());
            }
        }

        assertEquals(translateToUtilsCollection(expectedLists), translateToUtilsCollection(lists));
    }

}
