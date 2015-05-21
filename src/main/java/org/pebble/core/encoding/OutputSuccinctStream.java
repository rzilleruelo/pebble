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

package org.pebble.core.encoding;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntRBTreeMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntRBTreeMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongRBTreeSet;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import org.pebble.core.ListsClassifier;
import org.pebble.core.encoding.ints.datastructures.IntReferenceListsStore;
import org.pebble.core.encoding.longs.datastructures.LongReferenceListsStore;
import org.pebble.core.exceptions.DeltaValueIsTooBigException;
import org.pebble.core.exceptions.NotStrictlyIncrementalListException;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Implements Pebble's core lists compression algorithms.
 */
public class OutputSuccinctStream extends OutputBitStream {

    private final int minIntervalSize;
    private final IntList repeatsBuffer;
    private final IntList blocksBuffer;
    private final IntList intervalsBuffer;

    /**
     * Initialize a stream that will write into the bytes array <code>a</code>.
     * @param buffer output of the stream.
     */
    public OutputSuccinctStream(final byte[] buffer) {
        super(buffer);
        minIntervalSize = DefaultParametersValues.DEFAULT_MIN_INTERVAL_SIZE;
        repeatsBuffer = new IntArrayList();
        blocksBuffer = new IntArrayList();
        intervalsBuffer = new IntArrayList();
    }

    /**
     * Initialize a stream that will write into the output stream <code>os</code>.
     * @param os output of the stream.
     */
    public OutputSuccinctStream(final OutputStream os) {
        super(os);
        minIntervalSize = DefaultParametersValues.DEFAULT_MIN_INTERVAL_SIZE;
        repeatsBuffer = new IntArrayList();
        blocksBuffer = new IntArrayList();
        intervalsBuffer = new IntArrayList();
    }

    /**
     * Writes the succinct representation of the repetitions extracted from sorted <code>list</code>.
     * <ul>
     *     <li>Number of repetitions intervals.</li>
     *     <li>
     *         For each interval:
     *         <ul>
     *             <li>
     *                 Store the index of the repeated element minus the previous index minus 1, given that the
     *                 difference between the indexes is at least one. The first index is stored raw given there is no
     *                 previous index.
     *             </li>
     *             <li>
     *                 Store the number of repetitions minus 2, given that repetitions must be at least two, to be an
     *                 actual repetition.
     *             </li>
     *         </ul>
     *     </li>
     * </ul>
     *
     * Each number is stored using delta encoding. Given than some values can be zero, is added one to each
     * before apply delta encoding.
     * For the example, <code>list</code>:
     * <pre>    {1, 1, 2, 3, 3, 3, 5, 6, 6, 7, 10, 11, 11, 12, 12, 12, 16, 19, 19}</pre>
     * , it will generate the following encoding:
     * <pre>
     *     6     0 2 2   3   4   2 7   2 8 3   10  2 Repetitions Intervals.
     *     6     0 0 1   1   1   0 2   0 0 1   1   0 Delta representation.
     *     7     1 1 2   2   2   1 3   1 1 2   2   1 Add 1 to ensure non zeros.
     *     111   1 1 01  01  01  1 11  1 1 01  01  1 Binary representation.
     *     00111 1 1 010 010 010 1 011 1 1 010 010 1 Delta encoding.
     * </pre>
     *
     * @param list from which extract the repetitions to encode. List must be incremental with positives
     *             (including zero) values.
     * @return number of written bits.
     * @throws IOException when there is an exception writing into <code>out</code>.
     */
    protected int writeRepetitions(final IntList list) throws IOException {
        int offset = 0;
        int value = -1;
        int lastValue;
        int index = -1;
        int repetitionStartIndex = -1;
        int lastRepetitionStartIndex;
        int numberOfRepetitions = 0;
        final IntIterator listIterator = list.iterator();
        repeatsBuffer.clear();
        while (listIterator.hasNext()) {
            lastValue = value;
            value = listIterator.nextInt();
            if (lastValue == value) {
                if (numberOfRepetitions == 0) {
                    repetitionStartIndex = index;
                }
                numberOfRepetitions++;
                listIterator.remove();
            } else {
                if (numberOfRepetitions > 0) {
                    repeatsBuffer.add(repetitionStartIndex);
                    repeatsBuffer.add(numberOfRepetitions);
                    numberOfRepetitions = 0;
                }
                index++;
            }
        }
        if (numberOfRepetitions > 0) {
            repeatsBuffer.add(repetitionStartIndex);
            repeatsBuffer.add(numberOfRepetitions);
        }
        final IntIterator repeatsIterator = repeatsBuffer.iterator();
        lastRepetitionStartIndex = -1;
        offset += writeDelta(repeatsBuffer.size() / 2);
        while (repeatsIterator.hasNext()) {
            repetitionStartIndex = repeatsIterator.nextInt();
            offset += writeDelta(repetitionStartIndex - lastRepetitionStartIndex - 1);
            offset += writeDelta(repeatsIterator.nextInt() - 1);
            lastRepetitionStartIndex = repetitionStartIndex;
        }
        return offset;
    }

