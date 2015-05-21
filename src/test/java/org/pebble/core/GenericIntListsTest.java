package org.pebble.core;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.pebble.FastIntegrationTest;
import org.pebble.core.decoding.iterators.Helper.Input;
import org.pebble.core.decoding.iterators.ints.GenericListIterator;
import org.pebble.core.encoding.Helper;
import org.pebble.core.encoding.OutputSuccinctStream;
import org.pebble.core.encoding.ints.datastructures.IntReferenceListsIndex;
import org.pebble.core.encoding.ints.datastructures.IntReferenceListsStore;
import org.pebble.core.encoding.ints.datastructures.InvertedListIntReferenceListsIndex;
import org.pebble.utils.BytesArrayPebbleBytesStore;
import org.pebble.utils.LongListPebbleOffsetsStore;

import java.io.IOException;

import static junit.framework.TestCase.assertEquals;
import static org.pebble.core.decoding.iterators.Helper.getInput;
import static org.pebble.core.encoding.Helper.getOutput;
import static org.pebble.core.encoding.Helper.toBinaryString;

@Category(FastIntegrationTest.class)
public class GenericIntListsTest {

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
            new IntArrayList(new int[] {5, 8, 12, 13, 14, 15}),
            new IntArrayList(new int[] {5, 5, 8, 12, 12, 12, 13, 14, 14, 14, 15, 15}),
            new IntArrayList(new int[] {5, 8, 12, 13, 14, 12, 14, 5, 13, 14, 12, 15, 15})
        };
        /**
         * type="sorted set" list=[5, 8, 12, 13, 14, 15]
         * type=[0] reference=[0], intervals=[1, 12, 0], delta=[2, 5, 2]
         * 00       1              10   01100 1          11   00101 11
         * 00       1              2-0  01100 1          2-1  00101 2-1
         * 00       1              10-0 01100 1          10-1 00101 10-1
         * 00       1              0100 01100 1          0101 00101 0101
         * type="sorted list" list=[5, 5, 8, 12, 12, 12, 14, 14, 14, 15, 15]
         * type=[1] repetitions=[4, 0, 0, 1, 1, 1, 1, 0, 0] reference=[1, 0, 1], intervals=[0], delta=[0]
         * 01       101   1 1 10   10   10   10   1 1          10   1 1          1              1
         * 01       3-01  1 1 2-0  2-0  2-0  2-0  1 1          2-0  1 1          1              1
         * 01       11-01 1 1 10-0 10-0 10-0 10-0 1 1          10-0 1 1          1              1
         * 01       01101 1 1 0100 0100 0100 0100 1 1          0100 1 1          1              1
         * type="unsorted list" list=[5, 8, 12, 13, 14, 12, 14, 5, 13, 14, 12, 15, 15]
         * type=[2] values=[5, 8, 12, 13, 14, 15] indexes=[7, 0, 1, 2, 3, 4, 2, 4, 0, 3, 4, 2, 5, 5]
         *                                                                                     x
         * type=[2] reference=[1, 0, 1], intervals=[0], delta=[0], indexes=[5, 0, 2, 2, 2, 2, 3, 4, 7, 6, 2, 3, 6, 0]
         * 10       10   1 1             1              1          1000     1 11   11   11   11   100   101   1000     111   11   100   111   1
         * 10       2-0  1 1             1              1          4-000    1 2-1  2-1  2-1  2-1  3-00  3-01  4-000    3-11  2-1  3-00  3-11  1
         * 10       10-0 1 1             1              1          100-000  1 10-1 10-1 10-1 10-1 11-00 11-01 100-000  11-11 10-1 11-00 11-11 1
         * 10       0100 1 1             1              1          00100000 1 0101 0101 0101 0101 01100 01101 00100000 01111 0101 01100 01111 1
         */
        final String expectedOutput = (
            "00 1 0100 01100 1 0101 00101 0101" +
            "01 01101 1 1 0100 0100 0100 0100 1 1 0100 1 1 1 1" +
            "10 0100 1 1 1 1 00100000 1 0101 0101 0101 0101 01100 01101 00100000 01111 0101 01100 01111 1"
        ).replace(" ", "");;
        final int expectedTotalOffset = 134;

        int totalOffset = 0;
        for(int i = 0; i < lists.length; i++) {
            totalOffset += outputSuccinctStream.writeGenericList(lists[i], i, valueBitSize, referenceListsStore);
        }

        outputSuccinctStream.flush();
        out.close();
        assertEquals(expectedOutput, toBinaryString(out.buffer, totalOffset));
        assertEquals(expectedTotalOffset, totalOffset);
    }

    @Test
    public void itShouldDecompressLists() throws IOException {
        final Input input = getInput(
            "00 1 0100 01100 1 0101 00101 0101" +
            "01 01101 1 1 0100 0100 0100 0100 1 1 0100 1 1 1 1" +
            "10 0100 1 1 1 1 00100000 1 0101 0101 0101 0101 01100 01101 00100000 01111 0101 01100 01111 1"
        );
        final PebbleOffsetsStore offsetsStore = new LongListPebbleOffsetsStore(new long[] {0L, 26L, 61L});
        final PebbleBytesStore bytesStore = new BytesArrayPebbleBytesStore(input.buffer, offsetsStore);
        final int valueBitSize = 5;
        final IntList[] expectedLists = new IntList[] {
            new IntArrayList(new int[] {5, 8, 12, 13, 14, 15}),
            new IntArrayList(new int[] {5, 5, 8, 12, 12, 12, 13, 14, 14, 14, 15, 15}),
            new IntArrayList(new int[] {5, 8, 12, 13, 14, 12, 14, 5, 13, 14, 12, 15, 15})
        };
        final IntList[] lists = new IntList[expectedLists.length];
        IntList list;
        IntIterator iterator;

        for(int i = 0; i < lists.length; i++) {
            iterator = GenericListIterator.build(i, valueBitSize, bytesStore);
            lists[i] = list = new IntArrayList();
            while(iterator.hasNext()) {
                list.add(iterator.nextInt());
            }
        }

        assertEquals(
            Helper.<Integer, IntList>translateToUtilsCollection(expectedLists),
            Helper.<Integer, IntList>translateToUtilsCollection(lists)
        );
    }

}
