package org.pebble.core.encoding;

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

/**
 * Provides constants with default values for compression parameters.
 */
public class DefaultParametersValues {

    /**
     * Default number of bits to store an integer.
     */
    public static final int INT_BITS = 31;

    /**
     * Default number of bits to store a long integer.
     */
    public static final int LONG_BITS = 63;

    /**
     * Min interval size to be encoded as interval.
     */
    public static final int DEFAULT_MIN_INTERVAL_SIZE = 4;

    /**
     * Past list reference buffer size. If this value gets bigger, more lists are kept on the buffer, increasing
     * the chances of finding a better reference candidate and therefor increasing compression. But it will
     * increase the usage of memory and the time required to find a reference candidate.
     */
    public static final int DEFAULT_REFERENCE_WINDOW_SIZE = 1000;

    /**
     * Minimum size of reference list. Smaller lists are discarded from the potential candidates set.
     */
    public static final int DEFAULT_MIN_REFERENCE_LIST_SIZE = 1;

    /**
     * Maximum number of reference that a reference list can have in order to be use a reference. Increasing this numbers
     * increases the chances of finding a better reference candidate and therefor increasing compression. But it will
     * increase the time required to decompress a list.
     */
    public static final int DEFAULT_MAX_RECURSIVE_REFERENCES = 3;

    private DefaultParametersValues() {

    }

}
