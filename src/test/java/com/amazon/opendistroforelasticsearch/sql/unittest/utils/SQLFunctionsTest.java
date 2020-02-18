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

package com.amazon.opendistroforelasticsearch.sql.unittest.utils;

import com.alibaba.druid.sql.ast.SQLDataType;
import com.alibaba.druid.sql.ast.SQLDataTypeImpl;
import com.alibaba.druid.sql.ast.expr.SQLCastExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.base.ESDataType;
import com.amazon.opendistroforelasticsearch.sql.domain.ColumnTypeProvider;
import com.amazon.opendistroforelasticsearch.sql.domain.KVValue;
import com.amazon.opendistroforelasticsearch.sql.domain.MethodField;
import com.amazon.opendistroforelasticsearch.sql.domain.ScriptMethodField;
import com.amazon.opendistroforelasticsearch.sql.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.executor.format.Schema;
import com.amazon.opendistroforelasticsearch.sql.parser.FieldMaker;
import com.amazon.opendistroforelasticsearch.sql.utils.SQLFunctions;
import com.google.common.collect.ImmutableList;
import org.elasticsearch.common.collect.Tuple;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class SQLFunctionsTest {

    private FieldMaker fieldMaker;
    private SQLFunctions sqlFunctions;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Before
    public void init() {
        fieldMaker = new FieldMaker();
    }
    public void initSqlFunctions() { sqlFunctions = new SQLFunctions(); }

    @Test
    public void testAssign() throws SqlParseException {
        SQLFunctions sqlFunctions = new SQLFunctions();

        final SQLIntegerExpr sqlIntegerExpr = new SQLIntegerExpr(10);
        final Tuple<String, String> assign = sqlFunctions.function("assign",
                ImmutableList.of(new KVValue(null, sqlIntegerExpr)),
                null,
                true);

        assertTrue(assign.v1().matches("assign_[0-9]+"));
        assertTrue(assign.v2().matches("def assign_[0-9]+ = 10;return assign_[0-9]+;"));
    }

    @Test
    public void testAbsWithIntReturnType() {
        final SQLIntegerExpr sqlIntegerExpr = new SQLIntegerExpr(6);

        final SQLMethodInvokeExpr invokeExpr = new SQLMethodInvokeExpr("ABS");
        invokeExpr.addParameter(sqlIntegerExpr);
        List<KVValue> params = new ArrayList<>();

        final MethodField field = new ScriptMethodField("ABS", params, null, null);
        field.setExpression(invokeExpr);
        ColumnTypeProvider columnTypeProvider = new ColumnTypeProvider(ESDataType.INTEGER);

        final Schema.Type returnType = sqlFunctions.getScriptFunctionReturnType(0, field, columnTypeProvider);
        Assert.assertEquals(returnType, Schema.Type.INTEGER);
    }

    @Test
    public void testCastReturnType() {
        final SQLIdentifierExpr identifierExpr = new SQLIdentifierExpr("int_type");
        SQLDataType sqlDataType = new SQLDataTypeImpl("INT");
        final SQLCastExpr castExpr = new SQLCastExpr();
        castExpr.setExpr(identifierExpr);
        castExpr.setDataType(sqlDataType);

        List<KVValue> params = new ArrayList<>();
        final MethodField field = new ScriptMethodField("CAST", params, null, null);
        field.setExpression(castExpr);
        ColumnTypeProvider columnTypeProvider = new ColumnTypeProvider(ESDataType.INTEGER);

        final Schema.Type returnType = sqlFunctions.getScriptFunctionReturnType(0, field, columnTypeProvider);
        Assert.assertEquals(returnType, Schema.Type.INTEGER);
    }

    @Test
    public void testNullScriptReturnTypeThrowsException() throws UnsupportedOperationException {
        exceptionRule.expect(UnsupportedOperationException.class);
        exceptionRule.expectMessage("The following method is not supported in Schema: lo");

        List<KVValue> params = new ArrayList<>();
        MethodField field = new ScriptMethodField("LO", params, null, null);
        ColumnTypeProvider columnTypeProvider = new ColumnTypeProvider(ESDataType.INTEGER);
        final Schema.Type type = sqlFunctions.getScriptFunctionReturnType(0, field, columnTypeProvider);
    }
}