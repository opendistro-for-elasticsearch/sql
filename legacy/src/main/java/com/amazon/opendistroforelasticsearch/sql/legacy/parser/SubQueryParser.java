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

package com.amazon.opendistroforelasticsearch.sql.legacy.parser;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSubqueryTableSource;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Condition;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Field;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Order;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Select;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Where;
import com.amazon.opendistroforelasticsearch.sql.legacy.exception.SqlParseException;
import com.google.common.base.Strings;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Definition of SubQuery Parser
 */
public class SubQueryParser {
    private final SqlParser sqlParser;

    public SubQueryParser(SqlParser sqlParser) {
        this.sqlParser = sqlParser;
    }

    public boolean containSubqueryInFrom(MySqlSelectQueryBlock query) {
        return query.getFrom() instanceof SQLSubqueryTableSource;
    }

    public Select parseSubQueryInFrom(MySqlSelectQueryBlock query) throws SqlParseException {
        assert query.getFrom() instanceof SQLSubqueryTableSource;

        Select select = sqlParser.parseSelect(
                (MySqlSelectQueryBlock) ((SQLSubqueryTableSource) query.getFrom()).getSelect()
                .getQuery());
        String subQueryAlias = query.getFrom().getAlias();
        return pushSelect(query.getSelectList(), select, subQueryAlias);
    }

    private Select pushSelect(List<SQLSelectItem> selectItems, Select subquerySelect, String subQueryAlias) {
        Map<String, Function<String, String>> fieldAliasRewriter = prepareFieldAliasRewriter(
                selectItems,
                subQueryAlias);

        //1. rewrite field in select list
        Iterator<Field> fieldIterator = subquerySelect.getFields().iterator();
        while (fieldIterator.hasNext()) {
            Field field = fieldIterator.next();
            /*
             * return true if the subquerySelectItem in the final select list.
             * for example, subquerySelectItem is "SUM(emp.empno) as TEMP",
             * and final select list is TEMP. then return true.
             */
            String fieldIdentifier = Strings.isNullOrEmpty(field.getAlias()) ? field.getName() : field.getAlias();
            if (fieldAliasRewriter.containsKey(fieldIdentifier)) {
                field.setAlias(fieldAliasRewriter.get(fieldIdentifier).apply(fieldIdentifier));
            } else {
                fieldIterator.remove();
            }
        }

        //2. rewrite field in order by
        for (Order orderBy : subquerySelect.getOrderBys()) {
            if (fieldAliasRewriter.containsKey(orderBy.getName())) {
                String replaceOrderName = fieldAliasRewriter.get(orderBy.getName()).apply(orderBy.getName());
                orderBy.setName(replaceOrderName);
                orderBy.getSortField().setName(replaceOrderName);
            }
        }

        // 3. rewrite field in having
        if (subquerySelect.getHaving() != null) {
            for (Where condition : subquerySelect.getHaving().getConditions()) {
                Condition cond = (Condition) condition;
                if (fieldAliasRewriter.containsKey(cond.getName())) {
                    String replaceOrderName = fieldAliasRewriter.get(cond.getName()).apply(cond.getName());
                    cond.setName(replaceOrderName);
                }
            }
        }
        return subquerySelect;
    }

    private Map<String, Function<String, String>> prepareFieldAliasRewriter(List<SQLSelectItem> selectItems,
                                                                          String owner) {
        HashMap<String, Function<String, String>> selectMap = new HashMap<>();
        for (SQLSelectItem item : selectItems) {
            if (Strings.isNullOrEmpty(item.getAlias())) {
                selectMap.put(getFieldName(item.getExpr(), owner), Function.identity());
            } else {
                selectMap.put(getFieldName(item.getExpr(), owner), s -> item.getAlias());
            }
        }
        return selectMap;
    }

    private String getFieldName(SQLExpr expr, String owner) {
        return expr.toString().replace(String.format("%s.", owner), "");
    }
}
