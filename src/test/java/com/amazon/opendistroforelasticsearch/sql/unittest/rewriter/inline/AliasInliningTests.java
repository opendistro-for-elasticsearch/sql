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

package com.amazon.opendistroforelasticsearch.sql.unittest.rewriter.inline;

import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.amazon.opendistroforelasticsearch.sql.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.parser.ElasticSqlExprParser;
import com.amazon.opendistroforelasticsearch.sql.parser.SqlParser;
import com.amazon.opendistroforelasticsearch.sql.query.AggregationQueryAction;
import com.amazon.opendistroforelasticsearch.sql.query.DefaultQueryAction;
import org.elasticsearch.client.Client;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;

public class AliasInliningTests {

    @Test
    public void orderByAliasedFieldTest() throws SqlParseException {
        String originalQuery = "SELECT utc_time date " +
                "FROM kibana_sample_data_logs " +
                "ORDER BY date DESC";
        String originalDsl =
                new DefaultQueryAction(mock(Client.class), new SqlParser().parseSelect(parseQuery(originalQuery))).explain().explain();

        String rewrittenQuery =
                "SELECT utc_time date " +
                "FROM kibana_sample_data_logs " +
                "ORDER BY utc_time DESC";

        String rewrittenDsl =
                new DefaultQueryAction(mock(Client.class), new SqlParser().parseSelect(parseQuery(rewrittenQuery))).explain().explain();

        assertThat(originalDsl, equalTo(rewrittenDsl));
    }

    @Test
    public void orderByAliasedScriptedField() throws SqlParseException {
        String originalQuery = "SELECT date_format(utc_time, 'dd-MM-YYYY') date " +
                "FROM kibana_sample_data_logs " +
                "ORDER BY date";

        String originalDsl =
                new AggregationQueryAction(mock(Client.class), new SqlParser().parseSelect(parseQuery(originalQuery))).explain().explain();

        System.out.println(originalDsl);

        String rewrittenQuery = "SELECT date_format(utc_time, 'dd-MM-YYYY') date " +
                "FROM kibana_sample_data_logs " +
                "ORDER BY date_format(utc_time, 'dd-MM-YYYY')";

        String rewrittenDsl =
                new AggregationQueryAction(mock(Client.class), new SqlParser().parseSelect(parseQuery(rewrittenQuery))).explain().explain();

        System.out.println(rewrittenDsl);
        assertThat(originalDsl, equalTo(rewrittenDsl));
    }

    @Test
    @Ignore("GROUP BY is not yet supported")
    public void groupByAliasedFieldTest() throws SqlParseException {
        String originalQuery = "SELECT utc_time date " +
                "FROM kibana_sample_data_logs " +
                "GROUP BY date";

        String originalDsl =
                new AggregationQueryAction(mock(Client.class), new SqlParser().parseSelect(parseQuery(originalQuery))).explain().explain();

        System.out.println(originalDsl);

        String rewrittenQuery = "SELECT utc_time date " +
                "FROM kibana_sample_data_logs " +
                "GROUP BY utc_time DESC";

        String rewrittenDsl =
                new AggregationQueryAction(mock(Client.class), new SqlParser().parseSelect(parseQuery(rewrittenQuery))).explain().explain();

        System.out.println(rewrittenDsl);
        assertThat(originalDsl, equalTo(rewrittenDsl));
    }

    @Test
    @Ignore("GROUP BY is not yet supported")
    public void groupByAliasedExpressionTest() throws SqlParseException {
        String originalQuery = "SELECT date_format(utc_time, 'dd-MM-YYYY') date " +
                "FROM kibana_sample_data_logs " +
                "GROUP BY date";

        String originalDsl =
                new AggregationQueryAction(mock(Client.class), new SqlParser().parseSelect(parseQuery(originalQuery))).explain().explain();

        String rewrittenQuery = "SELECT date_format(utc_time, 'dd-MM-YYYY') date " +
                        "FROM kibana_sample_data_logs " +
                        "GROUP BY date_format(utc_time, 'dd-MM-YYYY')";

        String rewrittenDsl =
                new AggregationQueryAction(mock(Client.class), new SqlParser().parseSelect(parseQuery(rewrittenQuery))).explain().explain();
        assertThat(originalDsl, equalTo(rewrittenDsl));
    }

    @Test
    @Ignore
    public void expressionAndOrderByTest2() throws SqlParseException {
        String originalQuery =
                "SELECT "
                        + "floor(substring(address.keyword,0,3)*20) as key, "
                        + "sum(age) cvalue FROM elasticsearch-sql_test_index_account/account where address is not null "
                        + "group by key order by cvalue desc limit 10 ";

        System.out.println(
                new AggregationQueryAction(mock(Client.class), new SqlParser().parseSelect(parseQuery(originalQuery))).explain().explain());
    }

    static SQLQueryExpr parseQuery(String query) {
        ElasticSqlExprParser parser = new ElasticSqlExprParser(query);
        return (SQLQueryExpr) parser.expr();
    }
}
