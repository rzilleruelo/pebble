package org.pebble.core.encoding.small.datastructures;

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

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

/**
 * Handles the storage of reference lists used in Pebble's compression algorithm. Provides useful methods to
 * store and retrieve these lists.
 */
public class IntReferenceListsStore {

    private final int maxRecursiveReferences;
    private final int[] recursiveReferences;
    private final int minListSize;
    private final int[] offsets;
    private final IntList[] lists;
    private final IntReferenceListsIndex referenceListIndex;
    private int index;

    /**
     * Initializes a <code>IntReferenceListsStore</code> capable to store at most <code>size</code> lists with no more
     * than <code>maxRecursiveReferences</code> recursive references. If the number of lists exceeds <code>size</code>,
     * it will overwrite the oldest list on the store.
     * @param size maximum numbers of lists to be stored.
     * @param maxRecursiveReferences maximum number of allowed recursive references.
     * @param minListSize Minimum size of list required to be added to the store.
     * @param referenceListIndex index used to find the best reference list candidate.
     */
    public IntReferenceListsStore(
        final int size,
        final int maxRecursiveReferences,
        final int minListSize,
        IntReferenceListsIndex referenceListIndex
    ) {
        index = 0;
        this.maxRecursiveReferences = maxRecursiveReferences;
        recursiveReferences = new int[size];
        this.minListSize = minListSize;
        offsets = new int[size];
        lists = new IntList[size];
        this.referenceListIndex = referenceListIndex;
    }

    /**
     * Adds to the store the <code>list</code>. In case the store is full it will overwrite the oldest list on the
     * store.
     *
     * @param offset position of the <code>list</code> respect to the list of lists, starting from zero.
     * @param recursiveReferences number of recursive reference of the <code>list</code>.
     * @param list to add to the store.
     * @return true when the <code>list</code> is added to the store and false when is not.
     */
    public boolean add(final int offset, final int recursiveReferences, final IntList list) {
        if (recursiveReferences <= maxRecursiveReferences && minListSize <= list.size()) {
            if (lists[index] != null) {
                referenceListIndex.removeListFromListsInvertedIndex(index, lists[index]);
            }
            this.recursiveReferences[index] = recursiveReferences;
            offsets[index] = offset;
            lists[index] = new IntArrayList(list);
            referenceListIndex.addListIntoListsInvertedIndex(index, lists[index]);
            index = (index + 1) % offsets.length;
            return true;
        }
        return false;
    }

    /**
     * Removes from the store the given <code>list</code>.
     * @param list List to be removed from the store.
     */
    public void remove(ReferenceList list) {
        referenceListIndex.removeListFromListsInvertedIndex(list.index, list.list);
        lists[list.index] = null;
    }

    /**
     * Finds reference list to encode <code>list</code>.
     * @param list List that will be encoded using the reference.
     * @param valueBitSize maximum number of bits required to represent the lists values in binary representation.
     * @param listIndex index of the given <code>list</code>.
     * @return The best reference list in the store to encode <code>list.</code>. Can be null when there is not a good
     * candidate available.
     */
    public ReferenceList get(final IntList list, final int valueBitSize, final int listIndex) {
        final int i = referenceListIndex.getIndexOfReferenceList(
            list,
            valueBitSize,
            listIndex,
            lists,
            offsets,
            recursiveReferences
        );
        if (i >= 0) {
            return new ReferenceList(lists[i], offsets[i], recursiveReferences[i], i);
        }
        return null;
    }

    /**
     * Class that represents a reference list and its number of recursive references.
     */
    public static class ReferenceList {

        private final IntList list;
        private final int offset;
        private final int recursiveReferences;
        private final int index;

        private ReferenceList(IntList list, int offset, int recursiveReferences, int index) {
            this.index = index;
            this.list = list;
            this.offset = offset;
            this.recursiveReferences = recursiveReferences;
        }

        /**
         * Gets reference list.
         * @return reference list.
         */
        public IntList getList() {
            return list;
        }

        /**
         * Gets offset of the reference list.
         * @return offset of the reference list.
         */
        public int getOffset() {
            return offset;
        }

        /**
         * Gets number of recursive references.
         * @return number of recursive references.
         */
        public int getRecursiveReferences() {
            return recursiveReferences;
        }

    }

}
