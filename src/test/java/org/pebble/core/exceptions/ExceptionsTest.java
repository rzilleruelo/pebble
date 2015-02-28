package org.pebble.core.exceptions;

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

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;
import org.pebble.UnitTest;

@Category(UnitTest.class)
public class ExceptionsTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void itShouldThrowNotStrictlyIncrementalListExceptionSuccessfully() {
        expectedException.expect(NotStrictlyIncrementalListException.class);
        expectedException.expectMessage("List is not strictly incremental, found 0 after 5");

        throw new NotStrictlyIncrementalListException(5, 0);
    }

    @Test
    public void itShouldThrowDeltaValueIsTooBigExceptionSuccessfully() {
        expectedException.expect(DeltaValueIsTooBigException.class);
        expectedException.expectMessage("Delta value between 2147483648 and 0, is too big");

        throw new DeltaValueIsTooBigException(0, 2147483648L);
    }

}
