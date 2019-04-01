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

package com.amazon.opendistroforelasticsearch.sql.executor.format;

import org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.Strings;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.NumericMetricsAggregation;
import com.amazon.opendistroforelasticsearch.sql.domain.Field;
import com.amazon.opendistroforelasticsearch.sql.domain.JoinSelect;
import com.amazon.opendistroforelasticsearch.sql.domain.MethodField;
import com.amazon.opendistroforelasticsearch.sql.domain.Query;
import com.amazon.opendistroforelasticsearch.sql.domain.Select;
import com.amazon.opendistroforelasticsearch.sql.domain.TableOnJoinSelect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsResponse.FieldMappingMetaData;

public class SelectResultSet extends ResultSet {

    private Query query;
    private Object queryResult;

    private boolean selectAll;
    private String indexName;
    private String typeName;
    private List<Schema.Column> columns = new ArrayList<>();

    private List<String> head;
    private long size;
    private long totalHits;
    private List<DataRows.Row> rows;

    public SelectResultSet(Client client, Query query, Object queryResult) {
        this.client = client;
        this.query = query;
        this.queryResult = queryResult;
        this.selectAll = false;

        if (isJoinQuery()) {
            JoinSelect joinQuery = (JoinSelect) query;
            loadFromEsState(joinQuery.getFirstTable());
            loadFromEsState(joinQuery.getSecondTable());
        } else {
            loadFromEsState(query);
        }
        this.schema = new Schema(indexName, typeName, columns);
        this.head = schema.getHeaders();

        extractData();
        this.dataRows = new DataRows(size, totalHits, rows);
    }

    /***********************************************************
          Logic for loading Columns to be stored in Schema
     ***********************************************************/


    /**
     * Makes a request to local node to receive meta data information and maps each field specified in SELECT to its
     * type in the index mapping
     */
    private void loadFromEsState(Query query) {
        String indexName = fetchIndexName(query);
        String typeName = fetchTypeName(query);
        String[] fieldNames = fetchFieldsAsArray(query);

        if (fieldNames.length == 0)
            selectAll = true;

        GetFieldMappingsRequest request = new GetFieldMappingsRequest()
                .indices(indexName)
                .types(emptyArrayIfNull(typeName))
                .fields(selectAllFieldsIfEmpty(fieldNames))
                .local(true);
        GetFieldMappingsResponse response = client.admin().indices()
                .getFieldMappings(request)
                .actionGet();

        Map<String, Map<String, Map<String, FieldMappingMetaData>>> mappings = response.mappings();
        if (mappings.isEmpty()) {
            throw new IllegalArgumentException(String.format("Index type %s does not exist", query.getFrom()));
        }

        // Assumption is all indices share the same mapping which is validated in TermFieldRewriter.
        Map<String, Map<String, FieldMappingMetaData>> indexMappings = mappings.values().iterator().next();

        /*
         * There are three cases regarding type name to consider:
         * 1. If the correct type name was given, its typeMapping is retrieved
         * 2. If the incorrect type name was given then the response is null
         * 3. If no type name is given, the indexMapping is searched for a typeMapping
         */
        Map<String, FieldMappingMetaData> typeMappings = new HashMap<>();
        if (indexMappings.containsKey(typeName)) {
            typeMappings = indexMappings.get(typeName);
        } else {
            // Assuming ES version 6.x, there can be only one type per index so this for loop should grab the only type
            for (String type : indexMappings.keySet()) {
                typeMappings = indexMappings.get(type);
            }
        }

        this.indexName = this.indexName == null ? indexName : (this.indexName + "|" + indexName);
        this.typeName = this.typeName == null ? typeName : (this.typeName + "|" + typeName);
        this.columns.addAll(renameColumnWithTableAlias(query, populateColumns(query, fieldNames, typeMappings)));
    }

