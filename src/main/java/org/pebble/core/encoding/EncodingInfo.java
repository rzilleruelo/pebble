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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * Class used to keep track of relevant encoding information.
 */
public class EncodingInfo {

    private int currentIndex;
    private long offset;

    /**
     * Sets encoding info with initial values.
     */
    public EncodingInfo() {
        currentIndex = 0;
        offset = 0L;
    }

    /**
     * @return index for next encoded entry.
     */
    public int getCurrentIndex() {
        return currentIndex;
    }

    /**
     * @return current total numbers of bits used to encode past entries. Its value indicate the starting offset of the
     * encoded representation of next entry.
     */
    public long getOffset() {
        return offset;
    }

    /**
     * Increments in one the current index.
     * @return current index value before incrementation of its value.
     */
    public int incrementCurrentIndex() {
        return currentIndex++;
    }

    /**
     * Increments offset into <code>deltaOffset</code> and returns its new value.
     * @param deltaOffset value into what will be incremented offset.
     * @return value of offset after its incrementation.
     */
    public long incrementOffset(int deltaOffset) {
        offset += deltaOffset;
        return offset;
    }

    /**
     * Stores current encoding info into <code>outputStream</code>.
     * @param outputStream used to store the encoding info.
     */
    public void save(final OutputStream outputStream) {
        PrintWriter printWriter = new PrintWriter(outputStream);
        printWriter.printf("totalElements: %d\n", currentIndex);
        printWriter.printf("totalSize: %d\n", offset);
        printWriter.flush();
    }

    /**
     * Loads encoding info from <code>inputStream</code>.
     * @param inputStream from which encoding info is read.
     * @return returns instance of encoding info with the values read from <code>inputStream</code>.
     * @throws IOException in case there is an exception reading from <code>inputStream</code>.
     */
    public static EncodingInfo load(final InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        final EncodingInfo encodingInfo = new EncodingInfo();
        encodingInfo.currentIndex = Integer.parseInt(bufferedReader.readLine().split(": ")[1]);
        encodingInfo.offset = Long.parseLong(bufferedReader.readLine().split(": ")[1]);
        return encodingInfo;
    }

}
