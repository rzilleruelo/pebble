package org.pebble.core.encoding.longs;

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

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongList;
import org.pebble.core.encoding.DefaultParametersValues;
import org.pebble.core.encoding.commons.OutputOffset;

/**
 * Implements methods for the estimation of size in bits of the compression algorithms. This class is useful
 * to decide which strategy are convenient to follow to compress a list without actually compressing it,
 * saving computation time.
 */
public class LongOutputOffset extends OutputOffset {

    /**
     * Initializes instance of {@link LongOutputOffset}.
     */
    public LongOutputOffset() {
        super();
    }

    /**
     * Estimates the number of bits required for the succinct difference representation between the given strictly
     * incremental <code>list</code> and <code>referenceList</code>. For details of the representation (see
     * {@link org.pebble.core.encoding.OutputSuccinctStream#writeDifference(it.unimi.dsi.fastutil.longs.LongList, it.unimi.dsi.fastutil.longs.LongList) writeDifference}).
     * @param list from which it will encode the difference with the closest reference.
     * @param referenceList reference list from which the difference will be computed. List must be strictly incremental
     *                      with positives (including zero) values.
     * @return number of representation bits.
     */
    public int getWriteDifferenceOffset(final LongList list, final LongList referenceList) {
        int offset = 0;
        final LongIterator listIterator = list.listIterator();
        final LongIterator referenceListIterator = referenceList.listIterator();
        long listValue = listIterator.nextLong();
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
                listValue = listIterator.nextLong();
                referenceListValue = referenceListIterator.nextLong();
                pendingReference = true;
            } else if (listValue < referenceListValue) {
                if (!listIterator.hasNext()) {
                    break;
                }
                listValue = listIterator.nextLong();
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
        offset += getWriteDeltaOffset(blocksBuffer.size());
        offset += writeBitOffset(initialBit);
        final IntIterator blocksIterator = blocksBuffer.iterator();
        while (blocksIterator.hasNext()) {
            offset += getWriteDeltaOffset(blocksIterator.nextInt() - 1);
        }
        return offset;
    }

    /**
     * Estimates the number of bits required for the succinct intervals representation from strictly incremental
     * <code>list</code>. For details of the representation (see
     * {@link org.pebble.core.encoding.OutputSuccinctStream#writeIntervals(it.unimi.dsi.fastutil.longs.LongList, int) writeIntervals}).
     * @param list from which it will extracts the intervals to encode. List must be strictly incremental with
     *             positives (including zero) values.
     * @param valueBitSize fixed number of bits used to represent value in list to be encoded. It can be any value
     *                     between 1bit and 63 bits.
     * @return number of representation bits.
     */
    public int getWriteIntervalsOffset(final LongList list, final int valueBitSize) {
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
                if (deltaValue > 1) {
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
            offset += getWriteDeltaOffset(intervalsBuffer.size() / 2);
            if (!intervalsBuffer.isEmpty()) {
                IntIterator intervalIterator = intervalsBuffer.iterator();
                intervalInitialIndex = intervalIterator.nextInt();
                index = 0;
                listIterator = list.iterator();
                boolean firstWrite = true;
                while(true) {
                    value = listIterator.nextLong();
                    if (index == intervalInitialIndex) {
                        listIterator.remove();
                        if (firstWrite) {
                            offset += writeLongOffset(value, valueBitSize);
                            firstWrite = false;
                        } else {
                            deltaValue = value - lastValue - 2;
                            offset += getWriteDeltaOffset(deltaValue);
                        }
                        intervalInitialIndex = intervalIterator.nextInt();
                        offset += getWriteDeltaOffset(intervalInitialIndex - minIntervalSize);
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
            offset += getWriteDeltaOffset(0);
        }
        return offset;
    }

    /**
     * Estimates the number of bits required for the succinct delta representation from strictly incremental
     * <code>list</code>. For details of the representation (see
     * {@link org.pebble.core.encoding.OutputSuccinctStream#writeDelta(it.unimi.dsi.fastutil.longs.LongList, int) writeDelta}).
     * @param list to encode. List must be strictly incremental with positives (including zero) values.
     * @param valueBitSize fixed number of bits used to represent value in list to be encoded. It can be any value
     *                     between 1bit and 63 bits.
     * @return number of representation bits.
     */
    public int getWriteDeltaOffset(final LongList list, final int valueBitSize) {
        final LongIterator listIterator = list.iterator();
        int offset = getWriteDeltaOffset(list.size());
        if (listIterator.hasNext()) {
            long value;
            long deltaValue;
            long lastValue = listIterator.nextLong();
            offset += writeLongOffset(lastValue, valueBitSize);
            while (listIterator.hasNext()) {
                value = listIterator.nextLong();
                deltaValue = value - lastValue - 1;
                offset += getWriteDeltaOffset(deltaValue);
                lastValue = value;
            }
        }
        return offset;
    }

    /**
     * Estimates the number of bits required for the delta encoding of <code>x</code>.
     * @param x positive number (including zero).
     * @return number of bits required to represent <code>x</code> in delta encoding.
     */
    public static int getWriteDeltaOffset(final long x) {
        if (x < DELTA_OFFSET_X.length) {
            return DELTA_OFFSET_X[(int) x];
        }
        long log2x = lower_bound_log2(x + 1);
        return (int) (log2x + 2 * lower_bound_log2(log2x + 1) + 1);
    }

    private static int writeLongOffset(final long x, final int valueBitSize) {
        return valueBitSize;
    }

    private static long lower_bound_log2(long x) {
        return DefaultParametersValues.LONG_BITS - Long.numberOfLeadingZeros(x);
    }

}
