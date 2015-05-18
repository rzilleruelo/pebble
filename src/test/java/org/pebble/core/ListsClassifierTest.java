package org.pebble.core;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.pebble.FastIntegrationTest;

import static org.junit.Assert.assertEquals;

@Category(FastIntegrationTest.class)
public class ListsClassifierTest {

    @Test
    public void whenIntListIsStrictlyIncrementalListClassifyShouldReturnSortedSetList() {
        IntList list = new IntArrayList(new int[] {3, 5, 8, 10});

        assertEquals(ListsClassifier.SORTED_SET_LIST, ListsClassifier.classify(list));
    }

    @Test
    public void whenLongListIsStrictlyIncrementalListClassifyShouldReturnSortedSetList() {
        LongList list = new LongArrayList(new long[] {3L, 5L, 8L, 10L});

        assertEquals(ListsClassifier.SORTED_SET_LIST, ListsClassifier.classify(list));
    }

    @Test
    public void whenIntListIsIncrementalListClassifyShouldReturnSortedList() {
        IntList list = new IntArrayList(new int[] {3, 5, 8, 8, 10});

        assertEquals(ListsClassifier.SORTED_LIST, ListsClassifier.classify(list));
    }

    @Test
    public void whenLongListIsIncrementalListClassifyShouldReturnSortedList() {
        LongList list = new LongArrayList(new long[] {3L, 5L, 8L, 8L, 10L});

        assertEquals(ListsClassifier.SORTED_LIST, ListsClassifier.classify(list));
    }

    @Test
    public void whenIntListIsAnUnsortedListClassifyShouldReturnUnsortedList() {
        IntList list = new IntArrayList(new int[] {3, 5, 8, 6, 10});

        assertEquals(ListsClassifier.UNSORTED_LIST, ListsClassifier.classify(list));
    }

    @Test
    public void whenLongListIsAnUnsortedListClassifyShouldReturnUnsortedList() {
        LongList list = new LongArrayList(new long[] {3L, 5L, 8L, 6L, 10L});

        assertEquals(ListsClassifier.UNSORTED_LIST, ListsClassifier.classify(list));
    }

}
