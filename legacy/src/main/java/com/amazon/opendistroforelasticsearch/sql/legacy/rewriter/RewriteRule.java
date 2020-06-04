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

package com.amazon.opendistroforelasticsearch.sql.legacy.rewriter;

import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;

import java.sql.SQLFeatureNotSupportedException;

/**
 * Query Optimize Rule
 */
public interface RewriteRule<T extends SQLQueryExpr> {

    /**
     * Checking whether the rule match the query?
     *
     * @return true if the rule match to the query.
     * @throws SQLFeatureNotSupportedException
     */
    boolean match(T expr) throws SQLFeatureNotSupportedException;

    /**
     * Optimize the query.
     *
     * @throws SQLFeatureNotSupportedException
     */
    void rewrite(T expr) throws SQLFeatureNotSupportedException;
}
