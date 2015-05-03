package org.pebble.utils;

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

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import org.pebble.core.PebbleOffsetsStore;
import org.pebble.core.PebbleOffsetsStoreWriter;

import java.io.IOException;

/**
 * Class that implements offsets store for reading and writing offsets interfaces. This implementation
 * stores the offsets in a {@link it.unimi.dsi.fastutil.longs.LongList} and therefore any compression is performed
 * on the data. This is just a simple implementation that works as an example on how the
 * {@link org.pebble.core.PebbleOffsetsStore} and {@link org.pebble.core.PebbleOffsetsStoreWriter} can be implemented.
 * Though it should not be used on big data sets.
 */
public class LongListPebbleOffsetsStore implements PebbleOffsetsStore, PebbleOffsetsStoreWriter {

    protected final LongList offsets;

    /**
     * Initializes and empty offsets store
     */
    public LongListPebbleOffsetsStore() {
        offsets = new LongArrayList();
    }

    /**
     * Initializes an store containing the offsets provided in <code>offsets</code>.
     * @param offsets array containing offsets.
     */
    public LongListPebbleOffsetsStore(long[] offsets) {
        this.offsets = new LongArrayList(offsets);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void append(long offset) throws IOException {
        offsets.add(offset);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long get(int index) {
        return offsets.get(index);
    }

}
