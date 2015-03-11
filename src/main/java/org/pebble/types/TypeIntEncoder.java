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

import it.unimi.dsi.fastutil.ints.IntList;

import java.io.IOException;
import java.util.List;

/**
 * Abstract class used to encode a list of a complex types into a list of integers that can be compressed by the core
 * pebble functionality. This class defines the base for such kind of encoders, expanding pebble capabilities to
 * compress complex data types.
 * @param <T> type supported by the encoder.
 */
public abstract class TypeIntEncoder<T> {

    /**
     * Encodes a list of complex data types <code>T</code> into a list of integers applying
     * {@link org.pebble.types.TypeIntEncoder#encode(Object)} function.
     * @param list containing complex elements types.
     * @param listBuffer where the encoding will be stored.
     * @throws IOException in case there is an exception storing the encoded elements.
     */
    public void setIntList(final List<T> list, final IntList listBuffer) throws IOException {
        listBuffer.clear();
        for (T element : list) {
            listBuffer.add(encode(element));
        }
    }

    /**
     * Encodes an <code>element</code> into an integer.
     * @param element to be encoded to an integer.
     * @return <code>int</code> value of encoded <code>element</code>.
     * @throws IOException in case there is an exception storing the encoded element.
     */
    protected abstract int encode(final T element)  throws IOException;

}
