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
import com.alibaba.druid.sql.parser.ParserException;
import com.alibaba.druid.sql.parser.Token;
import com.amazon.opendistroforelasticsearch.sql.domain.Condition;
import com.amazon.opendistroforelasticsearch.sql.domain.Select;
import com.amazon.opendistroforelasticsearch.sql.domain.Where;
import com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants;
import com.amazon.opendistroforelasticsearch.sql.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.parser.ElasticSqlExprParser;
import com.amazon.opendistroforelasticsearch.sql.parser.ScriptFilter;
import com.amazon.opendistroforelasticsearch.sql.parser.SqlParser;
import com.amazon.opendistroforelasticsearch.sql.query.ESActionFactory;
import com.amazon.opendistroforelasticsearch.sql.query.QueryAction;
import com.amazon.opendistroforelasticsearch.sql.util.CheckScriptContents;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.Strings;
import org.elasticsearch.index.query.AbstractQueryBuilder;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.sql.SQLFeatureNotSupportedException;

import static org.elasticsearch.index.query.QueryBuilders.constantScoreQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

//import sun.tools.jstat.ParserException;

public class WhereWithBoolConditionTest {

    private static final String SELECT_ALL = "SELECT * ";
    private static final String SELECT_ADDRESS = "SELECT address ";
    private static final String FROM_BANK = "FROM " + TestsConstants.TEST_INDEX_BANK;
    private static final String ORDER_BY_AGE = "ORDER BY age ";
    private static final String GROUP_BY_BALANCE = "GROUP BY balance ";
    private static final String WHERE_CONDITION_TRUE = "WHERE male = true";     // 4 hits
    private static final String WHERE_CONDITION_FALSE = "WHERE male = false";   // 3 hits

    @Test
    public void whereWithBoolCompilationTest() {
       query(FROM_BANK, WHERE_CONDITION_FALSE);
    }

    @Test
    public void selectAllTest() {
        String matchQuery = "{\"from\":0,\"size\":200," +
                "\"query\":{\"bool\":{\"filter\":[{\"bool\":{\"must\":" +
                "[{\"term\":{\"male\":{\"value\":true,\"boost\":1.0}}}],\"adjust_pure_negative\":true,\"boost\":1.0}}],\"adjust_pure_negative\":true,\"boost\":1.0}}}";

        assertTrue(query(FROM_BANK, WHERE_CONDITION_TRUE).equals(matchQuery));
    }

    private String query(String from, String... statements) {
        return explain(SELECT_ALL + from + " " + String.join(" ", statements));
    }

    private String explain(String sql) {
        try {
            Client mockClient = Mockito.mock(Client.class);
            CheckScriptContents.stubMockClient(mockClient);
            QueryAction queryAction = ESActionFactory.create(mockClient, sql);
            return queryAction.explain().explain();
        } catch (SqlParseException | NullPointerException | SQLFeatureNotSupportedException s) {
            throw new ParserException("Exception found in sql expr: " + sql + " " + s);
        }
    }

    private Matcher<String> contains(AbstractQueryBuilder queryBuilder) {
        return containsString(Strings.toString(queryBuilder, false, false));
    }

    private SQLQueryExpr parseSql(String sql) {
        ElasticSqlExprParser parser = new ElasticSqlExprParser(sql);
        SQLExpr expr = parser.expr();
        if (parser.getLexer().token() != Token.EOF) {
            throw new ParserException("Illegal sql: " + sql);
        }
        return (SQLQueryExpr) expr;
    }

    private Select getSelect(String query) {
        try {
            return new SqlParser().parseSelect(parseSql(query));
        } catch (SqlParseException e) {
            throw new RuntimeException(e);
        }
    }

    private SQLExpr queryToExpr(String query) {
        return new ElasticSqlExprParser(query).expr();
    }
}
