/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.converter;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLAggregateExpr;
import com.alibaba.druid.sql.ast.expr.SQLAggregateOption;
import com.alibaba.druid.sql.ast.expr.SQLCastExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlSelectGroupByExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitorAdapter;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.ColumnTypeProvider;
import com.amazon.opendistroforelasticsearch.sql.legacy.expression.core.Expression;
import com.amazon.opendistroforelasticsearch.sql.legacy.expression.core.ExpressionFactory;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.core.ColumnNode;
import com.google.common.base.Strings;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The definition of SQL Aggregation Converter which will parse the query to project column node list and
 * aggregation list
 * e.g. parse the query: SELECT age, MAX(balance) - MIN(balance) FROM T GROUP BY age.
 * will generate the
 * node list: age, max_0 - min_0
 * aggregation list: age, max(balance) as max_0, min(balance) as min_0
 *
 */
@RequiredArgsConstructor
public class SQLAggregationParser {
    private final ColumnTypeProvider columnTypeProvider;
    private Context context;
    @Getter
    private List<ColumnNode> columnNodes = new ArrayList<>();

    public void parse(MySqlSelectQueryBlock queryBlock) {
        context = new Context(constructSQLExprAliasMapFromSelect(queryBlock));

        //1. extract raw names of selectItems
        List<String> selectItemNames = extractSelectItemNames(queryBlock.getSelectList());

        //2. rewrite all the function name to lower case.
        rewriteFunctionNameToLowerCase(queryBlock);

        //2. find all GroupKeyExpr from GroupBy expression.
        findAllGroupKeyExprFromGroupByAndSelect(queryBlock);
        findAllAggregationExprFromSelect(queryBlock);

        //3. parse the select list to expression
        parseExprInSelectList(queryBlock, selectItemNames, new SQLExprToExpressionConverter(context));
    }

    public List<SQLSelectItem> selectItemList() {
        List<SQLSelectItem> sqlSelectItems = new ArrayList<>();
        context.getGroupKeyExprMap().entrySet().forEach(entry -> sqlSelectItems
                .add(new SQLSelectItem(entry.getKey(), entry.getValue().getExpression().toString())));
        context.getAggregationExprMap().entrySet().forEach(entry -> sqlSelectItems
                .add(new SQLSelectItem(entry.getKey(), entry.getValue().getExpression().toString())));
        return sqlSelectItems;
    }

    private Map<SQLExpr, String> constructSQLExprAliasMapFromSelect(MySqlSelectQueryBlock queryBlock) {
        return queryBlock.getSelectList().stream().filter(item -> !Strings.isNullOrEmpty(item.getAlias()))
                .collect(Collectors.toMap(SQLSelectItem::getExpr, SQLSelectItem::getAlias));
    }

    /**
     * The SQL-92 require nonaggregated name column in the select list must appear in the GROUP BY, But the
     * existing uses cases violate this require. e.g. AggregationIT. countGroupByDateTest
     * Ref the https://dev.mysql.com/doc/refman/8.0/en/group-by-handling.html for detail information
     */
    private void findAllGroupKeyExprFromGroupByAndSelect(MySqlSelectQueryBlock queryBlock) {
        if (queryBlock.getGroupBy() == null) {
            return;
        }
        // 1. fetch the expr from groupby clause.
        List<SQLExpr> groupByKeyExprList =
                queryBlock.getGroupBy().getItems().stream().map(item -> ((MySqlSelectGroupByExpr) item).getExpr())
                        .collect(Collectors.toList());

        // 2. find the group expr from select.
        for (SQLSelectItem selectItem : queryBlock.getSelectList()) {
            SQLExpr selectItemExpr = selectItem.getExpr();
            // extension, group key in select could not in group by.
            if (selectItemExpr instanceof SQLIdentifierExpr) {
                context.addGroupKeyExpr(selectItemExpr);
            } else {
                for (SQLExpr groupByExpr : groupByKeyExprList) {
                    // SQL-92,nonaggregated name column in the select list must appear in the GROUP BY
                    if (compareSelectExprAndGroupByExpr(selectItemExpr, selectItem.getAlias(), groupByExpr)) {
                        context.addGroupKeyExpr(selectItemExpr);
                    } else if (groupByExpr instanceof SQLIdentifierExpr) {
                        // support expression over group key, e.g. SELECT log(G), max(A) FROM T GROUP BY G.
                        String groupByName = ((SQLIdentifierExpr) groupByExpr).getName();
                        selectItemExpr.accept(new MySqlASTVisitorAdapter() {
                            @Override
                            public boolean visit(SQLAggregateExpr x) {
                                return false;
                            }

                            @Override
                            public boolean visit(SQLIdentifierExpr expr) {
                                if (groupByName.equalsIgnoreCase(expr.getName())) {
                                    expr.setParent(selectItem.getParent());
                                    context.addGroupKeyExpr(expr);
                                }
                                return false;
                            }
                        });
                    }
                }
            }
        }
    }

    private boolean compareSelectExprAndGroupByExpr(SQLExpr selectItemExpr, String alias, SQLExpr groupByExpr) {
        if (groupByExpr.equals(selectItemExpr)) {
            return true;
        } else if (groupByExpr instanceof SQLIdentifierExpr
                   && ((SQLIdentifierExpr) groupByExpr).getName().equalsIgnoreCase(alias)) {
                return true;
        }
        return false;
    }

    private void findAllAggregationExprFromSelect(MySqlSelectQueryBlock queryBlock) {
        queryBlock.getSelectList().forEach(selectItem -> selectItem.accept(new MySqlASTVisitorAdapter() {
            @Override
            public boolean visit(SQLAggregateExpr expr) {
                context.addAggregationExpr(expr);
                return true;
            }
        }));
    }

