package org.pebble.core.decoding.iterators.ints;

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
import it.unimi.dsi.fastutil.ints.IntList;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.pebble.UnitTest;
import org.pebble.core.decoding.iterators.Helper;
import org.pebble.core.encoding.DefaultParametersValues;

import static org.junit.Assert.assertEquals;
import static org.pebble.core.decoding.iterators.Helper.getInput;

@Category(UnitTest.class)
public class IntervalIteratorTest {

    @Test
    public void whenThereIsAnEncodedNonEmptyIntervalsListItShouldRecoverOriginalIntervalsSuccessfully()
        throws Exception
    {
        Helper.Input input = getInput("01100 0000000000000000000000000000111 1 0101 1 1 0101");
        final IntList expectedList = new IntArrayList(new int[] {7, 8, 9, 10, 14, 15, 16, 17, 19, 20, 21, 22, 23, 24});
        final IntList list = new IntArrayList();

        IntervalIterator intervalIterator = new IntervalIterator(
            DefaultParametersValues.INT_BITS,
            DefaultParametersValues.DEFAULT_MIN_INTERVAL_SIZE,
            input.stream
        );
        while (intervalIterator.hasNext()) {
            list.add(intervalIterator.next());
        }

        assertEquals(expectedList, list);
    }

    @Test
    public void whenThereIsAnEncodedEmptyIntervalsListItShouldRecoverOriginalIntervalsSuccessfully() throws Exception {
        Helper.Input input = getInput("1");
        final IntList expectedList = new IntArrayList(new int[] {});
        final IntList list = new IntArrayList();

        IntervalIterator intervalIterator = new IntervalIterator(
            DefaultParametersValues.INT_BITS,
            DefaultParametersValues.DEFAULT_MIN_INTERVAL_SIZE,
            input.stream
        );
        while (intervalIterator.hasNext()) {
            list.add(intervalIterator.next());
        }

        assertEquals(expectedList, list);
    }

}
