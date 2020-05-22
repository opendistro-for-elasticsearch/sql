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

import com.amazon.opendistroforelasticsearch.sql.common.response.ResponseListener;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.client.ElasticsearchClient;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.client.ElasticsearchRestClient;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.executor.ElasticsearchExecutionEngine;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.ElasticsearchStorageEngine;
import com.amazon.opendistroforelasticsearch.sql.executor.ExecutionEngine;
import com.amazon.opendistroforelasticsearch.sql.executor.ExecutionEngine.QueryResponse;
import com.amazon.opendistroforelasticsearch.sql.ppl.antlr.PPLSyntaxParser;
import com.amazon.opendistroforelasticsearch.sql.ppl.domain.PPLQueryRequest;
import com.amazon.opendistroforelasticsearch.sql.protocol.response.QueryResult;
import com.amazon.opendistroforelasticsearch.sql.protocol.response.format.SimpleJsonResponseFormatter;
import com.amazon.opendistroforelasticsearch.sql.storage.StorageEngine;
import org.elasticsearch.client.Node;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import static com.amazon.opendistroforelasticsearch.sql.protocol.response.format.JsonResponseFormatter.Style.PRETTY;

/**
 * Run PPL with query engine outside Elasticsearch cluster.
 * The client application, ex. JDBC driver, needs to initialize all components itself required by ppl service.
 */
public class StandaloneIT extends PPLIntegTestCase {

    private RestHighLevelClient restClient;

    private PPLService pplService;

    @Override
    public void init() {
        restClient = new RestHighLevelClient(
            RestClient.builder(client().getNodes().toArray(new Node[0])));

        ElasticsearchClient client = new ElasticsearchRestClient(restClient);
        StorageEngine storageEngine = new ElasticsearchStorageEngine(client);
        ExecutionEngine executionEngine = new ElasticsearchExecutionEngine(client);
        pplService = new PPLService(new PPLSyntaxParser(), storageEngine, executionEngine);
    }

    @AfterEach
    public void tearDown() throws Exception {
        restClient.close();
        super.tearDown();
    }

    @Test
    public void testSourceFieldQuery() throws IOException {
        Request request1 = new Request("PUT", "/test/_doc/1?refresh=true");
        request1.setJsonEntity("{\"name\": \"hello\", \"age\": 20}");
        client().performRequest(request1);
        Request request2 = new Request("PUT", "/test/_doc/2?refresh=true");
        request2.setJsonEntity("{\"name\": \"world\", \"age\": 30}");
        client().performRequest(request2);

        AtomicReference<String> actual = new AtomicReference<>();
        pplService.execute(
            new PPLQueryRequest("source=test | fields name", null),
            new ResponseListener<QueryResponse>() {

            @Override
            public void onResponse(QueryResponse response) {
                QueryResult result = new QueryResult(response.getResults());
                String json = new SimpleJsonResponseFormatter(PRETTY).format(result);
                actual.set(json);
            }

            @Override
            public void onFailure(Exception e) {
                throw new IllegalStateException("Exception happened during execution", e);
            }
        });

        assertNotNull(actual.get());
        assertEquals(
            "{\n" +
            "  \"schema\": [{\n" +
            "    \"name\": \"name\",\n" +
            "    \"type\": \"string\"\n" +
            "  }],\n" +
            "  \"total\": 2,\n" +
            "  \"datarows\": [\n" +
            "    {\"row\": [\"hello\"]},\n" +
            "    {\"row\": [\"world\"]}\n" +
            "  ],\n" +
            "  \"size\": 2\n" +
            "}",
            actual.get()
        );
    }

}
