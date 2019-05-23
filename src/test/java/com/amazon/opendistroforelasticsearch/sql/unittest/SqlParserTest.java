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

package com.amazon.opendistroforelasticsearch.sql.unittest;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.amazon.opendistroforelasticsearch.sql.domain.Condition;
import com.amazon.opendistroforelasticsearch.sql.domain.Select;
import com.amazon.opendistroforelasticsearch.sql.domain.Where;
import com.amazon.opendistroforelasticsearch.sql.parser.ElasticSqlExprParser;
import com.amazon.opendistroforelasticsearch.sql.parser.ScriptFilter;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.regex.Pattern;

import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_ACCOUNT;

public class SqlParserTest {

    private static com.amazon.opendistroforelasticsearch.sql.parser.SqlParser parser;

    @BeforeClass
    public static void init() {
        parser = new com.amazon.opendistroforelasticsearch.sql.parser.SqlParser();
    }

    @Test
    public void whereConditionLeftFunctionRightPropertyGreatTest() throws Exception {

        String query = "SELECT " +
                " * from " +
                TEST_INDEX_ACCOUNT + "/account " +
                " where floor(split(address,' ')[0]+0) > b limit 1000  ";

        Select select = parser.parseSelect((SQLQueryExpr) queryToExpr(query));
        Where where = select.getWhere();
        Assert.assertTrue((where.getWheres().size() == 1));
        Assert.assertTrue(((Condition) (where.getWheres().get(0))).getValue() instanceof ScriptFilter);
        ScriptFilter scriptFilter = (ScriptFilter) (((Condition) (where.getWheres().get(0))).getValue());

        Assert.assertTrue(scriptFilter.getScript().contains("doc['address'].value.split(' ')[0]"));
        Pattern pattern = Pattern.compile("floor_\\d+ > doc\\['b'\\].value");
        java.util.regex.Matcher matcher = pattern.matcher(scriptFilter.getScript());
        Assert.assertTrue(matcher.find());
    }

    @Test
    public void whereConditionLeftFunctionRightFunctionEqualTest() throws Exception {

        String query = "SELECT " +
                " * from " +
                TEST_INDEX_ACCOUNT + "/account " +
                " where floor(split(address,' ')[0]+0) = floor(split(address,' ')[0]+0) limit 1000  ";

        Select select = parser.parseSelect((SQLQueryExpr) queryToExpr(query));
        Where where = select.getWhere();
        Assert.assertTrue((where.getWheres().size() == 1));
        Assert.assertTrue(((Condition) (where.getWheres().get(0))).getValue() instanceof ScriptFilter);
        ScriptFilter scriptFilter = (ScriptFilter) (((Condition) (where.getWheres().get(0))).getValue());
        Assert.assertTrue(scriptFilter.getScript().contains("doc['address'].value.split(' ')[0]"));
        Pattern pattern = Pattern.compile("floor_\\d+ == floor_\\d+");
        java.util.regex.Matcher matcher = pattern.matcher(scriptFilter.getScript());
        Assert.assertTrue(matcher.find());
    }

    @Test
    public void whereConditionVariableRightVariableEqualTest() throws Exception {

        String query = "SELECT " +
                " * from " +
                TEST_INDEX_ACCOUNT + "/account " +
                " where a = b limit 1000  ";

        Select select = parser.parseSelect((SQLQueryExpr) queryToExpr(query));
        Where where = select.getWhere();
        Assert.assertTrue((where.getWheres().size() == 1));
        Assert.assertTrue(((Condition) (where.getWheres().get(0))).getValue() instanceof ScriptFilter);
        ScriptFilter scriptFilter = (ScriptFilter) (((Condition) (where.getWheres().get(0))).getValue());
        Assert.assertTrue(scriptFilter.getScript().contains("doc['a'].value == doc['b'].value"));
    }

    private SQLExpr queryToExpr(String query) {
        return new ElasticSqlExprParser(query).expr();
    }
}
