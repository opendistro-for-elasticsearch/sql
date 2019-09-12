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

package com.amazon.opendistroforelasticsearch.sql.unittest.rewriter.subquery;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.amazon.opendistroforelasticsearch.sql.rewriter.subquery.SubQueryRewriteRule;
import com.amazon.opendistroforelasticsearch.sql.util.SqlParserUtils;

public abstract class SubQueryRewriterTestBase {

    SQLQueryExpr expr(String query) {
        return SqlParserUtils.parse(query);
    }

    SQLQueryExpr rewrite(SQLQueryExpr expr) {
        new SubQueryRewriteRule().rewrite(expr);
        return expr;
    }

    String sqlString(SQLObject expr) {
        return SQLUtils.toMySqlString(expr)
                .replaceAll("\n", " ")
                .replaceAll("\t", " ")
                .replaceAll(" +", " ");
    }
}
