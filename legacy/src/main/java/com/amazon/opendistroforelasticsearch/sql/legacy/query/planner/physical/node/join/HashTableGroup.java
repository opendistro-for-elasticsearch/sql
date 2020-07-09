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
import com.google.common.collect.Sets;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.logical.node.Join.JoinCondition;

/**
 * Hash table group with each hash table per AND join condition.
 */
public class HashTableGroup<T> implements HashTable<T> {

    private final HashTable<T>[] hashTables;

    /**
     * Number of rows stored in the hash table (in other words, = block size)
     */
    private int numOfRows = 0;

    @SuppressWarnings("unchecked")
    public HashTableGroup(JoinCondition condition) {
        int groupSize = condition.groupSize();
        if (groupSize == 0) {
            // Create one hash table (degraded to list) for Cross Join
            hashTables = new HashTable[]{new ListHashTable()};
        } else {
            hashTables = new HashTable[groupSize];
            for (int i = 0; i < groupSize; i++) {
                hashTables[i] = new DefaultHashTable<>(
                        condition.leftColumnNames(i),
                        condition.rightColumnNames(i)
                );
            }
        }
    }

    @Override
    public void add(Row<T> row) {
        for (HashTable<T> hashTable : hashTables) {
            hashTable.add(row);
        }
        numOfRows++;
    }

    @Override
    public Collection<Row<T>> match(Row<T> row) {
        Set<Row<T>> allMatched = Sets.newIdentityHashSet();
        for (HashTable<T> hashTable : hashTables) {
            allMatched.addAll(hashTable.match(row));
        }
        return allMatched;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Collection<Object>>[] rightFieldWithLeftValues() {
        return Arrays.stream(hashTables).
                map(hashTable -> hashTable.rightFieldWithLeftValues()[0]). // Make interface consistent
                toArray(Map[]::new);
    }

    @Override
    public boolean isEmpty() {
        return numOfRows == 0;
    }

    @Override
    public int size() {
        return numOfRows;
    }

    @Override
    public void clear() {
        for (HashTable<T> hashTable : hashTables) {
            hashTable.clear();
        }
        numOfRows = 0;
    }

}
