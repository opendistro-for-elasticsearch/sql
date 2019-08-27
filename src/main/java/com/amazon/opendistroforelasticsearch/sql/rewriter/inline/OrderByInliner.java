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

package com.amazon.opendistroforelasticsearch.sql.rewriter.inline;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLOrderBy;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectOrderByItem;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.amazon.opendistroforelasticsearch.sql.rewriter.RewriteRule;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Scans the query and inlines every ORDER BY expression
 */
public class OrderByInliner implements RewriteRule<SQLQueryExpr> {

    @Override
    public boolean match(SQLQueryExpr expr) {
        SQLSelect sqlSelect = expr.subQuery;
        MySqlSelectQueryBlock selectQuery = (MySqlSelectQueryBlock) sqlSelect.getQuery();

        SQLOrderBy orderBy = selectQuery.getOrderBy();
        return !(orderBy == null || orderBy.getItems().isEmpty());
    }

    public void rewrite(SQLQueryExpr queryExpr) {
        if (!match(queryExpr)) {
            return;
        }

        SQLSelect sqlSelect = queryExpr.subQuery;
        MySqlSelectQueryBlock selectQuery = (MySqlSelectQueryBlock) sqlSelect.getQuery();

        Map<String, SQLExpr> aliasesToExpressions = selectQuery
                .getSelectList()
                .stream()
                .filter(item->item.getAlias()!=null)
                .collect(Collectors.toMap(SQLSelectItem::getAlias, SQLSelectItem::getExpr));

        for (SQLSelectOrderByItem orderByItem: selectQuery.getOrderBy().getItems()) {
            SQLExpr orderByExpression = orderByItem.getExpr();

            if (!(orderByExpression instanceof SQLIdentifierExpr)) {
                continue;
            }

            String name = ((SQLIdentifierExpr) orderByExpression).getName();
            if (aliasesToExpressions.containsKey(name)) {
                orderByItem.setExpr(aliasesToExpressions.get(name));
            }
        }
    }
}
