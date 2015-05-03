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

package org.pebble.core;

/**
 * Interface used to access the mapping between an entry and the starting offset in bits of its encoded representation.
 */
public interface PebbleOffsetsStore {

    /**
     * Returns the offset of the start of the encoded representation of <code>index</code>-th entry.
     * @param index of entry.
     * @return offset in bits of the start of the encoded representation of <code>index</code>-th entry.
     */
    public long get(int index);

}
