package com.amazon.opendistroforelasticsearch.sql.unittest.rewriter.identifier;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.amazon.opendistroforelasticsearch.sql.rewriter.identifier.UnquoteIdentifierRule;
import com.amazon.opendistroforelasticsearch.sql.util.SqlParserUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test cases for backticks quoted identifiers
 */
public class UnquoteIdentifierRuleTest {

    @Test
    public void queryWithBackticksQuotedIndex() {
        query("SELECT lastname FROM `bank` WHERE balance > 1000 ORDER BY age"
        ).shouldBeAfterRewrite("SELECT lastname FROM bank WHERE balance > 1000 ORDER BY age");
    }

    @Test
    public void queryWithBackticksQuotedField() {
        query("SELECT `lastname` FROM bank ORDER BY age"
        ).shouldBeAfterRewrite("SELECT lastname FROM bank ORDER BY age");

        query("SELECT b.`lastname` FROM bank AS b ORDER BY age"
        ).shouldBeAfterRewrite("SELECT b.lastname FROM bank AS b ORDER BY age");
    }

    @Test
    public void queryWithBackticksQuotedAlias() {
        query("SELECT `b`.lastname FROM bank as `b` ORDER BY age"
        ).shouldBeAfterRewrite("SELECT b.lastname FROM bank as b ORDER BY age");

        query("SELECT `b`.`lastname` FROM `bank` as `b` ORDER BY age"
        ).shouldBeAfterRewrite("SELECT b.lastname FROM bank as b ORDER BY age");

        query("SELECT `b`.`lastname` AS `name` FROM bank as `b` ORDER BY age"
        ).shouldBeAfterRewrite("SELECT b.lastname AS name FROM bank as b ORDER BY age");
    }

    private QueryAssertion query(String sql) {
        return new QueryAssertion(sql);
    }

    private static class QueryAssertion {

        private final UnquoteIdentifierRule rule = new UnquoteIdentifierRule();
        private final SQLQueryExpr expr;

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
