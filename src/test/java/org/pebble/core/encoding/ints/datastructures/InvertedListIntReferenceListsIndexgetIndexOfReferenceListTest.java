package org.pebble.core.encoding.ints.datastructures;

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
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static junit.framework.TestCase.assertEquals;

@Category(UnitTest.class)
public class InvertedListIntReferenceListsIndexgetIndexOfReferenceListTest {

    @Test
    public void whenFindingListThatGeneratesSmallerCompressionThanPreviousOneItShouldReturnItsIndex() {
        final IntList[] lists = new IntList[] {
            new IntArrayList(new int[] {2, 4, 5, 6, 7, 9, 11}),
            new IntArrayList(new int[] {2, 4, 5, 6, 7, 9}),
        };
        final int valueBitSize = 3;
        final InvertedListIntReferenceListsIndex listsIndex = new InvertedListIntReferenceListsIndex() {{
            listsInvertedIndex.put(2, new IntArrayList(new int[] {0}));
            listsInvertedIndex.put(4, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(5, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(6, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(7, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(9, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(11, new IntArrayList(new int[] {1}));
        }};
        final int[] offsets = new int[] {0, 1};
        final int[] recursiveReferences = new int[] {0, 0};
        final int listIndex = 2;
        final IntList list = new IntArrayList(new int[] {2, 4, 5, 6, 7, 9, 10});
        final int expectedReferenceListIndex = 1;

        final int referenceListIndex = listsIndex.getIndexOfReferenceList(
            list,
            valueBitSize,
            listIndex,
            lists,
            offsets,
            recursiveReferences
        );

        assertEquals(expectedReferenceListIndex, referenceListIndex);
    }

    @Test
    public void whenFindingListThatGeneratesBiggerCompressionThanPreviousOneItShouldReturnItsIndex() {
        final IntList[] lists = new IntList[] {
            new IntArrayList(new int[] {2, 4, 5, 6, 7, 9}),
            new IntArrayList(new int[] {2, 4, 5, 6, 7, 9, 11})
        };
        final int valueBitSize = 3;
        final InvertedListIntReferenceListsIndex listsIndex = new InvertedListIntReferenceListsIndex() {{
            listsInvertedIndex.put(2, new IntArrayList(new int[] {1}));
            listsInvertedIndex.put(4, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(5, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(6, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(7, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(9, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(11, new IntArrayList(new int[] {0}));
        }};
        final int[] offsets = new int[] {0, 1};
        final int[] recursiveReferences = new int[] {0, 0};
        final int listIndex = 2;
        final IntList list = new IntArrayList(new int[] {2, 4, 5, 6, 7, 9, 10});
        final int expectedReferenceListIndex = 0;

        final int referenceListIndex = listsIndex.getIndexOfReferenceList(
            list,
            valueBitSize,
            listIndex,
            lists,
            offsets,
            recursiveReferences
        );

        assertEquals(expectedReferenceListIndex, referenceListIndex);
    }

    @Test
    public void whenFindingListThatGeneratesBiggerCompressionButHasLessRecursiveReferencesItShouldKeepPreviousOne() {
        final IntList[] lists = new IntList[] {
            new IntArrayList(new int[] {2, 4, 5, 6, 7, 9}),
            new IntArrayList(new int[] {2, 4, 5, 6, 7, 9, 11})
        };
        final int valueBitSize = 3;
        final InvertedListIntReferenceListsIndex listsIndex = new InvertedListIntReferenceListsIndex() {{
            listsInvertedIndex.put(2, new IntArrayList(new int[] {1}));
            listsInvertedIndex.put(4, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(5, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(6, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(7, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(9, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(11, new IntArrayList(new int[] {0}));
        }};
        final int[] offsets = new int[] {0, 1};
        final int[] recursiveReferences = new int[] {1, 0};
        final int listIndex = 2;
        final IntList list = new IntArrayList(new int[] {2, 4, 5, 6, 7, 9, 10});
        final int expectedReferenceListIndex = 0;

        final int referenceListIndex = listsIndex.getIndexOfReferenceList(
            list,
            valueBitSize,
            listIndex,
            lists,
            offsets,
            recursiveReferences
        );

        assertEquals(expectedReferenceListIndex, referenceListIndex);
    }

    @Test
    public void whenFindingListThatGeneratesCompressionBiggerOffsetAndDifferenceEncodingItShouldBeDiscarded() {
        final IntList[] lists = new IntList[] {
            new IntArrayList(new int[] {2, 4, 6, 8}),
            new IntArrayList(new int[] {3, 4, 5, 6, 7, 8, 10})
        };
        final int valueBitSize = 3;
        final InvertedListIntReferenceListsIndex listsIndex = new InvertedListIntReferenceListsIndex() {{
            listsInvertedIndex.put(2, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(3, new IntArrayList(new int[] {1}));
            listsInvertedIndex.put(4, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(5, new IntArrayList(new int[] {0}));
            listsInvertedIndex.put(6, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(7, new IntArrayList(new int[] {0}));
            listsInvertedIndex.put(8, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(10, new IntArrayList(new int[] {1}));
        }};
        final int[] offsets = new int[] {0, 1};
        final int[] recursiveReferences = new int[] {0, 0};
        final int listIndex = 2;
        final IntList list = new IntArrayList(new int[] {2, 4, 6, 8});
        final int expectedReferenceListIndex = 0;

        final int referenceListIndex = listsIndex.getIndexOfReferenceList(
            list,
            valueBitSize,
            listIndex,
            lists,
            offsets,
            recursiveReferences
        );

        assertEquals(expectedReferenceListIndex, referenceListIndex);
    }

    @Test
    public void whenFindingListThatGeneratesCompressionBiggerOffsetDifferenceAndIntervalEncodingItShouldBeDiscarded() {
        final IntList[] lists = new IntList[] {
            new IntArrayList(new int[] {11, 13, 15, 17, 19, 21}),
            new IntArrayList(new int[] {6, 13, 15, 17, 19, 21})
        };
        final int valueBitSize = 3;
        final InvertedListIntReferenceListsIndex listsIndex = new InvertedListIntReferenceListsIndex() {{
            listsInvertedIndex.put(6, new IntArrayList(new int[] {1}));
            listsInvertedIndex.put(10, new IntArrayList(new int[] {0}));
            listsInvertedIndex.put(12, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(14, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(16, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(18, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(20, new IntArrayList(new int[] {0, 1}));
        }};
        final int[] offsets = new int[] {0, 1};
        final int[] recursiveReferences = new int[] {0, 0};
        final int listIndex = 2;
        final IntList list = new IntArrayList(new int[] {2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 13, 15, 17, 19, 21});
        final int expectedReferenceListIndex = 0;

        final int referenceListIndex = listsIndex.getIndexOfReferenceList(
            list,
            valueBitSize,
            listIndex,
            lists,
            offsets,
            recursiveReferences
        );

        assertEquals(expectedReferenceListIndex, referenceListIndex);
    }

    @Test
    public void whenFindingListsThatGeneratesSameCompressionItShouldReturnTheOneWithLessRecursiveReferences() {
        final IntList[] lists = new IntList[] {
            new IntArrayList(new int[] {2, 4, 6, 8}),
            new IntArrayList(new int[] {2, 4, 6, 9})
        };
        final int valueBitSize = 3;
        final InvertedListIntReferenceListsIndex listsIndex = new InvertedListIntReferenceListsIndex() {{
            listsInvertedIndex.put(2, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(4, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(6, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(8, new IntArrayList(new int[] {0}));
            listsInvertedIndex.put(9, new IntArrayList(new int[] {1}));
        }};
        final int[] offsets = new int[] {0, 1};
        final int[] recursiveReferences = new int[] {1, 0};
        final int listIndex = 2;
        final IntList list = new IntArrayList(new int[] {2, 4, 6});
        final int expectedReferenceListIndex = 1;

        final int referenceListIndex = listsIndex.getIndexOfReferenceList(
            list,
            valueBitSize,
            listIndex,
            lists,
            offsets,
            recursiveReferences
        );

        assertEquals(expectedReferenceListIndex, referenceListIndex);
    }

    @Test
    public void whenFindingListsThatGeneratesSameCompressionAnRecursiveReferencesItShouldReturnFirstOne() {
        final IntList[] lists = new IntList[] {
            new IntArrayList(new int[] {2, 4, 6, 8}),
            new IntArrayList(new int[] {2, 4, 6, 9})
        };
        final int valueBitSize = 3;
        final InvertedListIntReferenceListsIndex listsIndex = new InvertedListIntReferenceListsIndex() {{
            listsInvertedIndex.put(2, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(4, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(6, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(8, new IntArrayList(new int[] {0}));
            listsInvertedIndex.put(9, new IntArrayList(new int[] {1}));
        }};
        final int[] offsets = new int[] {0, 1};
        final int[] recursiveReferences = new int[] {0, 0};
        final int listIndex = 2;
        final IntList list = new IntArrayList(new int[] {2, 4, 6});
        final int expectedReferenceListIndex = 0;

        final int referenceListIndex = listsIndex.getIndexOfReferenceList(
            list,
            valueBitSize,
            listIndex,
            lists,
            offsets,
            recursiveReferences
        );

        assertEquals(expectedReferenceListIndex, referenceListIndex);
    }

}
