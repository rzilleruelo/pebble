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
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;

import java.io.IOException;

import static org.pebble.core.decoding.iterators.ints.BaseListIteratorHelper.BaseListIteratorBuilder;
import static org.pebble.core.decoding.iterators.ints.Helper.getInput;
import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.when;

@Category(UnitTest.class)
public class BaseListIteratorNextIntTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void whenOnlyReferenceListHaveElementsNextIntShouldReturnExpectedValue() throws IOException {
        final Helper.Input input = getInput(
            "0100 1 1" + // Reference list bit=1 blocks=[1]
            "1" +        // Empty intervals list
            "1"          // Empty delta list
        );
        final int listIndex = 1;
        final BaseListIteratorBuilder baseListIteratorBuilder = new BaseListIteratorBuilder(input, listIndex);
        final int expectedValue = 1;
        baseListIteratorBuilder.getReferenceIterator().currentValue = expectedValue;
        when(baseListIteratorBuilder.getReferenceIterator().next()).thenReturn(expectedValue);

        final BaseListIterator baseListIterator = baseListIteratorBuilder.build();
        final int value = baseListIterator.nextInt();

        assertEquals(expectedValue, value);
    }

    @Test
    public void
        whenReferenceAndIntervalListsHasElementsAndReferenceListValueIsTheSmallestNextIntShouldReturnExpectedValue()
    throws IOException {
        final Helper.Input input = getInput(
            "0100 1 1" +                                // Reference list bit=1 blocks=[1]
            "0100 00000000000000000000000000000111 1" + // Interval list [7, 1]
            "1"                                         // Empty delta list
        );
        final int listIndex = 1;
        final BaseListIteratorBuilder baseListIteratorBuilder = new BaseListIteratorBuilder(input, listIndex);
        final int expectedValue = 1;
        baseListIteratorBuilder.getReferenceIterator().currentValue = expectedValue;
        when(baseListIteratorBuilder.getReferenceIterator().next()).thenReturn(expectedValue);

        final BaseListIterator baseListIterator = baseListIteratorBuilder.build();
        final int value = baseListIterator.nextInt();

        assertEquals(expectedValue, value);
    }

    @Test
    public void
        whenReferenceIntervalAndDeltaListsHasElementsAndReferenceListValueIsTheSmallestNextIntShouldReturnExpectedValue()
    throws IOException {
        final Helper.Input input = getInput(
            "0100 1 1" +                                // Reference list bit=1 blocks=[1]
            "0100 00000000000000000000000000000111 1" + // Interval list [7, 1]
            "0100 00000000000000000000000000000011"     // Delta list [3]
        );
        final int listIndex = 1;
        final BaseListIteratorBuilder baseListIteratorBuilder = new BaseListIteratorBuilder(input, listIndex);
        final int expectedValue = 1;
        baseListIteratorBuilder.getReferenceIterator().currentValue = expectedValue;
        when(baseListIteratorBuilder.getReferenceIterator().next()).thenReturn(expectedValue);

        final BaseListIterator baseListIterator = baseListIteratorBuilder.build();
        final int value = baseListIterator.nextInt();

        assertEquals(expectedValue, value);
    }

    @Test
    public void whenOnlyIntervalListHaveElementsNextIntShouldReturnExpectedValue() throws IOException {
        final Helper.Input input = getInput(
            "1"                                       + // Empty Reference list
            "0100 00000000000000000000000000000111 1" + // Interval list [7, 1]
            "1"                                         // Empty delta list
        );
        final int listIndex = 1;
        final BaseListIteratorBuilder baseListIteratorBuilder = new BaseListIteratorBuilder(input, listIndex);
        final int expectedValue = 7;
        baseListIteratorBuilder.getReferenceIterator().currentValue = -1;

        final BaseListIterator baseListIterator = baseListIteratorBuilder.build();
        final int value = baseListIterator.nextInt();

        assertEquals(expectedValue, value);
    }

    @Test
    public void
        whenReferenceAndIntervalListsHasElementsAndIntervalListValueIsTheSmallestNextIntShouldReturnExpectedValue()
    throws IOException {
        final Helper.Input input = getInput(
            "0100 1 1" +                                // Reference list bit=1 blocks=[1]
            "0100 00000000000000000000000000000111 1" + // Interval list [7, 1]
            "1"                                         // Empty delta list
        );
        final int listIndex = 1;
        final BaseListIteratorBuilder baseListIteratorBuilder = new BaseListIteratorBuilder(input, listIndex);
        final int expectedValue = 7;
        baseListIteratorBuilder.getReferenceIterator().currentValue = 8;
        when(baseListIteratorBuilder.getReferenceIterator().next()).thenReturn(expectedValue);

        final BaseListIterator baseListIterator = baseListIteratorBuilder.build();
        final int value = baseListIterator.nextInt();

        assertEquals(expectedValue, value);
    }

    @Test
    public void whenOnlyDeltaListHaveElementsNextIntShouldReturnExpectedValue() throws IOException {
        final Helper.Input input = getInput(
            "1" +                                   // Empty Reference list
            "1" +                                   // Empty intervals list
            "0100 00000000000000000000000000000011" // Delta list [3]
        );
        final int listIndex = 1;
        final BaseListIteratorBuilder baseListIteratorBuilder = new BaseListIteratorBuilder(input, listIndex);
        final int expectedValue = 3;
        baseListIteratorBuilder.getReferenceIterator().currentValue = -1;

        final BaseListIterator baseListIterator = baseListIteratorBuilder.build();
        final int value = baseListIterator.nextInt();

        assertEquals(expectedValue, value);
    }

    @Test
    public void
        whenReferenceAndDeltaListsHasElementsAndDeltaListValueIsTheSmallestNextIntShouldReturnExpectedValue()
    throws IOException {
        final Helper.Input input = getInput(
            "0100 1 1" +                            // Reference list bit=1 blocks=[1]
            "1" +                                   // Empty intervals list
            "0100 00000000000000000000000000000011" // Delta list [3]
        );
        final int listIndex = 1;
        final BaseListIteratorBuilder baseListIteratorBuilder = new BaseListIteratorBuilder(input, listIndex);
        final int expectedValue = 3;
        baseListIteratorBuilder.getReferenceIterator().currentValue = 5;
        when(baseListIteratorBuilder.getReferenceIterator().next()).thenReturn(expectedValue);

        final BaseListIterator baseListIterator = baseListIteratorBuilder.build();
        final int value = baseListIterator.nextInt();

        assertEquals(expectedValue, value);
    }

    @Test
    public void
        whenIntervalAndDeltaListsHasElementsAndIntervalListValueIsTheSmallestNextIntShouldReturnExpectedValue()
    throws IOException {
        final Helper.Input input = getInput(
            "1" +                                       // Empty Reference list
            "0100 00000000000000000000000000000111 1" + // Interval list [7, 1]
            "0100 00000000000000000000000000001000"     // Delta list [8]
        );
        final int listIndex = 1;
        final BaseListIteratorBuilder baseListIteratorBuilder = new BaseListIteratorBuilder(input, listIndex);
        final int expectedValue = 7;
        baseListIteratorBuilder.getReferenceIterator().currentValue = -1;

        final BaseListIterator baseListIterator = baseListIteratorBuilder.build();
        final int value = baseListIterator.nextInt();

        assertEquals(expectedValue, value);
    }

    @Test
    public void
        whenIntervalAndDeltaListsHasElementsAndDeltaListValueIsTheSmallestNextIntShouldReturnExpectedValue()
    throws IOException {
        final Helper.Input input = getInput(
            "1" +                                       // Empty Reference list
            "0100 00000000000000000000000000000111 1" + // Interval list [7, 1]
            "0100 00000000000000000000000000000011"     // Delta list [3]
        );
        final int listIndex = 1;
        final BaseListIteratorBuilder baseListIteratorBuilder = new BaseListIteratorBuilder(input, listIndex);
        final int expectedValue = 3;
        baseListIteratorBuilder.getReferenceIterator().currentValue = -1;

        final BaseListIterator baseListIterator = baseListIteratorBuilder.build();
        final int value = baseListIterator.nextInt();

        assertEquals(expectedValue, value);
    }

    @Test
    public void whenNextIntInternallyRaisesIOExceptionItShouldThrowExpectedIllegalStateException() throws IOException {
        final Helper.Input input = getInput(
            "0100 1 1" + // Reference list bit=1 blocks=[1]
            "1" +        // Empty intervals list
            "1"          // Empty delta list
        );
        final int listIndex = 1;
        final BaseListIteratorBuilder baseListIteratorBuilder = new BaseListIteratorBuilder(input, listIndex);
        baseListIteratorBuilder.getReferenceIterator().currentValue = 1;
        final String expectedExceptionMessage = "exception";
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(expectedExceptionMessage);
        when(baseListIteratorBuilder.getReferenceIterator().next()).thenThrow(
            new IOException(expectedExceptionMessage)
        );
        final BaseListIterator baseListIterator = baseListIteratorBuilder.build();

        baseListIterator.nextInt();
    }

}
