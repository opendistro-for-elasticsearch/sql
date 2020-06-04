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

import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;


/**
 * An Identifier that is wrapped in parenthesis.
 * This is for tracking in group bys the difference between "group by state, age" and "group by (state), (age)".
 * For non group by identifiers, it acts as a normal SQLIdentifierExpr.
 */
public class SQLParensIdentifierExpr extends SQLIdentifierExpr {

    public SQLParensIdentifierExpr() {
    }

    public SQLParensIdentifierExpr(String name) {
        super(name);
    }

    public SQLParensIdentifierExpr(SQLIdentifierExpr expr) {
        super(expr.getName());
    }
}
