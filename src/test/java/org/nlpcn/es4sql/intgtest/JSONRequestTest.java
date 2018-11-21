package org.nlpcn.es4sql.intgtest;

import com.alibaba.druid.sql.parser.ParserException;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.json.JSONObject;
import org.junit.Test;
import org.nlpcn.es4sql.MainTestSuite;
import org.nlpcn.es4sql.SearchDao;
import org.nlpcn.es4sql.SqlRequest;
import org.nlpcn.es4sql.exception.SqlParseException;
import org.nlpcn.es4sql.query.QueryAction;
import org.nlpcn.es4sql.query.SqlElasticSearchRequestBuilder;

import java.sql.SQLFeatureNotSupportedException;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.nlpcn.es4sql.TestsConstants.TEST_INDEX_ACCOUNT;
import static org.nlpcn.es4sql.TestsConstants.TEST_INDEX_NESTED_TYPE;

public class JSONRequestTest {

    @Test
    public void search() {
        int ageToCompare = 25;
        SearchHits response = query(String.format("{\"query\":\"" +
                "SELECT * " +
                "FROM %s/account " +
                "WHERE age > %s " +
                "LIMIT 1000\"}", TEST_INDEX_ACCOUNT, ageToCompare));
        SearchHit[] hits = response.getHits();
        for (SearchHit hit : hits) {
            int age = (int) hit.getSourceAsMap().get("age");
            assertThat(age, greaterThan(ageToCompare));
        }
    }

    @Test
    public void searchWithFilterAndNoWhere() {
        /*
         * Human readable format of the request defined below:
         * {
         *   "query": "SELECT * FROM accounts LIMIT 1000",
         *   "filter": {
         *     "range": {
         *       "age": {
         *         "gt": 25
         *       }
         *     }
         *   }
         * }
         */
        int ageToCompare = 25;
        SearchHits response = query(String.format("{\"query\":\"" +
                "SELECT * " +
                "FROM %s/account " +
                "LIMIT 1000\",\"filter\":{\"range\":{\"age\":{\"gt\":%s}}}}", TEST_INDEX_ACCOUNT, ageToCompare));
        SearchHit[] hits = response.getHits();
        for (SearchHit hit : hits) {
            int age = (int) hit.getSourceAsMap().get("age");
            assertThat(age, greaterThan(ageToCompare));
        }
    }

    @Test
    public void searchWithRangeFilter() {
        /*
         * Human readable format of the request defined below:
         * {
         *   "query": "SELECT * FROM accounts WHERE age > 25 LIMIT 1000",
         *   "filter": {
         *     "range": {
         *       "balance": {
         *         "lt": 35000
         *       }
         *     }
         *   }
         * }
         */
        int ageToCompare = 25;
        int balanceToCompare = 35000;
        SearchHits response = query(String.format("{\"query\":\"" +
                "SELECT * " +
                "FROM %s/account " +
                "WHERE age > %s " +
                "LIMIT 1000\",\"filter\":{\"range\":{\"balance\":{\"lt\":%s}}}}",
                TEST_INDEX_ACCOUNT, ageToCompare, balanceToCompare));
        SearchHit[] hits = response.getHits();
        for (SearchHit hit : hits) {
            int age = (int) hit.getSourceAsMap().get("age");
            int balance = (int) hit.getSourceAsMap().get("balance");
            assertThat(age, greaterThan(ageToCompare));
            assertThat(balance, lessThan(balanceToCompare));
        }
    }

    @Test
    /**
     * Using TEST_INDEX_NESTED_TYPE here since term filter does not work properly on analyzed fields like text.
     * The field 'someField' in TEST_INDEX_NESTED_TYPE is of type keyword.
     */
    public void searchWithTermFilter() {
        /*
         * Human readable format of the request defined below:
         * {
         *   "query": "SELECT * FROM nested_objects WHERE nested(comment.likes) < 3",
         *   "filter": {
         *     "term": {
         *       "someField": "a"
         *     }
         *   }
         * }
         */
        int likesToCompare = 3;
        String fieldToCompare = "a";
        SearchHits response = query(String.format("{\"query\":\"" +
                "SELECT * " +
                "FROM %s/nestedType " +
                "WHERE nested(comment.likes) < %s\"," +
                "\"filter\":{\"term\":{\"someField\":\"%s\"}}}",
                TEST_INDEX_NESTED_TYPE, likesToCompare, fieldToCompare));
        SearchHit[] hits = response.getHits();
        for (SearchHit hit : hits) {
            int likes = (int) ((Map) hit.getSourceAsMap().get("comment")).get("likes");
            String someField = hit.getSourceAsMap().get("someField").toString();
            assertThat(likes, lessThan(likesToCompare));
            assertThat(someField, equalTo(fieldToCompare));
        }
    }

    @Test
    public void searchWithNestedFilter() {
        /*
         * Human readable format of the request defined below:
         * {
         *   "query": "SELECT * FROM nested_objects WHERE nested(comment.likes) > 1",
         *   "filter": {
         *     "nested": {
         *       "path": "comment",
         *       "query": {
         *         "bool": {
         *           "must": {
         *             "term": {
         *               "comment.data": "aa"
         *             }
         *           }
         *         }
         *       }
         *     }
         *   }
         * }
         */
        int likesToCompare = 1;
        String dataToCompare = "aa";
        SearchHits response = query(String.format("{\"query\":\"" +
                "SELECT * " +
                "FROM %s/nestedType " +
                "WHERE nested(comment.likes) > %s\"," +
                "\"filter\":{\"nested\":{\"path\":\"comment\"," +
                "\"query\":{\"bool\":{\"must\":{\"term\":{\"comment.data\":\"%s\"}}}}}}}",
                TEST_INDEX_NESTED_TYPE, likesToCompare, dataToCompare));
        SearchHit[] hits = response.getHits();
        for (SearchHit hit : hits) {
            int likes = (int) ((Map) hit.getSourceAsMap().get("comment")).get("likes");
            String data = ((Map) hit.getSourceAsMap().get("comment")).get("data").toString();
            assertThat(likes, greaterThan(likesToCompare));
            assertThat(data, equalTo(dataToCompare));
        }
    }

    private SearchHits query(String request) {
        try {
            JSONObject jsonRequest = new JSONObject(request);
            String sql = jsonRequest.getString("query");

            SearchDao searchDao = MainTestSuite.getSearchDao();
            QueryAction queryAction = searchDao.explain(sql);

            SqlRequest sqlRequest = new SqlRequest(sql, jsonRequest);
            queryAction.setSqlRequest(sqlRequest);

            SqlElasticSearchRequestBuilder select = (SqlElasticSearchRequestBuilder) queryAction.explain();
            return ((SearchResponse) select.get()).getHits();

        } catch (SqlParseException | SQLFeatureNotSupportedException e) {
            throw new ParserException("Illegal sql expr in request: " + request);
        }
    }
}
