package org.pebble.core.exceptions;

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

/**
 * Exception raised when the delta value between two elements is too big.
 */
public class DeltaValueIsTooBigException extends IllegalArgumentException {

    private final long a;
    private final long b;

    /**
     * Initialize delta value exception for values <code>a</code> and <code>b</code>.
     * @param a Smaller value.
     * @param b Bigger value.
     */
    public DeltaValueIsTooBigException(final long a, final long b) {
        this.a = a;
        this.b = b;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMessage() {
        return String.format("Delta value between %d and %d, is too big", b, a);
    }

}
