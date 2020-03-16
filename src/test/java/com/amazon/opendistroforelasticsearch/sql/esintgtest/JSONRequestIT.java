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

package com.amazon.opendistroforelasticsearch.sql.esintgtest;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.xcontent.LoggingDeprecationHandler;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;

public class JSONRequestIT extends SQLIntegTestCase {

    @Override
    protected void init() throws Exception {
        loadIndex(Index.ACCOUNT);
        loadIndex(Index.NESTED);
    }

    @Test
    public void search() throws IOException {
        int ageToCompare = 25;
        SearchHits response = query(String.format("{\"query\":\"" +
                "SELECT * " +
                "FROM %s " +
                "WHERE age > %s " +
                "LIMIT 1000\"}", TestsConstants.TEST_INDEX_ACCOUNT, ageToCompare));
        SearchHit[] hits = response.getHits();
        for (SearchHit hit : hits) {
            int age = (int) hit.getSourceAsMap().get("age");
            assertThat(age, greaterThan(ageToCompare));
        }
    }

    @Test
    public void searchWithFilterAndNoWhere() throws IOException {
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
                "FROM %s " +
                "LIMIT 1000\",\"filter\":{\"range\":{\"age\":{\"gt\":%s}}}}", TestsConstants.TEST_INDEX_ACCOUNT, ageToCompare));
        SearchHit[] hits = response.getHits();
        for (SearchHit hit : hits) {
            int age = (int) hit.getSourceAsMap().get("age");
            assertThat(age, greaterThan(ageToCompare));
        }
    }

    @Test
    public void searchWithRangeFilter() throws IOException {
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
                "FROM %s " +
                "WHERE age > %s " +
                "LIMIT 1000\",\"filter\":{\"range\":{\"balance\":{\"lt\":%s}}}}",
                TestsConstants.TEST_INDEX_ACCOUNT, ageToCompare, balanceToCompare));
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
    public void searchWithTermFilter() throws IOException {
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
                "FROM %s " +
                "WHERE nested(comment.likes) < %s\"," +
                "\"filter\":{\"term\":{\"someField\":\"%s\"}}}",
                TestsConstants.TEST_INDEX_NESTED_TYPE, likesToCompare, fieldToCompare));
        SearchHit[] hits = response.getHits();
        for (SearchHit hit : hits) {
            int likes = (int) ((Map) hit.getSourceAsMap().get("comment")).get("likes");
            String someField = hit.getSourceAsMap().get("someField").toString();
            assertThat(likes, lessThan(likesToCompare));
            assertThat(someField, equalTo(fieldToCompare));
        }
    }

    @Test
    public void searchWithNestedFilter() throws IOException {
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
                "FROM %s " +
                "WHERE nested(comment.likes) > %s\"," +
                "\"filter\":{\"nested\":{\"path\":\"comment\"," +
                "\"query\":{\"bool\":{\"must\":{\"term\":{\"comment.data\":\"%s\"}}}}}}}",
                TestsConstants.TEST_INDEX_NESTED_TYPE, likesToCompare, dataToCompare));
        SearchHit[] hits = response.getHits();
        for (SearchHit hit : hits) {
            int likes = (int) ((Map) hit.getSourceAsMap().get("comment")).get("likes");
            String data = ((Map) hit.getSourceAsMap().get("comment")).get("data").toString();
            assertThat(likes, greaterThan(likesToCompare));
            assertThat(data, anyOf(equalTo(dataToCompare), equalTo("[aa, bb]")));
        }
    }

    private SearchHits query(String request) throws IOException {
        final JSONObject jsonObject = executeRequest(request);

        final XContentParser parser = XContentFactory.xContent(XContentType.JSON).createParser(
                NamedXContentRegistry.EMPTY,
                LoggingDeprecationHandler.INSTANCE,
                jsonObject.toString());
        return SearchResponse.fromXContent(parser).getHits();
    }
}
