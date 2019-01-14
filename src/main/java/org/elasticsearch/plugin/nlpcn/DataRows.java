package org.elasticsearch.plugin.nlpcn;

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
        this.rows = rows;
    }

    public long getSize() { return size; }

    public long getTotalHits() { return totalHits; }

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

        public Map<String, Object> getContents() { return data; }

        public boolean hasField(String field) { return data.containsKey(field); }

        public Object getData(String field) { return data.get(field); }

        public Object getDataOrDefault(String field, Object defaultValue) {
            return data.getOrDefault(field, defaultValue);
        }
    }
}