    /**
     * Writes the succinct representation of the repetitions extracted from sorted <code>list</code>.
     * <ul>
     *     <li>Number of repetitions intervals.</li>
     *     <li>
     *         For each interval:
     *         <ul>
     *             <li>
     *                 Store the index of the repeated element minus the previous index minus 1, given that the
     *                 difference between the indexes is at least one. The first index is stored raw given there is no
     *                 previous index.
     *             </li>
     *             <li>
     *                 Store the number of repetitions minus 2, given that repetitions must be at least two, to be an
     *                 actual repetition.
     *             </li>
     *         </ul>
     *     </li>
     * </ul>
     *
     * Each number is stored using delta encoding. Given than some values can be zero, is added one to each
     * before apply delta encoding.
     * For the example, <code>list</code>:
     * <pre>    {1, 1, 2, 3, 3, 3, 5, 6, 6, 7, 10, 11, 11, 12, 12, 12, 16, 19, 19}</pre>
     * , it will generate the following encoding:
     * <pre>
     *     6     0 2 2   3   4   2 7   2 8 3   10  2 Repetitions Intervals.
     *     6     0 0 1   1   1   0 2   0 0 1   1   0 Delta representation.
     *     7     1 1 2   2   2   1 3   1 1 2   2   1 Add 1 to ensure non zeros.
     *     111   1 1 01  01  01  1 11  1 1 01  01  1 Binary representation.
     *     00111 1 1 010 010 010 1 011 1 1 010 010 1 Delta encoding.
     * </pre>
     *
     * @param list from which extract the repetitions to encode. List must be incremental with positives
     *             (including zero) values.
     * @return number of written bits.
     * @throws IOException when there is an exception writing into <code>out</code>.
     */
    protected int writeRepetitions(final LongList list) throws IOException {
        int offset = 0;
        long value = -1L;
        long lastValue;
        int index = -1;
        int repetitionStartIndex = -1;
        int lastRepetitionStartIndex;
        int numberOfRepetitions = 0;
        final LongIterator listIterator = list.iterator();
        repeatsBuffer.clear();
        while (listIterator.hasNext()) {
            lastValue = value;
            value = listIterator.nextLong();
            if (lastValue == value) {
                if (numberOfRepetitions == 0) {
                    repetitionStartIndex = index;
                }
                numberOfRepetitions++;
                listIterator.remove();
            } else {
                if (numberOfRepetitions > 0) {
                    repeatsBuffer.add(repetitionStartIndex);
                    repeatsBuffer.add(numberOfRepetitions);
                    numberOfRepetitions = 0;
                }
                index++;
            }
        }
        if (numberOfRepetitions > 0) {
            repeatsBuffer.add(repetitionStartIndex);
            repeatsBuffer.add(numberOfRepetitions);
        }
        final IntIterator repeatsIterator = repeatsBuffer.iterator();
        lastRepetitionStartIndex = -1;
        offset += writeDelta(repeatsBuffer.size() / 2);
        while (repeatsIterator.hasNext()) {
            repetitionStartIndex = repeatsIterator.nextInt();
            offset += writeDelta(repetitionStartIndex - lastRepetitionStartIndex - 1);
            offset += writeDelta(repeatsIterator.nextInt() - 1);
            lastRepetitionStartIndex = repetitionStartIndex;
        }
        return offset;
    }

    /**
     * Writes the succinct reference representation of the given strictly incremental <code>list</code>.
     * <ul>
     *     <li>
     *         Retrieves from <code>referenceListsStore</code> the list that produces the smaller encoding for
     *         <code>list</code>.
     *     </li>
     *     <li>
     *         Adds <code>list</code> to <code>referenceListsStore</code> so it can be use as a reference by
     *         future lists.
     *     </li>
     *     <li>
     *         Writes the relative offset with the retrieved reference list (0 when no reference list is found)
     *         using delta encoding.
     *     </li>
     *     <li>
     *         Writes the difference
     *         (see {@link #writeDifference(IntList, IntList) writeDifference}).
     *     </li>
     * </ul>
     *
     * For the example, <code>list</code>
     * <pre>    {3, 4, 5, 8, 9, 11, 12, 13}</pre>
     * , <code>listLindex</code> 63, <code>referenceListsStore</code> containing the reference list
     * <pre>    {1, 2, 4, 8, 9, 10, 11}</pre>
     * of offset 60. It will generate the following encoding:
     * <pre>
     *     (63, 60) &lt;difference encoding&gt; Offsets.
     *     3        &lt;difference encoding&gt; Relative offset.
     *     4        &lt;difference encoding&gt; Add 1 to ensure non zeros.
     *     001      &lt;difference encoding&gt; Binary representation.
     *     01100    &lt;difference encoding&gt; Delta encoding.
     * </pre>
     *
     * @param list from which it will encode the difference with the closest reference. List must be strictly
     *             incremental with positives (including zero) values.
     * @param listIndex offset of the given <code>list</code>.
     * @param valueBitSize fixed number of bits used to represent value in list to be encoded. It can be any value
     *                     between 1bit and 31 bits.
     * @param referenceListsStore store that contains all available references lists.
     * @return number of written bits.
     * @throws IOException when there is an exception writing into <code>out</code>.
     * @throws IllegalArgumentException when the list is not strictly incremental.
     */
    protected int writeReference(
        final IntList list,
        final int listIndex, final int valueBitSize,
        final IntReferenceListsStore referenceListsStore
    ) throws IOException {
        int offset = 0;
        IntReferenceListsStore.ReferenceList referenceList = referenceListsStore.get(list, valueBitSize, listIndex);
        if (referenceList == null) {
            referenceListsStore.add(listIndex, 0, list);
            offset += writeDelta(0);
        } else {
            if (referenceList.getList().equals(list)) {
                referenceListsStore.remove(referenceList);
            }
            referenceListsStore.add(listIndex, referenceList.getRecursiveReferences() + 1, list);
            offset += writeDelta(listIndex - referenceList.getOffset());
            offset += writeDifference(list, referenceList.getList());
        }
        return offset;
    }

    /**
     * Writes the succinct reference representation of the given strictly incremental <code>list</code>.
     * <ul>
     *     <li>
     *         Retrieves from <code>referenceListsStore</code> the list that produces the smaller encoding for
     *         <code>list</code>.
     *     </li>
     *     <li>
     *         Adds <code>list</code> to <code>referenceListsStore</code> so it can be use as a reference by
     *         future lists.
     *     </li>
     *     <li>
     *         Writes the relative offset with the retrieved reference list (0 when no reference list is found)
     *         using delta encoding.
     *     </li>
     *     <li>
     *         Writes the difference
     *         (see {@link #writeDifference(IntList, IntList) writeDifference}).
     *     </li>
     * </ul>
     *
     * For the example, <code>list</code>
     * <pre>    {3, 4, 5, 8, 9, 11, 12, 13}</pre>
     * , <code>listLindex</code> 63, <code>referenceListsStore</code> containing the reference list
     * <pre>    {1, 2, 4, 8, 9, 10, 11}</pre>
     * of offset 60. It will generate the following encoding:
     * <pre>
     *     (63, 60) &lt;difference encoding&gt; Offsets.
     *     3        &lt;difference encoding&gt; Relative offset.
     *     4        &lt;difference encoding&gt; Add 1 to ensure non zeros.
     *     001      &lt;difference encoding&gt; Binary representation.
     *     01100    &lt;difference encoding&gt; Delta encoding.
     * </pre>
     *
     * @param list from which it will encode the difference with the closest reference. List must be strictly
     *             incremental with positives (including zero) values.
     * @param listIndex offset of the given <code>list</code>.
     * @param valueBitSize fixed number of bits used to represent value in list to be encoded. It can be any value
     *                     between 1bit and 63 bits.
     * @param referenceListsStore store that contains all available references lists.
     * @return number of written bits.
     * @throws IOException when there is an exception writing into <code>out</code>.
     * @throws IllegalArgumentException when the list is not strictly incremental.
     */
    protected int writeReference(
        final LongList list,
        final int listIndex, final int valueBitSize,
        final LongReferenceListsStore referenceListsStore
    ) throws IOException {
        int offset = 0;
        LongReferenceListsStore.ReferenceList referenceList = referenceListsStore.get(list, valueBitSize, listIndex);
        if (referenceList == null) {
            referenceListsStore.add(listIndex, 0, list);
            offset += writeDelta(0);
        } else {
            if (referenceList.getList().equals(list)) {
                referenceListsStore.remove(referenceList);
            }
            referenceListsStore.add(listIndex, referenceList.getRecursiveReferences() + 1, list);
            offset += writeDelta(listIndex - referenceList.getOffset());
            offset += writeDifference(list, referenceList.getList());
        }
        return offset;
    }