    /** Rename column name with table alias as prefix for join query */
    private List<Schema.Column> renameColumnWithTableAlias(Query query, List<Schema.Column> columns) {
        List<Schema.Column> renamedCols;
        if ((query instanceof TableOnJoinSelect)
                && !Strings.isNullOrEmpty(((TableOnJoinSelect) query).getAlias())) {

            TableOnJoinSelect joinQuery = (TableOnJoinSelect) query;
            renamedCols = new ArrayList<>();

            for (Schema.Column column : columns) {
                renamedCols.add(new Schema.Column(
                    joinQuery.getAlias() + "." + column.getName(),
                    column.getAlias(),
                    Schema.Type.valueOf(column.getType().toUpperCase())
                ));
            }
        } else {
            renamedCols = columns;
        }
        return renamedCols;
    }

    private boolean isSelectAll() { return selectAll; }

    private boolean containsWildcard(Query query) {
        for (Field field : fetchFields(query)) {
            if (!(field instanceof MethodField) && field.getName().contains("*"))
                return true;
        }

        return false;
    }

    private String fetchIndexName(Query query) { return query.getFrom().get(0).getIndex(); }

    private String fetchTypeName(Query query) { return query.getFrom().get(0).getType(); }

    /**
     * queryResult is checked to see if it's of type Aggregation in which case the aggregation fields in GROUP BY
     * are returned as well. This prevents returning a Schema of all fields when SELECT * is called with
     * GROUP BY (since all fields will be retrieved from the typeMappings request when no fields are returned from
     * fetchFields()).
     *
     * After getting all of the fields from GROUP BY, the fields from SELECT are iterated and only the fields of type
     * MethodField are added (to prevent duplicate field in Schema for queries like
     * "SELECT age, COUNT(*) FROM bank GROUP BY age" where 'age' is mentioned in both SELECT and GROUP BY).
     */
    private List<Field> fetchFields(Query query) {
        Select select = (Select) query;
        List<Field> fields;
        if (queryResult instanceof Aggregations) {
            fields = select.getGroupBys().isEmpty() ? new ArrayList<>() : select.getGroupBys().get(0);
            for (Field field : select.getFields()) {
                if (field instanceof MethodField) {
                    fields.add(field);
                }
            }
        } else {
            if (query instanceof TableOnJoinSelect) {
                fields = ((TableOnJoinSelect) query).getSelectedFields();
            } else {
                fields = select.getFields();
            }
        }

        return fields;
    }

    private String[] fetchFieldsAsArray(Query query) {
        List<Field> fields = fetchFields(query);
        return fields.stream()
                .map(this::getFieldName)
                .toArray(String[]::new);
    }

    private String getFieldName(Field field) {
        if (field instanceof MethodField)
            return field.getAlias();

        return field.getName();
    }

    private Map<String, Field> fetchFieldMap(Query query) {
        Map<String, Field> fieldMap = new HashMap<>();

        for (Field field : fetchFields(query)) {
            fieldMap.put(getFieldName(field), field);
        }

        return fieldMap;
    }

    private String[] selectAllFieldsIfEmpty(String[] fields) {
        if (isSelectAll())
            return new String[] {"*"};

        return fields;
    }

    private String[] emptyArrayIfNull(String typeName) {
        if (typeName != null)
            return new String[] {typeName};
        else
            return Strings.EMPTY_ARRAY;
    }

    private Schema.Type fetchMethodReturnType(Field field) {
        switch (field.getName().toLowerCase()) {
            case "count":
                return Schema.Type.LONG;
            case "sum":
            case "avg":
            case "min":
            case "max":
                return Schema.Type.DOUBLE;
            default:
                throw new UnsupportedOperationException(
                        String.format("The following method is not supported in Schema: %s", field.getName()));
        }
    }

