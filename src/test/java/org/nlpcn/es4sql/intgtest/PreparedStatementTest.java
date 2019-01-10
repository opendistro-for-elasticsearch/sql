package org.nlpcn.es4sql.intgtest;

import com.alibaba.druid.sql.parser.ParserException;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.json.JSONObject;
import org.junit.Test;
import org.nlpcn.es4sql.MainTestSuite;
import org.nlpcn.es4sql.PreparedStatementRequest;
import org.nlpcn.es4sql.SearchDao;
import org.nlpcn.es4sql.SqlRequest;
import org.nlpcn.es4sql.SqlRequestFactory;
import org.nlpcn.es4sql.exception.SqlParseException;
import org.nlpcn.es4sql.query.QueryAction;
import org.nlpcn.es4sql.query.SqlElasticSearchRequestBuilder;

import java.sql.SQLFeatureNotSupportedException;
import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertTrue;
import static org.nlpcn.es4sql.TestsConstants.TEST_INDEX_ACCOUNT;

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
                "      \"type\": \"integer\",\n" +
                "      \"value\": \"20\"\n" +
                "    }\n" +
                "  ]\n" +
                "}", TEST_INDEX_ACCOUNT));
        SearchHit[] hits = response.getHits();

        assertTrue(hits.length > 0);
        for (SearchHit hit : hits) {
            int age = (int) hit.getSourceAsMap().get("age");
            assertThat(age, greaterThan(ageToCompare));
        }
    }

    private SearchHits query(String request) {
        try {
            SqlRequest sqlRequest = SqlRequestFactory.getSqlRequest(new TestRestRequest(request));

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

    static class TestRestRequest extends RestRequest {
        private String payload;

        TestRestRequest(String payload) {
            super(NamedXContentRegistry.EMPTY, "", new HashMap<>());
            this.payload = payload;
        }

        @Override
        public Method method() {
            return Method.POST;
        }

        @Override
        public String uri() {
            return "uri";
        }

        @Override
        public boolean hasContent() {
            return true;
        }

        @Override
        public BytesReference content() {
            return new BytesArray(this.payload);
        }
    }
}