    /**
     * Writes the succinct difference representation between the given strictly incremental <code>list</code>
     * and <code>referenceList</code>.
     * <ul>
     *     <li>Number of blocks found on the difference representation.</li>
     *     <li>Bit that indicate if the first block matches elements in the list (1) or doesn't (0).</li>
     *     <li>
     *         For each block, except last one that is omitted because in can be inferred by knowing the total length
     *         of the list.
     *         <ul>
     *             <li>Size of the block minus one given that a block has at least one consecutive bit.</li>
     *         </ul>
     *     </li>
     * </ul>
     *
     * For the example, <code>list</code>
     * <pre>    {3, 4, 5, 8, 9, 11, 12, 13}</pre>
     * and <code>referenceList</code>
     * <pre>    {1, 2, 4, 8, 9, 10, 11}</pre>
     * . It will generate the following encoding:
     * <pre>
     *     {      3, 4, 5, 8, 9,     11, 12, 13} List.
     *     {1, 2,    4,    8, 9, 10, 11        } Reference list.
     *     {0, 0,    1,    1, 1, 0,  1         } Matches. 0 to indicate no match and 1 to indicate match.
     *     4     1    2    0                     Number of blocks (consecutive bits values) minus one given that at
     *                                           there is one block and blocks sizes minus one, given that the blocks
     *                                           have at least one element.
     *     5     2    3    1                     Add 1 to ensure non zeros.
     *     101   01   11   1                     Binary representation.
     *     01101 0100 0101 1                     Delta encoding.
     * </pre>
     * @param list from which it will encode the difference with the closest reference. List must be strictly
     *             incremental with positives (including zero) values.
     * @param referenceList reference list from which the difference will be computed.
     * @return number of written bits.
     * @throws IOException when there is an exception writing into <code>out</code>.
     * @throws IllegalArgumentException when the list is not strictly incremental.
     */
    protected int writeDifference(
        final LongList list,
        final LongList referenceList
    ) throws IOException {
        int offset = 0;
        final LongIterator listIterator = list.listIterator();
        final LongIterator referenceListIterator = referenceList.listIterator();
        long listValue = listIterator.nextLong();
        long lastListValue;
        long referenceListValue = referenceListIterator.nextLong();
        int blockSize = 0;
        boolean pendingReference = false;
        boolean intersected = true;
        blocksBuffer.clear();
        while (true) {
            if (listValue == referenceListValue) {
                if (intersected) {
                    blockSize++;
                } else {
                    blocksBuffer.add(blockSize);
                    blockSize = 1;
                    intersected = true;
                }
                pendingReference = false;
                listIterator.remove();
                if (!listIterator.hasNext() || !referenceListIterator.hasNext()) {
                    break;
                }
                lastListValue = listValue;
                listValue = listIterator.nextLong();
                if (lastListValue >= listValue) {
                    throw new NotStrictlyIncrementalListException(lastListValue, listValue);
                }
                referenceListValue = referenceListIterator.nextLong();
                pendingReference = true;
            } else if (listValue < referenceListValue) {
                if (!listIterator.hasNext()) {
                    break;
                }
                lastListValue = listValue;
                listValue = listIterator.nextLong();
                if (lastListValue >= listValue) {
                    throw new NotStrictlyIncrementalListException(lastListValue, listValue);
                }
            } else {
                if (intersected) {
                    blocksBuffer.add(blockSize);
                    blockSize = 1;
                    intersected = false;
                } else {
                    blockSize++;
                }
                pendingReference = false;
                if (!referenceListIterator.hasNext()) {
                    break;
                }
                referenceListValue = referenceListIterator.nextLong();
                pendingReference = true;
            }
        }

        while (referenceListIterator.hasNext()) {
            if (intersected) {
                blocksBuffer.add(blockSize);
                blockSize = 1;
                intersected = false;
            } else {
                blockSize++;
            }
            referenceListIterator.next();
        }

        if (pendingReference && intersected) {
            blocksBuffer.add(blockSize);
            intersected = false;
        }

        if (!blocksBuffer.isEmpty() && blocksBuffer.get(0) == 0) {
            blocksBuffer.remove(0);
        }

        final boolean initialBit = (blocksBuffer.size() & 1) == 0 ? intersected : !intersected;
        offset += writeDelta(blocksBuffer.size());
        offset += writeBit(initialBit);
        final IntIterator blocksIterator = blocksBuffer.iterator();
        while (blocksIterator.hasNext()) {
            offset += writeDelta(blocksIterator.nextInt() - 1);
        }
        return offset;
    }

