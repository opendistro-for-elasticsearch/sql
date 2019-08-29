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
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.amazon.opendistroforelasticsearch.sql.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.parser.ElasticSqlExprParser;
import com.amazon.opendistroforelasticsearch.sql.parser.SqlParser;
import com.amazon.opendistroforelasticsearch.sql.query.AggregationQueryAction;
import com.amazon.opendistroforelasticsearch.sql.query.DefaultQueryAction;
import com.amazon.opendistroforelasticsearch.sql.util.SqlParserUtils;
import org.elasticsearch.client.Client;
import org.junit.Ignore;
import org.junit.Test;

import static com.amazon.opendistroforelasticsearch.sql.util.SqlParserUtils.parse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;

public class AliasInliningTests {

    @Test
    public void orderByAliasedFieldTest() throws SqlParseException {
        String originalQuery = "SELECT utc_time date " +
                "FROM kibana_sample_data_logs " +
                "ORDER BY date DESC";
        String originalDsl = parseAsSimpleQuery(originalQuery);

        String rewrittenQuery =
                "SELECT utc_time date " +
                "FROM kibana_sample_data_logs " +
                "ORDER BY utc_time DESC";

        String rewrittenDsl = parseAsSimpleQuery(rewrittenQuery);

        assertThat(originalDsl, equalTo(rewrittenDsl));
    }

    private String parseAsSimpleQuery(String originalQuery) throws SqlParseException {
        return new DefaultQueryAction(mock(Client.class), new SqlParser().parseSelect(parse(originalQuery))).explain().explain();
    }

    @Test
    public void orderByAliasedScriptedField() throws SqlParseException {

        String originalDsl = parseAsSimpleQuery("SELECT date_format(utc_time, 'dd-MM-YYYY') date " +
                "FROM kibana_sample_data_logs " +
                "ORDER BY date");

        String rewrittenQuery = "SELECT date_format(utc_time, 'dd-MM-YYYY') date " +
                "FROM kibana_sample_data_logs " +
                "ORDER BY date_format(utc_time, 'dd-MM-YYYY')";

        String rewrittenDsl = parseAsSimpleQuery(rewrittenQuery);

        assertThat(originalDsl, equalTo(rewrittenDsl));
    }

    @Test
    public void groupByAliasedFieldTest() throws SqlParseException {
        String originalQuery = "SELECT utc_time date " +
                "FROM kibana_sample_data_logs " +
                "GROUP BY date";

        String originalDsl = parseAsAggregationQuery(originalQuery);

        String rewrittenQuery = "SELECT utc_time date " +
                "FROM kibana_sample_data_logs " +
                "GROUP BY utc_time DESC";

        String rewrittenDsl = parseAsAggregationQuery(rewrittenQuery);

        assertThat(originalDsl, equalTo(rewrittenDsl));
    }

    private String parseAsAggregationQuery(String originalQuery) throws SqlParseException {
        return new AggregationQueryAction(mock(Client.class),
                new SqlParser().parseSelect(parse(originalQuery))).explain().explain();
    }
}
