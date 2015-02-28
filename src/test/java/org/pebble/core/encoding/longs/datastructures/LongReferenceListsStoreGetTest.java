package org.pebble.core.encoding.longs.datastructures;

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
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.pebble.UnitTest;

import static junit.framework.TestCase.assertNull;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@Category(UnitTest.class)
public class LongReferenceListsStoreGetTest {

    @Test
    public void whenNotFindingGoodMatchForListItShouldReturnNull() {
        final int valueBitSize = 1;
        final LongReferenceListsIndex referenceListsIndex = mock(LongReferenceListsIndex.class);
        doReturn(-1).when(referenceListsIndex).getIndexOfReferenceList(
            any(LongList.class),
            anyInt(),
            anyInt(),
            any(LongList[].class),
            any(int[].class),
            any(int[].class)
        );
        final int size = 3;
        final int maxRecursiveReferences = 1;
        final int minListSize = 3;
        final LongReferenceListsStore referenceListsStore = new LongReferenceListsStore(
            size,
            maxRecursiveReferences,
            minListSize,
            referenceListsIndex
        );
        final int listIndex = 0;
        final LongList list = new LongArrayList(new long[] {1L, 2L, 3L, 4L, 5L, 6L});

        final LongReferenceListsStore.ReferenceList referenceList = referenceListsStore.get(list, valueBitSize, listIndex);

        assertNull(referenceList);
    }

    @Test
    public void whenFindingGoodMatchForListItShouldExpectedReferenceList() {
        final int valueBitSize = 1;
        final LongReferenceListsIndex referenceListsIndex = mock(LongReferenceListsIndex.class);
        doReturn(0).when(referenceListsIndex).getIndexOfReferenceList(
            any(LongList.class),
            anyInt(),
            anyInt(),
            any(LongList[].class),
            any(int[].class),
            any(int[].class)
        );
        final int size = 3;
        final int maxRecursiveReferences = 1;
        final int minListSize = 3;
        final LongReferenceListsStore referenceListsStore = new LongReferenceListsStore(
            size,
            maxRecursiveReferences,
            minListSize,
            referenceListsIndex
        );
        final int listIndex = 0;
        final LongList list = new LongArrayList(new long[] {1L, 2L, 3L, 4L, 5L, 6L});

        final LongReferenceListsStore.ReferenceList referenceList = referenceListsStore.get(list, valueBitSize, listIndex);

        assertThat(referenceList, instanceOf(LongReferenceListsStore.ReferenceList.class));
    }

}
