/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.legacy.unittest.rewriter.term;


import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.amazon.opendistroforelasticsearch.sql.legacy.rewriter.matchtoterm.TermFieldRewriter;
import com.amazon.opendistroforelasticsearch.sql.legacy.rewriter.matchtoterm.VerificationException;
import com.amazon.opendistroforelasticsearch.sql.legacy.util.MatcherUtils;
import com.amazon.opendistroforelasticsearch.sql.legacy.util.SqlParserUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.amazon.opendistroforelasticsearch.sql.legacy.util.MultipleIndexClusterUtils.mockMultipleIndexEnv;
import static org.hamcrest.MatcherAssert.assertThat;

public class TermFieldRewriterTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setup() {
        mockMultipleIndexEnv();
    }

    @Test
    public void testFromSubqueryShouldPass() {
        String sql = "SELECT t.age as a FROM (SELECT age FROM account1 WHERE address = 'sea') t";
        String expected = "SELECT t.age as a FROM (SELECT age FROM account1 WHERE address.keyword = 'sea') t";

        assertThat(rewriteTerm(sql),
                MatcherUtils.IsEqualIgnoreCaseAndWhiteSpace.equalToIgnoreCaseAndWhiteSpace(expected));
    }

    @Test
    public void testFromSubqueryWithoutTermShouldPass() {
        String sql = "SELECT t.age as a FROM (SELECT age FROM account1 WHERE age = 10) t";
        String expected = sql;

        assertThat(rewriteTerm(sql),
                MatcherUtils.IsEqualIgnoreCaseAndWhiteSpace.equalToIgnoreCaseAndWhiteSpace(expected));
    }

    @Test
    public void testFieldShouldBeRewritten() {
        String sql = "SELECT age FROM account1 WHERE address = 'sea'";
        String expected = "SELECT age FROM account1 WHERE address.keyword = 'sea'";

        assertThat(rewriteTerm(sql),
                MatcherUtils.IsEqualIgnoreCaseAndWhiteSpace.equalToIgnoreCaseAndWhiteSpace(expected));
    }

    @Test
    public void testSelectTheFieldWithCompatibleMappingShouldPass() {
        String sql = "SELECT id FROM account* WHERE id = 10";
        String expected = sql;

        assertThat(rewriteTerm(sql),
                MatcherUtils.IsEqualIgnoreCaseAndWhiteSpace.equalToIgnoreCaseAndWhiteSpace(expected));
    }

    @Test
    public void testSelectTheFieldOnlyInOneIndexShouldPass() {
        String sql = "SELECT address FROM account*";
        String expected = sql;

        assertThat(rewriteTerm(sql),
                MatcherUtils.IsEqualIgnoreCaseAndWhiteSpace.equalToIgnoreCaseAndWhiteSpace(expected));
    }

    /**
     * Ideally, it should fail. There are two reasons we didn't cover it now.
     * 1. The semantic check already done that.
     * 2. The {@link TermFieldRewriter} didn't touch allcolumn case.
     */
    @Test
    public void testSelectAllFieldWithConflictMappingShouldPass() {
        String sql = "SELECT * FROM account*";
        String expected = sql;

        assertThat(rewriteTerm(sql),
                MatcherUtils.IsEqualIgnoreCaseAndWhiteSpace.equalToIgnoreCaseAndWhiteSpace(expected));
    }

    @Test
    public void testSelectTheFieldWithConflictMappingShouldThrowException() {
        String sql = "SELECT age FROM account* WHERE age = 10";
        exception.expect(VerificationException.class);
        exception.expectMessage("Different mappings are not allowed for the same field[age]");
        rewriteTerm(sql);
    }

    private String rewriteTerm(String sql) {
        SQLQueryExpr sqlQueryExpr = SqlParserUtils.parse(sql);
        sqlQueryExpr.accept(new TermFieldRewriter());
        return SQLUtils.toMySqlString(sqlQueryExpr)
                .replaceAll("[\\n\\t]+", " ")
                .replaceAll("^\\(", " ")
                .replaceAll("\\)$", " ")
                .trim();
    }
}