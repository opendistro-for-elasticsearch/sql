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

package com.amazon.opendistroforelasticsearch.sql.legacy.rewriter.alias;

import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test cases for util class {@link Table}.
 */
public class TableTest {

    @Test
    public void identifierOfTableNameShouldReturnTheTableName() {
        Table table = new Table(new SQLExprTableSource(new SQLIdentifierExpr("accounts")));
        Assert.assertEquals("accounts", table.name());
    }

    @Test
    public void identifierOfTableAndTypeNameShouldReturnTheTableNameOnly() {
        Table table = new Table(new SQLExprTableSource(
            new SQLBinaryOpExpr(
                new SQLIdentifierExpr("accounts"),
                SQLBinaryOperator.Divide,
                new SQLIdentifierExpr("test")
            )
        ));
        Assert.assertEquals("accounts", table.name());
    }

}