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

import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.pebble.core.encoding.small.IntOutputOffset;

/**
 * Implements an inverted list index used to find reference lists that at least has one matching value with the list
 * to be referenced. This implementation finds the references list that will generate the maximum compression. In
 * the case is better to no use a reference list, this index will return not found list. To ensure the best
 * candidate is found, this implementation computes the number of bits required to describe the representation for
 * each potential candidate and selects the one that generates the minimal number of bits. In case the minimal number
 * of bits is achieved without using a reference list, none reference list index will be returned. In the case of
 * many reference list candidates generates the same number of bits, the candidate with the minimal number of recursive
 * references will be selected to increase reading speed.
 */
public class InvertedListIntReferenceListsIndex implements IntReferenceListsIndex {

    protected final Int2ReferenceMap<IntList> listsInvertedIndex;
    private final IntSet candidates;
    private final IntOutputOffset outputOffset;

    public InvertedListIntReferenceListsIndex() {
        listsInvertedIndex = new Int2ReferenceOpenHashMap<IntList>();
        candidates = new IntOpenHashSet();
        outputOffset = new IntOutputOffset();
    }

    /**
     * {@inheritDoc}
     */
    public int getIndexOfReferenceList(
        final IntList list,
        final int valueBitSize,
        final int listIndex,
        final IntList[] lists,
        final int[] offsets,
        final int[] recursiveReferences
    ) {
        setCandidates(list);
        int size;
        IntList cloneList;
        cloneList = new IntArrayList(list);
        int minSize = 1 + outputOffset.getWriteIntervalsOffset(cloneList, valueBitSize);
        minSize += outputOffset.getWriteDeltaOffset(cloneList, valueBitSize);
        int bestReferenceIndex = -1;
        int bestRecursiveReferences = 0;
        IntIterator candidatesIterator = candidates.iterator();
        int candidateIndex;
        while (candidatesIterator.hasNext()) {
            candidateIndex = candidatesIterator.nextInt();
            cloneList = new IntArrayList(list);
            size = IntOutputOffset.getWriteDeltaOffset(listIndex - offsets[candidateIndex]);
            size += outputOffset.getWriteDifferenceOffset(cloneList, lists[candidateIndex]);
            if (size < minSize) {
                size += outputOffset.getWriteIntervalsOffset(cloneList, valueBitSize);
                if (size < minSize) {
                    size += outputOffset.getWriteDeltaOffset(cloneList, valueBitSize);
                    if (
                        size < minSize ||
                        (size == minSize && bestRecursiveReferences > recursiveReferences[candidateIndex])
                    ) {
                        bestReferenceIndex = candidateIndex;
                        minSize = size;
                        bestRecursiveReferences = recursiveReferences[candidateIndex];
                    }
                }
            }
        }
        return bestReferenceIndex;
    }

    /**
     * {@inheritDoc}
     */
    public void addListIntoListsInvertedIndex(final int index, final IntList list) {
        final IntIterator listIterator = list.iterator();
        IntList listsIndexes;
        int value;
        while (listIterator.hasNext()) {
            value = listIterator.nextInt();
            listsIndexes = listsInvertedIndex.get(value);
            if (listsIndexes == null) {
                listsIndexes = new IntArrayList();
                listsInvertedIndex.put(value, listsIndexes);
            }
            listsIndexes.add(index);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void removeListFromListsInvertedIndex(final int index, final IntList list) {
        final IntIterator listIterator = list.iterator();
        int value;
        IntList listsIndexes;
        while (listIterator.hasNext()) {
            value = listIterator.nextInt();
            listsIndexes = listsInvertedIndex.get(value);
            listsIndexes.remove((Integer) index);
            if (listsIndexes.isEmpty()) {
                listsInvertedIndex.remove(value);
            }
        }
    }

    private void setCandidates(final IntList list) {
        IntList listsIndexes;
        IntIterator indexesIterator;
        final IntIterator listIterator = list.iterator();
        candidates.clear();
        while (listIterator.hasNext()) {
            listsIndexes = listsInvertedIndex.get(listIterator.nextInt());
            if (listsIndexes != null) {
                indexesIterator = listsIndexes.iterator();
                while (indexesIterator.hasNext()) {
                    candidates.add(indexesIterator.nextInt());
                }
            }
        }
    }

}
