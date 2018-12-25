package org.elasticsearch.plugin.nlpcn;

import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.NumericMetricsAggregation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DataRows implements Iterable<DataRows.Row> {

    private Object queryResult;
    private List<String> head;

    private long size;
    private long totalHits;
    private List<Row> rows;

    public DataRows(Object queryResult, List<String> head) {
        this.queryResult = queryResult;
        this.head = head;

        extractData();
    }

    public DataRows(List<Row> rows) {
        this.size = rows.size();
        this.rows = rows;
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
    private void extractData() {
        if (queryResult instanceof SearchHits) {
            SearchHits searchHits = (SearchHits) queryResult;

            this.size = searchHits.getHits().length;
            this.totalHits = searchHits.totalHits;
            this.rows = populateRows(searchHits);

        } else if (queryResult instanceof Aggregations) {
            Aggregations aggregations = (Aggregations) queryResult;

            this.rows = populateRows(aggregations);
            this.size = rows.size();
        }
    }

    private List<Row> populateRows(SearchHits searchHits) {
        List<Row> rows = new ArrayList<>();
        Set<String> newKeys = new HashSet<>(head);
        for (SearchHit hit : searchHits) {
            Map<String, Object> rowSource = hit.getSourceAsMap();
            List<Row> result = new ArrayList<>();
            result.add(new Row(rowSource));

            rowSource = flatRow(head, rowSource);
            rowSource.put("_score", hit.getScore());
            result = flatNestedField(newKeys, rowSource, hit.getInnerHits());

            rows.addAll(result);
        }

        return rows;
    }

    private List<Row> populateRows(Aggregations aggregations) {
        List<Row> rows = new ArrayList<>();
        List<Aggregation> aggs = aggregations.asList();
        if (hasTermAggregations(aggs)) {
            Terms terms = (Terms) aggs.get(0);
            String field = terms.getName();

            for (Terms.Bucket bucket : terms.getBuckets()) {
                List<Row> aggRows = new ArrayList<>();
                getAggsData(bucket, aggRows, addMap(field, bucket.getKey()));

                rows.addAll(aggRows);
            }
        } else {
            // This occurs for cases like "SELECT AVG(age) FROM bank" where we aggregate in SELECT with no GROUP BY
            rows.add(
                    new Row(
                            addNumericAggregation(aggs, new HashMap<>())
                    )
            );
        }
        return rows;
    }

    /**
     * This recursive method goes through the buckets iterated through populateRows() and flattens any inner
     * aggregations and puts that data as a Map into a Row (this nested aggregation happens when we GROUP BY
     * multiple fields)
     */
    private void getAggsData(Terms.Bucket bucket, List<Row> aggRows, Map<String, Object> data) {
        List<Aggregation> aggs = bucket.getAggregations().asList();
        if (hasTermAggregations(aggs)) {
            Terms terms = (Terms) aggs.get(0);
            String field = terms.getName();

            for (Terms.Bucket innerBucket : terms.getBuckets()) {
                data.put(field, innerBucket.getKey());
                getAggsData(innerBucket, aggRows, data);
                data.remove(field);
            }
        } else {
            data = addNumericAggregation(aggs, data);
            aggRows.add(new Row(new HashMap<>(data)));
        }
    }

    /**
     * hasTermAggregations() checks for specific type of aggregation, one that contains Terms. This is the case when the
     * aggregations contains the contents of a GROUP BY field.
     *
     * If the aggregation contains the data for an aggregation function (ex. COUNT(*)), the items in the list will
     * be of instance InternalValueCount, InternalSum, etc. (depending on the aggregation function) and will be
     * considered a base case of getAggsData() which will add that data to the Row (if it exists).
     */
    private boolean hasTermAggregations(List<Aggregation> aggs) {
        return !aggs.isEmpty() && aggs.get(0) instanceof Terms;
    }

    /**
     * Adds the contents of Aggregation (specifically the NumericMetricsAggregation.SingleValue instance) from
     * bucket.aggregations into the data map
     */
    private Map<String, Object> addNumericAggregation(List<Aggregation> aggs, Map<String, Object> data) {
        for (Aggregation aggregation : aggs) {
            NumericMetricsAggregation.SingleValue numericAggs = (NumericMetricsAggregation.SingleValue) aggregation;
            data.put(numericAggs.getName(), numericAggs.value());
        }

        return data;
    }

    /**
     * Simplifies the structure of row's source Map by flattening it, making the full path of an object the key
     * and the Object it refers to the value. This handles the case of regular object since nested objects will not
     * be in hit.source but rather in hit.innerHits
     *
     * Sample input:
     *   keys = ['comments.likes']
     *   row = comments: {
     *     likes: 2
     *   }
     *
     * Return:
     *   flattenedRow = {comment.likes: 2}
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> flatRow(List<String> keys, Map<String, Object> row) {
        Map<String, Object> flattenedRow = new HashMap<>();
        for (String key : keys) {
            String[] splitKeys = key.split("\\.");
            boolean found = true;
            Object currentObj = row;

            for (String splitKey : splitKeys) {
                // This check is made to prevent Cast Exception as an ArrayList of objects can be in the sourceMap
                if (!(currentObj instanceof Map)) {
                    found = false;
                    break;
                }

                // TODO need to check case where 'row' is null (ex. sourceMap is null, maybe when field doesn't exist?)
                Map<String, Object> currentMap = (Map<String, Object>) currentObj;
                if (!currentMap.containsKey(splitKey)) {
                    found = false;
                    break;
                }

                currentObj = currentMap.get(splitKey);
            }

            if (found) {
                flattenedRow.put(key, currentObj);
            }
        }

        return flattenedRow;
    }

    /**
     * If innerHits associated with column name exists, flatten both the inner field name and the inner rows in it.
     *
     * Sample input:
     *   newKeys = {'region', 'employees.age'}, row = {'region': 'US'}
     *   innerHits = employees: {
     *     hits: [{
     *       source: {
     *         age: 26,
     *         firstname: 'Hank'
     *       }
     *     },{
     *       source: {
     *         age: 30,
     *         firstname: 'John'
     *       }
     *     }]
     *  }
     */
    private List<Row> flatNestedField(Set<String> newKeys, Map<String, Object> row, Map<String, SearchHits> innerHits) {
        List<Row> result = new ArrayList<>();
        result.add(new Row(row));

        if (innerHits == null) {
            return result;
        }

        for (String colName : innerHits.keySet()) {
            SearchHit[] colValue = innerHits.get(colName).getHits();
            doFlatNestedFieldName(colName, colValue, newKeys);
            result = doFlatNestedFieldValue(colName, colValue, result);
        }

        return result;
    }

    private void doFlatNestedFieldName(String colName, SearchHit[] colValue, Set<String> keys) {
        Map<String, Object> innerRow = colValue[0].getSourceAsMap();
        for (String field : innerRow.keySet()) {
            String innerName = colName + "." + field;
            keys.add(innerName);
        }

        keys.remove(colName);
    }

    /**
     * Do Cartesian Product between current outer row and inner rows by nested loop and remove original outer row.
     *
     * Sample input:
     *   colName = 'employees', rows = [{region: 'US'}]
     *   colValue= [{
     *     source: {
     *       age: 26,
     *       firstname: 'Hank'
     *     }
     *   },{
     *     source: {
     *       age: 30,
     *       firstname: 'John'
     *     }
     *   }]
     *
     *   Return:
     *   [
     *     {region:'US', employees.age:26, employees.firstname:'Hank'},
     *     {region:'US', employees.age:30, employees.firstname:'John'}
     *   ]
     */
    private List<Row> doFlatNestedFieldValue(String colName, SearchHit[] colValue, List<Row> rows) {
        List<Row> result = new ArrayList<>();
        for (Row row : rows) {
            for (SearchHit hit : colValue) {
                Map<String, Object> innerRow = hit.getSourceAsMap();
                Map<String, Object> copy = new HashMap<>();

                for (String field : row.getContents().keySet()) {
                    copy.put(field, row.getData(field));
                }
                for (String field : innerRow.keySet()) {
                    copy.put(colName + "." + field, innerRow.get(field));
                }

                copy.remove(colName);
                result.add(new Row(copy));
            }
        }

        return result;
    }

    private Map<String, Object> addMap(String field, Object term) {
        Map<String, Object> data = new HashMap<>();
        data.put(field, term);
        return data;
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

        public Map<String, Object> getContents() { return data; }

        public boolean hasField(String field) { return data.containsKey(field); }

        public Object getData(String field) { return data.get(field); }

        public Object getDataOrDefault(String field, Object defaultValue) {
            return data.getOrDefault(field, defaultValue);
        }
    }
}
