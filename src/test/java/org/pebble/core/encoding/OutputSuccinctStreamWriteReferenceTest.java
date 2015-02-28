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
import org.pebble.core.encoding.ints.datastructures.IntReferenceListsStore;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Matchers;

import static org.pebble.core.encoding.Helper.getOutput;
import static org.pebble.core.encoding.Helper.toBinaryString;
import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Category(UnitTest.class)
public class OutputSuccinctStreamWriteReferenceTest {

    @Test
    public void whenMissingReferenceListItShouldWriteItsSuccinctRepresentationAndListToStoreSuccessfully()
        throws Exception
    {
        final int valueBitSize = 1;
        final int listIndex = 12;
        final IntList list = new IntArrayList(new int[] {1, 2, 3, 5, 6, 7, 10, 11, 16, 19});
        final IntReferenceListsStore referenceListsStore = mock(IntReferenceListsStore.class);
        doReturn(null).when(referenceListsStore).get(list, valueBitSize, listIndex);
        final String expectedOutput = "1".replace(" ", "");
        final int expectedOffset = 1;
        final Helper.Output out = getOutput();
        final OutputSuccinctStream outStreamSpy = spy(out.stream);

        final int offset = out.stream.writeReference(list, listIndex, valueBitSize, referenceListsStore);
        out.close();

        assertEquals(expectedOutput, toBinaryString(out.buffer, offset));
        assertEquals(expectedOffset, offset);
        verify(referenceListsStore, never()).remove(Matchers.<IntReferenceListsStore.ReferenceList>any());
        verify(referenceListsStore, times(1)).add(listIndex, 0, list);
        verify(outStreamSpy, never()).writeDifference(Matchers.<IntList>any(), Matchers.<IntList>any());
    }

    @Test
    public void whenReferenceListMatchListShouldWriteItsSuccinctRepresentationRemoveOldOneListAndNewOneToStoreSuccessfully()
        throws Exception
    {
        final int valueBitSize = 1;
        final int listIndex = 12;
        final IntList list = new IntArrayList(new int[] {1, 2, 3, 5, 6, 7, 10, 11, 16, 19});
        final IntList referenceList = new IntArrayList(new int[] {1, 2, 3, 5, 6, 7, 10, 11, 16, 19});
        IntReferenceListsStore.ReferenceList storeReferenceList = mock(IntReferenceListsStore.ReferenceList.class);
        doReturn(referenceList).when(storeReferenceList).getList();
        doReturn(10).when(storeReferenceList).getOffset();
        doReturn(1).when(storeReferenceList).getRecursiveReferences();
        final IntReferenceListsStore referenceListsStore = mock(IntReferenceListsStore.class);
        doReturn(storeReferenceList).when(referenceListsStore).get(list, valueBitSize, listIndex);
        final Helper.Output out = getOutput();
        final OutputSuccinctStream outStreamSpy = spy(out.stream);
        final int stubDifferenceOffset = 2;
        doReturn(stubDifferenceOffset).when(outStreamSpy).writeDifference(list, referenceList);
        final String expectedOutput = "0101".replace(" ", "");
        final int expectedOffset = 4 + stubDifferenceOffset;

        final int offset = outStreamSpy.writeReference(list, listIndex, valueBitSize, referenceListsStore);
        outStreamSpy.close();

        assertEquals(expectedOutput, toBinaryString(out.buffer, offset - stubDifferenceOffset));
        assertEquals(expectedOffset, offset);
        verify(referenceListsStore, times(1)).remove(storeReferenceList);
        verify(referenceListsStore, times(1)).add(listIndex, storeReferenceList.getRecursiveReferences() + 1, list);
        verify(outStreamSpy, times(1)).writeDifference(list, referenceList);
    }

    @Test
    public void whenReferenceListMatchPartiallyListShouldWriteItsSuccinctRepresentationAndAddListToStoreSuccessfully()
        throws Exception
    {
        final int valueBitSize = 1;
        final int listIndex = 12;
        final IntList list = new IntArrayList(new int[] {1, 2, 3, 5, 6, 7, 10, 11, 16, 19});
        final IntList referenceList = new IntArrayList(new int[] {1, 2, 3, 5, 7, 10, 11, 16, 19});
        IntReferenceListsStore.ReferenceList storeReferenceList = mock(IntReferenceListsStore.ReferenceList.class);
        doReturn(referenceList).when(storeReferenceList).getList();
        doReturn(10).when(storeReferenceList).getOffset();
        doReturn(1).when(storeReferenceList).getRecursiveReferences();
        final IntReferenceListsStore referenceListsStore = mock(IntReferenceListsStore.class);
        doReturn(storeReferenceList).when(referenceListsStore).get(list, valueBitSize, listIndex);
        final Helper.Output out = getOutput();
        final OutputSuccinctStream outStreamSpy = spy(out.stream);
        final int stubDifferenceOffset = 2;
        doReturn(stubDifferenceOffset).when(outStreamSpy).writeDifference(list, referenceList);
        final String expectedOutput = "0101".replace(" ", "");
        final int expectedOffset = 4 + stubDifferenceOffset;

        final int offset = outStreamSpy.writeReference(list, listIndex, valueBitSize, referenceListsStore);
        outStreamSpy.close();

        assertEquals(expectedOutput, toBinaryString(out.buffer, offset - stubDifferenceOffset));
        assertEquals(expectedOffset, offset);
        verify(referenceListsStore, never()).remove(storeReferenceList);
        verify(referenceListsStore, times(1)).add(listIndex, storeReferenceList.getRecursiveReferences() + 1, list);
        verify(outStreamSpy, times(1)).writeDifference(list, referenceList);
    }

}
