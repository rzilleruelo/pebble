package org.pebble.core.decoding.iterators.ints;

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

import it.unimi.dsi.io.InputBitStream;
import org.pebble.UnitTest;
import org.pebble.core.encoding.DefaultParametersValues;
import org.pebble.utils.decoding.BytesArrayPebbleBytesStore;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;

import static org.pebble.core.decoding.iterators.ints.Helper.getInput;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;

@Category(UnitTest.class)
public class ReferenceIteratorTest {

    private static class ReferenceIteratorBuilder {

        private final int listIndex;
        private final Helper.Input input;
        private final IntIterator iterator;

        public ReferenceIteratorBuilder(final Helper.Input input, final int listIndex, final IntList referenceList) {
            this.listIndex = listIndex;
            this.input = input;
            this.iterator = referenceList.iterator();
        }

        public ReferenceIterator build() throws IOException {
            return new ReferenceIterator(
                listIndex,
                DefaultParametersValues.INT_BITS,
                DefaultParametersValues.DEFAULT_MIN_INTERVAL_SIZE,
                input.stream,
                new BytesArrayPebbleBytesStore(input.buffer, new long[] {0l, 0l})
            ) {
                @Override
                public IntIterator getReferenceListIterator(
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
        final IntList referenceList = new IntArrayList(new int[] {0, 2, 3, 5, 9, 12, 13});
        final IntList expectedList = new IntArrayList(new int[] {2, 3, 5});
        final IntList list = new IntArrayList();

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
        final IntList referenceList = new IntArrayList();

        ReferenceIterator referenceIterator = new ReferenceIteratorBuilder(input, listIndex, referenceList).build();

        assertFalse(referenceIterator.hasNext());
    }

    @Test
    public void whenReferenceListFirstElementsMatchesWithListShouldRecoverListSegmentOnReferenceListSuccessfully()
        throws Exception
    {
        final Helper.Input input = getInput("0100 0100 1 01100");
        final int listIndex = 1;
        final IntList referenceList = new IntArrayList(new int[] {1, 2, 3, 5, 9, 12, 13});
        final IntList expectedList = new IntArrayList(new int[] {1, 2, 3, 5});
        final IntList list = new IntArrayList();

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
        final IntList referenceList = new IntArrayList(new int[] {0, 2, 3, 5, 9, 12, 13});
        final IntList expectedList = new IntArrayList(new int[] {2, 3, 5, 12, 13});
        final IntList list = new IntArrayList();

        ReferenceIterator referenceIterator = new ReferenceIteratorBuilder(input, listIndex, referenceList).build();
        while (referenceIterator.hasNext()) {
            list.add(referenceIterator.next());
        }

        assertEquals(expectedList, list);
    }

}
