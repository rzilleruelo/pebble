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
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;

import static org.pebble.core.decoding.iterators.small.BaseListIteratorHelper.BaseListIteratorBuilder;
import static org.pebble.core.decoding.iterators.small.Helper.getInput;
import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.when;

@Category(UnitTest.class)
public class BaseListIteratorHasNextTest {

    @Test
    public void whenAllListAreEmptyElementsHasNextShouldReturnFalse() throws IOException {
        final Helper.Input input = getInput("1 1 1");
        final int listIndex = 1;
        final BaseListIteratorBuilder baseListIteratorBuilder = new BaseListIteratorBuilder(input, listIndex);
        when(baseListIteratorBuilder.getReferenceIterator().hasNext()).thenReturn(false);

        final BaseListIterator baseListIterator = baseListIteratorBuilder.build();
        final boolean hasNext = baseListIterator.hasNext();

        assertEquals(false, hasNext);
    }

    @Test
    public void whenOnlyReferenceListHaveElementsHasNextShouldReturnTrue() throws IOException {
        final Helper.Input input = getInput(
            "0100 1 1" + // Reference list bit=1 blocks=[1]
            "1" +        // Empty intervals list
            "1"          // Empty delta list
        );
        final int listIndex = 1;
        final BaseListIteratorBuilder baseListIteratorBuilder = new BaseListIteratorBuilder(input, listIndex);
        when(baseListIteratorBuilder.getReferenceIterator().hasNext()).thenReturn(true);

        final BaseListIterator baseListIterator = baseListIteratorBuilder.build();
        final boolean hasNext = baseListIterator.hasNext();

        assertEquals(true, hasNext);
    }

    @Test
    public void whenOnlyIntervalListHaveElementsHasNextShouldReturnTrue() throws IOException {
        final Helper.Input input = getInput(
            "1" +                                       // Empty Reference list
            "0100 00000000000000000000000000000111 1" + // Interval list [7, 1]
            "1"                                         // Empty delta list
        );
        final int listIndex = 1;
        final BaseListIteratorBuilder baseListIteratorBuilder = new BaseListIteratorBuilder(input, listIndex);
        when(baseListIteratorBuilder.getReferenceIterator().hasNext()).thenReturn(false);

        final BaseListIterator baseListIterator = baseListIteratorBuilder.build();
        final boolean hasNext = baseListIterator.hasNext();

        assertEquals(true, hasNext);
    }

    @Test
    public void whenOnlyDeltaListHaveElementsHasNextShouldReturnTrue() throws IOException {
        final Helper.Input input = getInput(
            "1" + // Empty Reference list
            "1" + // Empty intervals list
            "0100 00000000000000000000000000001001" // Delta list [9]
        );
        final int listIndex = 1;
        final BaseListIteratorBuilder baseListIteratorBuilder = new BaseListIteratorBuilder(input, listIndex);
        when(baseListIteratorBuilder.getReferenceIterator().hasNext()).thenReturn(false);

        final BaseListIterator baseListIterator = baseListIteratorBuilder.build();
        final boolean hasNext = baseListIterator.hasNext();

        assertEquals(true, hasNext);
    }
}
