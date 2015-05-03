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

package org.pebble.utils;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.pebble.UnitTest;
import org.pebble.core.PebbleOffsetsStore;
import org.pebble.core.decoding.iterators.Helper;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.pebble.core.decoding.iterators.Helper.getInput;

@Category(UnitTest.class)
public class BytesArrayPebbleBytesStoreTest {

    @Test
    public void getShouldReturnExpectedByteArray() throws IOException {
        final Helper.Input input = getInput(
            "1 1 01101 00101 0101 01100 1 01101 01101 0100 0100 01101 0101 01110 01111 01100" +
            "0100 1 1 1 1 01111 01111 1 01110 0101 0100 0101 0101 0101 0100 1"
        );
        final PebbleOffsetsStore offsetsStore = new LongListPebbleOffsetsStore(new long[] {0L, 64L});
        final BytesArrayPebbleBytesStore pebbleBytesStore = new BytesArrayPebbleBytesStore(input.buffer, offsetsStore);

        assertSame(pebbleBytesStore.get(0), input.buffer);
    }

    @Test
    public void offsetShouldReturnExpectedOffsetFromListIndex() throws IOException {
        final Helper.Input input = getInput(
            "1 1 01101 00101 0101 01100 1 01101 01101 0100 0100 01101 0101 01110 01111 01100" +
            "0100 1 1 1 1 01111 01111 1 01110 0101 0100 0101 0101 0101 0100 1"
        );
        final PebbleOffsetsStore offsetsStore = new LongListPebbleOffsetsStore(new long[] {0L, 64L});
        final BytesArrayPebbleBytesStore pebbleBytesStore = new BytesArrayPebbleBytesStore(input.buffer, offsetsStore);

        assertEquals(64L, pebbleBytesStore.offset(1));
    }

}
