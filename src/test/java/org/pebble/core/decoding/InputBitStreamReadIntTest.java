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

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.pebble.UnitTest;
import org.pebble.core.decoding.iterators.Helper;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.pebble.core.decoding.iterators.Helper.getInput;

@Category(UnitTest.class)
public class InputBitStreamReadIntTest {

    @Test
    public void itShouldReadExpectedValues() throws IOException {
        final IntList expectedValues = new IntArrayList(new int[] {19, Integer.MAX_VALUE, -1});
        final int[] sizes = new int[] {5, 31, 32};
        final Helper.Input input = getInput("10011 1111111111111111111111111111111 11111111111111111111111111111111");
        final InputBitStream inputStream = new InputBitStream(input.buffer);
        final IntList values = new IntArrayList();

        for (int i = 0; i < sizes.length; i++) {
            values.add(inputStream.readInt(sizes[i]));
        }

        assertEquals(expectedValues, values);
    }

}
