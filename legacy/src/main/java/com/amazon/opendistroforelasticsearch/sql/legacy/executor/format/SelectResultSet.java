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

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLCaseExpr;
import com.alibaba.druid.sql.ast.expr.SQLCastExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.ColumnTypeProvider;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Field;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.JoinSelect;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.MethodField;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Query;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Select;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.TableOnJoinSelect;
import com.amazon.opendistroforelasticsearch.sql.legacy.esdomain.mapping.FieldMapping;
import com.amazon.opendistroforelasticsearch.sql.legacy.exception.SqlFeatureNotImplementedException;
import com.amazon.opendistroforelasticsearch.sql.legacy.executor.Format;
import com.amazon.opendistroforelasticsearch.sql.legacy.cursor.Cursor;
import com.amazon.opendistroforelasticsearch.sql.legacy.cursor.DefaultCursor;
import com.amazon.opendistroforelasticsearch.sql.legacy.metrics.MetricName;
import com.amazon.opendistroforelasticsearch.sql.legacy.metrics.Metrics;
import com.amazon.opendistroforelasticsearch.sql.legacy.utils.SQLFunctions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsResponse;
import org.elasticsearch.action.search.ClearScrollResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.NumericMetricsAggregation;
import org.elasticsearch.search.aggregations.metrics.Percentile;
import org.elasticsearch.search.aggregations.metrics.Percentiles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toSet;
import static org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsResponse.FieldMappingMetadata;

public class SelectResultSet extends ResultSet {

    private static final Logger LOG = LogManager.getLogger(SelectResultSet.class);

    public static final String SCORE = "_score";
    private final String formatType;

    private Query query;
    private Object queryResult;

    private boolean selectAll;
    private String indexName;
    private String typeName;
    private List<Schema.Column> columns = new ArrayList<>();
    private ColumnTypeProvider outputColumnType;

    private List<String> head;
    private long size;
    private long totalHits;
    private long internalTotalHits;
    private List<DataRows.Row> rows;
    private Cursor cursor;

    private DateFieldFormatter dateFieldFormatter;
    // alias -> base field name
    private Map<String, String> fieldAliasMap = new HashMap<>();

    public SelectResultSet(Client client,
                           Query query,
                           Object queryResult,
                           ColumnTypeProvider outputColumnType,
                           String formatType,
                           Cursor cursor) {
        this.client = client;
        this.query = query;
        this.queryResult = queryResult;
        this.selectAll = false;
        this.formatType = formatType;
        this.outputColumnType = outputColumnType;
        this.cursor = cursor;

        if (isJoinQuery()) {
            JoinSelect joinQuery = (JoinSelect) query;
            loadFromEsState(joinQuery.getFirstTable());
            loadFromEsState(joinQuery.getSecondTable());
        } else {
            loadFromEsState(query);
        }
        this.schema = new Schema(indexName, typeName, columns);
        this.head = schema.getHeaders();
        this.dateFieldFormatter = new DateFieldFormatter(indexName, columns, fieldAliasMap);

        extractData();
        populateCursor();
        this.dataRows = new DataRows(size, totalHits, rows);
    }

    public SelectResultSet(Client client, Object queryResult, String formatType, Cursor cursor) {
        this.cursor = cursor;
        this.client = client;
        this.queryResult = queryResult;
        this.selectAll = false;
        this.formatType = formatType;
        populateResultSetFromCursor(cursor);
    }

    public String indexName(){
        return this.indexName;
    }

    public Map<String, String> fieldAliasMap() {
        return unmodifiableMap(this.fieldAliasMap);
    }

    public void populateResultSetFromCursor(Cursor cursor) {
        switch (cursor.getType()) {
            case DEFAULT:
                populateResultSetFromDefaultCursor((DefaultCursor) cursor);
            default:
                return;
        }
    }

