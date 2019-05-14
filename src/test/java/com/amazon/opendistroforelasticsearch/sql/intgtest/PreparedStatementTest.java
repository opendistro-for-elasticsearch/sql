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

package com.amazon.opendistroforelasticsearch.sql.intgtest;

import com.alibaba.druid.sql.parser.ParserException;
import com.amazon.opendistroforelasticsearch.sql.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.plugin.SearchDao;
import com.amazon.opendistroforelasticsearch.sql.query.QueryAction;
import com.amazon.opendistroforelasticsearch.sql.query.SqlElasticSearchRequestBuilder;
import com.amazon.opendistroforelasticsearch.sql.request.SqlRequest;
import com.amazon.opendistroforelasticsearch.sql.request.SqlRequestFactory;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.http.HttpChannel;
import org.elasticsearch.http.HttpRequest;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.json.JSONObject;
import org.junit.Test;

import java.sql.SQLFeatureNotSupportedException;

import static org.elasticsearch.rest.RestRequest.Method.POST;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PreparedStatementTest {

    @Test
    public void search() {
        int ageToCompare = 35;
        SearchHits response = query(String.format("{\n" +
                "  \"query\": \"SELECT * FROM %s/account WHERE age > ? AND state in (?, ?) LIMIT ?\",\n" +
                "  \"parameters\": [\n" +
                "    {\n" +
                "      \"type\": \"integer\",\n" +
                "      \"value\": \"" + ageToCompare + "\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"string\",\n" +
                "      \"value\": \"TN\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"string\",\n" +
                "      \"value\": \"UT\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"short\",\n" +
                "      \"value\": \"20\"\n" +
                "    }\n" +
                "  ]\n" +
                "}", TestsConstants.TEST_INDEX_ACCOUNT));
        SearchHit[] hits = response.getHits();

        assertTrue(hits.length > 0);
        for (SearchHit hit : hits) {
            int age = (int) hit.getSourceAsMap().get("age");
            assertThat(age, greaterThan(ageToCompare));
        }
    }

    private SearchHits query(String request) {
        try {
            final HttpRequest httpRequest = mock(HttpRequest.class);
            when(httpRequest.uri()).thenReturn("uri");
            when(httpRequest.method()).thenReturn(POST);
            when(httpRequest.content()).thenReturn(new BytesArray(request));

            SqlRequest sqlRequest = SqlRequestFactory.getSqlRequest(RestRequest.request(NamedXContentRegistry.EMPTY, httpRequest, mock(HttpChannel.class)));

            JSONObject jsonRequest = new JSONObject(request);
            String sql = sqlRequest.getSql();

            SearchDao searchDao = MainTestSuite.getSearchDao();
            QueryAction queryAction = searchDao.explain(sql);

            queryAction.setSqlRequest(sqlRequest);

            SqlElasticSearchRequestBuilder select = (SqlElasticSearchRequestBuilder) queryAction.explain();
            return ((SearchResponse) select.get()).getHits();

        } catch (SqlParseException | SQLFeatureNotSupportedException e) {
            throw new ParserException("Illegal sql expr in request: " + request, e);
        }
    }
}
