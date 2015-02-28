package org.pebble.core.decoding.iterators.longs;

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

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.pebble.UnitTest;
import org.pebble.core.decoding.iterators.Helper;
import org.pebble.core.encoding.DefaultParametersValues;

import static org.junit.Assert.assertEquals;
import static org.pebble.core.decoding.iterators.Helper.getInput;

@Category(UnitTest.class)
public class DeltaIteratorTest {

    @Test
    public void whenThereIsAnEncodedNonEmptyListItShouldRecoverOriginalListSuccessfully() throws Exception {
        Helper.Input input = getInput(
            "01111 000000000000000000000000000000000000000000000000000000000000001 1 1 0100 0100 0101"
        );
        final LongList expectedList = new LongArrayList(new long[] {1L, 2L, 3L, 5L, 7L, 10L});
        final LongList list = new LongArrayList();

        DeltaIterator deltaIterator = new DeltaIterator(DefaultParametersValues.LONG_BITS, input.stream);
        while (deltaIterator.hasNext()) {
            list.add(deltaIterator.next());
        }

        assertEquals(expectedList, list);
    }

    @Test
    public void whenThereIsAnEncodedEmptyListItShouldRecoverOriginalListSuccessfully() throws Exception {
        Helper.Input input = getInput("1");
        final LongList expectedList = new LongArrayList(new long[] {});
        final LongList list = new LongArrayList();

        DeltaIterator deltaIterator = new DeltaIterator(DefaultParametersValues.LONG_BITS, input.stream);
        while (deltaIterator.hasNext()) {
            list.add(deltaIterator.next());
        }

        assertEquals(expectedList, list);
    }

}
