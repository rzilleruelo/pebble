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

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.pebble.FastIntegrationTest;
import org.pebble.core.decoding.iterators.Helper.Input;
import org.pebble.core.PebbleBytesStore;
import org.pebble.core.PebbleOffsetsStore;
import org.pebble.core.PebbleOffsetsStoreWriter;
import org.pebble.core.encoding.EncodingInfo;
import org.pebble.core.encoding.Helper;
import org.pebble.core.encoding.OutputSuccinctStream;
import org.pebble.core.encoding.ints.datastructures.IntReferenceListsIndex;
import org.pebble.core.encoding.ints.datastructures.IntReferenceListsStore;
import org.pebble.core.encoding.ints.datastructures.InvertedListIntReferenceListsIndex;
import org.pebble.types.text.TextIntDecoder;
import org.pebble.types.text.TextIntEncoder;
import org.pebble.types.timestamp.TimestampIntDecoder;
import org.pebble.types.timestamp.TimestampIntEncoder;
import org.pebble.types.uuid.UUIDIntDecoder;
import org.pebble.types.uuid.UUIDIntEncoder;
import org.pebble.utils.BytesArrayPebbleBytesStore;
import org.pebble.utils.LongListPebbleOffsetsStore;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.pebble.core.decoding.iterators.Helper.getInput;
import static org.pebble.core.encoding.Helper.getOutput;
import static org.pebble.core.encoding.Helper.toBinaryString;

@Category(FastIntegrationTest.class)
public class MultiTypeTableTest {

