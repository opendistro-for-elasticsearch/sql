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
import com.amazon.opendistroforelasticsearch.sql.parser.SqlParser;
import com.amazon.opendistroforelasticsearch.sql.query.AggregationQueryAction;
import com.amazon.opendistroforelasticsearch.sql.rewriter.inline.GroupByInliner;
import com.amazon.opendistroforelasticsearch.sql.rewriter.inline.OrderByInliner;
import org.elasticsearch.client.Client;
import org.junit.Ignore;
import org.junit.Test;

import static com.amazon.opendistroforelasticsearch.sql.unittest.rewriter.inline.OrderByInlinerTest.parseQuery;
import static com.amazon.opendistroforelasticsearch.sql.unittest.rewriter.inline.OrderByInlinerTest.rewrite;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;

public class GroupByInlinerTest {
    @Test
    public void fieldTest() throws SqlParseException {
        String query = "SELECT utc_time date " +
                "FROM kibana_sample_data_logs " +
                "GROUP BY date DESC";

        assertThat(rewrite(query, new GroupByInliner()), equalTo(parseQuery(
                "SELECT utc_time date " +
                        "FROM kibana_sample_data_logs " +
                        "GROUP BY utc_time DESC")));


        System.out.println(new AggregationQueryAction(mock(Client.class), new SqlParser().parseSelect(rewrite(query, new GroupByInliner()))).explain().explain());
    }

    @Test
    public void expressionTest() throws SqlParseException {
        String query = "SELECT date_format(utc_time, 'dd-MM-YYYY') date " +
                "FROM kibana_sample_data_logs " +
                "GROUP BY date";

        assertThat(rewrite(query, new GroupByInliner()), equalTo(parseQuery(
                "SELECT date_format(utc_time, 'dd-MM-YYYY') date " +
                        "FROM kibana_sample_data_logs " +
                        "GROUP BY date_format(utc_time, 'dd-MM-YYYY')")));

        System.out.println(new AggregationQueryAction(mock(Client.class), new SqlParser().parseSelect(rewrite(query, new GroupByInliner()))).explain().explain());
    }

    @Test
    public void expressionAndOrderByTest() throws SqlParseException {
        String originalQuery = "SELECT date_format(utc_time, 'dd-MM-YYYY') date " +
                "FROM kibana_sample_data_logs " +
                "GROUP BY date " +
                "ORDER BY date DESC";

        SQLQueryExpr rewrittenOriginalQuery = rewrite(originalQuery, new OrderByInliner());
        new GroupByInliner().rewrite(rewrittenOriginalQuery);

        new AggregationQueryAction(mock(Client.class), new SqlParser().parseSelect(rewrittenOriginalQuery)).explain().explain();

        SQLQueryExpr parsedExpectedRewrittenQuery = parseQuery(
                "SELECT date_format(utc_time, 'dd-MM-YYYY') date " +
                        "FROM kibana_sample_data_logs " +
                        "GROUP BY date_format(utc_time, 'dd-MM-YYYY') " +
                        "ORDER BY date_format(utc_time, 'dd-MM-YYYY') DESC");

        new AggregationQueryAction(mock(Client.class), new SqlParser().parseSelect(parsedExpectedRewrittenQuery)).explain().explain();

        assertThat(rewrittenOriginalQuery, equalTo(parsedExpectedRewrittenQuery));
    }

    @Test
    @Ignore("Inline in HAVING not implemented")
    public void expressionWithHavingTest() throws SqlParseException {
        String query = "SELECT date_format(utc_time, 'dd-MM-YYYY') date " +
                "FROM kibana_sample_data_logs " +
                "GROUP BY date DESC " +
                "HAVING date > '15-06-2015'";

        assertThat(rewrite(query, new GroupByInliner()), equalTo(parseQuery(
                "SELECT date_format(utc_time, 'dd-MM-YYYY') date " +
                        "FROM kibana_sample_data_logs " +
                        "GROUP BY date_format(utc_time, 'dd-MM-YYYY') DESC " +
                        "HAVING date_format(utc_time, 'dd-MM-YYYY') > '15-06-2015'")));

        System.out.println(new AggregationQueryAction(mock(Client.class), new SqlParser().parseSelect(rewrite(query, new GroupByInliner()))).explain().explain());
    }
}
