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

package com.amazon.opendistroforelasticsearch.sql.legacy.rewriter.matchtoterm;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLInListExpr;
import com.alibaba.druid.sql.ast.expr.SQLInSubQueryExpr;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectOrderByItem;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlSelectGroupByExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitorAdapter;
import com.alibaba.druid.sql.parser.ParserException;
import com.amazon.opendistroforelasticsearch.sql.legacy.esdomain.LocalClusterState;
import com.amazon.opendistroforelasticsearch.sql.legacy.esdomain.mapping.FieldMappings;
import com.amazon.opendistroforelasticsearch.sql.legacy.esdomain.mapping.IndexMappings;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Visitor to rewrite AST (abstract syntax tree) for supporting term_query in WHERE and IN condition
 * Simple changing the matchQuery() to termQuery() will not work when mapping is both text and keyword
 * The approach is to implement SQLIdentifier.visit() based on the correct field mapping.
 */

public class TermFieldRewriter extends MySqlASTVisitorAdapter {

    private Deque<TermFieldScope> environment = new ArrayDeque<>();
    private TermRewriterFilter filterType;

    public TermFieldRewriter() {
        this.filterType = TermRewriterFilter.COMMA;
    }

    public TermFieldRewriter(TermRewriterFilter filterType) {
        this.filterType = filterType;
    }

    @Override
    public boolean visit(MySqlSelectQueryBlock query) {
        environment.push(new TermFieldScope());
        if (query.getFrom() == null) {
            return false;
        }

        Map<String, String> indexToType = new HashMap<>();
        collect(query.getFrom(), indexToType, curScope().getAliases());
        if (indexToType.isEmpty()) {
            // no index found for current scope, continue.
            return true;
        }
        curScope().setMapper(getMappings(indexToType));

        if (this.filterType == TermRewriterFilter.COMMA || this.filterType == TermRewriterFilter.MULTI_QUERY) {
            checkMappingCompatibility(curScope(), indexToType);
        }

        return true;
    }

    @Override
    public void endVisit(MySqlSelectQueryBlock query) {
        environment.pop();
    }

    @Override
    public boolean visit(SQLSelectItem sqlSelectItem) {
        return false;
    }

    @Override
    public boolean visit(SQLJoinTableSource tableSource) {
        return false;
    }

    @Override
    public boolean visit(SQLExprTableSource tableSource) {
        return false;
    }

    /**
     * Fix null parent problem which is required when visiting SQLIdentifier
     */
    public boolean visit(SQLInListExpr inListExpr) {
        inListExpr.getExpr().setParent(inListExpr);
        return true;
    }


    @SuppressWarnings("unchecked")
    @Override
    public boolean visit(SQLIdentifierExpr expr) {
        if (isValidIdentifierForTerm(expr)) {
            Map<String, Object> source = null;
            if (this.filterType == TermRewriterFilter.COMMA || this.filterType == TermRewriterFilter.MULTI_QUERY) {
                Optional<Map<String, Object>> optionalMap = curScope().resolveFieldMapping(expr.getName());
                if (optionalMap.isPresent()) {
                    source = optionalMap.get();
                } else {
                    return true;
                }

            } else if (this.filterType == TermRewriterFilter.JOIN) {
                String[] arr = expr.getName().split("\\.", 2);
                if (arr.length < 2) {
                    throw new VerificationException("table alias or field name missing");
                }
                String alias = arr[0];
                String fullFieldName = arr[1];

                String index = curScope().getAliases().get(alias);
                FieldMappings fieldMappings = curScope().getMapper().mapping(index).firstMapping();
                if (fieldMappings.has(fullFieldName)) {
                    source = fieldMappings.mapping(fullFieldName);
                } else {
                    return true;
                }
            }

            String keywordAlias = isBothTextAndKeyword(source);
            if (keywordAlias != null) {
                expr.setName(expr.getName() + "." + keywordAlias);
            }
        }
        return true;
    }