    @Test
    public void itShouldCompressTable() throws IOException {
        final int storeSize = 10;
        final int maxRecursiveReferences = 10;
        final int minListSize = 3;
        final int valSize = 5;
        final Helper.Output out = getOutput();
        final IntReferenceListsIndex referenceListsIndex = new InvertedListIntReferenceListsIndex();
        final IntReferenceListsStore refListsStore = new IntReferenceListsStore(
            storeSize,
            maxRecursiveReferences,
            minListSize,
            referenceListsIndex
        );
        final OutputSuccinctStream outputStream = new OutputSuccinctStream(out.buffer);
        final PebbleOffsetsStoreWriter offsetsStore = new LongListPebbleOffsetsStore();
        final EncodingInfo encInfo = new EncodingInfo();
        final UUIDIntEncoder columnAEncoder = new UUIDIntEncoder(storeSize, outputStream, offsetsStore, encInfo);
        final IntList columnABuffer = new IntArrayList();
        final TextIntEncoder columnBEncoder = new TextIntEncoder(storeSize, outputStream, offsetsStore, encInfo);
        final IntList columnBBuffer = new IntArrayList();
        final TimestampIntEncoder columnCEncoder = new TimestampIntEncoder();
        final IntList columnCBuffer = new IntArrayList();
        final Map<Integer, List<UUID>> columnA = buildColumn(new UUID[][] {
            new UUID[] {new UUID(0L, 1L), new UUID(0L, 2L), new UUID(0L, 1L), new UUID(0L, 1L)},
            new UUID[] {new UUID(0L, 2L), new UUID(0L, 1L), new UUID(0L, 3L), new UUID(0L, 3L)}
        });
        final Map<Integer, List<String>> columnB = buildColumn(new String[][] {
            new String[] {"LABEL 1", "LABEL 2", "LABEL 1", "LABEL 2"},
            new String[] {"LABEL 1", "LABEL 1", "LABEL 3", "LABEL 2"},
        });
        final Map<Integer, List<Timestamp>> columnC = buildColumn(new Timestamp[][]{
            new Timestamp[]{new Timestamp(1000L), new Timestamp(2000L), new Timestamp(4000L), new Timestamp(5000L)},
            new Timestamp[]{new Timestamp(3000L), new Timestamp(4000L), new Timestamp(5000L), new Timestamp(6000L)}
        });

        /**
         * list=[1, 2, 1, 1]
         * mapping={1: 0, 2: 1}
         * 0...125...01
         * 0...124...010
         *
         * ["LABEL 1", "LABEL 2", "LABEL 1", "LABEL 2"]
         * mapping={"LABEL 1": 2, "LABEL 2": 3}
         * 7        L        A        B        E        L                 1
         * 00100000 01001100 01000001 01000010 01000101 01001100 00100000 00110001
         * 7        L        A        B        E        L                 2
         * 00100000 01001100 01000001 01000010 01000101 01001100 00100000 00110010
         *
         * mapped_list=[0, 1, 0, 0]
         * values=[0, 1] indexes=[0, 1, 0, 0]
         * reference=[0], intervals=[0], delta=[2, 0, 0],       indexes=[2, 0, 2, 1, 0]
         * 1              1              11   00000 1           11   1 11   10   1
         * 1              1              2-1  00000 1           2-1  1 2-1  2-0  1
         * 1              1              10-1 00000 1           10-1 1 10-1 10-0 1
         * 1              1              0101 00000 1           0101 1 0101 0100 1
         *
         * mapped_list=[2, 3, 2, 3]
         * values=[2, 3] indexes=[0, 1, 0, 1]
         * reference=[0], intervals=[0], delta=[2, 2, 0],       indexes=[2, 0, 2, 1, 2]
         * 1              1              11   00010 1           11   1 11   10   11
         * 1              1              2-1  00010 1           2-1  1 2-1  2-0  2-1
         * 1              1              10-1 00010 1           10-1 1 10-1 10-0 10-1
         * 1              1              0101 00010 1           0101 1 0101 0100 0101
         *
         * [1000, 2000 4000, 5000]
         * values=[1, 2, 4, 5] indexes=[0, 1, 2, 3]
         * reference=[0], intervals=[0], delta=[4, 1, 0, 1, 0], indexes=[0, 0, 2, 2, 2]
         * 1              1              101   00001 1 10   1   1 1 11   11   11
         * 1              1              3-01  00001 1 2-0  1   1 1 2-1  2-1  2-1
         * 1              1              11-01 00001 1 10-0 1   1 1 10-1 10-1 10-1
         * 1              1              01101 00001 1 0100 1   1 1 0101 0101 0101
         *
         * [2, 1, 3, 3]
         * mapping={1: 0, 2: 1, 3: 7}
         * 0...124...011
         *
         * ["LABEL 1", "LABEL 1", "LABEL 3", "LABEL 2"]
         * mapping={"LABEL 1": 2, "LABEL 2": 3, "LABEL 3": 8}
         * 7        L        A        B        E        L                 3
         * 00100000 01001100 01000001 01000010 01000101 01001100 00100000 00110011
         *
         * mapped_list=[1, 0, 7, 7]
         * values=[0, 1, 7] indexes=[1, 0, 2, 2]
         * reference=[0], intervals=[0], delta=[3, 0, 0, 5],    indexes=[1, 2, 1, 4, 0]
         * 1              1              100   00000 1 110      10   11   10   101   1
         * 1              1              3-00  00000 1 3-10     2-0  2-1  2-0  3-01  1
         * 1              1              11-00 00000 1 11-10    10-0 10-1 10-0 11-01 1
         * 1              1              01100 00000 1 01110    0100 0101 0100 01101 1
         *
         * mapped_list=[2, 2, 8, 3]
         * values=[2, 3, 8] indexes=[0, 0, 2, 1]
         * reference=[0], intervals=[0], delta=[3, 2, 0, 4],    indexes=[1, 0, 0, 4, 1]
         * 1              1              100   00010 1 101      10   1 1 101   10
         * 1              1              3-00  00010 1 3-01     2-0  1 1 3-01  2-0
         * 1              1              11-00 00010 1 11-01    10-0 1 1 11-01 10-0
         * 1              1              01100 00010 1 01101    0100 1 1 01101 0100
         *
         * [3000, 4000 5000, 6000]
         * values=[3, 4, 5, 6] indexes=[0, 1, 2, 3]
         * reference=[0], intervals=[1, 3, 0], delta=[0],       indexes=[0, 0, 2, 2, 2]
         * 1              10   00011 1         1                1 1 11   11   11
         * 1              2-0  00011 1         1                1 1 2-1  2-1  2-1
         * 1              10-0 00011 1         1                1 1 10-1 10-1 10-1
         * 1              0100 00011 1         1                1 1 0101 0101 0101
         */
        final String expectedOutput = (
            "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001" +
            "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000010" +
            "00100000 01001100 01000001 01000010 01000101 01001100 00100000 00110001" +
            "00100000 01001100 01000001 01000010 01000101 01001100 00100000 00110010" +
            "1 1 0101 00000 1 0101 1 0101 0100 1" +
            "1 1 0101 00010 1 0101 1 0101 0100 0101" +
            "1 1 01101 00001 1 0100 1 1 1 0101 0101 0101" +
            "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000011" +
            "00100000 01001100 01000001 01000010 01000101 01001100 00100000 00110011" +
            "1 1 01100 00000 1 01110 0100 0101 0100 01101 1" +
            "1 1 01100 00010 1 01101   0100 1 1 01101 0100" +
            "1 0100 00011 1 1 1 1 0101 0101 0101"
        ).replaceAll(" ", "");
        final int expectedTotalOffset = 758;
        final int expectedFinalIndex = 12;

        columnAEncoder.setIntList(columnA.get(0), columnABuffer);
        columnBEncoder.setIntList(columnB.get(0), columnBBuffer);
        columnCEncoder.setIntList(columnC.get(0), columnCBuffer);
        encInfo.incrementOffset(outputStream.writeList(columnABuffer, encInfo.getCurrentIndex(), valSize, refListsStore));
        encInfo.incrementCurrentIndex();
        encInfo.incrementOffset(outputStream.writeList(columnBBuffer, encInfo.getCurrentIndex(), valSize, refListsStore));
        encInfo.incrementCurrentIndex();
        encInfo.incrementOffset(outputStream.writeList(columnCBuffer, encInfo.getCurrentIndex(), valSize, refListsStore));
        encInfo.incrementCurrentIndex();

        columnAEncoder.setIntList(columnA.get(1), columnABuffer);
        columnBEncoder.setIntList(columnB.get(1), columnBBuffer);
        columnCEncoder.setIntList(columnC.get(1), columnCBuffer);
        encInfo.incrementOffset(outputStream.writeList(columnABuffer, encInfo.getCurrentIndex(), valSize, refListsStore));
        encInfo.incrementCurrentIndex();
        encInfo.incrementOffset(outputStream.writeList(columnBBuffer, encInfo.getCurrentIndex(), valSize, refListsStore));
        encInfo.incrementCurrentIndex();
        encInfo.incrementOffset(outputStream.writeList(columnCBuffer, encInfo.getCurrentIndex(), valSize, refListsStore));
        encInfo.incrementCurrentIndex();

        outputStream.close();
        assertEquals(expectedOutput, toBinaryString(out.buffer, (int) encInfo.getOffset()));
        assertEquals(expectedTotalOffset, encInfo.getOffset());
        assertEquals(expectedFinalIndex, encInfo.getCurrentIndex());
    }

