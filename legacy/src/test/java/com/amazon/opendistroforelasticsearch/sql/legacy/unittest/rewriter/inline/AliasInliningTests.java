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

package com.amazon.opendistroforelasticsearch.sql.legacy.unittest.rewriter.inline;

import com.amazon.opendistroforelasticsearch.sql.legacy.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.legacy.parser.SqlParser;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.AggregationQueryAction;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.DefaultQueryAction;
import com.amazon.opendistroforelasticsearch.sql.legacy.request.SqlRequest;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.elasticsearch.client.Client;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

import static com.amazon.opendistroforelasticsearch.sql.legacy.util.CheckScriptContents.mockLocalClusterState;
import static com.amazon.opendistroforelasticsearch.sql.legacy.util.SqlParserUtils.parse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;

public class AliasInliningTests {

    private static final String TEST_MAPPING_FILE = "mappings/semantics.json";
    @Before
    public void setUp() throws IOException {
        URL url = Resources.getResource(TEST_MAPPING_FILE);
        String mappings = Resources.toString(url, Charsets.UTF_8);
        mockLocalClusterState(mappings);
    }

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

    @Test
    public void orderByAliasedScriptedField() throws SqlParseException {
        String originalDsl = parseAsSimpleQuery("SELECT date_format(birthday, 'dd-MM-YYYY') date " +
                "FROM bank " +
                "ORDER BY date");
        String rewrittenQuery = "SELECT date_format(birthday, 'dd-MM-YYYY') date " +
                "FROM bank " +
                "ORDER BY date_format(birthday, 'dd-MM-YYYY')";

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

    @Test
    public void groupAndSortBySameExprAlias() throws SqlParseException {
        String query = "SELECT date_format(timestamp, 'yyyy-MM') es-table.timestamp_tg, COUNT(*) count, COUNT(DistanceKilometers) es-table.DistanceKilometers_count\n" +
                "FROM kibana_sample_data_flights\n" +
                "GROUP BY date_format(timestamp, 'yyyy-MM')\n" +
                "ORDER BY date_format(timestamp, 'yyyy-MM') DESC\n" +
                "LIMIT 2500";
        String dsl = parseAsAggregationQuery(query);

        JSONObject parseQuery = new JSONObject(dsl);

        assertThat(parseQuery.query("/aggregations/es-table.timestamp_tg/terms/script"), notNullValue());

    }

    @Test
    public void groupByAndSortAliased() throws SqlParseException {
        String dsl = parseAsAggregationQuery(
                "SELECT date_format(utc_time, 'dd-MM-YYYY') date " +
                        "FROM kibana_sample_data_logs " +
                        "GROUP BY date " +
                        "ORDER BY date DESC");

        JSONObject parsedQuery = new JSONObject(dsl);

        JSONObject query = (JSONObject)parsedQuery.query("/aggregations/date/terms/script");

        assertThat(query, notNullValue());
    }

    private String parseAsSimpleQuery(String originalQuery) throws SqlParseException {
        SqlRequest sqlRequest = new SqlRequest(originalQuery, new JSONObject());
        DefaultQueryAction defaultQueryAction = new DefaultQueryAction(mock(Client.class),
                new SqlParser().parseSelect(parse(originalQuery)));
        defaultQueryAction.setSqlRequest(sqlRequest);
        return defaultQueryAction.explain().explain();
    }

    private String parseAsAggregationQuery(String originalQuery) throws SqlParseException {
        return new AggregationQueryAction(mock(Client.class),
                new SqlParser().parseSelect(parse(originalQuery))).explain().explain();
    }
}
