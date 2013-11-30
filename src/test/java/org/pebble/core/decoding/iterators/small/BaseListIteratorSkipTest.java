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

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@Category(UnitTest.class)
public class BaseListIteratorSkipTest {

    @Test
    public void whenTheListHasMoreElementsThanSkipElementsItShouldSkipExpectedElements() {
        final int expectedNumberOfSkips = 3;
        final BaseListIterator baseListIterator = mock(BaseListIterator.class, CALLS_REAL_METHODS);
        doReturn(true).when(baseListIterator).hasNext();
        doReturn(1).when(baseListIterator).nextInt();

        final int numberOfSkips = baseListIterator.skip(expectedNumberOfSkips);

        verify(baseListIterator, times(expectedNumberOfSkips)).nextInt();
        assertEquals(expectedNumberOfSkips, numberOfSkips);
    }

    @Test
    public void whenTheListHasLessElementsThanSkipElementsItShouldSkipUntilEndOfList() {
        final int expectedNumberOfSkips = 3;
        final int numberOfSkips = 5;
        final BaseListIterator baseListIterator = mock(BaseListIterator.class, CALLS_REAL_METHODS);
        doReturn(true).doReturn(true).doReturn(true).doReturn(false).when(baseListIterator).hasNext();
        doReturn(1).when(baseListIterator).nextInt();

        final int actualNumberOfSkips = baseListIterator.skip(numberOfSkips);

        verify(baseListIterator, times(expectedNumberOfSkips)).nextInt();
        assertEquals(expectedNumberOfSkips, actualNumberOfSkips);
    }

}
