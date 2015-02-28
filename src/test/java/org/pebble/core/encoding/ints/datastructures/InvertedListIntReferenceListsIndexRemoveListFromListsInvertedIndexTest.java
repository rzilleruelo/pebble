package org.pebble.core.encoding.ints.datastructures;

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

import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.pebble.UnitTest;
import org.pebble.core.encoding.Helper;

import static junit.framework.TestCase.assertEquals;

@Category(UnitTest.class)
public class InvertedListIntReferenceListsIndexRemoveListFromListsInvertedIndexTest {

    @Test
    public void whenIndexHavePreviousIndexedListsItShouldRemoveListFromIndexAsExpected() {
        final Int2ReferenceMap<IntList> expectedListsInvertedIndex = new Int2ReferenceOpenHashMap<IntList>() {{
            put(2, new IntArrayList(new int[] {2}));
            put(3, new IntArrayList(new int[] {0}));
            put(5, new IntArrayList(new int[] {0, 2}));
            put(9, new IntArrayList(new int[] {2}));
        }};
        final InvertedListIntReferenceListsIndex listsIndex = new InvertedListIntReferenceListsIndex() {{
            listsInvertedIndex.put(1, new IntArrayList(new int[] {1}));
            listsInvertedIndex.put(2, new IntArrayList(new int[] {2}));
            listsInvertedIndex.put(3, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(4, new IntArrayList(new int[] {1}));
            listsInvertedIndex.put(5, new IntArrayList(new int[] {0, 1, 2}));
            listsInvertedIndex.put(8, new IntArrayList(new int[] {1}));
            listsInvertedIndex.put(9, new IntArrayList() {{ add(2); }});
        }};
        final int index = 1;
        final IntList list = new IntArrayList(new int[] {1, 3, 4, 5, 8});

        listsIndex.removeListFromListsInvertedIndex(index, list);

        assertEquals(
            Helper.<Integer, Int2ReferenceMap<IntList>>translateToUtilsCollection(expectedListsInvertedIndex),
            Helper.<Integer, Int2ReferenceMap<IntList>>translateToUtilsCollection(listsIndex.listsInvertedIndex)
        );
    }

}
