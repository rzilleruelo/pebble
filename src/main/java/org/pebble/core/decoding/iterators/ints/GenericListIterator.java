package org.pebble.core.decoding.iterators.ints;

import org.pebble.core.PebbleBytesStore;
import org.pebble.core.decoding.InputBitStream;

import java.io.IOException;

import static org.pebble.core.encoding.DefaultParametersValues.DEFAULT_MIN_INTERVAL_SIZE;

public class GenericListIterator extends BaseListIterator {

    private GenericListIterator(
        final int listIndex,
        final int valueBitSize,
        final int minIntervalSize,
        final InputBitStream inputBitStream,
        final PebbleBytesStore bytesStore
    ) throws IOException {
        super(listIndex, valueBitSize, minIntervalSize, inputBitStream, bytesStore);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ReferenceIterator initializeReferenceIterator(
        final int listIndex,
        final InputBitStream inputBitStream
    ) throws IOException {
        return new GenericReferenceIterator(
            listIndex,
            valueBitSize,
            minIntervalSize,
            inputBitStream,
            bytesStore
        );
    }

    /**
     * Instance builder.
     * @param listIndex index of the current list.
     * @param valueBitSize fixed number of bits used to represent value in list to be encoded. It can be any value
     *                     between 1bit and 31 bits.
     * @param bytesStore mapping between list offsets and data bytes arrays and bytes offsets.
     * @return built instance.
     * @throws IOException when there is an exception reading from <code>inputBitStream</code>.
     */
    public static GenericListIterator build(
        final int listIndex,
        final int valueBitSize,
        final PebbleBytesStore bytesStore
    ) throws IOException {
        return build(listIndex, valueBitSize, bytesStore, bytesStore.getInputBitStream(listIndex));
    }

    /**
     * Instance builder.
     * @param listIndex index of the current list.
     * @param valueBitSize fixed number of bits used to represent value in list to be encoded. It can be any value
     *                     between 1bit and 31 bits.
     * @param inputBitStream input bit stream used to read the compressed lists representations. The cursor must be
     *                       positioned at the beginning of encoding.
     * @param bytesStore mapping between list offsets and data bytes arrays and bytes offsets.
     * @return built instance.
     * @throws IOException when there is an exception reading from <code>inputBitStream</code>.
     */
    public static GenericListIterator build(
        final int listIndex,
        final int valueBitSize,
        final PebbleBytesStore bytesStore,
        final InputBitStream inputBitStream
    ) throws IOException {
        inputBitStream.readInt(2);
        return new GenericListIterator(listIndex, valueBitSize, DEFAULT_MIN_INTERVAL_SIZE, inputBitStream, bytesStore);
    }

}
