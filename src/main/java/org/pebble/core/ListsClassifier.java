package org.pebble.core;

import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongListIterator;

public class ListsClassifier {

    public static final int SORTED_SET_LIST = 0;
    public static final int SORTED_LIST = 1;
    public static final int UNSORTED_LIST = 2;

    public static int classify(final IntList list) {
        final IntListIterator iterator = list.iterator();
        int type = SORTED_SET_LIST;
        int lastElement = iterator.next();
        int element;
        while (type != UNSORTED_LIST && iterator.hasNext()) {
            element = iterator.next();
            if (element < lastElement) {
                type = UNSORTED_LIST;
            } else if (element == lastElement) {
                type = SORTED_LIST;
            }
            lastElement = element;
        }
        return type;
    }

    public static int classify(final LongList list) {
        final LongListIterator iterator = list.iterator();
        int type = SORTED_SET_LIST;
        long lastElement = iterator.next();
        long element;
        while (type != UNSORTED_LIST && iterator.hasNext()) {
            element = iterator.next();
            if (element < lastElement) {
                type = UNSORTED_LIST;
            } else if (element == lastElement) {
                type = SORTED_LIST;
            }
            lastElement = element;
        }
        return type;
    }

}
