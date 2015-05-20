package org.pebble.core;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.pebble.FastIntegrationTest;
import org.pebble.core.encoding.Helper;
import org.pebble.core.encoding.OutputSuccinctStream;
import org.pebble.core.encoding.ints.datastructures.IntReferenceListsIndex;
import org.pebble.core.encoding.ints.datastructures.IntReferenceListsStore;
import org.pebble.core.encoding.ints.datastructures.InvertedListIntReferenceListsIndex;

import java.io.IOException;

import static junit.framework.TestCase.assertEquals;
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
            new IntArrayList(new int[] {5, 8, 12, 14, 15, 16}),
            new IntArrayList(new int[] {5, 5, 8, 12, 12, 12, 14, 14, 14, 15, 15, 16}),
            new IntArrayList(new int[] {5, 16, 8, 12, 14, 12, 14, 5, 14, 12, 15, 16, 15})
        };
        /**
         *
         */
        final String expectedOutput = ("");
        final int expectedTotalOffset = 113;

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

    }

}
