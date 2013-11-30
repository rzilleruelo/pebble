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
 * Exception raised when an expected strictly incremental list is found not strictly incremental.
 */
public class NotStrictlyIncrementalListException extends IllegalArgumentException {

    private final long a;
    private final long b;

    /**
     * Initialize exception for values <code>a</code> and <code>b</code>.
     * @param a First value in list which is bigger than <code>b</code>.
     * @param b Following value in list which is smaller than <code>a</code>.
     */
    public NotStrictlyIncrementalListException (final long a, final long b) {
        this.a = a;
        this.b = b;
    }

    /**
     * {@inheritDoc}
     */
    public String getMessage() {
        return String.format("List is not strictly incremental, found %d after %d", b, a);
    }

}
