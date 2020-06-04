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

package com.amazon.opendistroforelasticsearch.sql.legacy.domain;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelectGroupByClause;
import com.alibaba.druid.util.StringUtils;
import com.amazon.opendistroforelasticsearch.sql.legacy.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.legacy.parser.HavingParser;
import com.amazon.opendistroforelasticsearch.sql.legacy.parser.NestedType;
import com.amazon.opendistroforelasticsearch.sql.legacy.parser.WhereParser;
import com.google.common.collect.Iterables;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.aggregations.AggregationBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.joining;
import static org.elasticsearch.search.aggregations.PipelineAggregatorBuilders.bucketSelector;

/**
 * Domain object for HAVING clause in SQL which covers both the parsing and explain logic.
 * <p>
 * Responsibilities:
 * 1. Parsing: parse conditions out during initialization
 * 2. Explain: translate conditions to ES query DSL (Bucket Selector Aggregation)
 */
public class Having {

    private static final String BUCKET_SELECTOR_NAME = "bucket_filter";
    private static final String PARAMS = "params.";
    private static final String AND = " && ";
    private static final String OR = " || ";

    /**
     * Conditions parsed out of HAVING clause
     */
    private final List<Where> conditions;

    private HavingParser havingParser;

    public List<Field> getHavingFields() {
        return havingParser.getHavingFields();
    }

    /**
     * Construct by HAVING expression
     *
     * @param havingExpr having expression
     * @param parser     where parser
     * @throws SqlParseException exception thrown by where parser
     */
    public Having(SQLExpr havingExpr, WhereParser parser) throws SqlParseException {
        havingParser = new HavingParser(parser);
        conditions = parseHavingExprToConditions(havingExpr, havingParser);
    }

    public List<Where> getConditions() {
        return conditions;
    }

    /**
     * Construct by GROUP BY expression with null check
     *
     * @param groupByExpr group by expression
     * @param parser      where parser
     * @throws SqlParseException exception thrown by where parser
     */
    public Having(SQLSelectGroupByClause groupByExpr, WhereParser parser) throws SqlParseException {
        this(groupByExpr == null ? null : groupByExpr.getHaving(), parser);
    }

    /**
     * Add Bucket Selector Aggregation under group by aggregation with sibling of aggregation of fields in SELECT.
     * ES makes sure that all sibling runs before bucket selector aggregation.
     *
     * @param groupByAgg aggregation builder for GROUP BY clause
     * @param fields     fields in SELECT clause
     * @throws SqlParseException exception thrown for unknown expression
     */
    public void explain(AggregationBuilder groupByAgg, List<Field> fields) throws SqlParseException {
        if (groupByAgg == null || conditions.isEmpty()) {
            return;
        }

        // parsing the fields from SELECT and HAVING clause
        groupByAgg.subAggregation(bucketSelector(BUCKET_SELECTOR_NAME,
                contextForFieldsInSelect(Iterables.concat(fields, getHavingFields())),
                explainConditions()));
    }

    private List<Where> parseHavingExprToConditions(SQLExpr havingExpr, HavingParser parser)
            throws SqlParseException {
        if (havingExpr == null) {
            return Collections.emptyList();
        }

        Where where = Where.newInstance();
        parser.parseWhere(havingExpr, where);
        return where.getWheres();
    }

    private Map<String, String> contextForFieldsInSelect(Iterable<Field> fields) {
        Map<String, String> context = new HashMap<>();
        for (Field field : fields) {
            if (field instanceof MethodField) {
                // It's required to add to context even if alias in SELECT is exactly same name as that in script
                context.put(field.getAlias(), bucketsPath(field.getAlias(), ((MethodField) field).getParams()));
            }
        }
        return context;
    }

    private Script explainConditions() throws SqlParseException {
        return new Script(doExplain(conditions));
    }

    /**
     * Explain conditions recursively.
     * Example: HAVING c >= 2 OR NOT (a > 20 AND c <= 10 OR a < 1) OR a < 5
     * Object: Where(?:
     * [
     * Condition(?:c>=2),
     * Where(or:
     * [
     * Where(?:a<=20), Where(or:c>10), Where(and:a>=1)],
     * ]),
     * Condition(or:a<5)
     * ])
     * <p>
     * Note: a) Where(connector : condition expression).
     * b) Condition is a subclass of Where.
     * c) connector=? means it doesn't matter for first condition in the list
     *
     * @param wheres conditions
     * @return painless script string
     * @throws SqlParseException unknown type of expression other than identifier and value
     */
    private String doExplain(List<Where> wheres) throws SqlParseException {
        if (wheres == null || wheres.isEmpty()) {
            return "";
        }

        StringBuilder script = new StringBuilder();
        for (Where cond : wheres) {
            if (script.length() > 0) {
                script.append(cond.getConn() == Where.CONN.AND ? AND : OR);
            }

            if (cond instanceof Condition) {
                script.append(createScript((Condition) cond));
            } else {
                script.append('(').
                        append(doExplain(cond.getWheres())).
                        append(')');
            }
        }
        return script.toString();
    }

    private String createScript(Condition cond) throws SqlParseException {
        String name = cond.getName();
        Object value = cond.getValue();
        switch (cond.getOPERATOR()) {
            case EQ:
            case GT:
            case LT:
            case GTE:
            case LTE:
            case IS:
            case ISN:
                return expr(name, cond.getOpertatorSymbol(), value);
            case N:
                return expr(name, "!=", value);
            case BETWEEN: {
                Object[] values = (Object[]) value;
                return expr(name, ">=", values[0]) + AND + expr(name, "<=", values[1]);
            }
            case NBETWEEN: {
                Object[] values = (Object[]) value;
                return expr(name, "<", values[0]) + OR + expr(name, ">", values[1]);
            }
            case IN:
                return Arrays.stream((Object[]) value).
                        map(val -> expr(name, "==", val)).
                        collect(joining(OR));
            case NIN:
                return Arrays.stream((Object[]) value).
                        map(val -> expr(name, "!=", val)).
                        collect(joining(AND));
            default:
                throw new SqlParseException("Unsupported operation in HAVING clause: " + cond.getOPERATOR());
        }
    }

    private String expr(String name, String operator, Object value) {
        return String.join(" ", PARAMS + name, operator, value.toString());
    }

    /**
     * Build the buckets_path.
     * If the field is nested field, using the bucket path.
     * else using the alias.
     */
    private String bucketsPath(String alias, List<KVValue> kvValueList) {
        if (kvValueList.size() == 1) {
            KVValue kvValue = kvValueList.get(0);
            if (StringUtils.equals(kvValue.key, "nested")
                && kvValue.value instanceof NestedType) {
                return ((NestedType) kvValue.value).getBucketPath();
            }
        }
        return alias;
    }
}
