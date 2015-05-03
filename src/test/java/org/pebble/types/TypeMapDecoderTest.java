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

import it.unimi.dsi.io.InputBitStream;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.pebble.UnitTest;
import org.pebble.core.PebbleBytesStore;
import org.pebble.core.PebbleOffsetsStore;
import org.pebble.core.decoding.iterators.Helper;
import org.pebble.utils.BytesArrayPebbleBytesStore;
import org.pebble.utils.LongListPebbleOffsetsStore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.pebble.core.decoding.iterators.Helper.getInput;

@Category(UnitTest.class)
public class TypeMapDecoderTest {

    @Test
    public void readItShouldReturnIteratorContainingExpectedEncodedValue() throws IOException {
        final List<String> expectedDecodedValues = new ArrayList<String>();
        expectedDecodedValues.add("a");
        expectedDecodedValues.add("b");
        expectedDecodedValues.add("a");
        expectedDecodedValues.add("a");
        expectedDecodedValues.add("a");
        expectedDecodedValues.add("b");
        expectedDecodedValues.add("b");
        expectedDecodedValues.add("a");
        /**
         * list=["a", "b", "a", "a", "a", "b", "b", "a"]
         * mapping={"a": 0, "b": 1}
         * mapped_list=[0, 1, 0, 0, 0, 1, 1, 0]
         * values=[0, 1] indexes=[0, 1, 0, 0, 0, 1, 1, 0]
         * reference=[0], intervals=[0], delta=[2, 0, 0],   indexes=[6, 0, 2, 1, 0, 0, 2, 0, 1]
         * 1              1              11   00000 1       111   1 11   10   1 1 11   1 10
         * 1              1              2-1  00000 1       3-11  1 2-1  2-0  1 1 2-1  1 2-0
         * 1              1              10-1 00000 1       11-11 1 10-1 10-0 1 1 10-1 1 10-0
         * 1              1              0101 00000 1       01111 1 0101 0100 1 1 0101 1 0100
         */
        final Helper.Input input = getInput(
            "01100001" +
            "01100010" +
            "1 1 0101 00000 1 01111 1 0101 0100 1 1 0101 1 0100"
        );
        final TypeMapDecoder<String> decoder = build(input, new long[] {0L, 8L, 16L});
        final List<String> decodedValues = new ArrayList<String>();
        final int listIndex = 2;
        final int valueBitSize = 5;

        final Iterator<String> iterator = decoder.read(listIndex, valueBitSize);
        while(iterator.hasNext()) {
            decodedValues.add(iterator.next());
        }

        assertEquals(expectedDecodedValues, decodedValues);
    }

    private static TypeMapDecoder<String> build(final Helper.Input input, final long[] offsets) {
        final PebbleOffsetsStore offsetsStore = new LongListPebbleOffsetsStore(offsets);
        final PebbleBytesStore bytesStore = new BytesArrayPebbleBytesStore(input.buffer, offsetsStore);
        return new TypeMapDecoder<String>(bytesStore) {
            @Override
            protected String read(InputBitStream inputBitStream) throws IOException {
                return ((char) inputBitStream.readInt(8)) + "";
            }
        };
    }

}
