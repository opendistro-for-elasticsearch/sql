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

package com.amazon.opendistroforelasticsearch.sql.legacy.unittest.parser;

import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.ast.expr.SQLNumberExpr;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.MethodField;
import com.amazon.opendistroforelasticsearch.sql.legacy.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.legacy.parser.FieldMaker;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FieldMakerTest {

    private static final String ALIAS = "a";

    private static final String TABLE_ALIAS = "t";

    private FieldMaker fieldMaker;

    @Before
    public void init() {
        fieldMaker = new FieldMaker();
    }

    @Test
    public void makeFieldAssign() throws SqlParseException {
        final SQLIntegerExpr sqlExpr = new SQLIntegerExpr(10);
        final MethodField field = (MethodField) fieldMaker.makeField(sqlExpr, ALIAS, TABLE_ALIAS);

        assertEquals("script", field.getName());
        assertEquals(ALIAS, field.getParams().get(0).value);
        assertTrue(((String)field.getParams().get(1).value).matches("def assign_[0-9]+ = 10;return assign_[0-9]+;"));
        assertEquals(ALIAS, field.getAlias());
    }

    @Test
    public void makeFieldAssignDouble() throws SqlParseException {
        final SQLNumberExpr sqlExpr = new SQLNumberExpr(10.0);
        final MethodField field = (MethodField) fieldMaker.makeField(sqlExpr, ALIAS, TABLE_ALIAS);

        assertEquals("script", field.getName());
        assertEquals(ALIAS, field.getParams().get(0).value);
        assertTrue(((String)field.getParams().get(1).value).matches("def assign_[0-9]+ = 10.0;return assign_[0-9]+;"));
        assertEquals(ALIAS, field.getAlias());
    }
}