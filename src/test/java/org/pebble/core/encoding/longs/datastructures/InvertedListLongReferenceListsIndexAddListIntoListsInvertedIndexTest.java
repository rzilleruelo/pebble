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

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.pebble.UnitTest;
import org.pebble.core.encoding.Helper;

import static org.junit.Assert.assertEquals;

@Category(UnitTest.class)
public class InvertedListLongReferenceListsIndexAddListIntoListsInvertedIndexTest {

    @Test
    public void whenIndexDoesNotHaveAnyPreviousIntersectingListItShouldIndexListAsExpected() {
        final Long2ReferenceMap<IntList> expectedListsInvertedIndex = new Long2ReferenceOpenHashMap<IntList>() {{
            put(1L, new IntArrayList(new int[] {0}));
            put(3L, new IntArrayList(new int[] {0}));
            put(4L, new IntArrayList(new int[] {0}));
            put(5L, new IntArrayList(new int[] {0}));
            put(8L, new IntArrayList(new int[] {0}));
        }};
        final InvertedListLongReferenceListsIndex listsIndex = new InvertedListLongReferenceListsIndex();
        final int index = 0;
        final LongList list = new LongArrayList(new long[] {1L, 3L, 4L, 5L, 8L});

        listsIndex.addListIntoListsInvertedIndex(index, list);
        assertEquals(
            Helper.<Long, Long2ReferenceMap<IntList>>translateToUtilsCollection(expectedListsInvertedIndex),
            Helper.<Long, Long2ReferenceMap<IntList>>translateToUtilsCollection(listsIndex.listsInvertedIndex)
        );
    }

    @Test
    public void whenIndexHavePreviousIntersectingListItShouldIndexListAsExpected() {
        final Long2ReferenceMap<IntList> expectedListsInvertedIndex = new Long2ReferenceOpenHashMap<IntList>() {{
            put(1L, new IntArrayList(new int[] {1}));
            put(2L, new IntArrayList(new int[] {0}));
            put(3L, new IntArrayList(new int[] {0, 1}));
            put(4L, new IntArrayList(new int[] {1}));
            put(5L, new IntArrayList(new int[] {0, 1}));
            put(8L, new IntArrayList(new int[] {1}));
        }};
        final InvertedListLongReferenceListsIndex listsIndex = new InvertedListLongReferenceListsIndex();
        listsIndex.listsInvertedIndex.put(2L, new IntArrayList(new int[]{0}));
        listsIndex.listsInvertedIndex.put(3L, new IntArrayList(new int[]{0}));
        listsIndex.listsInvertedIndex.put(5L, new IntArrayList(new int[]{0}));
        final int index = 1;
        final LongList list = new LongArrayList(new long[] {1L, 3L, 4L, 5L, 8L});

        listsIndex.addListIntoListsInvertedIndex(index, list);

        assertEquals(
            Helper.<Long, Long2ReferenceMap<IntList>>translateToUtilsCollection(expectedListsInvertedIndex),
            Helper.<Long, Long2ReferenceMap<IntList>>translateToUtilsCollection(listsIndex.listsInvertedIndex)
        );
    }

}
