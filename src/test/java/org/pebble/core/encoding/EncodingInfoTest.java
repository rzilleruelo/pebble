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

package org.pebble.core.encoding;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.pebble.UnitTest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

@Category(UnitTest.class)
public class EncodingInfoTest {

    @Test
    public void incrementCurrentIndexItShouldIncrementCurrentIndexValueInOneAndReturnPreviousValue() {
        final EncodingInfo encodingInfo = new EncodingInfo();

        assertEquals(0, encodingInfo.incrementCurrentIndex());
        assertEquals(1, encodingInfo.getCurrentIndex());
    }

    @Test
    public void incrementOffsetItShouldIncrementOffsetInGivenValueAndShouldReturnIncrementedValue() {
        final int deltaOffset = 5;
        final EncodingInfo encodingInfo = new EncodingInfo();

        assertEquals(deltaOffset, encodingInfo.incrementOffset(deltaOffset));
        assertEquals(deltaOffset, encodingInfo.getOffset());
    }

    @Test
    public void saveItShouldSaveExpectedInformation() {
        final int expectedTotalElements = 1;
        final long expectedTotalSize = 2L;
        final String expectedOutput = String.format(
            "totalElements: %d\ntotalSize: %d\n",
            expectedTotalElements,
            expectedTotalSize
        );
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final EncodingInfo encodingInfo = new EncodingInfo();
        encodingInfo.incrementCurrentIndex();
        encodingInfo.incrementOffset((int)expectedTotalSize);

        encodingInfo.save(outputStream);

        assertEquals(expectedOutput, outputStream.toString());
    }

    @Test
    public void loadItShouldLoadExpectedInformation() throws IOException {
        final int expectedTotalElements = 1;
        final long expectedTotalSize = 2L;
        final InputStream inputStream = new ByteArrayInputStream(String.format(
            "totalElements: %d\ntotalSize: %d\n",
            expectedTotalElements,
            expectedTotalSize
        ).getBytes());

        final EncodingInfo encodingInfo = EncodingInfo.load(inputStream);

        assertEquals(expectedTotalElements, encodingInfo.getCurrentIndex());
        assertEquals(expectedTotalSize, encodingInfo.getOffset());
    }

}