    private void populateResultSetFromDefaultCursor(DefaultCursor cursor) {
        this.columns = cursor.getColumns();
        this.schema = new Schema(null, null, columns);
        this.head = schema.getHeaders();
        this.dateFieldFormatter = new DateFieldFormatter(
                cursor.getIndexPattern(),
                columns,
                cursor.getFieldAliasMap()
        );
        extractData();
        this.dataRows = new DataRows(size, totalHits, rows);
    }

    //***********************************************************
    //      Logic for loading Columns to be stored in Schema
    //***********************************************************

    /**
     * Makes a request to local node to receive meta data information and maps each field specified in SELECT to its
     * type in the index mapping
     */
    private void loadFromEsState(Query query) {
        String indexName = fetchIndexName(query);
        String typeName = fetchTypeName(query);
        String[] fieldNames = fetchFieldsAsArray(query);

        // Reset boolean in the case of JOIN query where multiple calls to loadFromEsState() are made
        selectAll = isSimpleQuerySelectAll(query) || isJoinQuerySelectAll(query, fieldNames);

        GetFieldMappingsRequest request = new GetFieldMappingsRequest()
                .indices(indexName)
                .types(emptyArrayIfNull(typeName))
                .fields(selectAllFieldsIfEmpty(fieldNames))
                .local(true);
        GetFieldMappingsResponse response = client.admin().indices()
                .getFieldMappings(request)
                .actionGet();

        Map<String, Map<String, Map<String, FieldMappingMetadata>>> mappings = response.mappings();
        if (mappings.isEmpty()) {
            throw new IllegalArgumentException(String.format("Index type %s does not exist", query.getFrom()));
        }

        // Assumption is all indices share the same mapping which is validated in TermFieldRewriter.
        Map<String, Map<String, FieldMappingMetadata>> indexMappings = mappings.values().iterator().next();

        // if index mappings size is 0 and the expression is a cast: that means that we are casting by alias
        // if so, add the original field that was being looked at to the mapping (how?)

        /*
         * There are three cases regarding type name to consider:
         * 1. If the correct type name was given, its typeMapping is retrieved
         * 2. If the incorrect type name was given then the response is null
         * 3. If no type name is given, the indexMapping is searched for a typeMapping
         */
        Map<String, FieldMappingMetadata> typeMappings = new HashMap<>();
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

    /**
     * Rename column name with table alias as prefix for join query
     */
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
                        Schema.Type.valueOf(column.getType().toUpperCase()),
                        true
                ));
            }
        } else {
            renamedCols = columns;
        }
        return renamedCols;
    }

    private boolean isSelectAll() {
        return selectAll;
    }

    /**
     * Is a simple (non-join/non-group-by) query with SELECT * explicitly
     */
    private boolean isSimpleQuerySelectAll(Query query) {
        return (query instanceof Select) && ((Select) query).isSelectAll();
    }

    /**
     * Is a join query with SELECT * on either one of the tables  some fields specified
     */
    private boolean isJoinQuerySelectAll(Query query, String[] fieldNames) {
        return fieldNames.length == 0 && !fieldsSelectedOnAnotherTable(query);
    }

    /**
     * In the case of a JOIN query, if no fields are SELECTed on for a particular table, the other table's fields are
     * checked in SELECT to ensure a table is not incorrectly marked as a isSelectAll() case.
     */
    private boolean fieldsSelectedOnAnotherTable(Query query) {
        if (isJoinQuery()) {
            TableOnJoinSelect otherTable = getOtherTable(query);
            return otherTable.getSelectedFields().size() > 0;
        }

        return false;
    }

    private TableOnJoinSelect getOtherTable(Query currJoinSelect) {
        JoinSelect joinQuery = (JoinSelect) query;
        if (joinQuery.getFirstTable() == currJoinSelect) {
            return joinQuery.getSecondTable();
        } else {
            return joinQuery.getFirstTable();
        }
    }

    private boolean containsWildcard(Query query) {
        for (Field field : fetchFields(query)) {
            if (!(field instanceof MethodField) && field.getName().contains("*")) {
                return true;
            }
        }

        return false;
    }

    private String fetchIndexName(Query query) {
        return query.getFrom().get(0).getIndex();
    }

    private String fetchTypeName(Query query) {
        return query.getFrom().get(0).getType();
    }

    /**
     * queryResult is checked to see if it's of type Aggregation in which case the aggregation fields in GROUP BY
     * are returned as well. This prevents returning a Schema of all fields when SELECT * is called with
     * GROUP BY (since all fields will be retrieved from the typeMappings request when no fields are returned from
     * fetchFields()).
     * <p>
     * After getting all of the fields from GROUP BY, the fields from SELECT are iterated and only the fields of type
     * MethodField are added (to prevent duplicate field in Schema for queries like
     * "SELECT age, COUNT(*) FROM bank GROUP BY age" where 'age' is mentioned in both SELECT and GROUP BY).
     */
    private List<Field> fetchFields(Query query) {
        Select select = (Select) query;

        if (queryResult instanceof Aggregations) {
            List<Field> groupByFields = select.getGroupBys().isEmpty() ? new ArrayList<>() :
                    select.getGroupBys().get(0);


            for (Field selectField : select.getFields()) {
                if (selectField instanceof MethodField && !selectField.isScriptField()) {
                    groupByFields.add(selectField);
                } else if (selectField.isScriptField()
                        && selectField.getAlias().equals(groupByFields.get(0).getName())) {
                    return select.getFields();
                }
            }
            return groupByFields;
        }

        if (query instanceof TableOnJoinSelect) {
            return ((TableOnJoinSelect) query).getSelectedFields();
        }

        return select.getFields();
    }

    private String[] fetchFieldsAsArray(Query query) {
        List<Field> fields = fetchFields(query);
        return fields.stream()
                .map(this::getFieldName)
                .toArray(String[]::new);
    }

    private String getFieldName(Field field) {
        if (field instanceof MethodField) {
            return field.getAlias();
        }

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
        if (isSelectAll()) {
            return new String[]{"*"};
        }

        return fields;
    }

    private String[] emptyArrayIfNull(String typeName) {
        if (typeName != null) {
            return new String[]{typeName};
        } else {
            return Strings.EMPTY_ARRAY;
        }
    }

    private Schema.Type fetchMethodReturnType(int fieldIndex, MethodField field) {
        switch (field.getName().toLowerCase()) {
            case "count":
                return Schema.Type.LONG;
            case "sum":
            case "avg":
            case "min":
            case "max":
            case "percentiles":
                return Schema.Type.DOUBLE;
            case "script": {
                // TODO: return type information is disconnected from the function definitions in SQLFunctions.
                // Refactor SQLFunctions to have functions self-explanatory (types, scripts) and pluggable
                // (similar to Strategy pattern)
                if (field.getExpression() instanceof SQLCaseExpr) {
                    return Schema.Type.TEXT;
                }
                Schema.Type resolvedType = outputColumnType.get(fieldIndex);
                return SQLFunctions.getScriptFunctionReturnType(field, resolvedType);
            }
            default:
                throw new UnsupportedOperationException(
                        String.format("The following method is not supported in Schema: %s", field.getName()));
        }
    }

    /**
     * Returns a list of Column objects which contain names identifying the field as well as its type.
     * <p>
     * If all fields are being selected (SELECT *) then the order of fields returned will be random, otherwise
     * the output will be in the same order as how they were selected.
     * <p>
     * If an alias was given for a field, that will be used to identify the field in Column, otherwise the field name
     * will be used.
     */
    private List<Schema.Column> populateColumns(Query query, String[] fieldNames, Map<String,
            FieldMappingMetadata> typeMappings) {
        List<String> fieldNameList;

        if (isSelectAll() || containsWildcard(query)) {
            fieldNameList = new ArrayList<>(typeMappings.keySet());
        } else {
            fieldNameList = Arrays.asList(fieldNames);
        }

        /*
         * The reason the 'fieldMap' mapping is needed on top of 'fieldNameList' is because the map would be
         * empty in cases like 'SELECT *' but List<String> fieldNameList will always be set in either case.
         * That way, 'fieldNameList' is used to access field names in order that they were selected, if given,
         * and then 'fieldMap' is used to access the respective Field object to check for aliases.
         */
        Map<String, Field> fieldMap = fetchFieldMap(query);
        List<Schema.Column> columns = new ArrayList<>();
        for (String fieldName : fieldNameList) {
            // _score is a special case since it is not included in typeMappings, so it is checked for here
            if (fieldName.equals(SCORE)) {
                columns.add(new Schema.Column(fieldName, fetchAlias(fieldName, fieldMap), Schema.Type.FLOAT));
                continue;
            }
            /*
             * Methods are also a special case as their type cannot be determined from typeMappings, so it is checked
             * for here.
             *
             * Note: When adding the Column for Method, alias is used in place of getName() because the default name
             * is set as alias (ex. COUNT(*)) and overwritten if an alias is given. So alias is used as the
             * name instead.
             */
            if (fieldMap.get(fieldName) instanceof MethodField) {
                MethodField methodField = (MethodField) fieldMap.get(fieldName);
                int fieldIndex = fieldNameList.indexOf(fieldName);

                SQLExpr expr = methodField.getExpression();
                if (expr instanceof SQLCastExpr) {
                    // Since CAST expressions create an alias for a field, we need to save the original field name
                    // for this alias for formatting data later.
                    SQLIdentifierExpr castFieldIdentifier = (SQLIdentifierExpr) ((SQLCastExpr) expr).getExpr();
                    fieldAliasMap.put(methodField.getAlias(), castFieldIdentifier.getName());
                }

                columns.add(
                        new Schema.Column(
                                methodField.getAlias(),
                                null,
                                fetchMethodReturnType(fieldIndex, methodField)
                        )
                );
                continue;
            }

            /*
             * Unnecessary fields (ex. _index, _parent) are ignored.
             * Fields like field.keyword will be ignored when isSelectAll is true but will be returned if
             * explicitly selected.
             */
            FieldMapping field = new FieldMapping(fieldName, typeMappings, fieldMap);
            if (!field.isMetaField()) {

                if (field.isMultiField() && !field.isSpecified()) {
                    continue;
                }
                if (field.isPropertyField() && !field.isSpecified() && !field.isWildcardSpecified()) {
                    continue;
                }

                /*
                 * Three cases regarding Type:
                 * 1. If Type exists, create Column
                 * 2. If Type doesn't exist and isSelectAll() is false, throw exception
                 * 3. If Type doesn't exist and isSelectAll() is true, Column creation for fieldName is skipped
                 */
                String type = field.type().toUpperCase();
                if (Schema.hasType(type)) {

                    // If the current field is a group key, we should use alias as the identifier
                    boolean isGroupKey = false;
                    Select select = (Select) query;
                    if (null != select.getGroupBys()
                            && !select.getGroupBys().isEmpty()
                            && select.getGroupBys().get(0).contains(fieldMap.get(fieldName))) {
                        isGroupKey = true;
                    }

                    columns.add(
                            new Schema.Column(
                                    fieldName,
                                    fetchAlias(fieldName, fieldMap),
                                    Schema.Type.valueOf(type),
                                    isGroupKey
                            )
                    );
                } else if (!isSelectAll()) {
                    throw new IllegalArgumentException(
                            String.format("%s fieldName types are currently not supported.", type));
                }
            }
        }

        if (isSelectAllOnly(query)) {
            populateAllNestedFields(columns, fieldNameList);
        }
        return columns;
    }

    /**
     * SELECT * only without other columns or wildcard pattern specified.
     */
    private boolean isSelectAllOnly(Query query) {
        return isSelectAll() && fetchFields(query).isEmpty();
    }

    /**
     * Special case which trades off consistency of SELECT * meaning for more intuition from customer perspective.
     * In other cases, * means all regular fields on the level.
     * The only exception here is * picks all non-regular (nested) fields as JSON without flatten.
     */
    private void populateAllNestedFields(List<Schema.Column> columns, List<String> fields) {
        Set<String> nestedFieldPaths = fields.stream().
                map(FieldMapping::new).
                filter(FieldMapping::isPropertyField).
                filter(f -> !f.isMultiField()).
                map(FieldMapping::path).
                collect(toSet());

        for (String nestedFieldPath : nestedFieldPaths) {
            columns.add(
                    new Schema.Column(nestedFieldPath, "", Schema.Type.TEXT)
            );
        }
    }

    /**
     * Since this helper method is called within a check to see if the field exists in type mapping, it's
     * already confirmed that the fieldName is valid. The check for fieldName in fieldMap has to be done in the case
     * that 'SELECT *' was called since the map will be empty.
     */
    private String fetchAlias(String fieldName, Map<String, Field> fieldMap) {
        if (fieldMap.containsKey(fieldName)) {
            return fieldMap.get(fieldName).getAlias();
        }

        return null;
    }

    //***********************************************************
    //      Logic for loading Rows to be stored in DataRows
    //***********************************************************

    /**
     * Extract data from query results into Row objects
     * Need to cover two cases:
     * 1. queryResult is a SearchHits object
     * 2. queryResult is an Aggregations object
     * <p>
     * Ignoring queryResult being ActionResponse (from executeDeleteAction), there should be no data in this case
     */
    private void extractData() {
        if (queryResult instanceof SearchHits) {
            SearchHits searchHits = (SearchHits) queryResult;

            this.rows = populateRows(searchHits);
            this.size = rows.size();
            this.internalTotalHits = Optional.ofNullable(searchHits.getTotalHits()).map(th -> th.value).orElse(0L);
            // size may be greater than totalHits after nested rows be flatten
            this.totalHits = Math.max(size, internalTotalHits);
        } else if (queryResult instanceof Aggregations) {
            Aggregations aggregations = (Aggregations) queryResult;

            this.rows = populateRows(aggregations);
            this.size = rows.size();
            this.internalTotalHits = size;
            // Total hits is not available from Aggregations so 'size' is used
            this.totalHits = size;
        }
    }

    private void populateCursor() {
        switch(cursor.getType()) {
            case DEFAULT:
                populateDefaultCursor((DefaultCursor) cursor);
            default:
                return;
        }
    }

    private void populateDefaultCursor(DefaultCursor cursor) {
        /**
         * Assumption: scrollId, fetchSize, limit already being set in
         * @see PrettyFormatRestExecutor.buildProtocolForDefaultQuery()
         */

        Integer limit = cursor.getLimit();
        long rowsLeft = rowsLeft(cursor.getFetchSize(), cursor.getLimit());
        if (rowsLeft <= 0) {
            // close the cursor
            String scrollId = cursor.getScrollId();
            ClearScrollResponse clearScrollResponse = client.prepareClearScroll().addScrollId(scrollId).get();
            if (!clearScrollResponse.isSucceeded()) {
                Metrics.getInstance().getNumericalMetric(MetricName.FAILED_REQ_COUNT_SYS).increment();
                LOG.error("Error closing the cursor context {} ", scrollId);
            }
            return;
        }

        cursor.setRowsLeft(rowsLeft);
        cursor.setIndexPattern(indexName);
        cursor.setFieldAliasMap(fieldAliasMap());
        cursor.setColumns(columns);
        this.totalHits = limit != null && limit < internalTotalHits ? limit : internalTotalHits;
    }

    private long rowsLeft(Integer fetchSize, Integer limit) {
        long rowsLeft = 0;
        long totalHits = internalTotalHits;
        if (limit != null && limit < totalHits) {
            rowsLeft = limit - fetchSize;
        } else {
            rowsLeft = totalHits - fetchSize;
        }
        return rowsLeft;
    }

    private List<DataRows.Row> populateRows(SearchHits searchHits) {
        List<DataRows.Row> rows = new ArrayList<>();
        Set<String> newKeys = new HashSet<>(head);
        for (SearchHit hit : searchHits) {
            Map<String, Object> rowSource = hit.getSourceAsMap();
            List<DataRows.Row> result;

            if (!isJoinQuery()) {
                // Row already flatten in source in join. And join doesn't support nested fields for now.
                rowSource = flatRow(head, rowSource);
                rowSource.put(SCORE, hit.getScore());

                for (Map.Entry<String, DocumentField> field : hit.getFields().entrySet()) {
                    rowSource.put(field.getKey(), field.getValue().getValue());
                }
                if (formatType.equalsIgnoreCase(Format.JDBC.getFormatName())) {
                    dateFieldFormatter.applyJDBCDateFormat(rowSource);
                }
                result = flatNestedField(newKeys, rowSource, hit.getInnerHits());
            } else {
                if (formatType.equalsIgnoreCase(Format.JDBC.getFormatName())) {
                    dateFieldFormatter.applyJDBCDateFormat(rowSource);
                }
                result = new ArrayList<>();
                result.add(new DataRows.Row(rowSource));
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
     * <p>
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
            if (aggregation instanceof NumericMetricsAggregation.SingleValue) {
                NumericMetricsAggregation.SingleValue singleValueAggregation =
                        (NumericMetricsAggregation.SingleValue) aggregation;
                data.put(singleValueAggregation.getName(), !Double.isInfinite(singleValueAggregation.value())
                        ? singleValueAggregation.getValueAsString() : "null");
            } else if (aggregation instanceof Percentiles) {
                Percentiles percentiles = (Percentiles) aggregation;

                data.put(percentiles.getName(), StreamSupport
                        .stream(percentiles.spliterator(), false)
                        .collect(
                                Collectors.toMap(
                                        Percentile::getPercent,
                                        Percentile::getValue,
                                        (v1, v2) -> {
                                            throw new IllegalArgumentException(
                                                    String.format("Duplicate key for values %s and %s", v1, v2));
                                        },
                                        TreeMap::new)));
            } else {
                throw new SqlFeatureNotImplementedException("Aggregation type " + aggregation.getType()
                        + " is not yet implemented");
            }
        }

        return data;
    }

    /**
     * Simplifies the structure of row's source Map by flattening it, making the full path of an object the key
     * and the Object it refers to the value. This handles the case of regular object since nested objects will not
     * be in hit.source but rather in hit.innerHits
     * <p>
     * Sample input:
     * keys = ['comments.likes']
     * row = comments: {
     * likes: 2
     * }
     * <p>
     * Return:
     * flattenedRow = {comment.likes: 2}
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
     * <p>
     * Sample input:
     * newKeys = {'region', 'employees.age'}, row = {'region': 'US'}
     * innerHits = employees: {
     * hits: [{
     * source: {
     * age: 26,
     * firstname: 'Hank'
     * }
     * },{
     * source: {
     * age: 30,
     * firstname: 'John'
     * }
     * }]
     * }
     */
    private List<DataRows.Row> flatNestedField(Set<String> newKeys, Map<String, Object> row,
                                               Map<String, SearchHits> innerHits) {
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
     * <p>
     * Sample input:
     * colName = 'employees', rows = [{region: 'US'}]
     * colValue= [{
     * source: {
     * age: 26,
     * firstname: 'Hank'
     * }
     * },{
     * source: {
     * age: 30,
     * firstname: 'John'
     * }
     * }]
     * <p>
     * Return:
     * [
     * {region:'US', employees.age:26, employees.firstname:'Hank'},
     * {region:'US', employees.age:30, employees.firstname:'John'}
     * ]
     */
    private List<DataRows.Row> doFlatNestedFieldValue(String colName, SearchHit[] colValue, List<DataRows.Row> rows) {
        List<DataRows.Row> result = new ArrayList<>();
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

    private Map<String, Object> addMap(String field, Object term) {
        Map<String, Object> data = new HashMap<>();
        data.put(field, term);
        return data;
    }

    private boolean isJoinQuery() {
        return query instanceof JoinSelect;
    }
}