    /**
     * Returns a list of Column objects which contain names identifying the field as well as its type.
     *
     * If all fields are being selected (SELECT *) then the order of fields returned will be random, otherwise
     * the output will be in the same order as how they were selected.
     *
     * If an alias was given for a field, that will be used to identify the field in Column, otherwise the field name
     * will be used.
     */
    private List<Schema.Column> populateColumns(Query query, String[] fieldNames, Map<String, FieldMappingMetaData> typeMappings) {
        List<String> fields;

        if (isSelectAll() || containsWildcard(query)) {
            fields = new ArrayList<>(typeMappings.keySet());
        } else {
            fields = Arrays.asList(fieldNames);
        }

        /*
         * The reason the 'fieldMap' mapping is needed on top of 'fields' is because the map would be empty in cases
         * like 'SELECT *' but List<String> fields will always be set in either case. That way, 'fields' is used to
         * access field names in order that they were selected, if given, and then 'fieldMap' is used to access the
         * respective Field object to check for aliases.
         */
        Map<String, Field> fieldMap = fetchFieldMap(query);
        List<Schema.Column> columns = new ArrayList<>();
        for (String field : fields) {
            // _score is a special case since it is not included in typeMappings, so it is checked for here
            if (field.equals("_score")) {
                columns.add(new Schema.Column(field, fetchAlias(field, fieldMap), Schema.Type.FLOAT));
            }
            /*
             * Methods are also a special case as their type cannot be determined from typeMappings, so it is checked
             * for here.
             *
             * Note: When adding the Column for Method, alias is used in place of getName() because the default name
             * is set as alias (ex. COUNT(*)) and overwritten if an alias is given. So alias is used as the
             * name instead.
             */
            if (fieldMap.get(field) instanceof MethodField) {
                Field methodField = fieldMap.get(field);
                columns.add(
                        new Schema.Column(
                                methodField.getAlias(),
                                null,
                                fetchMethodReturnType(methodField)
                        )
                );
            }

            /*
             * Unnecessary fields (ex. _index, _parent) are ignored.
             * Fields like field.keyword will be ignored when isSelectAll is true but will be returned if
             * explicitly selected.
             */
            if (typeMappings.containsKey(field) && !field.startsWith("_")) {
                if (!isSelectAll() || !field.endsWith(".keyword")) {
                    FieldMappingMetaData metaData = typeMappings.get(field);

                    // Ignore nested fields during SELECT *, expectation is that user will SELECT them specifically
                    // TODO isPropertyType() logic should be changed to check for nested more effectively
                    if (isSelectAll() && isPropertyType(field)) { continue; }

                    String type = getTypeFromMetaData(field, metaData).toUpperCase();

                    /*
                     * Three cases regarding Type:
                     * 1. If Type exists, create Column
                     * 2. If Type doesn't exist and isSelectAll() is false, throw exception
                     * 3. If Type doesn't exist and isSelectAll() is true, Column creation for field is skipped
                     */
                    if (Schema.hasType(type)) {
                        columns.add(
                                new Schema.Column(
                                        field,
                                        fetchAlias(field, fieldMap),
                                        Schema.Type.valueOf(type)
                                )
                        );
                    } else if (!isSelectAll()) {
                        throw new IllegalArgumentException(
                                String.format("%s field types are currently not supported.", type));
                    }
                }
            }
        }

        return columns;
    }

    /**
     * Since this helper method is called within a check to see if the field exists in type mapping, it's
     * already confirmed that the fieldName is valid. The check for fieldName in fieldMap has to be done in the case
     * that 'SELECT *' was called since the map will be empty.
     */
    private String fetchAlias(String fieldName, Map<String, Field> fieldMap) {
        if (fieldMap.containsKey(fieldName))
            return fieldMap.get(fieldName).getAlias();

        return null;
    }

    // TODO change this method, a temporary solution and not an effective way for checking if a field is nested
    // Unfortunately the typeMapping when returning all fields returns the full path so the outer field can't be
    // retrieved to check type for "nested"
    private boolean isPropertyType(String fieldName) {
        int lastDot = fieldName.lastIndexOf(".");

        return lastDot > -1 && !fieldName.substring(lastDot + 1).equals("keyword");
    }

    /** Used to retrieve the type of fields from metaData map structures for both regular and nested fields */
    @SuppressWarnings("unchecked")
    private String getTypeFromMetaData(String fieldName, FieldMappingMetaData metaData) {
        Map<String, Object> source = metaData.sourceAsMap();
        String[] fieldPath = fieldName.split("\\.");

        /*
         * When field is not nested the metaData source is fieldName -> type
         * When it is nested or contains "." in general (ex. fieldName.nestedName) the source is nestedName -> type
         */
        Map<String, Object> fieldMapping;
        if (fieldPath.length < 2) {
            fieldMapping = (Map<String, Object>) source.get(fieldName);
        } else {
            fieldMapping = (Map<String, Object>) source.get(fieldPath[1]);
        }

        for (int i = 2; i < fieldPath.length; i++) {
            fieldMapping = (Map<String, Object>) fieldMapping.get(fieldPath[i]);
        }

        return (String) fieldMapping.get("type");
    }

