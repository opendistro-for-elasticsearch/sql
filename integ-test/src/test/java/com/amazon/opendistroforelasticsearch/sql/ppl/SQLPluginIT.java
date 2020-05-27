/*
 *    Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License").
 *    You may not use this file except in compliance with the License.
 *    A copy of the License is located at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    or in the "license" file accompanying this file. This file is distributed
 *    on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *    express or implied. See the License for the specific language governing
 *    permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.sql.ppl;

import com.amazon.opendistroforelasticsearch.sql.esintgtest.RestIntegTestCase;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestUtils.getResponseBody;

public class SQLPluginIT extends RestIntegTestCase {

    @Test
    public void testQueryWithNestedFunctionCall() throws IOException {
        Index index = new Index("test").addDoc("{\"firstname\": \"hello\", \"lastname\": \"John\"}").
                                        addDoc("{\"firstname\": \"world\", \"lastname\": \"Smith\"}").
                                        create();

        String results = index.query("SELECT 1 + 2 FROM %s");
    }

    static class Index {
        private final String indexName;
        private final List<Request> pendingRequests = new ArrayList<>();

        Index(String indexName) {
            this.indexName = indexName;
        }

        public Index addDoc(String doc) {
            Request request = new Request("PUT", getRequestPath());
            request.setJsonEntity(doc);
            pendingRequests.add(request);
            return this;
        }

        public Index create() throws IOException {
            for (Request pendingRequest : pendingRequests) {
                client().performRequest(pendingRequest);
            }
            return this;
        }

        public String query(String sql) throws IOException {
            Response response = client().performRequest(buildRequest(String.format(sql, indexName)));
            Assert.assertEquals(200, response.getStatusLine().getStatusCode());
            return getResponseBody(response, true);
        }

        private String getRequestPath() {
            return String.format("/%s/_doc/%d?refresh=true", indexName, pendingRequests.size());
        }

        private Request buildRequest(String query) {
            Request request = new Request("POST", "_opendistro/_sql");
            request.setJsonEntity(String.format(Locale.ROOT,
                "{\n" +
                    "  \"query\": \"%s\"\n" +
                    "}", query));

            RequestOptions.Builder restOptionsBuilder = RequestOptions.DEFAULT.toBuilder();
            restOptionsBuilder.addHeader("Content-Type", "application/json");
            request.setOptions(restOptionsBuilder);
            return request;
        }
    }

}
