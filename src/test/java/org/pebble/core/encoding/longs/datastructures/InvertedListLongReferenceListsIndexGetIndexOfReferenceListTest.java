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
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.pebble.UnitTest;

import static junit.framework.TestCase.assertEquals;

@Category(UnitTest.class)
public class InvertedListLongReferenceListsIndexGetIndexOfReferenceListTest {

    @Test
    public void whenFindingListThatGeneratesSmallerCompressionThanPreviousOneItShouldReturnItsIndex() {
        final LongList[] lists = new LongList[] {
            new LongArrayList(new long[] {2L, 4L, 5L, 6L, 7L, 9L, 11L}),
            new LongArrayList(new long[] {2L, 4L, 5L, 6L, 7L, 9L}),
        };
        final int valueBitSize = 3;
        final InvertedListLongReferenceListsIndex listsIndex = new InvertedListLongReferenceListsIndex() {{
            listsInvertedIndex.put(2L, new IntArrayList(new int[] {0}));
            listsInvertedIndex.put(4L, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(5L, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(6L, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(7L, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(9L, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(11L, new IntArrayList(new int[] {1}));
        }};
        final int[] offsets = new int[] {0, 1};
        final int[] recursiveReferences = new int[] {0, 0};
        final int listIndex = 2;
        final LongList list = new LongArrayList(new long[] {2L, 4L, 5L, 6L, 7L, 9L, 10L});
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
        final LongList[] lists = new LongList[] {
            new LongArrayList(new long[] {2L, 4L, 5L, 6L, 7L, 9L}),
            new LongArrayList(new long[] {2L, 4L, 5L, 6L, 7L, 9L, 11L})
        };
        final int valueBitSize = 3;
        final InvertedListLongReferenceListsIndex listsIndex = new InvertedListLongReferenceListsIndex() {{
            listsInvertedIndex.put(2L, new IntArrayList(new int[] {1}));
            listsInvertedIndex.put(4L, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(5L, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(6L, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(7L, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(9L, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(11L, new IntArrayList(new int[] {0}));
        }};
        final int[] offsets = new int[] {0, 1};
        final int[] recursiveReferences = new int[] {0, 0};
        final int listIndex = 2;
        final LongList list = new LongArrayList(new long[] {2L, 4L, 5L, 6L, 7L, 9L, 10L});
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
        final LongList[] lists = new LongList[] {
            new LongArrayList(new long[] {2L, 4L, 5L, 6L, 7L, 9L}),
            new LongArrayList(new long[] {2L, 4L, 5L, 6L, 7L, 9L, 11L})
        };
        final int valueBitSize = 3;
        final InvertedListLongReferenceListsIndex listsIndex = new InvertedListLongReferenceListsIndex() {{
            listsInvertedIndex.put(2L, new IntArrayList(new int[] {1}));
            listsInvertedIndex.put(4L, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(5L, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(6L, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(7L, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(9L, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(11L, new IntArrayList(new int[] {0}));
        }};
        final int[] offsets = new int[] {0, 1};
        final int[] recursiveReferences = new int[] {1, 0};
        final int listIndex = 2;
        final LongList list = new LongArrayList(new long[] {2L, 4L, 5L, 6L, 7L, 9L, 10L});
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
        final LongList[] lists = new LongList[] {
            new LongArrayList(new long[] {2L, 4L, 6L, 8L}),
            new LongArrayList(new long[] {3L, 4L, 5L, 6L, 7L, 8L, 10L})
        };
        final int valueBitSize = 3;
        final InvertedListLongReferenceListsIndex listsIndex = new InvertedListLongReferenceListsIndex() {{
            listsInvertedIndex.put(2L, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(3L, new IntArrayList(new int[] {1}));
            listsInvertedIndex.put(4L, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(5L, new IntArrayList(new int[] {0}));
            listsInvertedIndex.put(6L, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(7L, new IntArrayList(new int[] {0}));
            listsInvertedIndex.put(8L, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(10L, new IntArrayList(new int[] {1}));
        }};
        final int[] offsets = new int[] {0, 1};
        final int[] recursiveReferences = new int[] {0, 0};
        final int listIndex = 2;
        final LongList list = new LongArrayList(new long[] {2L, 4L, 6L, 8L});
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
        final LongList[] lists = new LongList[] {
            new LongArrayList(new long[] {11L, 13L, 15L, 17L, 19L, 21L}),
            new LongArrayList(new long[] {6L, 13L, 15L, 17L, 19L, 21L})
        };
        final int valueBitSize = 3;
        final InvertedListLongReferenceListsIndex listsIndex = new InvertedListLongReferenceListsIndex() {{
            listsInvertedIndex.put(6L, new IntArrayList(new int[] {1}));
            listsInvertedIndex.put(10L, new IntArrayList(new int[] {0}));
            listsInvertedIndex.put(12L, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(14L, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(16L, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(18L, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(20L, new IntArrayList(new int[] {0, 1}));
        }};
        final int[] offsets = new int[] {0, 1};
        final int[] recursiveReferences = new int[] {0, 0};
        final int listIndex = 2;
        final LongList list = new LongArrayList(
            new long[] {2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 13L, 15L, 17L, 19L, 21L}
        );
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
        final LongList[] lists = new LongList[] {
            new LongArrayList(new long[] {2L, 4L, 6L, 8L}),
            new LongArrayList(new long[] {2L, 4L, 6L, 9L})
        };
        final int valueBitSize = 3;
        final InvertedListLongReferenceListsIndex listsIndex = new InvertedListLongReferenceListsIndex() {{
            listsInvertedIndex.put(2L, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(4L, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(6L, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(8L, new IntArrayList(new int[] {0}));
            listsInvertedIndex.put(9L, new IntArrayList(new int[] {1}));
        }};
        final int[] offsets = new int[] {0, 1};
        final int[] recursiveReferences = new int[] {1, 0};
        final int listIndex = 2;
        final LongList list = new LongArrayList(new long[] {2L, 4L, 6L});
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
        final LongList[] lists = new LongList[] {
            new LongArrayList(new long[] {2L, 4L, 6L, 8L}),
            new LongArrayList(new long[] {2L, 4L, 6L, 9L})
        };
        final int valueBitSize = 3;
        final InvertedListLongReferenceListsIndex listsIndex = new InvertedListLongReferenceListsIndex() {{
            listsInvertedIndex.put(2L, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(4L, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(6L, new IntArrayList(new int[] {0, 1}));
            listsInvertedIndex.put(8L, new IntArrayList(new int[] {0}));
            listsInvertedIndex.put(9L, new IntArrayList(new int[] {1}));
        }};
        final int[] offsets = new int[] {0, 1};
        final int[] recursiveReferences = new int[] {0, 0};
        final int listIndex = 2;
        final LongList list = new LongArrayList(new long[] {2L, 4L, 6L});
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
