package org.pebble.core.encoding.longs.datastructures;

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
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.pebble.UnitTest;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@Category(UnitTest.class)
public class LongReferenceListsStoreAddTest {

    @Test
    public void whenNumberOfRecursiveReferencesIsBiggerThanMaxNumberOfRecursiveReferencesItShouldNotAddList() {
        final LongReferenceListsIndex referenceListsIndex = mock(LongReferenceListsIndex.class);
        final int size = 3;
        final int maxRecursiveReferences = 1;
        final int minListSize = 3;
        final LongReferenceListsStore referenceListsStore = new LongReferenceListsStore(
            size,
            maxRecursiveReferences,
            minListSize,
           referenceListsIndex
        );
        final int listIndex = 0;
        final int recursiveReferences = maxRecursiveReferences + 1;
        final LongList list = new LongArrayList(new long[] {1L, 2L, 3L, 4L, 5L, 6L});

        final boolean added = referenceListsStore.add(listIndex, recursiveReferences, list);

        assertFalse(added);
        verify(referenceListsIndex, never()).removeListFromListsInvertedIndex(anyInt(), any(LongList.class));
        verify(referenceListsIndex, never()).addListIntoListsInvertedIndex(anyInt(), any(LongList.class));
    }

    @Test
    public void whenListSizeIsSmallerThanMinListSizeItShouldNotAddList() {
        final LongReferenceListsIndex referenceListsIndex = mock(LongReferenceListsIndex.class);
        final int size = 3;
        final int maxRecursiveReferences = 1;
        final int minListSize = 3;
        final LongReferenceListsStore referenceListsStore = new LongReferenceListsStore(
            size,
            maxRecursiveReferences,
            minListSize,
            referenceListsIndex
        );
        final int listIndex = 0;
        final int recursiveReferences = 0;
        final LongList list = new LongArrayList(new long[] {1L, 2L});

        final boolean added = referenceListsStore.add(listIndex, recursiveReferences, list);

        assertFalse(added);
        verify(referenceListsIndex, never()).removeListFromListsInvertedIndex(anyInt(), any(LongList.class));
        verify(referenceListsIndex, never()).addListIntoListsInvertedIndex(anyInt(), any(LongList.class));
    }

    @Test
    public void whenAddListInEmptySlotItShouldAddListSuccessfully() {
        final LongReferenceListsIndex referenceListsIndex = mock(LongReferenceListsIndex.class);
        final int size = 3;
        final int maxRecursiveReferences = 1;
        final int minListSize = 3;
        final LongReferenceListsStore referenceListsStore = new LongReferenceListsStore(
            size,
            maxRecursiveReferences,
            minListSize,
            referenceListsIndex
        );
        final int listIndex = 0;
        final int recursiveReferences = 0;
        final LongList list = new LongArrayList(new long[] {1L, 2L, 3L, 4L, 5L, 6L});

        final boolean added = referenceListsStore.add(listIndex, recursiveReferences, list);

        assertTrue(added);
        verify(referenceListsIndex, never()).removeListFromListsInvertedIndex(anyInt(), any(LongList.class));
        verify(referenceListsIndex).addListIntoListsInvertedIndex(anyInt(), eq(list));
    }

    @Test
    public void whenAddListInANonEmptySlotItShouldAddListSuccessfully() {
        final LongReferenceListsIndex referenceListsIndex = mock(LongReferenceListsIndex.class);
        final int size = 1;
        final int maxRecursiveReferences = 1;
        final int minListSize = 3;
        final LongReferenceListsStore referenceListsStore = new LongReferenceListsStore(
            size,
            maxRecursiveReferences,
            minListSize,
            referenceListsIndex
        );
        final int listIndex = 0;
        final int recursiveReferences = 0;
        final LongList previousList = new LongArrayList(new long[] {0L, 3L, 5L, 7L});
        final LongList list = new LongArrayList(new long[] {1L, 2L, 3L, 4L, 5L, 6L});
        referenceListsStore.add(listIndex, recursiveReferences, previousList);

        final boolean added = referenceListsStore.add(listIndex, recursiveReferences, list);

        assertTrue(added);
        verify(referenceListsIndex).removeListFromListsInvertedIndex(anyInt(), eq(previousList));
        verify(referenceListsIndex).addListIntoListsInvertedIndex(anyInt(), eq(list));
    }

}
