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

package com.amazon.opendistroforelasticsearch.sql.legacy.rewriter.subquery.rewriter;

import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;

/**
 * Interface of SQL Rewriter
 */
public interface Rewriter {

    /**
     * Whether the Rewriter can rewrite the SQL?
     */
    boolean canRewrite();

    /**
     * Rewrite the SQL.
     */
    void rewrite();

    default SQLBinaryOpExpr and(SQLBinaryOpExpr left, SQLBinaryOpExpr right) {
        return new SQLBinaryOpExpr(left, SQLBinaryOperator.BooleanAnd, right);
    }
}
