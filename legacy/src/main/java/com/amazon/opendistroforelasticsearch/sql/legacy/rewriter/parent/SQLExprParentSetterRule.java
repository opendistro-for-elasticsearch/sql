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

package com.amazon.opendistroforelasticsearch.sql.legacy.rewriter.parent;

import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.amazon.opendistroforelasticsearch.sql.legacy.rewriter.RewriteRule;

/**
 * The {@link RewriteRule} which will apply {@link SQLExprParentSetter} for {@link SQLQueryExpr}
 */
public class SQLExprParentSetterRule implements RewriteRule<SQLQueryExpr> {

    @Override
    public boolean match(SQLQueryExpr expr) {
        return true;
    }

    @Override
    public void rewrite(SQLQueryExpr expr) {
        expr.accept(new SQLExprParentSetter());
    }
}
