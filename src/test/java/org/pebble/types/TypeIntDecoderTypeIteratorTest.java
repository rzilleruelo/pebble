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

package org.pebble.types;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;
import org.pebble.UnitTest;

import java.io.IOException;
import java.util.Iterator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(UnitTest.class)
public class TypeIntDecoderTypeIteratorTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void itShouldThrowUnsupportedOperationExceptionWhenCallingRemove() throws IOException {
        expectedException.expect(UnsupportedOperationException.class);
        expectedException.expectMessage("List is immutable");
        Iterator iterator = build().read(0, 0);

        iterator.remove();
    }

    @Test
    public void itShouldReturnFalseWhenCallingHasNextAndThereIsNoRemainingElements() throws IOException {
        Iterator iterator = build(new IntArrayList()).read(0, 0);

        assertFalse(iterator.hasNext());
    }

    @Test
    public void itShouldReturnTrueWhenCallingHasNextAndThereIsRemainingElements() throws IOException {
        Iterator iterator = build(new IntArrayList(new int[]{1})).read(0, 0);

        assertTrue(iterator.hasNext());
    }

    private static TypeIntDecoder build() {
        return build(null);
    }

    private static TypeIntDecoder build(final IntList list) {
        return new TypeIntDecoder(null) {
            @Override
            public Iterator read(int listIndex, int valueBitSize) throws IOException {
                return new TypeIterator(list == null ? null : list.iterator()) {
                    @Override
                    public Object next() {
                        return iterator.next();
                    }
                };
            }
        };
    }

}
