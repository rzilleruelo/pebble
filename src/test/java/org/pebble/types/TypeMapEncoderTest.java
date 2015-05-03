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

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.pebble.UnitTest;
import org.pebble.core.PebbleOffsetsStoreWriter;
import org.pebble.core.encoding.EncodingInfo;
import org.pebble.core.encoding.OutputBitStream;
import org.pebble.utils.LongListPebbleOffsetsStore;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

@Category(UnitTest.class)
public class TypeMapEncoderTest {

    @Test
    public void whenValueIsMissingFromMapEncodeItShouldReturnNewIndex() throws IOException {
        final TypeMapEncoder encoder = build();
        final Object element = new Object();

        assertEquals(0, encoder.encode(element));
    }

    @Test
    public void whenValueAlreadyExistsInMapEncodeItShouldReturnExistingIndex() throws IOException {
        final TypeMapEncoder encoder = build();
        final Object element = new Object();
        encoder.encode(element);

        assertEquals(0, encoder.encode(element));
    }

    @Test
    public void whenValueAlreadyExistsInMapButReferenceIsBiggerThanReferenceWindowSizeEncodeItShouldReturnNewIndex()
        throws IOException
    {
        final EncodingInfo encodingInfo = new EncodingInfo();
        final TypeMapEncoder encoder = build(encodingInfo);
        final Object element = new Object();
        encoder.encode(element);
        encodingInfo.incrementCurrentIndex();
        encodingInfo.incrementCurrentIndex();
        encodingInfo.incrementCurrentIndex();

        assertEquals(4, encoder.encode(element));
    }

    @Test
    public void whenMapIsFullAndValueIsMissingFromMapEncodeItShouldReturnNewIndex() throws IOException {
        final TypeMapEncoder encoder = build();
        final Object element = new Object();
        encoder.encode(new Object());
        encoder.encode(new Object());

        assertEquals(2, encoder.encode(element));
    }

    @Test
    public void whenMapIsFullAndValueToBeRemovedWasAssignedNewerIndexAndValueIsMissingEncodeItShouldReturnNewIndex()
        throws IOException
    {
        final EncodingInfo encodingInfo = new EncodingInfo();
        final TypeMapEncoder encoder = build(encodingInfo);
        final Object previousElement = new Object();
        final Object element = new Object();
        encoder.encode(previousElement);
        encodingInfo.incrementCurrentIndex();
        encodingInfo.incrementCurrentIndex();
        encoder.encode(previousElement);

        assertEquals(4, encoder.encode(element));
    }

    @Test
    public void whenValueToBeReplacedInTheBufferHasBeenAlreadyRemovedFromMapAndValueIsMissingEncodeItShouldReturnNewIndex()
        throws IOException
    {
        final EncodingInfo encodingInfo = new EncodingInfo();
        final TypeMapEncoder encoder = build(encodingInfo);
        final Object previousElement = new Object();
        final Object element = new Object();
        encoder.encode(previousElement);
        encodingInfo.incrementCurrentIndex();
        encodingInfo.incrementCurrentIndex();
        encoder.encode(previousElement);
        encodingInfo.incrementCurrentIndex();
        encodingInfo.incrementCurrentIndex();
        encoder.encode(new Object());

        assertEquals(7, encoder.encode(element));
    }

    private static TypeMapEncoder build() {
        final EncodingInfo encodingInfo = new EncodingInfo();
        return build(encodingInfo);
    }

    private static TypeMapEncoder build(final EncodingInfo encodingInfo) {
        final int referenceWindowSize = 2;
        final byte[] buffer = new byte[0];
        final OutputBitStream outputBitStream = new OutputBitStream(buffer);
        final PebbleOffsetsStoreWriter offsetsStore = new LongListPebbleOffsetsStore();
        return new TypeMapEncoder(referenceWindowSize, outputBitStream, offsetsStore, encodingInfo) {
            @Override
            public int write(Object element) throws IOException {
                return 1;
            }
        };
    }

}