    /**
     * Writes the succinct difference representation between the given strictly incremental <code>list</code>
     * and <code>referenceList</code>.
     * <ul>
     *     <li>Number of blocks found on the difference representation.</li>
     *     <li>Bit that indicate if the first block matches elements in the list (1) or doesn't (0).</li>
     *     <li>
     *         For each block, except last one that is omitted because in can be inferred by knowing the total length
     *         of the list.
     *         <ul>
     *             <li>Size of the block minus one given that a block has at least one consecutive bit.</li>
     *         </ul>
     *     </li>
     * </ul>
     *
     * For the example, <code>list</code>
     * <pre>    {3, 4, 5, 8, 9, 11, 12, 13}</pre>
     * and <code>referenceList</code>
     * <pre>    {1, 2, 4, 8, 9, 10, 11}</pre>
     * . It will generate the following encoding:
     * <pre>
     *     {      3, 4, 5, 8, 9,     11, 12, 13} List.
     *     {1, 2,    4,    8, 9, 10, 11        } Reference list.
     *     {0, 0,    1,    1, 1, 0,  1         } Matches. 0 to indicate no match and 1 to indicate match.
     *     4     1    2    0                     Number of blocks (consecutive bits values) minus one given that at
     *                                           there is one block and blocks sizes minus one, given that the blocks
     *                                           have at least one element.
     *     5     2    3    1                     Add 1 to ensure non zeros.
     *     101   01   11   1                     Binary representation.
     *     01101 0100 0101 1                     Delta encoding.
     * </pre>
     * @param list from which it will encode the difference with the closest reference. List must be strictly
     *             incremental with positives (including zero) values.
     * @param referenceList reference list from which the difference will be computed.
     * @return number of written bits.
     * @throws IOException when there is an exception writing into <code>out</code>.
     * @throws IllegalArgumentException when the list is not strictly incremental.
     */
    protected int writeDifference(
        final IntList list,
        final IntList referenceList
    ) throws IOException {
        int offset = 0;
        final IntIterator listIterator = list.listIterator();
        final IntIterator referenceListIterator = referenceList.listIterator();
        int listValue = listIterator.nextInt();
        int lastListValue;
        int referenceListValue = referenceListIterator.nextInt();
        int blockSize = 0;
        boolean pendingReference = false;
        boolean intersected = true;
        blocksBuffer.clear();
        while (true) {
            if (listValue == referenceListValue) {
                if (intersected) {
                    blockSize++;
                } else {
                    blocksBuffer.add(blockSize);
                    blockSize = 1;
                    intersected = true;
                }
                pendingReference = false;
                listIterator.remove();
                if (!listIterator.hasNext() || !referenceListIterator.hasNext()) {
                    break;
                }
                lastListValue = listValue;
                listValue = listIterator.nextInt();
                if (lastListValue >= listValue) {
                    throw new NotStrictlyIncrementalListException(lastListValue, listValue);
                }
                referenceListValue = referenceListIterator.nextInt();
                pendingReference = true;
            } else if (listValue < referenceListValue) {
                if (!listIterator.hasNext()) {
                    break;
                }
                lastListValue = listValue;
                listValue = listIterator.nextInt();
                if (lastListValue >= listValue) {
                    throw new NotStrictlyIncrementalListException(lastListValue, listValue);
                }
            } else {
                if (intersected) {
                    blocksBuffer.add(blockSize);
                    blockSize = 1;
                    intersected = false;
                } else {
                    blockSize++;
                }
                pendingReference = false;
                if (!referenceListIterator.hasNext()) {
                    break;
                }
                referenceListValue = referenceListIterator.nextInt();
                pendingReference = true;
            }
        }

        while (referenceListIterator.hasNext()) {
            if (intersected) {
                blocksBuffer.add(blockSize);
                blockSize = 1;
                intersected = false;
            } else {
                blockSize++;
            }
            referenceListIterator.next();
        }

        if (pendingReference && intersected) {
            blocksBuffer.add(blockSize);
            intersected = false;
        }

        if (!blocksBuffer.isEmpty() && blocksBuffer.get(0) == 0) {
            blocksBuffer.remove(0);
        }

        final boolean initialBit = (blocksBuffer.size() & 1) == 0 ? intersected : !intersected;
        offset += writeDelta(blocksBuffer.size());
        offset += writeBit(initialBit);
        final IntIterator blocksIterator = blocksBuffer.iterator();
        while (blocksIterator.hasNext()) {
            offset += writeDelta(blocksIterator.nextInt() - 1);
        }
        return offset;
    }

    /**
     * Writes the succinct intervals representation from strictly incremental <code>list</code>. Where an interval
     * is an incremental consecutive sequence of numbers.
     * <ul>
     *     <li>Number of intervals.</li>
     *     <li>
     *         For each interval:
     *         <ul>
     *             <li>
     *                 Store the value on the beginning of the interval minus (min interval size plus one).
     *                 Because in case is smaller, the element must belong to same interval. Only the first element
     *                 of the first interval is stored without any subtraction given there is not previous interval.
     *             </li>
     *             <li>
     *                 Store the length of the interval minus the min interval size.
     *             </li>
     *         </ul>
     *     </li>
     * </ul>
     *
     * For the example <code>list</code>
     * <pre>    {1, 3, 4, 5, 7, 8, 9, 10, 12, 14, 15, 16, 17, 19, 20, 21, 22, 23, 24, 30}</pre>
     * and <code>valueBitSize</code> = 3, it will generate the following encoding:
     * <pre>
     *     3 [7, 8, 9, 10] [14, 15, 16, 17] [19, 20, 21, 22, 23, 24] Intervals from list.
     *     3 [7, 10]       [14, 17]         [19, 24]                 Interval format.
     *     3 [7, 0]        [2,  0]          [0, 2]                   Interval delta format.
     *     4     7   1 3    1 1 3                                    Add 1 to ensure non zeros.
     *     001   111 1 11   1 1 11                                   Binary representation.
     *     3-00  111 1 2-1  1 1 2-1                                  Decimal Gamma Prefix and Binary Gamma Suffix.
     *     11-00 111 1 01-1 1 1 01-1                                 Binary Gamma Prefix and Binary Gamma Suffix.
     *     01100 111 1 0101 1 1 0101                                 Delta Encoding.
     * </pre>
     * @param list from which it will extracts the intervals to encode. List must be strictly incremental with
     *             positives (including zero) values.
     * @param valueBitSize fixed number of bits used to represent value in list to be encoded. It can be any value
     *                     between 1bit and 31 bits.
     * @return number of written bits.
     * @throws IOException when there is an exception writing into <code>out</code>.
     * @throws IllegalArgumentException when the list is not strictly incremental.
     */
    protected int writeIntervals(final IntList list, final int valueBitSize) throws IOException {
        int offset = 0;
        intervalsBuffer.clear();
        if (list.size() >= minIntervalSize) {
            IntIterator listIterator = list.iterator();
            int intervalInitialIndex = 0;
            int index = 1;
            int lastValue = listIterator.nextInt();
            int value;
            int deltaValue;
            intervalsBuffer.clear();
            while (listIterator.hasNext()) {
                value = listIterator.nextInt();
                deltaValue = value - lastValue;
                if (deltaValue <= 0) {
                    throw new NotStrictlyIncrementalListException(lastValue, value);
                } else if (deltaValue > 1) {
                    if (index - intervalInitialIndex >= minIntervalSize) {
                        intervalsBuffer.add(intervalInitialIndex);
                        intervalsBuffer.add(index - intervalInitialIndex);
                    }
                    intervalInitialIndex = index;
                }
                lastValue = value;
                index++;
            }
            if (index - intervalInitialIndex >= minIntervalSize) {
                intervalsBuffer.add(intervalInitialIndex);
                intervalsBuffer.add(index - intervalInitialIndex);
            }
            offset += writeDelta(intervalsBuffer.size() / 2);
            if (!intervalsBuffer.isEmpty()) {
                IntIterator intervalIterator = intervalsBuffer.iterator();
                intervalInitialIndex = intervalIterator.nextInt();
                index = 0;
                listIterator = list.iterator();
                boolean firstWrite = true;
                while (true) {
                    value = listIterator.nextInt();
                    if (index == intervalInitialIndex) {
                        listIterator.remove();
                        if (firstWrite) {
                            offset += writeInt(value, valueBitSize);
                            firstWrite = false;
                        } else {
                            deltaValue = value - lastValue - 2;
                            offset += writeDelta(deltaValue);
                        }
                        intervalInitialIndex = intervalIterator.nextInt();
                        offset += writeDelta(intervalInitialIndex - minIntervalSize);
                        while (--intervalInitialIndex > 0) {
                            value = listIterator.nextInt();
                            listIterator.remove();
                            index++;
                        }
                        lastValue = value;
                        if (!intervalIterator.hasNext()) {
                            break;
                        }
                        intervalInitialIndex = intervalIterator.nextInt();
                    }
                    index++;
                }
            }
        } else {
            offset += writeDelta(0);
        }
        return offset;
    }

