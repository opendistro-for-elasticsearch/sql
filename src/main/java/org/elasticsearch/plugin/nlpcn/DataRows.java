package org.elasticsearch.plugin.nlpcn;

import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DataRows implements Iterable<DataRows.Row> {

    private Object queryResult;

    private long size;
    private long totalHits;
    private List<Row> rows;

    public DataRows(Object queryResult) {
        this.queryResult = queryResult;

        extractData();
    }

    public long getSize() { return size; }

    public long getTotalHits() { return totalHits; }

    /**
     * Extract data from query results into Row objects
     * Need to cover two cases:
     * 1. queryResult is a SearchHits object
     * 2. queryResult is an Aggregations object
     *
     * Ignoring queryResult being ActionResponse (from executeDeleteAction), there should be no data in this case
     */
    public void extractData() {
        if (queryResult instanceof SearchHits) {
            SearchHits searchHits = (SearchHits) queryResult;

            this.size = searchHits.getHits().length;
            this.totalHits = searchHits.totalHits;
            this.rows = populateRows(searchHits);

        } else if (queryResult instanceof Aggregations) {
            Aggregations aggregations = (Aggregations) queryResult;

            // TODO retrieve size and totalHits information if relevant here (is totalHits individual or number of aggegrate groups?)
            this.rows = populateRows(aggregations);
        }
    }

    private List<Row> populateRows(SearchHits searchHits) {
        List<Row> rows = new ArrayList<>();
        for (SearchHit hit : searchHits) {
            rows.add(new Row(hit.getSourceAsMap()));
        }
        return rows;
    }

    private List<Row> populateRows(Aggregations aggregations) {
        List<Row> rows = new ArrayList<>();
        List<Aggregation> aggs = aggregations.asList();
        // TODO try GROUP BY different types to see other instances of Aggregations (so far only expecting Long/Float/StringTerms)
        if (!aggs.isEmpty()) {
            Terms terms = (Terms) aggs.get(0);
            String field = terms.getName();

            for (Terms.Bucket bucket : terms.getBuckets()) {
                rows.add(new Row(addMap(field, bucket.getKey())));
            }
        }
        return rows;
    }

    private Map<String, Object> addMap(String field, Object term) {
        Map<String, Object> data = new HashMap<>();
        data.put(field, term);
        return data;
    }

    // TODO implement an iterator method
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
    public class Row {

        private Map<String, Object> data;

        public Row(Map<String, Object> data) {
            this.data = data;
        }

        public Map<String, Object> getData() { return data; }

        public boolean hasField(String field) { return data.containsKey(field); }

        public Object getData(String field) { return data.get(field); }
    }
}
