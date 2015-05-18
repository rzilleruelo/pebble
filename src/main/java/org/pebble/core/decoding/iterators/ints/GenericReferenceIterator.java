package org.pebble.core.decoding.iterators.ints;

import it.unimi.dsi.fastutil.ints.IntIterator;
import org.pebble.core.ListsClassifier;
import org.pebble.core.PebbleBytesStore;
import org.pebble.core.decoding.InputBitStream;

import java.io.IOException;

public class GenericReferenceIterator extends ReferenceIterator {

    /**
     * @param listIndex       offset of the current list that is described in terms of reference.
     * @param valueBitSize    fixed number of bits used to represent value in list to be encoded. It can be any value
     *                        between 1bit and 31 bits.
     * @param minIntervalSize min size of intervals used to encode the compressed list.
     * @param inputBitStream  input bit stream used to read the compressed lists representations.
     * @param bytesStore      mapping between list offsets and data bytes arrays and bytes offsets.
     * @throws java.io.IOException when there is an exception reading from <code>inputBitStream</code>.
     */
    public GenericReferenceIterator(
        final int listIndex,
        final int valueBitSize,
        final int minIntervalSize,
        final InputBitStream inputBitStream,
        final PebbleBytesStore bytesStore
    ) throws IOException {
        super(listIndex, valueBitSize, minIntervalSize, inputBitStream, bytesStore);
    }
    
    @Override
    public IntIterator getReferenceListIterator(int listIndex, InputBitStream inputBitStream) throws IOException {
        final int type = inputBitStream.readInt(2);
        switch (type) {
            case ListsClassifier.SORTED_SET_LIST:
                return StrictlyIncrementalListIterator.build(listIndex, valueBitSize, bytesStore, inputBitStream);
            case ListsClassifier.SORTED_LIST:
                return IncrementalListIterator.build(listIndex, valueBitSize, bytesStore, inputBitStream);
            default:
                return ListIterator.build(listIndex, valueBitSize, bytesStore, inputBitStream);
        }
    }
}
