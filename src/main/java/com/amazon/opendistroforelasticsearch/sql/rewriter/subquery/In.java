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

package com.amazon.opendistroforelasticsearch.sql.rewriter.subquery;


import com.alibaba.druid.sql.ast.expr.SQLInSubQueryExpr;
import com.amazon.opendistroforelasticsearch.sql.rewriter.subquery.rewriter.InRewriter;
import com.amazon.opendistroforelasticsearch.sql.rewriter.subquery.rewriter.Rewriter;
import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * In SubQuery rewriter.
 */
public class In extends SubQuery {
    private final List<Rewriter> rewriterList;

    public In(SQLInSubQueryExpr inExpr, BlackBoard bb) {
        super(inExpr.getSubQuery(), bb);

        this.rewriterList = new ImmutableList.Builder<Rewriter>()
                .add(new InRewriter(inExpr, bb))
                .build();
    }

    @Override
    public List<Rewriter> rewriterList() {
        return rewriterList;
    }
}
