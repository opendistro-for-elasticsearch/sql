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
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectGroupByClause;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlSelectGroupByExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.amazon.opendistroforelasticsearch.sql.rewriter.RewriteRule;

import java.util.Map;
import java.util.stream.Collectors;

public class GroupByInliner implements RewriteRule<SQLQueryExpr> {

    @Override
    public boolean match(SQLQueryExpr expr) {
        SQLSelect sqlSelect = expr.subQuery;
        if (!(sqlSelect.getQuery() instanceof MySqlSelectQueryBlock)) {
            return false;
        }

        MySqlSelectQueryBlock selectQuery = (MySqlSelectQueryBlock) sqlSelect.getQuery();

        return selectQuery.getGroupBy() != null && !selectQuery.getGroupBy().getItems().isEmpty();
    }

    @Override
    public void rewrite(SQLQueryExpr expr) {
        if (!match(expr)) {
            return;
        }

        MySqlSelectQueryBlock selectQuery = (MySqlSelectQueryBlock) expr.subQuery.getQuery();
        selectQuery.getHintsSize();

        Map<String, SQLExpr> aliasesToExpressions = selectQuery
                .getSelectList()
                .stream()
                .filter(item -> item.getAlias() != null)
                .collect(Collectors.toMap(SQLSelectItem::getAlias, SQLSelectItem::getExpr));

        SQLSelectGroupByClause groupBy = selectQuery.getGroupBy();
        for (SQLExpr groupByItem: groupBy.getItems()) {

            if (!(groupByItem instanceof MySqlSelectGroupByExpr)) {
                continue;
            }

            MySqlSelectGroupByExpr groupByExpr = (MySqlSelectGroupByExpr) groupByItem;

            SQLExpr groupedByExpr = groupByExpr.getExpr();
            if (!(groupedByExpr instanceof SQLIdentifierExpr)) {
                continue;
            }

            String name = ((SQLIdentifierExpr) groupedByExpr).getName();
            if (aliasesToExpressions.containsKey(name)) {
                SQLExpr replacementExpr = aliasesToExpressions.get(name);
                groupByExpr.setExpr(replacementExpr);
            }
        }

        // TODO: implement inlining in HAVING later, but for that we have to be able to inline in WHERE
    }
}
