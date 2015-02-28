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

import org.pebble.UnitTest;
import org.pebble.utils.decoding.BytesArrayPebbleBytesStore;
import org.pebble.core.decoding.PebbleBytesStore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;

import java.io.IOException;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

@Category(UnitTest.class)
public class IncrementalListIteratorTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void whenNonRemainingElementsHasNextItShouldReturnFalse() throws IOException {
        final Helper.Input input = Helper.getInput("1 1 1 1");
        final IncrementalListIterator iterator = buildIncrementalIterator(input);

        assertFalse(iterator.hasNext());
    }

    @Test
    public void whenRemainingElementsHasNextItShouldReturnTrue() throws IOException {
        final Helper.Input input = Helper.getInput("1 1 1 0100 11");
        final IncrementalListIterator iterator = buildIncrementalIterator(input);

        assertTrue(iterator.hasNext());
    }

    @Test
    public void whenNonRemainingElementsNextIntItShouldReturnMinusOne() throws IOException {
        final Helper.Input input = Helper.getInput("1 1 1 1");
        final IncrementalListIterator iterator = buildIncrementalIterator(input);

        assertEquals(-1, iterator.nextInt());
    }

    @Test
    public void whenRemainingElementsNextIntItShouldReturnFirstRemainingElement() throws IOException {
        final Helper.Input input = Helper.getInput("1 1 1 0100 11");
        final IncrementalListIterator iterator = buildIncrementalIterator(input);
        final int expectedFirstRemainingElement = 3;

        assertEquals(expectedFirstRemainingElement, iterator.nextInt());
    }

    @Test
    public void whenRemainingRepeatedElementsNextIntItShouldReturnFirstRemainingElement() throws IOException {
        final Helper.Input input = Helper.getInput("0100 1 1 1 1 0100 11");
        final IncrementalListIterator iterator = buildIncrementalIterator(input);
        final int expectedFirstRemainingElement = 3;

        assertEquals(expectedFirstRemainingElement, iterator.nextInt());
    }

    @Test
    public void whenInputStreamIsCorruptedNextIntItShouldThrowExpectedIllegalStateException() throws IOException {
        final Helper.Input input = Helper.getInput("0101 1 1 1 1 1 1 0101 11 1");
        final IncrementalListIterator iterator = buildIncrementalIterator(input);
        input.buffer[0] = 0;
        input.buffer[1] = 0;
        input.buffer[2] = 0;
        iterator.nextInt();
        expectedException.expect(IllegalStateException.class);

        iterator.nextInt();
    }

    private static IncrementalListIterator buildIncrementalIterator(final Helper.Input input) throws IOException {
        final int valueBitSize = 2;
        final int listIndex = 0;
        final long[] offsets = new long[] {0l};
        final PebbleBytesStore bytesStore = new BytesArrayPebbleBytesStore(input.buffer, offsets);
        return IncrementalListIterator.build(listIndex, valueBitSize, bytesStore);
    }

}
