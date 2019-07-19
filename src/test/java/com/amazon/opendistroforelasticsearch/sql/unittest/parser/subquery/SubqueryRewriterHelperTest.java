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

package com.amazon.opendistroforelasticsearch.sql.unittest.parser.subquery;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.parser.ParserException;
import com.alibaba.druid.sql.parser.Token;
import com.amazon.opendistroforelasticsearch.sql.parser.ElasticSqlExprParser;
import com.amazon.opendistroforelasticsearch.sql.parser.subquery.SubqueryRewriterHelper;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SubqueryRewriterHelperTest {

    @Test
    public void isSubquery(){
        assertTrue(SubqueryRewriterHelper.isSubquery(parse("SELECT * FROM A WHERE a IN (SELECT b FROM B)")));
    }

    @Test
    public void notSupportedSubquery(){
        assertFalse(SubqueryRewriterHelper.isSubquery(parse("SELECT * FROM A WHERE a NOT IN (SELECT * FROM B)")));
    }

    private SQLQueryExpr parse(String sql) {
        ElasticSqlExprParser parser = new ElasticSqlExprParser(sql);
        SQLExpr expr = parser.expr();
        if (parser.getLexer().token() != Token.EOF) {
            throw new ParserException("Illegal sql: " + sql);
        }
        return (SQLQueryExpr) expr;
    }
}