    /**
     * Writes the succinct intervals representation from strictly incremental <code>list</code>. Where an interval
     * is an incremental consecutive sequence of numbers.
     * <ul>
     *     <li>Number of intervals.</li>
     *     <li>
     *         For each interval:
     *         <ul>
     *             <li>
     *                 Store the value on the beginning of the interval minus (min interval size plus one).
     *                 Because in case is smaller, the element must belong to same interval. Only the first element
     *                 of the first interval is stored without any subtraction given there is not previous interval.
     *             </li>
     *             <li>
     *                 Store the length of the interval minus the min interval size.
     *             </li>
     *         </ul>
     *     </li>
     * </ul>
     *
     * For the example <code>list</code>
     * <pre>    {1, 3, 4, 5, 7, 8, 9, 10, 12, 14, 15, 16, 17, 19, 20, 21, 22, 23, 24, 30}</pre>
     * and <code>valueBitSize</code> = 3, it will generate the following encoding:
     * <pre>
     *     3 [7, 8, 9, 10] [14, 15, 16, 17] [19, 20, 21, 22, 23, 24] Intervals from list.
     *     3 [7, 10]       [14, 17]         [19, 24]                 Interval format.
     *     3 [7, 0]        [2,  0]          [0, 2]                   Interval delta format.
     *     4     7   1 3    1 1 3                                    Add 1 to ensure non zeros.
     *     001   111 1 11   1 1 11                                   Binary representation.
     *     3-00  111 1 2-1  1 1 2-1                                  Decimal Gamma Prefix and Binary Gamma Suffix.
     *     11-00 111 1 01-1 1 1 01-1                                 Binary Gamma Prefix and Binary Gamma Suffix.
     *     01100 111 1 0101 1 1 0101                                 Delta Encoding.
     * </pre>
     * @param list from which it will extracts the intervals to encode. List must be strictly incremental with
     *             positives (including zero) values.
     * @param valueBitSize fixed number of bits used to represent value in list to be encoded. It can be any value
     *                     between 1bit and 63 bits.
     * @return number of written bits.
     * @throws IOException when there is an exception writing into <code>out</code>.
     * @throws IllegalArgumentException when the list is not strictly incremental.
     */
    protected int writeIntervals(final LongList list, final int valueBitSize) throws IOException {
        int offset = 0;
        intervalsBuffer.clear();
        if (list.size() >= minIntervalSize) {
            LongIterator listIterator = list.iterator();
            int intervalInitialIndex = 0;
            int index = 1;
            long lastValue = listIterator.nextLong();
            long value;
            long deltaValue;
            intervalsBuffer.clear();
            while (listIterator.hasNext()) {
                value = listIterator.nextLong();
                deltaValue = value - lastValue;
                if (deltaValue <= 0) {
                    throw new NotStrictlyIncrementalListException(lastValue, value);
                } else if (deltaValue > 1) {
                    if (index - intervalInitialIndex >= minIntervalSize) {
                        intervalsBuffer.add(intervalInitialIndex);
                        intervalsBuffer.add(index - intervalInitialIndex);
                    }
                    intervalInitialIndex = index;
                }
                lastValue = value;
                index++;
            }
            if (index - intervalInitialIndex >= minIntervalSize) {
                intervalsBuffer.add(intervalInitialIndex);
                intervalsBuffer.add(index - intervalInitialIndex);
            }
            offset += writeDelta(intervalsBuffer.size() / 2);
            if (!intervalsBuffer.isEmpty()) {
                IntIterator intervalIterator = intervalsBuffer.iterator();
                intervalInitialIndex = intervalIterator.nextInt();
                index = 0;
                listIterator = list.iterator();
                boolean firstWrite = true;
                while (true) {
                    value = listIterator.nextLong();
                    if (index == intervalInitialIndex) {
                        listIterator.remove();
                        if (firstWrite) {
                            offset += writeLong(value, valueBitSize);
                            firstWrite = false;
                        } else {
                            deltaValue = value - lastValue - 2;
                            if (deltaValue > Integer.MAX_VALUE) {
                                throw new DeltaValueIsTooBigException(lastValue, value);
                            }
                            offset += writeDelta((int) deltaValue);
                        }
                        intervalInitialIndex = intervalIterator.nextInt();
                        offset += writeDelta(intervalInitialIndex - minIntervalSize);
                        while (--intervalInitialIndex > 0) {
                            value = listIterator.nextLong();
                            listIterator.remove();
                            index++;
                        }
                        lastValue = value;
                        if (!intervalIterator.hasNext()) {
                            break;
                        }
                        intervalInitialIndex = intervalIterator.nextInt();
                    }
                    index++;
                }
            }
        } else {
            offset += writeDelta(0);
        }
        return offset;
    }

