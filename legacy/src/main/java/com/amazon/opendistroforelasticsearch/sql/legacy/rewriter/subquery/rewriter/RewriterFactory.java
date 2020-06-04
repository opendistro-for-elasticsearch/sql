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

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLExistsExpr;
import com.alibaba.druid.sql.ast.expr.SQLInSubQueryExpr;
import com.amazon.opendistroforelasticsearch.sql.legacy.rewriter.subquery.RewriterContext;
import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * Factory for generating the {@link Rewriter}.
 */
public class RewriterFactory {

    /**
     * Create list of {@link Rewriter}.
     */
    public static List<Rewriter> createRewriterList(SQLExpr expr, RewriterContext bb) {
        if (expr instanceof SQLExistsExpr) {
            return existRewriterList((SQLExistsExpr) expr, bb);
        } else if (expr instanceof SQLInSubQueryExpr) {
            return inRewriterList((SQLInSubQueryExpr) expr, bb);
        }
        return ImmutableList.of();
    }

    private static List<Rewriter> existRewriterList(SQLExistsExpr existsExpr, RewriterContext bb) {
        return new ImmutableList.Builder<Rewriter>()
                .add(new NestedExistsRewriter(existsExpr, bb))
                .build();
    }

    private static List<Rewriter> inRewriterList(SQLInSubQueryExpr inExpr, RewriterContext bb) {
        return new ImmutableList.Builder<Rewriter>()
                .add(new InRewriter(inExpr, bb))
                .build();
    }
}
