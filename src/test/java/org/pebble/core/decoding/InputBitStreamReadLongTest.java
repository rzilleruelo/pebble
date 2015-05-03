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

package org.pebble.core.decoding;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.pebble.UnitTest;
import org.pebble.core.decoding.iterators.Helper;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.pebble.core.decoding.iterators.Helper.getInput;

@Category(UnitTest.class)
public class InputBitStreamReadLongTest {

    @Test
    public void itShouldReadExpectedValues() throws IOException {
        final LongList expectedValues = new LongArrayList(new long[] {19L, Long.MAX_VALUE, -1});
        final int[] sizes = new int[] {5, 63, 64};
        final Helper.Input input = getInput(
            "10011" +
            "111111111111111111111111111111111111111111111111111111111111111" +
            "1111111111111111111111111111111111111111111111111111111111111111"
        );
        final InputBitStream inputStream = new InputBitStream(input.buffer);
        final LongList values = new LongArrayList();

        for (int i = 0; i < sizes.length; i++) {
            values.add(inputStream.readLong(sizes[i]));
        }

        assertEquals(expectedValues, values);
    }

}
