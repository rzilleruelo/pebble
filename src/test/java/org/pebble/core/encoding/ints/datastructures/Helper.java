package org.pebble.core.encoding.ints.datastructures;

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

import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import it.unimi.dsi.fastutil.ints.IntList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Helper {

    public static Map<Integer, List<Integer>> translateToUtilsCollection(final Int2ReferenceMap<IntList> map) {
        //TODO: fix equals of Int2ReferenceMap or replace with trove library.
        final Map<Integer, List<Integer>> translatedMap = new HashMap<Integer, List<Integer>>();
        List<Integer> list;
        for(Map.Entry<Integer, IntList> entry : map.entrySet()) {
            translatedMap.put(entry.getKey(), list = new ArrayList<Integer>());
            for(int value : entry.getValue()) {
                list.add(value);
            }
        }
        return translatedMap;
    }

    public static List<List<Integer>> translateToUtilsCollection(final IntList[] lists) {
        //TODO: fix equals of Int2ReferenceMap or replace with trove library.
        final List<List<Integer>> translatedLists = new ArrayList<List<Integer>>();
        List<Integer> translatedList;
        for(IntList list : lists) {
            translatedLists.add(translatedList = new ArrayList<Integer>());
            for(Integer value : list) {
                translatedList.add(value);
            }
        }
        return translatedLists;
    }

}
