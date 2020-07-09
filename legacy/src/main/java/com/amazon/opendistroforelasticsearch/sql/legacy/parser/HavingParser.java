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
import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.expr.SQLAggregateExpr;
import com.alibaba.druid.sql.ast.expr.SQLBetweenExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLInListExpr;
import com.alibaba.druid.sql.ast.expr.SQLNotExpr;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitorAdapter;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Field;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Where;
import com.amazon.opendistroforelasticsearch.sql.legacy.exception.SqlParseException;

import java.util.ArrayList;
import java.util.List;

/**
 * Parse expression in the Having clause.
 */
public class HavingParser {
    private final WhereParser whereParser;
    private final List<Field> havingFields;
    private final HavingConditionRewriter havingConditionRewriter;

    public HavingParser(WhereParser whereParser) {
        this.whereParser = whereParser;
        this.havingFields = new ArrayList<>();
        this.havingConditionRewriter = new HavingConditionRewriter();
    }

    public void parseWhere(SQLExpr expr, Where where) throws SqlParseException {
        expr.accept(havingConditionRewriter);
        whereParser.parseWhere(expr, where);
    }

    public List<Field> getHavingFields() {
        return havingFields;
    }

    private class HavingConditionRewriter extends MySqlASTVisitorAdapter {
        private int aliasSuffix = 0;

        @Override
        public boolean visit(SQLAggregateExpr expr) {
            SQLIdentifierExpr translatedExpr = translateAggExpr(expr);
            SQLObject parent = expr.getParent();
            // Rewrite {@link SQLAggregateExpr} in {@link SQLBinaryOpExpr}, e.g. HAVING AVG(age) > 30)
            if (parent instanceof SQLBinaryOpExpr) {
                SQLBinaryOpExpr parentOpExpr = (SQLBinaryOpExpr) parent;
                if (parentOpExpr.getLeft() == expr) {
                    parentOpExpr.setLeft(translatedExpr);
                } else {
                    parentOpExpr.setRight(translatedExpr);
                }
                // Rewrite {@link SQLAggregateExpr} in {@link SQLNotExpr}, e.g. HAVING NOT (AVG(a) > 30)
            } else if (parent instanceof SQLNotExpr) {
                SQLNotExpr parentNotExpr = (SQLNotExpr) parent;
                parentNotExpr.setExpr(translatedExpr);
                // Rewrite {@link SQLAggregateExpr} in {@link SQLInListExpr}, e.g. HAVING AVG(a) IN (30, 40, 50)
            } else if (parent instanceof SQLInListExpr) {
                SQLInListExpr parentInListExpr = (SQLInListExpr) parent;
                parentInListExpr.setExpr(translatedExpr);
                // Rewrite {@link SQLAggregateExpr} in {@link SQLBetweenExpr}, e.g. HAVING AVG(a) BETWEEN 30, 40
            } else if (parent instanceof SQLBetweenExpr) {
                SQLBetweenExpr parentBetweenExpr = (SQLBetweenExpr) parent;
                parentBetweenExpr.setTestExpr(translatedExpr);
            } else {
                throw new IllegalStateException("Unsupported aggregation function in having clause "
                                                + parent.getClass());
            }

            return true;
        }

        /**
         * If the expr is {@link SQLAggregateExpr}
         * 1) rewrite as {@link SQLIdentifierExpr}
         * 2) add the {@link SQLIdentifierExpr} to the havingFields
         * <p>
         * For example, the COUNT(age) is the {@link SQLAggregateExpr} in expression COUNT(age) > 1
         * 1) parsing COUNT(age) as {@link SQLIdentifierExpr} count_1
         * 2) return {@link SQLIdentifierExpr} count_1  to the havingFields
         */
        private SQLIdentifierExpr translateAggExpr(SQLAggregateExpr expr) {
            String methodAlias = methodAlias(expr.getMethodName());
            SQLIdentifierExpr sqlExpr = new SQLIdentifierExpr(methodAlias);
            try {
                havingFields.add(new FieldMaker().makeField(
                        expr,
                        methodAlias,
                        null));
                return sqlExpr;
            } catch (SqlParseException e) {
                throw new IllegalStateException(e);
            }
        }

        private String methodAlias(String methodName) {
            return String.format("%s_%d", methodName.toLowerCase(), nextAlias());
        }

        private Integer nextAlias() {
            return aliasSuffix++;
        }
    }
}
