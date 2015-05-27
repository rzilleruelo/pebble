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
import org.pebble.core.PebbleBytesStore;
import org.pebble.core.PebbleOffsetsStore;
import org.pebble.core.decoding.iterators.Helper;
import org.pebble.utils.BytesArrayPebbleBytesStore;
import org.pebble.utils.LongListPebbleOffsetsStore;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.pebble.core.decoding.iterators.Helper.getInput;

@Category(UnitTest.class)
public class TimestampIntDecoderTest {

    @Test
    public void readItShouldReturnIteratorContainingExpectedDecodedValues() throws IOException {
        final List<Timestamp> expectedTimestamps = new ArrayList<Timestamp>();
        expectedTimestamps.add(new Timestamp(1430356174000L));
        expectedTimestamps.add(new Timestamp(1430356179000L));
        expectedTimestamps.add(new Timestamp(1430356181000L));
        /**
         * list=["Wed Apr 29 18:09:34 -0700 2015", "Wed Apr 29 18:09:39 -0700 2015", "Wed Apr 29 18:09:41 -0700 2015"]
         * mapping={
         *   "Wed Apr 29 18:09:34 -0700 2015": 1430356174,
         *   "Wed Apr 29 18:09:39 -0700 2015": 1430356179,
         *   "Wed Apr 29 18:09:41 -0700 2015": 1430356181
         * }
         * mapped_list=[1430356174, 1430356179, 1430356181]
         * values=[1430356174, 1430356179, 1430356181] indexes=[0, 1, 2]
         * reference=[0], intervals=[0], delta=[3, 1430356174, 4, 1],                           indexes=[0, 0, 2, 2]
         * 1              1                    100   1010101010000011000000011001110 101   10   1 1 11   11
         * 1              1                    3-00  1010101010000011000000011001110 3-01  2-0  1 1 2-1  2-1
         * 1              1                    11-00 1010101010000011000000011001110 11-01 10-0 1 1 10-1 10-1
         * 1              1                    01100 1010101010000011000000011001110 01101 0100 1 1 0101 0101
         */
        final Helper.Input input = getInput("1 1 01100 1010101010000011000000011001110 01101 0100 1 1 0101 0101");
        final PebbleOffsetsStore offsetsStore = new LongListPebbleOffsetsStore(new long[] {0L});
        final PebbleBytesStore bytesStore = new BytesArrayPebbleBytesStore(input.buffer, offsetsStore);
        final TimestampIntDecoder decoder = new TimestampIntDecoder(bytesStore);

        final Iterator<Timestamp> iterator = decoder.read(0, 31);

        final List<Timestamp> timestamps = new ArrayList<Timestamp>();
        while (iterator.hasNext()) {
            timestamps.add(iterator.next());
        }
        assertEquals(expectedTimestamps, timestamps);
    }

}
