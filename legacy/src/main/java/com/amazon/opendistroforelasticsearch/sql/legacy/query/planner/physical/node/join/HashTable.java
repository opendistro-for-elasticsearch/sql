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

import java.util.Collection;
import java.util.Map;

/**
 * Hash table interface
 *
 * @param <T> data object type
 */
public interface HashTable<T> {

    /**
     * Add one row to the hash table
     *
     * @param row row
     */
    void add(Row<T> row);


    /**
     * Find all matched row(s) in the hash table.
     *
     * @param row row to be matched
     * @return all matches
     */
    Collection<Row<T>> match(Row<T> row);


    /**
     * Mapping from right field to value(s) of left size
     *
     * @return
     */
    Map<String, Collection<Object>>[] rightFieldWithLeftValues();


    /**
     * Get size of hash table
     *
     * @return size of hash table
     */
    int size();


    /**
     * Is hash table empty?
     *
     * @return true for yes
     */
    boolean isEmpty();


    /**
     * Clear internal data structure
     */
    void clear();

}
