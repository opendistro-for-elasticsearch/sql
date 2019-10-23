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

package com.amazon.opendistroforelasticsearch.sql.unittest.rewriter.ordinal;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;

import com.amazon.opendistroforelasticsearch.sql.rewriter.matchtoterm.VerificationException;
import com.amazon.opendistroforelasticsearch.sql.rewriter.ordinal.OrdinalRewriterRule;
import com.amazon.opendistroforelasticsearch.sql.util.SqlParserUtils;

import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLFeatureNotSupportedException;

import static org.hamcrest.Matchers.containsString;

/**
 * Test cases for ordinal aliases in GROUP BY and ORDER BY
 */

public class OrdinalRewriterRuleTest {

    @Test
    public void ordinalInGroupByShouldMatch() throws SQLFeatureNotSupportedException {
        query("SELECT lastname FROM bank GROUP BY 1").shouldMatchRule();
    }

    @Test
    public void ordinalInOrderByShouldMatch() throws SQLFeatureNotSupportedException {
        query("SELECT lastname FROM bank ORDER BY 1").shouldMatchRule();
    }


    @Test
    public void ordinalInGroupAndOrderByShouldMatch() throws SQLFeatureNotSupportedException {
        query("SELECT lastname, age FROM bank GROUP BY 2, 1 ORDER BY 1").shouldMatchRule();
    }

    @Test
    public void noOrdinalInGroupByShouldNotMatch() throws SQLFeatureNotSupportedException {
        query("SELECT lastname FROM bank GROUP BY lastname").shouldNotMatchRule();
    }

    @Test
    public void noOrdinalInOrderByShouldNotMatch() throws SQLFeatureNotSupportedException {
        query("SELECT lastname, age FROM bank ORDER BY age").shouldNotMatchRule();
    }

    @Test
    public void noOrdinalInGroupAndOrderByShouldNotMatch() throws SQLFeatureNotSupportedException {
        query("SELECT lastname, age FROM bank GROUP BY lastname, age ORDER BY age").shouldNotMatchRule();
    }

    @Test
    public void simpleGroupByOrdinal() throws SQLFeatureNotSupportedException {
        query("SELECT lastname FROM bank GROUP BY 1"
        ).shouldBeAfterRewrite("SELECT lastname FROM bank GROUP BY lastname");
    }

    @Test
    public void multipleGroupByOrdinal() throws SQLFeatureNotSupportedException {
        query("SELECT lastname, age FROM bank GROUP BY 1, 2 "
        ).shouldBeAfterRewrite("SELECT lastname, age FROM bank GROUP BY lastname, age");
    }

    @Test
    public void multipleGroupByOrdinalDifferentorder() throws SQLFeatureNotSupportedException {
        query("SELECT lastname, age FROM bank GROUP BY 2, 1"
        ).shouldBeAfterRewrite("SELECT lastname, age FROM bank GROUP BY age, lastname");
    }

    @Test
    public void simpleOrderByOrdinal() throws SQLFeatureNotSupportedException {
        query("SELECT lastname FROM bank ORDER BY 1"
        ).shouldBeAfterRewrite("SELECT lastname FROM bank ORDER BY lastname");
    }

    @Test
    public void multipleOrderByOrdinal() throws SQLFeatureNotSupportedException {
        query("SELECT lastname, age FROM bank ORDER BY 1, 2 "
        ).shouldBeAfterRewrite("SELECT lastname, age FROM bank ORDER BY lastname, age");
    }

    @Test
    public void multipleOrderByOrdinalDifferentorder() throws SQLFeatureNotSupportedException {
        query("SELECT lastname, age FROM bank ORDER BY 2, 1"
        ).shouldBeAfterRewrite("SELECT lastname, age FROM bank ORDER BY age, lastname");
    }
    

    // TODO: Some more Tests

    private QueryAssertion query(String sql) {
        return new QueryAssertion(sql);
    }
    private static class QueryAssertion {

        private OrdinalRewriterRule rule;
        private SQLQueryExpr expr;

        QueryAssertion(String sql) {
            this.expr = SqlParserUtils.parse(sql);
            this.rule = new OrdinalRewriterRule(sql);
        }

        void shouldBeAfterRewrite(String expected) throws SQLFeatureNotSupportedException {
            shouldMatchRule();
            rule.rewrite(expr);
            Assert.assertEquals(
                SQLUtils.toMySqlString(SqlParserUtils.parse(expected)),
                SQLUtils.toMySqlString(expr)
            );
        }

        void shouldMatchRule() throws SQLFeatureNotSupportedException {
            Assert.assertTrue(match());
        }

        void shouldNotMatchRule() throws SQLFeatureNotSupportedException {
            Assert.assertFalse(match());
        }

        void shouldThrowException(int ordinal) throws SQLFeatureNotSupportedException {
            try {
                shouldMatchRule();
                rule.rewrite(expr);
                Assert.fail("Expected VerificationException, but none was thrown");
            } catch (VerificationException e) {
                Assert.assertThat(e.getMessage(), containsString("Invalid ordinal ["+ ordinal +"] specified in"));
            }
        }

        private boolean match() throws SQLFeatureNotSupportedException {
            return rule.match(expr);
        }
    }
}