    /**
     * Writes the succinct delta representation for an strictly incremental <code>list</code>.
     * <ul>
     *     <li>List length.</li>
     *     <li>
     *         For each element in the list:
     *         <ul>
     *             <li>
     *                 Store the element minus previous value minus one. The first element is stored in its binary
     *                 representation given there is not previous value to subtract from.
     *             </li>
     *         </ul>
     *     </li>
     * </ul>
     *
     * For the example <code>list</code>
     * <pre>    {1, 2, 3, 5, 7, 10}</pre>
     * and <code>valueBitSize</code> = 1, it will generate the following encoding:
     * <pre>
     *      6     1 0 0 1    1    2    Delta list.
     *      7     1 1 1 2    2    3    Add 1 to ensure non zeros.
     *      111   1 1 1 01   01   11   Binary representation.
     *      3-11  1 1 1 2-0  2-0  2-1  Decimal Gamma Prefix and Binary Gamma Suffix.
     *      11-11 1 1 1 01-0 01-0 01-1 Binary Gamma Prefix and Binary Gamma Suffix.
     *      01111 1 1 1 0100 0100 0101 Delta Encoding.
     * </pre>
     * @param list to encode. List must be strictly incremental with positives (including zero) values.
     * @param valueBitSize fixed number of bits used to represent value in list to be encoded. It can be any value
     *                     between 1bit and 31 bits.
     * @return number of written bits.
     * @throws IOException when there is an exception writing into <code>out</code>.
     * @throws IllegalArgumentException when the list is not strictly incremental.
     */
    protected int writeDelta(final IntList list, final int valueBitSize) throws IOException {
        final IntIterator listIterator = list.iterator();
        int offset = writeDelta(list.size());
        if (listIterator.hasNext()) {
            int value;
            int deltaValue;
            int lastValue = listIterator.nextInt();
            offset += writeInt(lastValue, valueBitSize);
            while (listIterator.hasNext()) {
                value = listIterator.nextInt();
                deltaValue = value - lastValue - 1;
                if (deltaValue < 0) {
                    throw new NotStrictlyIncrementalListException(lastValue, value);
                }
                offset += writeDelta(deltaValue);
                lastValue = value;
            }
        }
        return offset;
    }

    /**
     * Writes the succinct delta representation for an strictly incremental <code>list</code>.
     * <ul>
     *     <li>List length.</li>
     *     <li>
     *         For each element in the list:
     *         <ul>
     *             <li>
     *                 Store the element minus previous value minus one. The first element is stored in its binary
     *                 representation given there is not previous value to subtract from.
     *             </li>
     *         </ul>
     *     </li>
     * </ul>
     *
     * For the example <code>list</code>
     * <pre>    {1, 2, 3, 5, 7, 10}</pre>
     * and <code>valueBitSize</code> = 1, it will generate the following encoding:
     * <pre>
     *      6     1 0 0 1    1    2    Delta list.
     *      7     1 1 1 2    2    3    Add 1 to ensure non zeros.
     *      111   1 1 1 01   01   11   Binary representation.
     *      3-11  1 1 1 2-0  2-0  2-1  Decimal Gamma Prefix and Binary Gamma Suffix.
     *      11-11 1 1 1 01-0 01-0 01-1 Binary Gamma Prefix and Binary Gamma Suffix.
     *      01111 1 1 1 0100 0100 0101 Delta Encoding.
     * </pre>
     * @param list to encode. List must be strictly incremental with positives (including zero) values.
     * @param valueBitSize fixed number of bits used to represent value in list to be encoded. It can be any value
     *                     between 1bit and 63 bits.
     * @return number of written bits.
     * @throws IOException when there is an exception writing into <code>out</code>.
     * @throws IllegalArgumentException when the list is not strictly incremental.
     */
    protected int writeDelta(final LongList list, final int valueBitSize) throws IOException {
        final LongIterator listIterator = list.iterator();
        int offset = writeDelta(list.size());
        if (listIterator.hasNext()) {
            long value;
            long deltaValue;
            long lastValue = listIterator.nextLong();
            offset += writeLong(lastValue, valueBitSize);
            while (listIterator.hasNext()) {
                value = listIterator.nextLong();
                deltaValue = value - lastValue - 1;
                if (deltaValue < 0) {
                    throw new NotStrictlyIncrementalListException(lastValue, value);
                } else if (deltaValue > Integer.MAX_VALUE) {
                    throw new DeltaValueIsTooBigException(lastValue, value);
                }
                offset += writeDelta((int) deltaValue);
                lastValue = value;
            }
        }
        return offset;
    }

    /**
     * Writes the compressed representation of an strictly incremental list with positive numbers, including zero.
     * <ul>
     *     <li>
     *         Writes the reference (see
     *         {@link #writeReference(it.unimi.dsi.fastutil.ints.IntList, int, int, org.pebble.core.encoding.ints.datastructures.IntReferenceListsStore) writeReference}).
     *     </li>
     *     <li>
     *         Writes the intervals from remaining list (see
     *         {@link #writeIntervals(it.unimi.dsi.fastutil.ints.IntList, int) writeIntervals}).
     *     </li>
     *     <li>
     *         Writes the deltas of remaining list (see
     *         {@link #writeDelta(it.unimi.dsi.fastutil.ints.IntList, int) writeDelta}).
     *     </li>
     * </ul>
     *
     * @param list list to be written. The list must be strictly incremental with positives (including zero) values.
     * @param listIndex offset of the given <code>list</code>.
     * @param valueBitSize fixed number of bits used to represent value in list to be encoded. It can be any value
     *                     between 1bit and 31 bits.
     * @param referenceListsStore store that contains all available references lists.
     * @return numbers of written bits.
     * @throws IOException when there is an exception writing into <code>out</code>.
     */
    public int writeStrictlyIncrementalList(
        final IntList list,
        final int listIndex,
        final int valueBitSize,
        final IntReferenceListsStore referenceListsStore
    ) throws IOException {
        int offset = writeReference(list, listIndex, valueBitSize, referenceListsStore);
        offset += writeIntervals(list, valueBitSize);
        offset += writeDelta(list, valueBitSize);
        return offset;
    }

    /**
     * Writes the compressed representation of an strictly incremental list with positive numbers, including zero.
     * <ul>
     *     <li>
     *         Writes the reference (see
     *         {@link #writeReference(it.unimi.dsi.fastutil.longs.LongList, int, int, org.pebble.core.encoding.longs.datastructures.LongReferenceListsStore) writeReference}).
     *     </li>
     *     <li>
     *         Writes the intervals from remaining list (see
     *         {@link #writeIntervals(it.unimi.dsi.fastutil.longs.LongList, int) writeIntervals}).
     *     </li>
     *     <li>
     *         Writes the deltas of remaining list (see
     *         {@link #writeDelta(it.unimi.dsi.fastutil.longs.LongList, int) writeDelta}).
     *     </li>
     * </ul>
     *
     * @param list list to be written. The list must be strictly incremental with positives (including zero) values.
     * @param listIndex offset of the given <code>list</code>.
     * @param valueBitSize fixed number of bits used to represent value in list to be encoded. It can be any value
     *                     between 1bit and 63 bits.
     * @param referenceListsStore store that contains all available references lists.
     * @return numbers of written bits.
     * @throws IOException when there is an exception writing into <code>out</code>.
     */
    public int writeStrictlyIncrementalList(
        final LongList list,
        final int listIndex,
        final int valueBitSize,
        final LongReferenceListsStore referenceListsStore
    ) throws IOException {
        int offset = writeReference(list, listIndex, valueBitSize, referenceListsStore);
        offset += writeIntervals(list, valueBitSize);
        offset += writeDelta(list, valueBitSize);
        return offset;
    }

