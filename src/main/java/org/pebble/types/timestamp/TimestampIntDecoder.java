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

import it.unimi.dsi.fastutil.ints.IntIterator;
import org.pebble.core.PebbleBytesStore;
import org.pebble.core.decoding.iterators.ints.ListIterator;
import org.pebble.types.TypeIntDecoder;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Iterator;

/**
 * Class used to decode a list of {@link java.sql.Timestamp} from an encoded list of integers obtained using an
 * instance of {@link org.pebble.types.timestamp.TimestampIntEncoder}.
 */
public class TimestampIntDecoder extends TypeIntDecoder<Timestamp> {

    /**
     *
     * @param bytesStore mapping between list offsets and data bytes arrays and bytes offsets.
     */
    public TimestampIntDecoder(final PebbleBytesStore bytesStore) {
        super(bytesStore);
    }

    /**
     * Builds iterator over a list of {@link java.sql.Timestamp} list.
     * @param listIndex index of the current list.
     * @param valueBitSize fixed number of bits used to represent the values in the mapping list. It can be any value
     *                     between 1bit and 31 bits.
     * @return iterator of decoded list.
     * @throws IOException in case there is an exception reading the information required to build iterator.
     */
    public Iterator<Timestamp> read(final int listIndex, final int valueBitSize) throws IOException {
        return new TimestampIterator(ListIterator.build(listIndex, valueBitSize, bytesStore));
    }

    private class TimestampIterator extends TypeIterator {

        public TimestampIterator(final IntIterator iterator) {
            super(iterator);
        }

        @Override
        public Timestamp next() {
            return new Timestamp(iterator.nextInt() * 1000L);
        }

    }

}
