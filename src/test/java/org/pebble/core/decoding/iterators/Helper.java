package org.pebble.core.decoding.iterators;

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

import it.unimi.dsi.io.InputBitStream;

import java.io.IOException;

public class Helper {

    public static byte[] binaryStringToBytes(String binaryString) {
        byte[] bytes = new byte[(int) Math.ceil(binaryString.length() / 8.0)];
        for (int i = 0, j = 0, k = 7; i < binaryString.length(); i++) {
            byte bit = (byte) (binaryString.charAt(i) - '0');
            bytes[j] = (byte) (bytes[j] | bit << k);
            if (k == 0) {
                j++;
                k = 7;
            } else {
                k--;
            }
        }
        return bytes;
    }

    public static Input getInput(String data) throws IOException {
        byte[] buffer = binaryStringToBytes(data.replace(" ", ""));
        final InputBitStream in = new InputBitStream(buffer);
        return new Input(buffer, in);
    }

    public static class Input {

        public final byte[] buffer;
        public final InputBitStream stream;

        private Input(byte[] buffer, InputBitStream stream) {
            this.buffer = buffer;
            this.stream = stream;
        }

    }

}
