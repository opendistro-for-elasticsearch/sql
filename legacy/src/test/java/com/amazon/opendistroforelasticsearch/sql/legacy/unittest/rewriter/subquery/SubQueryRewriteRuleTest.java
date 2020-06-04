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

package com.amazon.opendistroforelasticsearch.sql.legacy.unittest.rewriter.subquery;

import com.amazon.opendistroforelasticsearch.sql.legacy.rewriter.subquery.SubQueryRewriteRule;
import com.amazon.opendistroforelasticsearch.sql.legacy.util.SqlParserUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.SQLFeatureNotSupportedException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SubQueryRewriteRuleTest {

    final SubQueryRewriteRule rewriteRule = new SubQueryRewriteRule();

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void isInMatch() throws SQLFeatureNotSupportedException {
        String sql = "SELECT * " +
                     "FROM A " +
                     "WHERE a IN (SELECT b FROM B)";
        assertTrue(rewriteRule.match(SqlParserUtils.parse(sql)));
    }

    @Test
    public void isNotInMatch() throws SQLFeatureNotSupportedException {
        String sql = "SELECT * " +
                     "FROM A " +
                     "WHERE a NOT IN (SELECT b FROM B)";
        assertTrue(rewriteRule.match(SqlParserUtils.parse(sql)));
    }

    @Test
    public void isExistsMatch() throws SQLFeatureNotSupportedException {
        String sql = "SELECT * " +
                     "FROM A WHERE " +
                     "EXISTS (SELECT 1 FROM B WHERE A.a_v = B.b_v)";
        assertTrue(rewriteRule.match(SqlParserUtils.parse(sql)));
    }

    @Test
    public void isNotExistsMatch() throws SQLFeatureNotSupportedException {
        String sql = "SELECT * " +
                     "FROM A " +
                     "WHERE NOT EXISTS (SELECT 1 FROM B WHERE A.a_v = B.b_v)";
        assertTrue(rewriteRule.match(SqlParserUtils.parse(sql)));
    }

    @Test
    public void subQueryInSelectNotMatch() throws SQLFeatureNotSupportedException {
        String sql = "SELECT A.v as v, (SELECT MAX(b) FROM B WHERE A.id = B.id) as max_age " +
                     "FROM A";
        assertFalse(rewriteRule.match(SqlParserUtils.parse(sql)));
    }

    @Test
    public void moreThanOneInIsNotSupporeted() throws SQLFeatureNotSupportedException {
        String sql = "SELECT * " +
                     "FROM A " +
                     "WHERE a IN (SELECT b FROM B) and d IN (SELECT e FROM F)";
        exceptionRule.expect(SQLFeatureNotSupportedException.class);
        exceptionRule.expectMessage("Unsupported subquery. Only one EXISTS or IN is supported");
        rewriteRule.match(SqlParserUtils.parse(sql));
    }

    @Test
    public void moreThanOneExistsIsNotSupporeted() throws SQLFeatureNotSupportedException {
        String sql = "SELECT * " +
                     "FROM A WHERE " +
                     "EXISTS (SELECT 1 FROM B WHERE A.a_v = B.b_v) AND EXISTS (SELECT 1 FROM C)";
        exceptionRule.expect(SQLFeatureNotSupportedException.class);
        exceptionRule.expectMessage("Unsupported subquery. Only one EXISTS or IN is supported");
        rewriteRule.match(SqlParserUtils.parse(sql));
    }
}