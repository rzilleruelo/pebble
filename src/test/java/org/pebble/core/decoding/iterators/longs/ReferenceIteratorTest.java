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

package org.pebble.core.decoding.iterators.longs;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.io.InputBitStream;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.pebble.UnitTest;
import org.pebble.core.PebbleOffsetsStore;
import org.pebble.core.decoding.iterators.Helper;
import org.pebble.core.encoding.DefaultParametersValues;
import org.pebble.utils.BytesArrayPebbleBytesStore;
import org.pebble.utils.LongListPebbleOffsetsStore;

import java.io.IOException;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.pebble.core.decoding.iterators.Helper.getInput;

@Category(UnitTest.class)
public class ReferenceIteratorTest {

    private static class ReferenceIteratorBuilder {

        private final int listIndex;
        private final Helper.Input input;
        private final LongIterator iterator;

        public ReferenceIteratorBuilder(final Helper.Input input, final int listIndex, final LongList referenceList) {
            this.listIndex = listIndex;
            this.input = input;
            this.iterator = referenceList.iterator();
        }

        public ReferenceIterator build() throws IOException {
            final PebbleOffsetsStore offsetsStore = new LongListPebbleOffsetsStore(new long[] {0L, 0L});
            return new ReferenceIterator(
                listIndex,
                DefaultParametersValues.LONG_BITS,
                DefaultParametersValues.DEFAULT_MIN_INTERVAL_SIZE,
                input.stream,
                new BytesArrayPebbleBytesStore(input.buffer, offsetsStore)
            ) {
                @Override
                public LongIterator getReferenceListIterator(
                    final int listIndex,
                    final InputBitStream inputBitStream
                ) throws IOException {
                    return iterator;
                }
            };
        }
    }

    @Test
    public void whenThereIsAReferenceListItShouldRecoverListSegmentOnReferenceListSuccessfully() throws Exception {
        final Helper.Input input = getInput("0100 0101 0 1 0101");
        final int listIndex = 1;
        final LongList referenceList = new LongArrayList(new long[] {0L, 2L, 3L, 5L, 9L, 12L, 13L});
        final LongList expectedList = new LongArrayList(new long[] {2L, 3L, 5L});
        final LongList list = new LongArrayList();

        ReferenceIterator referenceIterator = new ReferenceIteratorBuilder(input, listIndex, referenceList).build();
        while (referenceIterator.hasNext()) {
            list.add(referenceIterator.next());
        }

        assertEquals(expectedList, list);
    }

    @Test
    public void whenThereNoAReferenceListItShouldRecoverEmptyListSuccessfully() throws Exception {
        final Helper.Input input = getInput("1");
        final int listIndex = 1;
        final LongList referenceList = new LongArrayList();

        ReferenceIterator referenceIterator = new ReferenceIteratorBuilder(input, listIndex, referenceList).build();

        assertFalse(referenceIterator.hasNext());
    }

    @Test
    public void whenReferenceListFirstElementsMatchesWithListShouldRecoverListSegmentOnReferenceListSuccessfully()
        throws Exception
    {
        final Helper.Input input = getInput("0100 0100 1 01100");
        final int listIndex = 1;
        final LongList referenceList = new LongArrayList(new long[] {1L, 2L, 3L, 5L, 9L, 12L, 13L});
        final LongList expectedList = new LongArrayList(new long[] {1L, 2L, 3L, 5L});
        final LongList list = new LongArrayList();

        ReferenceIterator referenceIterator = new ReferenceIteratorBuilder(input, listIndex, referenceList).build();
        while (referenceIterator.hasNext()) {
            list.add(referenceIterator.next());
        }

        assertEquals(expectedList, list);
    }

    @Test
    public void whenReferenceListLastElementsMatchesWithListShouldRecoverListSegmentOnReferenceListSuccessfully()
        throws Exception
    {
        final Helper.Input input = getInput("0100 01100 0 1 0101 1");
        final int listIndex = 1;
        final LongList referenceList = new LongArrayList(new long[] {0L, 2L, 3L, 5L, 9L, 12L, 13L});
        final LongList expectedList = new LongArrayList(new long[] {2L, 3L, 5L, 12L, 13L});
        final LongList list = new LongArrayList();

        ReferenceIterator referenceIterator = new ReferenceIteratorBuilder(input, listIndex, referenceList).build();
        while (referenceIterator.hasNext()) {
            list.add(referenceIterator.next());
        }

        assertEquals(expectedList, list);
    }

}
