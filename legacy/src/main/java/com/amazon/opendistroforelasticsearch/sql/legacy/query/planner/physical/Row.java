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

package com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.physical;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 * Abstraction for access methods of data object, ex. SearchHit.
 *
 * @param <T> data object underlying
 */
public interface Row<T> {

    Row NULL = null;

    /**
     * Generate key to represent identity of the row.
     *
     * @param colNames column names as keys
     * @return row key
     */
    RowKey key(String[] colNames);


    /**
     * Combine current row and another row together to generate a new combined row.
     *
     * @param otherRow another row
     * @return combined row
     */
    Row<T> combine(Row<T> otherRow);


    /**
     * Retain columns specified and rename to alias if any.
     *
     * @param colNameAlias column names to alias mapping
     */
    void retain(Map<String, String> colNameAlias);


    /**
     * @return raw data of row wrapped inside
     */
    T data();


    /**
     * Key that help Row be sorted or hashed.
     */
    class RowKey implements Comparable<RowKey> {

        /**
         * Represent null key if any joined column value is NULL
         */
        public static final RowKey NULL = null;

        /**
         * Values of row key
         */
        private final Object[] keys;

        /**
         * Cached hash code since this class is intended to be used by hash table
         */
        private final int hashCode;

        public RowKey(Object... keys) {
            this.keys = keys;
            this.hashCode = Objects.hash(keys);
        }

        public Object[] keys() {
            return keys;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof RowKey && Arrays.deepEquals(this.keys, ((RowKey) other).keys);
        }

        @SuppressWarnings("unchecked")
        @Override
        public int compareTo(RowKey other) {
            for (int i = 0; i < keys.length; i++) {

                /*
                 * Only one is null, otherwise (both null or non-null) go ahead.
                 * Always consider NULL is smaller value which means NULL comes last in ASC and first in DESC
                 */
                if (keys[i] == null ^ other.keys[i] == null) {
                    return keys[i] == null ? 1 : -1;
                }

                if (keys[i] instanceof Comparable) {
                    int result = ((Comparable) keys[i]).compareTo(other.keys[i]);
                    if (result != 0) {
                        return result;
                    }
                } // Ignore incomparable field silently?
            }
            return 0;
        }

        @Override
        public String toString() {
            return "RowKey: " + Arrays.toString(keys);
        }

    }
}