    /**
     * Writes the compressed representation of an incremental list with positive numbers, including zero.
     * <ul>
     *     <li>
     *         Writes the repetitions (see
     *         {@link #writeRepetitions(it.unimi.dsi.fastutil.ints.IntList) writeRepetitions}).
     *     </li>
     *     <li>
     *         Writes the reference (see
     *         {@link #writeReference(it.unimi.dsi.fastutil.ints.IntList, int, int, org.pebble.core.encoding.ints.datastructures.IntReferenceListsStore) writeReference}).
     *     </li>
     *     <li>
     *         Writes the intervals from remaining list (see
     *         {@link #writeIntervals(it.unimi.dsi.fastutil.ints.IntList, int) writeIntervals}).
     *     </li>
     *     <li>
     *         Writes the deltas of remaining list (see
     *         {@link #writeDelta(it.unimi.dsi.fastutil.ints.IntList, int) writeDelta}).
     *     </li>
     * </ul>
     *
     * @param list to be written. The list must be an incremental with positives (including zero) values.
     * @param listIndex offset of the given <code>list</code>.
     * @param valueBitSize fixed number of bits used to represent value in list to be encoded. It can be any value
     *                     between 1bit and 31 bits.
     * @param referenceListsStore store that contains all available references lists.
     * @return numbers of written bits.
     * @throws IOException when there is an exception writing into <code>out</code>.
     */
    public int writeIncrementalList(
        final IntList list,
        final int listIndex,
        final int valueBitSize,
        final IntReferenceListsStore referenceListsStore
    ) throws IOException {
        int offset = writeRepetitions(list);
        offset += writeReference(list, listIndex, valueBitSize, referenceListsStore);
        offset += writeIntervals(list, valueBitSize);
        offset += writeDelta(list, valueBitSize);
        return offset;
    }

    /**
     * Writes the compressed representation of an incremental list with positive numbers, including zero.
     * <ul>
     *     <li>
     *         Writes the repetitions (see
     *         {@link #writeRepetitions(it.unimi.dsi.fastutil.longs.LongList) writeRepetitions}).
     *     </li>
     *     <li>
     *         Writes the reference (see
     *         {@link #writeReference(it.unimi.dsi.fastutil.longs.LongList, int, int, org.pebble.core.encoding.longs.datastructures.LongReferenceListsStore) writeReference}).
     *     </li>
     *     <li>
     *         Writes the intervals from remaining list (see
     *         {@link #writeIntervals(it.unimi.dsi.fastutil.longs.LongList, int) writeIntervals}).
     *     </li>
     *     <li>
     *         Writes the deltas of remaining list (see
     *         {@link #writeDelta(it.unimi.dsi.fastutil.longs.LongList, int) writeDelta}).
     *     </li>
     * </ul>
     *
     * @param list to be written. The list must be an incremental with positives (including zero) values.
     * @param listIndex offset of the given <code>list</code>.
     * @param valueBitSize fixed number of bits used to represent value in list to be encoded. It can be any value
     *                     between 1bit and 63 bits.
     * @param referenceListsStore store that contains all available references lists.
     * @return numbers of written bits.
     * @throws IOException when there is an exception writing into <code>out</code>.
     */
    public int writeIncrementalList(
        final LongList list,
        final int listIndex,
        final int valueBitSize,
        final LongReferenceListsStore referenceListsStore
    ) throws IOException {
        int offset = writeRepetitions(list);
        offset += writeReference(list, listIndex, valueBitSize, referenceListsStore);
        offset += writeIntervals(list, valueBitSize);
        offset += writeDelta(list, valueBitSize);
        return offset;
    }

    /**
     * Writes the compressed representation of a list with positive numbers, including zero.
     * <ul>
     *     <li>
     *         Writes values in lists as strictly incremental list (see
     *         {@link #writeStrictlyIncrementalList(it.unimi.dsi.fastutil.ints.IntList, int, int, org.pebble.core.encoding.ints.datastructures.IntReferenceListsStore) writeStrictlyIncrementalList}).
     *     </li>
     *     <li>
     *         Replaces the values from original list, for its respective index on the previous list of values.
     *     </li>
     *     <li>
     *         Writes using delta encoding the difference between the size of the list of values and the size of
     *         list of indexes.
     *     </li>
     *     <li>
     *         For each index on the list of indexes:
     *         <ul>
     *             <li>
     *                 Computes the difference between current value and previous one by exception of the first element.
     *             </li>
     *             <li>
     *                 In case the previous difference &Delta; is positive, writes the difference as:
     *                 2&#8901;&Delta;. In case &Delta; is negative, writes the difference as:
     *                 2&#8901;|&Delta;| - 1.
     *             </li>
     *         </ul>
     *     </li>
     * </ul>
     * For the example <code>list</code>
     * <pre>    {13, 13, 5, 8, 5, 8, 12, 13, 12, 12}</pre>
     * , it will generate the following strictly incremental list containing each value of original list as:
     * <pre>    {5, 8, 12, 13}</pre>
     * . And will generate the following list of indexes:
     * <pre>    {3, 3, 0, 1, 0, 1, 2, 3, 2, 2}</pre>
     * . It will generate the following encoding:
     * <pre>
     *     (10, 4)  3     3 0     1    0    1    2    3    2    2 &lt;strictly incremental list encoding&gt; Indexes list.
     *     7        6     0 5     2    1    2    2    2    1    0 &lt;strictly incremental list encoding&gt; Delta encoding.
     *     8        7     1 6     3    2    3    3    3    2    1 &lt;strictly incremental list encoding&gt; Add 1 to ensure non zeros.
     *     1000     111   1 110   11   10   11   11   11   10   1 &lt;strictly incremental list encoding&gt; Binary representation.
     *     00100000 01111 1 01110 0101 0100 0101 0101 0101 0100 1 &lt;strictly incremental list encoding&gt; Delta encoding.
     * </pre>
     *
     * @param list to be written. The list must be contains positives (including zero) values.
     * @param listIndex offset of the given <code>list</code>.
     * @param valueBitSize fixed number of bits used to represent value in list to be encoded. It can be any value
     *                     between 1bit and 31 bits.
     * @param referenceListsStore store that contains all available references lists.
     * @return numbers of written bits.
     * @throws IOException when there is an exception writing into <code>out</code>.
     */
    public int writeList(
        final IntList list,
        final int listIndex,
        final int valueBitSize,
        final IntReferenceListsStore referenceListsStore
    ) throws IOException {
        final int listSize = list.size();
        final IntSortedSet values = new IntRBTreeSet(list);
        int offset = writeStrictlyIncrementalList(
            new IntArrayList(values),
            listIndex,
            valueBitSize,
            referenceListsStore
        );
        final Int2IntMap valuesIndex = new Int2IntRBTreeMap();
        final IntIterator valuesIterator = values.iterator();
        int index = 0;
        while (valuesIterator.hasNext()) {
            valuesIndex.put(valuesIterator.nextInt(), index++);
        }
        offset += writeDelta(listSize - values.size());
        final IntIterator listIterator = list.iterator();
        int lastIndex = 0;
        while (listIterator.hasNext()) {
            index = valuesIndex.get(listIterator.nextInt());
            if (lastIndex <= index) {
                offset += writeDelta(2 * (index - lastIndex));
            } else {
                offset += writeDelta(2 * (lastIndex - index) - 1);
            }
            lastIndex = index;
        }
        return offset;
    }

