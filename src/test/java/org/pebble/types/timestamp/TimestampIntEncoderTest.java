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

package org.pebble.types.timestamp;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.pebble.UnitTest;

import java.io.IOException;
import java.sql.Timestamp;

import static org.junit.Assert.assertEquals;

@Category(UnitTest.class)
public class TimestampIntEncoderTest {

    @Test
    public void encodeItShouldReturnExpectedEncodedValue() throws IOException {
        final TimestampIntEncoder encoder = new TimestampIntEncoder();
        final Timestamp value = new Timestamp(1426034806966l);
        final int expectedEncodedValue = 1426034806;

        assertEquals(expectedEncodedValue, encoder.encode(value));
    }

}