    @Test
    public void itShouldDecompressTable() throws IOException {
        final Input input = getInput(
            "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001" +
            "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000010" +
            "00100000 01001100 01000001 01000010 01000101 01001100 00100000 00110001" +
            "00100000 01001100 01000001 01000010 01000101 01001100 00100000 00110010" +
            "1 1 0101 00000 1 0101 1 0101 0100 1" +
            "1 1 0101 00010 1 0101 1 0101 0100 0101" +
            "1 1 01101 00001 1 0100 1 1 1 0101 0101 0101" +
            "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000011" +
            "00100000 01001100 01000001 01000010 01000101 01001100 00100000 00110011" +
            "1 1 01100 00000 1 01110 0100 0101 0100 01101 1" +
            "1 1 01100 00010 1 01101   0100 1 1 01101 0100" +
            "1 0100 00011 1 1 1 1 0101 0101 0101"
        );
        final PebbleOffsetsStore offsetsStore = new LongListPebbleOffsetsStore(new long[] {
            0L, 128L, 256L, 320L, 384L, 410L, 439, 471L, 599L, 663L, 699L, 732L
        });
        final PebbleBytesStore bytesStore = new BytesArrayPebbleBytesStore(input.buffer, offsetsStore);
        final int valueBitSize = 5;
        final UUIDIntDecoder columnADecoder = new UUIDIntDecoder(bytesStore);
        final TextIntDecoder columnBDecoder = new TextIntDecoder(bytesStore);
        final TimestampIntDecoder columnCDecoder = new TimestampIntDecoder(bytesStore);
        final Map<Integer, List<UUID>> expectedColumnA = buildColumn(new UUID[][] {
            new UUID[] {new UUID(0L, 1L), new UUID(0L, 2L), new UUID(0L, 1L), new UUID(0L, 1L)},
            new UUID[] {new UUID(0L, 2L), new UUID(0L, 1L), new UUID(0L, 3L), new UUID(0L, 3L)}
        });
        final Map<Integer, List<String>> expectedColumnB = buildColumn(new String[][] {
            new String[] {"LABEL 1", "LABEL 2", "LABEL 1", "LABEL 2"},
            new String[] {"LABEL 1", "LABEL 1", "LABEL 3", "LABEL 2"},
        });
        final Map<Integer, List<Timestamp>> expectedColumnC = buildColumn(new Timestamp[][] {
            new Timestamp[] {new Timestamp(1000L), new Timestamp(2000L), new Timestamp(4000L), new Timestamp(5000L)},
            new Timestamp[] {new Timestamp(3000L), new Timestamp(4000L), new Timestamp(5000L), new Timestamp(6000L)}
        });
        final int[] listIndexes = new int[] {4, 9};
        final Map<Integer, List<UUID>> colA = buildColumn(new UUID[][] {new UUID[] {}, new UUID[] {}});
        final Map<Integer, List<String>> colB = buildColumn(new String[][] {new String[] {}, new String[] {}});
        final Map<Integer, List<Timestamp>> colC = buildColumn(new Timestamp[][] {new Timestamp[] {}, new Timestamp[] {}});
        Iterator<UUID> colAIterator;
        Iterator<String> colBIterator;
        Iterator<Timestamp> colCIterator;

        for (int i = 0; i < listIndexes.length; i++) {
            colAIterator = columnADecoder.read(listIndexes[i], valueBitSize);
            colBIterator = columnBDecoder.read(listIndexes[i] + 1, valueBitSize);
            colCIterator = columnCDecoder.read(listIndexes[i] + 2, valueBitSize);
            while (colAIterator.hasNext()) {
                colA.get(i).add(colAIterator.next());
                colB.get(i).add(colBIterator.next());
                colC.get(i).add(colCIterator.next());
            }
        }

        assertEquals(expectedColumnA, colA);
        assertEquals(expectedColumnB, colB);
        assertEquals(expectedColumnC, colC);
    }

    private static <T> Map<Integer, List<T>> buildColumn(T[][] values) {
        Map<Integer, List<T>> column = new HashMap<Integer, List<T>>();
        List<T> records;
        for (int i = 0; i < values.length; i ++) {
            column.put(i, records = new ArrayList<T>());
            for (int j = 0; j < values[i].length; j++) {
                records.add(values[i][j]);
            }
        }
        return column;
    }

}
