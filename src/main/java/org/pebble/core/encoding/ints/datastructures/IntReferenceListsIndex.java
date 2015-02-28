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

import it.unimi.dsi.fastutil.ints.IntList;

/**
 * Interface for {@link IntReferenceListsStore} reference lists index. This interface allows the implementation
 * of custom strategies for indexing and searching reference list candidates that maximize compression and reading speed.
 */
public interface IntReferenceListsIndex {

    /**
     * Returns index of best reference list in terms of compression and speed.
     * @param list for which the reference will be found.
     * @param valueBitSize maximum number of bits required to represent the lists values in binary representation.
     * @param listIndex index of the given <code>list</code>.
     * @param lists set of reference lists.
     * @param offsets respective offset of reference lists for <code>lists</code>.
     * @param recursiveReferences respective number of recursive references for <code>lists</code>.
     * @return index of best reference list in terms of compression and speed. Can be -1 when a good candidate can't
     *         be found.
     */
    public int getIndexOfReferenceList(
        final IntList list,
        final int valueBitSize,
        final int listIndex,
        final IntList[] lists,
        final int[] offsets,
        final int[] recursiveReferences
    );

    /**
     * Adds to index the given <code>list</code>.
     * @param index of <code>list</code>.
     * @param list to be added to reference list index.
     */
    public void addListIntoListsInvertedIndex(final int index, final IntList list);

    /**
     * Removes from index the given <code>list</code>.
     * @param index of <code>list</code>.
     * @param list to be added to reference list index.
     */
    public void removeListFromListsInvertedIndex(final int index, final IntList list);

}