    private void parseExprInSelectList(
            MySqlSelectQueryBlock queryBlock, List<String> selectItemNames,
            SQLExprToExpressionConverter exprConverter) {
        List<SQLSelectItem> selectItems = queryBlock.getSelectList();
        for (int i = 0; i < selectItems.size(); i++) {
            Expression expression = exprConverter.convert(selectItems.get(i).getExpr());
            ColumnNode columnNode = ColumnNode.builder()
                    .name(selectItemNames.get(i))
                    .alias(selectItems.get(i).getAlias())
                    .type(columnTypeProvider.get(i))
                    .expr(expression)
                    .build();
            columnNodes.add(columnNode);
        }
    }

    private List<String> extractSelectItemNames(List<SQLSelectItem> selectItems) {
        List<String> selectItemNames = new ArrayList<>();
        for (SQLSelectItem selectItem: selectItems){
            selectItemNames.add(nameOfSelectItem(selectItem));
        }
        return selectItemNames;
    }

    private void rewriteFunctionNameToLowerCase(MySqlSelectQueryBlock query) {
        query.accept(new MySqlASTVisitorAdapter() {
            @Override
            public boolean visit(SQLMethodInvokeExpr x) {
                x.setMethodName(x.getMethodName().toLowerCase());
                return true;
            }
        });
    }

    private String nameOfSelectItem(SQLSelectItem selectItem) {
        return Strings.isNullOrEmpty(selectItem.getAlias()) ? Context
                .nameOfExpr(selectItem.getExpr()) : selectItem.getAlias();
    }

    @RequiredArgsConstructor
    public static class Context {
        private final AliasGenerator aliasGenerator = new AliasGenerator();

        private final Map<SQLExpr, String> selectSQLExprAliasMap;

        @Getter
        private final Map<SQLExpr, GroupKeyExpr> groupKeyExprMap = new LinkedHashMap<>();
        @Getter
        private final Map<SQLExpr, AggregationExpr> aggregationExprMap = new LinkedHashMap<>();

        Optional<Expression> resolve(SQLExpr expr) {
            if (groupKeyExprMap.containsKey(expr)) {
                return Optional.of(groupKeyExprMap.get(expr).getExpression());
            } else if (aggregationExprMap.containsKey(expr)) {
                return Optional.of(aggregationExprMap.get(expr).getExpression());
            } else {
                return Optional.empty();
            }
        }

        public void addGroupKeyExpr(SQLExpr groupKeyExpr) {
            if (!groupKeyExprMap.containsKey(groupKeyExpr)) {
                groupKeyExprMap.put(groupKeyExpr, new GroupKeyExpr(groupKeyExpr));
            }
        }

        public void addAggregationExpr(SQLAggregateExpr aggregationExpr) {
            if (!aggregationExprMap.containsKey(aggregationExpr)) {
                aggregationExprMap.put(aggregationExpr, new AggregationExpr(aggregationExpr));
            }
        }

        @Getter
        public class GroupKeyExpr {
            private final SQLExpr expr;
            private final Expression expression;

            public GroupKeyExpr(SQLExpr expr) {
                this.expr = expr;
                String exprName = nameOfExpr(expr).replace(".", "#");
                if (expr instanceof SQLIdentifierExpr
                    && selectSQLExprAliasMap.values().contains(((SQLIdentifierExpr) expr).getName())) {
                    exprName = ((SQLIdentifierExpr) expr).getName();
                }
                this.expression = ExpressionFactory.ref(selectSQLExprAliasMap.getOrDefault(expr, exprName));
            }
        }

        @Getter
        public class AggregationExpr {
            private final SQLAggregateExpr expr;
            private final Expression expression;

            public AggregationExpr(SQLAggregateExpr expr) {
                this.expr = expr;
                this.expression =
                        ExpressionFactory.ref(selectSQLExprAliasMap.getOrDefault(expr, aliasGenerator
                                .nextAlias(expr.getMethodName())));
            }
        }

        public static String nameOfExpr(SQLExpr expr) {
            String exprName = expr.toString().toLowerCase();
            if (expr instanceof SQLAggregateExpr) {
                SQLAggregateExpr aggExpr = (SQLAggregateExpr) expr;
                SQLAggregateOption option = aggExpr.getOption();
                exprName = option == null
                        ? String.format("%s(%s)", aggExpr.getMethodName(), aggExpr.getArguments().get(0))
                        : String.format("%s(%s %s)", aggExpr.getMethodName(), option.name(),
                            aggExpr.getArguments().get(0));
            } else if (expr instanceof SQLMethodInvokeExpr) {
                exprName = String.format("%s(%s)", ((SQLMethodInvokeExpr) expr).getMethodName(),
                        nameOfExpr(((SQLMethodInvokeExpr) expr).getParameters().get(0)));
            } else if (expr instanceof SQLIdentifierExpr) {
                exprName = ((SQLIdentifierExpr) expr).getName();
            } else if (expr instanceof SQLCastExpr) {
                exprName = String.format("CAST(%s AS %s)", ((SQLCastExpr) expr).getExpr(),
                                         ((SQLCastExpr) expr).getDataType().getName());
            }
            return exprName;
        }

        static class AliasGenerator {
            private int aliasSuffix = 0;

            private String nextAlias(String name) {
                return String.format("%s_%d", name, next());
            }

            private Integer next() {
                return aliasSuffix++;
            }
        }
    }
}
