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

package org.pebble.core.decoding.iterators.longs;

import it.unimi.dsi.io.InputBitStream;
import org.pebble.core.PebbleOffsetsStore;
import org.pebble.core.decoding.iterators.Helper;
import org.pebble.core.encoding.DefaultParametersValues;
import org.pebble.utils.BytesArrayPebbleBytesStore;
import org.pebble.utils.LongListPebbleOffsetsStore;

import java.io.IOException;

import static org.mockito.Mockito.mock;

public class BaseListIteratorHelper {

    public static class BaseListIteratorBuilder {

        private final int listIndex;
        private final Helper.Input input;
        private final ReferenceIterator referenceIterator;

        public BaseListIteratorBuilder(final Helper.Input input, final int listIndex) {
            this.listIndex = listIndex;
            this.input = input;
            referenceIterator = mock(ReferenceIterator.class);
        }

        public ReferenceIterator getReferenceIterator() {
            return referenceIterator;
        }

        public BaseListIterator build() throws IOException {
            final PebbleOffsetsStore offsetsStore = new LongListPebbleOffsetsStore(new long[] {0L});
            return new BaseListIterator(
                listIndex,
                DefaultParametersValues.LONG_BITS,
                DefaultParametersValues.DEFAULT_MIN_INTERVAL_SIZE,
                input.stream,
                new BytesArrayPebbleBytesStore(input.buffer, offsetsStore)
            ) {
                @Override
                protected ReferenceIterator initializeReferenceIterator(int listIndex, InputBitStream inputBitStream)
                    throws IOException
                {
                    int offset = inputBitStream.readDelta();
                    if (offset > 0) {
                        inputBitStream.readBit();
                        int number_of_blocks = inputBitStream.readDelta();
                        while (number_of_blocks-- > 0) {
                            inputBitStream.readDelta();
                        }
                    }
                    return referenceIterator;
                }
            };
        }
    }
}
