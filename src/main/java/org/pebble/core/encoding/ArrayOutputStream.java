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

import java.io.IOException;
import java.io.OutputStream;

/**
 * Byte array wrapper into an OutputStream
 */
class ArrayOutputStream extends OutputStream {

    private final byte[] buffer;
    private int i;

    /**
     * Initializes an empty output stream associated to <code>buffer</code>
     * @param buffer where the data will be written.
     */
    public ArrayOutputStream(final byte[] buffer) {
        this.buffer = buffer;
        i = 0;
    }

    @Override
    public void write(int b) throws IOException {
        buffer[i++] = (byte) b;
    }

}
