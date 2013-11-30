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
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.pebble.core.decoding.iterators.small.Helper.getInput;
import static org.junit.Assert.assertEquals;

@Category(UnitTest.class)
public class RepeatsIteratorTest {

    @Test
    public void whenThereIsAnEncodedRepetitionsListItShouldRecoverOriginalRepetitionsSuccessfully()
        throws Exception
    {
        final int originalListSize = 16;
        Helper.Input input = getInput("01110 1 1 0100 0100 0100 1 0101 1 0100 1");
        /**
         * 1, 1, 2, 3, 3, 3, 5, 6, 6, 7, 10, 11, 11, 16, 19, 19 List from which the repetitions where extracted
         * 1, 0, 0, 1, 1, 0, 0, 1, 0, 0,  0,  1,  0,  0,  1,  0 Repetition flags
         */
        final IntList expectedRepetitions = new IntArrayList(
            new int[] {1, 0, 0, 1, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0}
        );
        final IntList repetitions = new IntArrayList();

        RepeatsIterator repeatsIterator = new RepeatsIterator(input.stream);
        for(int i = 0; i < originalListSize; i++) {
           repetitions.add(repeatsIterator.next());
        }

        assertEquals(expectedRepetitions, repetitions);
    }

    @Test
    public void whenEncodedRepetitionsListIsEmptyItShouldRecoverOriginalRepetitionsSuccessfully()
        throws Exception
    {
        final int originalListSize = 10;
        Helper.Input input = getInput("1");
        /**
         * 1, 2, 3, 5, 6, 7, 10, 11, 16, 19 List from which the repetitions where extracted
         * 0, 0, 0, 0, 0, 0,  0,  0,  0,  0 Repetition flags
         */
        final IntList expectedRepetitions = new IntArrayList(
            new int[] {0, 0, 0, 0, 0, 0, 0 ,0 ,0, 0}
        );
        final IntList repetitions = new IntArrayList();
        RepeatsIterator repeatsIterator = new RepeatsIterator(input.stream);
        for(int i = 0; i < originalListSize; i++) {
            repetitions.add(repeatsIterator.next());
        }

        assertEquals(expectedRepetitions, repetitions);
    }

    @Test
    public void whenThereIsAnEncodedRepetitionsListItShouldReturnExpectedRemainingElementsSuccessfully()
        throws Exception
    {
        final int originalListSize = 16;
        Helper.Input input = getInput("01110 1 1 0100 0100 0100 1 0101 1 0100 1");
        /**
         * (0, 2), (2, 3), (4, 2), (7, 2), (9, 2) Repetitions
         */
        final IntList expectedRemainingElements = new IntArrayList(
            new int[] {4, 4, 3, 3, 3, 3, 2, 2, 2, 1, 1, 1, 1, 0, 0, 0, 0}
        );
        final IntList remainingElements = new IntArrayList();

        RepeatsIterator repeatsIterator = new RepeatsIterator(input.stream);
        for(int i = 0; i < originalListSize; i++) {
            remainingElements.add(repeatsIterator.getRemainingElements());
            repeatsIterator.next();
        }
        remainingElements.add(repeatsIterator.getRemainingElements());

        assertEquals(expectedRemainingElements, remainingElements);
    }

}
