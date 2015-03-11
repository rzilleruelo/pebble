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

package org.pebble.types.timestamp;

import org.pebble.types.TypeIntEncoder;

import java.io.IOException;
import java.sql.Timestamp;

/**
 * Class used to encode a list of {@link java.sql.Timestamp} types into a list of integers that can be compressed by
 * the core pebble functionality.
 */
public class TimestampIntEncoder extends TypeIntEncoder<Timestamp> {

    /**
     * Gets the unix timestamp of <code>element</code> in seconds and store it as integer.
     * @param element to be encoded.
     * @return <code>int</code> value of encoded <code>element</code>.
     * @throws IOException is never raised. Current encoding implementation cannot raise this exception.
     */
    @Override
    protected int encode(final Timestamp element) throws IOException {
        return (int) (element.getTime() / 1000);
    }

}
