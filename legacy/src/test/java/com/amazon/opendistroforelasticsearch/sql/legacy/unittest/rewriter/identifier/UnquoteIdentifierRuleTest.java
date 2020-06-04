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

package com.amazon.opendistroforelasticsearch.sql.legacy.unittest.rewriter.identifier;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.amazon.opendistroforelasticsearch.sql.legacy.rewriter.identifier.UnquoteIdentifierRule;
import com.amazon.opendistroforelasticsearch.sql.legacy.util.SqlParserUtils;
import org.junit.Assert;
import org.junit.Test;


/**
 * Test cases for backticks quoted identifiers
 */
public class UnquoteIdentifierRuleTest {

    @Test
    public void queryWithQuotedIndex() {
        query("SELECT lastname FROM `bank` WHERE balance > 1000 ORDER BY age"
        ).shouldBeAfterRewrite("SELECT lastname FROM bank WHERE balance > 1000 ORDER BY age");
    }

    @Test
    public void queryWithQuotedField() {
        query("SELECT `lastname` FROM bank ORDER BY age"
        ).shouldBeAfterRewrite("SELECT lastname FROM bank ORDER BY age");

        query("SELECT b.`lastname` FROM bank AS b ORDER BY age"
        ).shouldBeAfterRewrite("SELECT b.lastname FROM bank AS b ORDER BY age");
    }

    @Test
    public void queryWithQuotedAlias() {
        query("SELECT `b`.lastname FROM bank AS `b` ORDER BY age"
        ).shouldBeAfterRewrite("SELECT b.lastname FROM bank AS b ORDER BY age");

        query("SELECT `b`.`lastname` FROM bank AS `b` ORDER BY age"
        ).shouldBeAfterRewrite("SELECT b.lastname FROM bank AS b ORDER BY age");

        query("SELECT `b`.`lastname` AS `name` FROM bank AS `b` ORDER BY age"
        ).shouldBeAfterRewrite("SELECT b.lastname AS name FROM bank AS b ORDER BY age");
    }

    @Test
    public void selectSpecificFieldsUsingQuotedTableNamePrefix() {
        query("SELECT `bank`.`lastname` FROM `bank`"
        ).shouldBeAfterRewrite("SELECT bank.lastname FROM bank");
    }

    @Test
    public void queryWithQuotedAggrAndFunc() {
        query("" +
                "SELECT `b`.`lastname` AS `name`, AVG(`b`.`balance`) FROM `bank` AS `b` " +
                "WHERE ABS(`b`.`age`) > 20 GROUP BY `b`.`lastname` ORDER BY `b`.`lastname`"
        ).shouldBeAfterRewrite(
                "SELECT b.lastname AS name, AVG(b.balance) FROM bank AS b " +
                        "WHERE ABS(b.age) > 20 GROUP BY b.lastname ORDER BY b.lastname"
        );
    }

    private QueryAssertion query(String sql) {
        return new QueryAssertion(sql);
    }

    private static class QueryAssertion {

        private UnquoteIdentifierRule rule = new UnquoteIdentifierRule();
        private SQLQueryExpr expr;

        QueryAssertion(String sql) {
            this.expr = SqlParserUtils.parse(sql);
        }

        void shouldBeAfterRewrite(String expected) {
            rule.rewrite(expr);
            Assert.assertEquals(
                    SQLUtils.toMySqlString(SqlParserUtils.parse(expected)),
                    SQLUtils.toMySqlString(expr)
            );
        }
    }
}
