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

package com.amazon.opendistroforelasticsearch.sql.legacy.query;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLCastExpr;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Field;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.KVValue;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.MethodField;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Order;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Select;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Where;
import com.amazon.opendistroforelasticsearch.sql.legacy.esdomain.LocalClusterState;
import com.amazon.opendistroforelasticsearch.sql.legacy.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.legacy.executor.Format;
import com.amazon.opendistroforelasticsearch.sql.legacy.executor.format.Schema;
import com.amazon.opendistroforelasticsearch.sql.legacy.metrics.MetricName;
import com.amazon.opendistroforelasticsearch.sql.legacy.metrics.Metrics;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.maker.QueryMaker;
import com.amazon.opendistroforelasticsearch.sql.legacy.rewriter.nestedfield.NestedFieldProjection;
import com.amazon.opendistroforelasticsearch.sql.legacy.utils.SQLFunctions;

import com.google.common.annotations.VisibleForTesting;
import org.elasticsearch.action.search.SearchAction;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.NestedSortBuilder;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.ScriptSortBuilder.ScriptSortType;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.amazon.opendistroforelasticsearch.sql.legacy.plugin.SqlSettings.CURSOR_ENABLED;
import static com.amazon.opendistroforelasticsearch.sql.legacy.plugin.SqlSettings.CURSOR_KEEPALIVE;

/**
 * Transform SQL query to standard Elasticsearch search query
 */
public class DefaultQueryAction extends QueryAction {

    private final Select select;
    private SearchRequestBuilder request;

    private final List<String> fieldNames = new LinkedList<>();

    public DefaultQueryAction(Client client, Select select) {
        super(client, select);
        this.select = select;
    }

    public void initialize(SearchRequestBuilder request) {
        this.request = request;
    }

    @Override
    public SqlElasticSearchRequestBuilder explain() throws SqlParseException {
        Objects.requireNonNull(this.sqlRequest, "SqlRequest is required for ES request build");
        buildRequest();
        checkAndSetScroll();
        return new SqlElasticSearchRequestBuilder(request);
    }

    private void buildRequest() throws SqlParseException {
        this.request = new SearchRequestBuilder(client, SearchAction.INSTANCE);
        setIndicesAndTypes();
        setFields(select.getFields());
        setWhere(select.getWhere());
        setSorts(select.getOrderBys());
        updateRequestWithIndexAndRoutingOptions(select, request);
        updateRequestWithHighlight(select, request);
        updateRequestWithCollapse(select, request);
        updateRequestWithPostFilter(select, request);
        updateRequestWithInnerHits(select, request);
    }

    @VisibleForTesting
    public void checkAndSetScroll() {
        LocalClusterState clusterState = LocalClusterState.state();

        Integer fetchSize = sqlRequest.fetchSize();
        TimeValue timeValue = clusterState.getSettingValue(CURSOR_KEEPALIVE);
        Boolean cursorEnabled = clusterState.getSettingValue(CURSOR_ENABLED);
        Integer rowCount = select.getRowCount();

        if (checkIfScrollNeeded(cursorEnabled, fetchSize, rowCount)) {
            Metrics.getInstance().getNumericalMetric(MetricName.DEFAULT_CURSOR_REQUEST_COUNT_TOTAL).increment();
            Metrics.getInstance().getNumericalMetric(MetricName.DEFAULT_CURSOR_REQUEST_TOTAL).increment();
            request.setSize(fetchSize).setScroll(timeValue);
        } else {
            request.setSearchType(SearchType.DFS_QUERY_THEN_FETCH);
            setLimit(select.getOffset(), rowCount != null ? rowCount : Select.DEFAULT_LIMIT);
        }
    }


    private boolean checkIfScrollNeeded(boolean cursorEnabled, Integer fetchSize, Integer rowCount) {
        return cursorEnabled
                && (format !=null && format.equals(Format.JDBC))
                && fetchSize > 0
                && (rowCount == null || (rowCount > fetchSize));
    }

    @Override
    public Optional<List<String>> getFieldNames() {
        return Optional.of(fieldNames);
    }


    public Select getSelect() {
        return select;
    }

    /**
     * Set indices and types to the search request.
     */
    private void setIndicesAndTypes() {
        request.setIndices(query.getIndexArr());

        String[] typeArr = query.getTypeArr();
        if (typeArr != null) {
            request.setTypes(typeArr);
        }
    }

    /**
     * Set source filtering on a search request.
     *
     * @param fields list of fields to source filter.
     */
    public void setFields(List<Field> fields) throws SqlParseException {

        if (!select.getFields().isEmpty() && !select.isSelectAll()) {
            ArrayList<String> includeFields = new ArrayList<>();
            ArrayList<String> excludeFields = new ArrayList<>();

            for (Field field : fields) {
                if (field instanceof MethodField) {
                    MethodField method = (MethodField) field;
                    if (method.getName().toLowerCase().equals("script")) {
                        handleScriptField(method);
                        if (method.getExpression() instanceof SQLCastExpr) {
                            includeFields.add(method.getParams().get(0).toString());
                        }
                    } else if (method.getName().equalsIgnoreCase("include")) {
                        for (KVValue kvValue : method.getParams()) {
                            includeFields.add(kvValue.value.toString());
                        }
                    } else if (method.getName().equalsIgnoreCase("exclude")) {
                        for (KVValue kvValue : method.getParams()) {
                            excludeFields.add(kvValue.value.toString());
                        }
                    }
                } else if (field != null) {
                    if (isNotNested(field)) {
                        includeFields.add(field.getName());
                    }
                }
            }

            fieldNames.addAll(includeFields);
            request.setFetchSource(includeFields.toArray(new String[0]), excludeFields.toArray(new String[0]));
        }
    }