    /**
     * Writes the compressed representation of a list with positive numbers, including zero.
     * <ul>
     *     <li>
     *         Writes values in lists as strictly incremental list (see
     *         {@link #writeStrictlyIncrementalList(it.unimi.dsi.fastutil.longs.LongList, int, int, org.pebble.core.encoding.longs.datastructures.LongReferenceListsStore) writeStrictlyIncrementalList}).
     *     </li>
     *     <li>
     *         Replaces the values from original list, for its respective index on the previous list of values.
     *     </li>
     *     <li>
     *         Writes using delta encoding the difference between the size of the list of values and the size of
     *         list of indexes.
     *     </li>
     *     <li>
     *         For each index on the list of indexes:
     *         <ul>
     *             <li>
     *                 Computes the difference between current value and previous one by exception of the first element.
     *             </li>
     *             <li>
     *                 In case the previous difference &Delta; is positive, writes the difference as:
     *                 2&#8901;&Delta;. In case &Delta; is negative, writes the difference as:
     *                 2&#8901;|&Delta;| - 1.
     *             </li>
     *         </ul>
     *     </li>
     * </ul>
     * For the example <code>list</code>
     * <pre>    {13, 13, 5, 8, 5, 8, 12, 13, 12, 12}</pre>
     * , it will generate the following strictly incremental list containing each value of original list as:
     * <pre>    {5, 8, 12, 13}</pre>
     * . And will generate the following list of indexes:
     * <pre>    {3, 3, 0, 1, 0, 1, 2, 3, 2, 2}</pre>
     * . It will generate the following encoding:
     * <pre>
     *     (10, 4)  3     3 0     1    0    1    2    3    2    2 &lt;strictly incremental list encoding&gt; Indexes list.
     *     7        6     0 5     2    1    2    2    2    1    0 &lt;strictly incremental list encoding&gt; Delta encoding.
     *     8        7     1 6     3    2    3    3    3    2    1 &lt;strictly incremental list encoding&gt; Add 1 to ensure non zeros.
     *     1000     111   1 110   11   10   11   11   11   10   1 &lt;strictly incremental list encoding&gt; Binary representation.
     *     00100000 01111 1 01110 0101 0100 0101 0101 0101 0100 1 &lt;strictly incremental list encoding&gt; Delta encoding.
     * </pre>
     *
     * @param list to be written. The list must be contains positives (including zero) values.
     * @param listIndex offset of the given <code>list</code>.
     * @param valueBitSize fixed number of bits used to represent value in list to be encoded. It can be any value
     *                     between 1bit and 63 bits.
     * @param referenceListsStore store that contains all available references lists.
     * @return numbers of written bits.
     * @throws IOException when there is an exception writing into <code>out</code>.
     */
    public int writeList(
        final LongList list,
        final int listIndex,
        final int valueBitSize,
        final LongReferenceListsStore referenceListsStore
    ) throws IOException {
        final int listSize = list.size();
        final LongSortedSet values = new LongRBTreeSet(list);
        int offset = writeStrictlyIncrementalList(
            new LongArrayList(values),
            listIndex,
            valueBitSize,
            referenceListsStore
        );
        final Long2IntMap valuesIndex = new Long2IntRBTreeMap();
        final LongIterator valuesIterator = values.iterator();
        int index = 0;
        while (valuesIterator.hasNext()) {
            valuesIndex.put(valuesIterator.nextLong(), index++);
        }
        offset += writeDelta(listSize - values.size());
        final LongIterator listIterator = list.iterator();
        int lastIndex = 0;
        while (listIterator.hasNext()) {
            index = valuesIndex.get(listIterator.nextLong());
            if (lastIndex <= index) {
                offset += writeDelta(2 * (index - lastIndex));
            } else {
                offset += writeDelta(2 * (lastIndex - index) - 1);
            }
            lastIndex = index;
        }
        return offset;
    }

    public int writeGenericList(
        final IntList list,
        final int listIndex,
        final int valueBitSize,
        final IntReferenceListsStore referenceListsStore
    ) throws IOException {
        switch (ListsClassifier.classify(list)) {
            case ListsClassifier.SORTED_SET_LIST:
                writeInt(ListsClassifier.SORTED_SET_LIST, 2);
                return 2 + writeStrictlyIncrementalList(list, listIndex, valueBitSize, referenceListsStore);
            case ListsClassifier.SORTED_LIST:
                writeInt(ListsClassifier.SORTED_LIST, 2);
                return 2 + writeIncrementalList(list, listIndex, valueBitSize, referenceListsStore);
            default:
                writeInt(ListsClassifier.UNSORTED_LIST, 2);
                return 2 + writeList(list, listIndex, valueBitSize, referenceListsStore);
        }
    }

    public int writeGenericList(
        final LongList list,
        final int listIndex,
        final int valueBitSize,
        final LongReferenceListsStore referenceListsStore
    ) throws IOException {
        switch (ListsClassifier.classify(list)) {
            case ListsClassifier.SORTED_SET_LIST:
                writeInt(ListsClassifier.SORTED_SET_LIST, 2);
                return 2 + writeStrictlyIncrementalList(list, listIndex, valueBitSize, referenceListsStore);
            case ListsClassifier.SORTED_LIST:
                writeInt(ListsClassifier.SORTED_LIST, 2);
                return 2 + writeIncrementalList(list, listIndex, valueBitSize, referenceListsStore);
            default:
                writeInt(ListsClassifier.UNSORTED_LIST, 2);
                return 2 + writeList(list, listIndex, valueBitSize, referenceListsStore);
        }
    }

}
