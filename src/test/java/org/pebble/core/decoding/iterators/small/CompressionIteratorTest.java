package org.pebble.core.decoding.iterators.small;

/*
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

import org.pebble.UnitTest;
import org.pebble.core.decoding.iterators.small.Helper.Input;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertNotEquals;
import static org.pebble.core.decoding.iterators.small.Helper.getInput;

@Category(UnitTest.class)
public class CompressionIteratorTest {

    @Test
    public void updateOffsetItShouldSetOffsetWithTheCurrentStreamOffsetSuccessfully() throws Exception {
        final Input input = getInput("01110");
        final CompressionIterator compressionIterator = new CompressionIterator(input.stream) {
            @Override
            public int next() throws IOException {
                return 0;
            }
        };
        final int expectedOffset = 5;

        assertEquals(0, compressionIterator.offset);

        compressionIterator.recordOffset();

        assertEquals(expectedOffset, compressionIterator.offset);
    }

    @Test
    public void seekItShouldSetStreamOffsetWithTheCurrentOffsetSuccessfully() throws Exception {
        final Input input = getInput("01110 1 1 1 1 1");
        final CompressionIterator compressionIterator = new CompressionIterator(input.stream) {
            @Override
            public int next() throws IOException {
                return 0;
            }
        };
        final int expectedOffset = 5;
        compressionIterator.offset = expectedOffset;
        input.stream.readInt(expectedOffset);

        assertNotEquals(expectedOffset, input.stream.position());

        compressionIterator.seek();

        assertEquals(expectedOffset, input.stream.position());
    }

    @Test
    public void whenCurrentValueIsNotNegativeOneHasNextItShouldReturnTrueSuccessfully() throws Exception {
        final Input input = getInput("1");
        final CompressionIterator compressionIterator = new CompressionIterator(input.stream) {
            @Override
            public int next() throws IOException {
                return 0;
            }
        };
        compressionIterator.currentValue = 0;

        assertTrue(compressionIterator.hasNext());
    }

    @Test
    public void whenCurrentValueIsNegativeOneHasNextItShouldReturnFalseSuccessfully() throws Exception {
        final Input input = getInput("1");
        final CompressionIterator compressionIterator = new CompressionIterator(input.stream) {
            @Override
            public int next() throws IOException {
                return 0;
            }
        };
        compressionIterator.currentValue = -1;

        assertFalse(compressionIterator.hasNext());
    }

}