    private void handleScriptField(final MethodField method) throws SqlParseException {

        final List<KVValue> params = method.getParams();
        final int numOfParams = params.size();

        if (2 != numOfParams && 3 != numOfParams) {
            throw new SqlParseException("scripted_field only allows 'script(name,script)' "
                    + "or 'script(name,lang,script)'");
        }

        final String fieldName = params.get(0).value.toString();
        fieldNames.add(fieldName);

        final String secondParam = params.get(1).value.toString();
        final Script script = (2 == numOfParams) ? new Script(secondParam) :
                new Script(ScriptType.INLINE, secondParam, params.get(2).value.toString(), Collections.emptyMap());
        request.addScriptField(fieldName, script);
    }

    /**
     * Create filters or queries based on the Where clause.
     *
     * @param where the 'WHERE' part of the SQL query.
     * @throws SqlParseException if the where clause does not represent valid sql
     */
    private void setWhere(Where where) throws SqlParseException {
        BoolQueryBuilder boolQuery = null;
        if (where != null) {
            boolQuery = QueryMaker.explain(where, this.select.isQuery);
        }
        // Used to prevent NullPointerException in old tests as they do not set sqlRequest in QueryAction
        if (sqlRequest != null) {
            boolQuery = sqlRequest.checkAndAddFilter(boolQuery);
        }
        request.setQuery(boolQuery);
    }

    /**
     * Add sorts to the elasticsearch query based on the 'ORDER BY' clause.
     *
     * @param orderBys list of Order object
     */
    private void setSorts(List<Order> orderBys) {
        Map<String, FieldSortBuilder> sortBuilderMap = new HashMap<>();

        for (Order order : orderBys) {
            String orderByName = order.getName();
            SortOrder sortOrder = SortOrder.valueOf(order.getType());

            if (order.getNestedPath() != null) {
                request.addSort(
                        SortBuilders.fieldSort(orderByName)
                                .order(sortOrder)
                                .setNestedSort(new NestedSortBuilder(order.getNestedPath())));
            } else if (order.isScript()) {
                // TODO: Investigate how to find the type of expression (string or number)
                // As of now this shouldn't be a problem, because the support is for date_format function
                request.addSort(
                    SortBuilders
                        .scriptSort(new Script(orderByName), getScriptSortType(order))
                        .order(sortOrder));
            } else if (orderByName.equals(ScoreSortBuilder.NAME)) {
                request.addSort(orderByName, sortOrder);
            } else {
                FieldSortBuilder fieldSortBuilder = sortBuilderMap.computeIfAbsent(orderByName, key -> {
                    FieldSortBuilder fs = SortBuilders.fieldSort(key);
                    request.addSort(fs);
                    return fs;
                });
                setSortParams(fieldSortBuilder, order);
            }
        }
    }


    private void setSortParams(FieldSortBuilder fieldSortBuilder, Order order) {
        fieldSortBuilder.order(SortOrder.valueOf(order.getType()));

        SQLExpr expr = order.getSortField().getExpression();
        if (expr instanceof SQLBinaryOpExpr) {
            // we set SQLBinaryOpExpr in Field.setExpression() to support ORDER by IS NULL/IS NOT NULL
            fieldSortBuilder.missing(getNullOrderString((SQLBinaryOpExpr) expr));
        }
    }

    private String getNullOrderString(SQLBinaryOpExpr expr) {
        SQLBinaryOperator operator = expr.getOperator();
        return operator == SQLBinaryOperator.IsNot ? "_first" : "_last";
    }

    private ScriptSortType getScriptSortType(Order order) {
        ScriptSortType scriptSortType;
        Schema.Type scriptFunctionReturnType = SQLFunctions.getOrderByFieldType(order.getSortField());


        // as of now script function return type returns only text and double
        switch (scriptFunctionReturnType) {
            case TEXT:
                scriptSortType = ScriptSortType.STRING;
                break;

            case DOUBLE:
            case FLOAT:
            case INTEGER:
            case LONG:
                scriptSortType = ScriptSortType.NUMBER;
                break;
            default:
                throw new IllegalStateException("Unknown type: " + scriptFunctionReturnType);
        }
        return scriptSortType;
    }

    /**
     * Add from and size to the ES query based on the 'LIMIT' clause
     *
     * @param from starts from document at position from
     * @param size number of documents to return.
     */
    private void setLimit(int from, int size) {
        request.setFrom(from);

        if (size > -1) {
            request.setSize(size);
        }
    }

    public SearchRequestBuilder getRequestBuilder() {
        return request;
    }

    private boolean isNotNested(Field field) {
        return !field.isNested() || field.isReverseNested();
    }

    private void updateRequestWithInnerHits(Select select, SearchRequestBuilder request) {
        new NestedFieldProjection(request).project(select.getFields(), select.getNestedJoinType());
    }
}