    /***********************************************************
          Logic for loading Rows to be stored in DataRows
     ***********************************************************/

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
            // Total hits is not available from Aggregations so 'size' is used
            this.totalHits = size;
        }
    }

    private List<DataRows.Row> populateRows(SearchHits searchHits) {
        List<DataRows.Row> rows = new ArrayList<>();
        Set<String> newKeys = new HashSet<>(head);
        for (SearchHit hit : searchHits) {
            Map<String, Object> rowSource = hit.getSourceAsMap();
            List<DataRows.Row> result = new ArrayList<>();
            result.add(new DataRows.Row(rowSource));

            if (!isJoinQuery()) { // Row already flatten in source in join. And join doesn't support nested fields for now.
                rowSource = flatRow(head, rowSource);
                rowSource.put("_score", hit.getScore());
                result = flatNestedField(newKeys, rowSource, hit.getInnerHits());
            }

            rows.addAll(result);
        }

        return rows;
    }

    private List<DataRows.Row> populateRows(Aggregations aggregations) {
        List<DataRows.Row> rows = new ArrayList<>();
        List<Aggregation> aggs = aggregations.asList();
        if (hasTermAggregations(aggs)) {
            Terms terms = (Terms) aggs.get(0);
            String field = terms.getName();

            for (Terms.Bucket bucket : terms.getBuckets()) {
                List<DataRows.Row> aggRows = new ArrayList<>();
                getAggsData(bucket, aggRows, addMap(field, bucket.getKey()));

                rows.addAll(aggRows);
            }
        } else {
            // This occurs for cases like "SELECT AVG(age) FROM bank" where we aggregate in SELECT with no GROUP BY
            rows.add(
                    new DataRows.Row(
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
    private void getAggsData(Terms.Bucket bucket, List<DataRows.Row> aggRows, Map<String, Object> data) {
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
            aggRows.add(new DataRows.Row(new HashMap<>(data)));
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
    private List<DataRows.Row> flatNestedField(Set<String> newKeys, Map<String, Object> row, Map<String, SearchHits> innerHits) {
        List<DataRows.Row> result = new ArrayList<>();
        result.add(new DataRows.Row(row));

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
    private List<DataRows.Row> doFlatNestedFieldValue(String colName, SearchHit[] colValue, List<DataRows.Row> rows) {
        List<DataRows.Row> result = new ArrayList<>();
        Map<String, Schema.Type> fieldTypeMap = fetchFieldTypeMap();
        for (DataRows.Row row : rows) {
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
                result.add(new DataRows.Row(copy));
            }
        }

        return result;
    }

    /**
     * Column information is kept as a List to maintain order but in cases like the convertData() method, when
     * repeatedly checking the Type of a field before adding it into Row, it can be troublesome to iterate through all
     * of the list of Columns just to get the Type for one field. This method returns a map to make a check like this
     * more efficient.
     */

    private Map<String, Schema.Type> fetchFieldTypeMap() {
        Map<String, Schema.Type> fieldTypeMap = new HashMap<>();
        for (Schema.Column column : schema) {
            fieldTypeMap.put(column.getName(), column.getEnumType());
        }

        return fieldTypeMap;
    }

    /**
     * Created as a generic solution for applying any transformations or conversions to data before adding it into the
     * resulting Row object.
     *
     * For the time being it is only used for converting Date values into the millis-since-epoch format.
     */
    private Object convertData(Map<String, Schema.Type> fieldTypeMap, String field, Object data) {
        Schema.Type type = fieldTypeMap.get(field);
        switch (type) {
            case DATE:
                // convert date
            default:
                return data;
        }
    }

    private long convertDateToEpochInMillis() {
        return 0;
    }

    private Map<String, Object> addMap(String field, Object term) {
        Map<String, Object> data = new HashMap<>();
        data.put(field, term);
        return data;
    }

    private boolean isJoinQuery() {
        return query instanceof JoinSelect;
    }
}
