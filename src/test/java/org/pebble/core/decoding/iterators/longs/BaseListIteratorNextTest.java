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

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.pebble.UnitTest;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@Category(UnitTest.class)
public class BaseListIteratorNextTest {

    @Test
    public void whenNextIntReturnsPositiveValueNextItShouldReturnAnIntegerWrappingValue() {
        final BaseListIterator baseListIterator = mock(BaseListIterator.class, CALLS_REAL_METHODS);
        final Long expectedValue = new Long(1L);
        doReturn(expectedValue.longValue()).when(baseListIterator).nextLong();

        final Long value = baseListIterator.next();

        assertEquals(new Long(expectedValue), value);
    }

    @Test
    public void whenNextIntReturnsNegativeValueNextItShouldReturnNull() {
        BaseListIterator baseListIterator = mock(BaseListIterator.class, CALLS_REAL_METHODS);
        doReturn(-1L).when(baseListIterator).nextLong();

        final Long value = baseListIterator.next();

        assertNull(value);
    }

}
