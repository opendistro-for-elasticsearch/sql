/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.physical.node.join;

import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.physical.Row;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * List implementation to avoid normal hash table degrading into linked list.
 */
public class ListHashTable<T> implements HashTable<T> {

    private List<Row<T>> rows = new ArrayList<>();

    @Override
    public void add(Row<T> row) {
        rows.add(row);
    }

    @Override
    public Collection<Row<T>> match(Row<T> row) {
        return rows;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Collection<Object>>[] rightFieldWithLeftValues() {
        return new Map[]{new HashMap()};
    }

    @Override
    public int size() {
        return rows.size();
    }

    @Override
    public boolean isEmpty() {
        return rows.isEmpty();
    }

    @Override
    public void clear() {
        rows.clear();
    }
}
