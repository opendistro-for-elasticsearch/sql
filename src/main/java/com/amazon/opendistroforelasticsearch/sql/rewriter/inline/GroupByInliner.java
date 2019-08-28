package com.amazon.opendistroforelasticsearch.sql.rewriter.inline;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLOrderBy;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectGroupByClause;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectOrderByItem;
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
        MySqlSelectQueryBlock selectQuery = (MySqlSelectQueryBlock) expr.subQuery.getQuery();

        Map<String, SQLExpr> aliasesToExpressions = selectQuery
                .getSelectList()
                .stream()
                .filter(item -> item.getAlias() != null)
                .collect(Collectors.toMap(SQLSelectItem::getAlias, SQLSelectItem::getExpr));

        SQLSelectGroupByClause groupBy = selectQuery.getGroupBy();
        for (SQLExpr ex: groupBy.getItems()) {

            if (!(ex instanceof MySqlSelectGroupByExpr)) {
                continue;
            }

            MySqlSelectGroupByExpr groupByExpr = (MySqlSelectGroupByExpr) ex;

            if (!(groupByExpr.getExpr() instanceof SQLIdentifierExpr)) {
                continue;
            }

            String name = ((SQLIdentifierExpr) groupByExpr.getExpr()).getName();
            if (aliasesToExpressions.containsKey(name)) {
                SQLExpr replacementExpr = aliasesToExpressions.get(name);
                groupByExpr.setExpr(replacementExpr);
            }

            SQLExpr expr1 = groupByExpr.getExpr();
        }
    }
}