    public void collect(SQLTableSource tableSource, Map<String, String> indexToType, Map<String, String> aliases) {
        if (tableSource instanceof SQLExprTableSource) {

            String tableName = null;
            SQLExprTableSource sqlExprTableSource = (SQLExprTableSource) tableSource;

            if (sqlExprTableSource.getExpr() instanceof SQLIdentifierExpr) {
                SQLIdentifierExpr sqlIdentifier = (SQLIdentifierExpr) sqlExprTableSource.getExpr();
                tableName = sqlIdentifier.getName();
                indexToType.put(tableName, null);
            } else if (sqlExprTableSource.getExpr() instanceof SQLBinaryOpExpr) {
                SQLBinaryOpExpr sqlBinaryOpExpr = (SQLBinaryOpExpr) sqlExprTableSource.getExpr();
                tableName = ((SQLIdentifierExpr) sqlBinaryOpExpr.getLeft()).getName();
                SQLExpr rightSideOfExpression = sqlBinaryOpExpr.getRight();

                // This assumes that right side of the expression is different name in query
                if (rightSideOfExpression instanceof SQLIdentifierExpr) {
                    SQLIdentifierExpr right = (SQLIdentifierExpr) rightSideOfExpression;
                    indexToType.put(tableName, right.getName());
                } else {
                    throw new ParserException("Right side of the expression [" + rightSideOfExpression.toString()
                            + "] is expected to be an identifier");
                }
            }
            if (tableSource.getAlias() != null) {
                aliases.put(tableSource.getAlias(), tableName);
            } else {
                aliases.put(tableName, tableName);
            }

        } else if (tableSource instanceof SQLJoinTableSource) {
            collect(((SQLJoinTableSource) tableSource).getLeft(), indexToType, aliases);
            collect(((SQLJoinTableSource) tableSource).getRight(), indexToType, aliases);
        }
    }

    /**
     * Current scope which is top of the stack
     */
    private TermFieldScope curScope() {
        return environment.peek();
    }

    public String isBothTextAndKeyword(Map<String, Object> source) {
        if (source.containsKey("fields")) {
            for (Object key : ((Map) source.get("fields")).keySet()) {
                if (key instanceof String
                        && ((Map) ((Map) source.get("fields")).get(key)).get("type").equals("keyword")) {
                    return (String) key;
                }
            }
        }
        return null;
    }

    public boolean isValidIdentifierForTerm(SQLIdentifierExpr expr) {
        /**
         * Only for following conditions Identifier will be modified
         *  Where:  WHERE identifier = 'something'
         *  IN list: IN ('Tom', 'Dick', 'Harry')
         *  IN subquery: IN (SELECT firstname from accounts/account where firstname = 'John')
         *  Group by: GROUP BY state , employer , ...
         *  Order by: ORDER BY firstname, lastname , ...
         *
         * NOTE: Does not impact fields on ON condition clause in JOIN as we skip visiting SQLJoinTableSource
         */
        return !expr.getName().startsWith("_") && (isValidIdentifier(expr) || checkIfNestedIdentifier(expr));
    }

    private boolean checkIfNestedIdentifier(SQLIdentifierExpr expr) {
        return
            expr.getParent() instanceof SQLMethodInvokeExpr
                && ((SQLMethodInvokeExpr) expr.getParent()).getMethodName().equals("nested")
                && isValidIdentifier(expr.getParent());
    }

    private boolean isValidIdentifier(SQLObject expr) {
        SQLObject parent = expr.getParent();
        return isBinaryExprWithValidOperators(parent)
                || parent instanceof SQLInListExpr
                || parent instanceof SQLInSubQueryExpr
                || parent instanceof SQLSelectOrderByItem
                || parent instanceof MySqlSelectGroupByExpr;
    }

    private boolean isBinaryExprWithValidOperators(SQLObject expr) {
        if (!(expr instanceof SQLBinaryOpExpr)) {
            return false;
        }
        return Stream.of(
            SQLBinaryOperator.Equality,
            SQLBinaryOperator.Is,
            SQLBinaryOperator.IsNot
        ).anyMatch(operator -> operator == ((SQLBinaryOpExpr) expr).getOperator());
    }

    private void checkMappingCompatibility(TermFieldScope scope, Map<String, String> indexToType) {
        if (scope.getMapper().isEmpty()) {
            throw new VerificationException("Unknown index " + indexToType.keySet());
        }
    }

    public IndexMappings getMappings(Map<String, String> indexToType) {
        String[] allIndexes = indexToType.keySet().stream().toArray(String[]::new);
        // GetFieldMappingsRequest takes care of wildcard index expansion
        return LocalClusterState.state().getFieldMappings(allIndexes);
    }

    public enum TermRewriterFilter {
        COMMA(","), // No joins, multiple tables in SELECT
        JOIN("JOIN"), // All JOINs
        MULTI_QUERY("MULTI_QUERY"); // MINUS and UNION

        public final String name;

        TermRewriterFilter(String name) {
            this.name = name;
        }

        public static String toString(TermRewriterFilter filter) {
            return filter.name;
        }
    }
}
