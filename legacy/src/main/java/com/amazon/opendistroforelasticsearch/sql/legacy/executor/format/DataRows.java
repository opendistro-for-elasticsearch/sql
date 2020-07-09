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

package com.amazon.opendistroforelasticsearch.sql.legacy.executor.format;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DataRows implements Iterable<DataRows.Row> {

    private long size;
    private long totalHits;
    private List<Row> rows;

    public DataRows(long size, long totalHits, List<Row> rows) {
        this.size = size;
        this.totalHits = totalHits;
        this.rows = rows;
    }

    public DataRows(List<Row> rows) {
        this.size = rows.size();
        this.totalHits = rows.size();
        this.rows = rows;
    }

    public long getSize() {
        return size;
    }

    public long getTotalHits() {
        return totalHits;
    }

    // Iterator method for DataRows
    @Override
    public Iterator<Row> iterator() {
        return new Iterator<Row>() {
            private final Iterator<Row> iter = rows.iterator();

            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }

            @Override
            public Row next() {
                return iter.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("No changes allowed to DataRows rows");
            }
        };
    }

    // Inner class for Row object
    public static class Row {

        private Map<String, Object> data;

        public Row(Map<String, Object> data) {
            this.data = data;
        }

        public Map<String, Object> getContents() {
            return data;
        }

        public boolean hasField(String field) {
            return data.containsKey(field);
        }

        public Object getData(String field) {
            return data.get(field);
        }

        public Object getDataOrDefault(String field, Object defaultValue) {
            return data.getOrDefault(field, defaultValue);
        }
    }
}
