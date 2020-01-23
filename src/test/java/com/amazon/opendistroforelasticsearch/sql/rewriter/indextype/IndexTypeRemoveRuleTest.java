package com.amazon.opendistroforelasticsearch.sql.rewriter.indextype;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.amazon.opendistroforelasticsearch.sql.util.SqlParserUtils;
import org.junit.Assert;
import org.junit.Test;

public class IndexTypeRemoveRuleTest {

    private IndexTypeRemoveRule rule = new IndexTypeRemoveRule();

    @Test
    public void indexNameSlashTypeNameInFromClauseShouldKeepOnlyIndexName() {
        SQLQueryExpr actual = SqlParserUtils.parse("SELECT * FROM accounts/test");
        Assert.assertTrue(rule.match(actual));
        rule.rewrite(actual);

        String expected = SQLUtils.toMySqlString(SqlParserUtils.parse("SELECT * FROM accounts"));
        Assert.assertEquals(expected, SQLUtils.toMySqlString(actual));
    }

}