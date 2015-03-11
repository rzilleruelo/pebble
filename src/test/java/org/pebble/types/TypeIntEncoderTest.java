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
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.pebble.UnitTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

@Category(UnitTest.class)
public class TypeIntEncoderTest {

    @Test
    public void setIntListItShouldSetExpectedEncodedValuesList() throws IOException {
        TypeIntEncoder encoder = new TypeIntEncoder() {
            @Override
            protected int encode(Object element) throws IOException {
                return element.hashCode();
            }
        };
        List values = new ArrayList(Arrays.asList(new Object[]{
            new Object(),
            new Object(),
            new Object()
        }));
        final IntList listBuffer = new IntArrayList();
        final IntList expectedListBuffer = new IntArrayList(new int[] {
            values.get(0).hashCode(),
            values.get(1).hashCode(),
            values.get(2).hashCode()
        });

        encoder.setIntList(values, listBuffer);

        assertEquals(expectedListBuffer, listBuffer);
    }

}
