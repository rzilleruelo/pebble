package org.pebble.core.decoding.iterators.small;

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

import org.pebble.UnitTest;
import org.pebble.core.encoding.DefaultParametersValues;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.pebble.core.decoding.iterators.small.Helper.getInput;
import static org.junit.Assert.assertEquals;

@Category(UnitTest.class)
public class DeltaIteratorTest {

    @Test
    public void whenThereIsAnEncodedNonEmptyListItShouldRecoverOriginalListSuccessfully() throws Exception {
        Helper.Input input = getInput("01111 00000000000000000000000000000001 1 1 0100 0100 0101");
        final IntList expectedList = new IntArrayList(new int[] {1, 2, 3, 5, 7, 10});
        final IntList list = new IntArrayList();

        DeltaIterator deltaIterator = new DeltaIterator(DefaultParametersValues.INT_BITS, input.stream);
        while (deltaIterator.hasNext()) {
            list.add(deltaIterator.next());
        }

        assertEquals(expectedList, list);
    }

    @Test
    public void whenThereIsAnEncodedEmptyListItShouldRecoverOriginalListSuccessfully() throws Exception {
        Helper.Input input = getInput("1");
        final IntList expectedList = new IntArrayList(new int[] {});
        final IntList list = new IntArrayList();

        DeltaIterator deltaIterator = new DeltaIterator(DefaultParametersValues.INT_BITS, input.stream);
        while (deltaIterator.hasNext()) {
            list.add(deltaIterator.next());
        }

        assertEquals(expectedList, list);
    }

}
