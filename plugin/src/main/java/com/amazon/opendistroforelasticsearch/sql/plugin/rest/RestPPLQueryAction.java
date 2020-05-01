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

package com.amazon.opendistroforelasticsearch.sql.plugin.rest;

import com.amazon.opendistroforelasticsearch.sql.DatabaseEngine;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.executor.ElasticsearchExecutionEngine;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.security.SecurityAccess;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.ElasticsearchStorageEngine;
import com.amazon.opendistroforelasticsearch.sql.executor.ExecutionEngine;
import com.amazon.opendistroforelasticsearch.sql.plugin.request.PPLQueryRequestFactory;
import com.amazon.opendistroforelasticsearch.sql.ppl.PPLService;
import com.amazon.opendistroforelasticsearch.sql.executor.ResponseListener;
import com.amazon.opendistroforelasticsearch.sql.ppl.antlr.PPLSyntaxParser;
import com.amazon.opendistroforelasticsearch.sql.ppl.config.PPLServiceConfig;
import com.amazon.opendistroforelasticsearch.sql.ppl.domain.PPLQueryResponse;
import com.amazon.opendistroforelasticsearch.sql.query.QueryEngine;
import com.amazon.opendistroforelasticsearch.sql.storage.BindingTuple;
import com.amazon.opendistroforelasticsearch.sql.storage.HybridStorageEngine;
import com.amazon.opendistroforelasticsearch.sql.storage.StorageEngine;
import com.amazon.opendistroforelasticsearch.sql.storage.file.FileStorageEngine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.elasticsearch.rest.RestStatus.INTERNAL_SERVER_ERROR;
import static org.elasticsearch.rest.RestStatus.OK;

public class RestPPLQueryAction extends BaseRestHandler {
    public static final String QUERY_API_ENDPOINT = "/_opendistro/_ppl";

    private static final Logger LOG = LogManager.getLogger(RestPPLQueryAction.class);

    public RestPPLQueryAction(RestController restController) {
        super();
        restController.registerHandler(RestRequest.Method.POST, QUERY_API_ENDPOINT, this);
    }

    @Override
    public String getName() {
        return "ppl_query_action";
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) throws IOException {
        AnnotationConfigApplicationContext context = SecurityAccess.doPrivileged(
                () -> new AnnotationConfigApplicationContext(PPLServiceConfig.class));
        //PPLService pplService = context.getBean(PPLService.class);

        StorageEngine fileStorageEngine = new FileStorageEngine(getDatabaseFileContent());
        StorageEngine esStorageEngine = new ElasticsearchStorageEngine(client); // TODO: client is per-request. how to make PPLService long living?
        StorageEngine storageEngine = new HybridStorageEngine(fileStorageEngine, esStorageEngine);

        QueryEngine queryEngine = new QueryEngine(new PPLSyntaxParser(), storageEngine);
        ExecutionEngine executionEngine = new ElasticsearchExecutionEngine(client);

        DatabaseEngine databaseEngine = new DatabaseEngine(queryEngine, storageEngine, executionEngine);
        PPLService pplService = new PPLService(databaseEngine);

        return channel -> pplService.execute(PPLQueryRequestFactory.getPPLRequest(request),
                new ResponseListener<List<BindingTuple>>() {
                    @Override
                    public void onResponse(List<BindingTuple> result) {
                        channel.sendResponse(new BytesRestResponse(
                            OK, "application/json; charset=UTF-8",
                            result.stream().
                                   map(BindingTuple::toString).
                                   collect(Collectors.joining("\n\t", "[\n\t", "\n]\n"))));
                    }

                    @Override
                    public void onFailure(Exception e) {
                        LOG.error("Error", e);
                        channel.sendResponse(
                                new BytesRestResponse(INTERNAL_SERVER_ERROR, "application/json; charset=UTF-8",
                                        e.getMessage() == null ? "error" : e.getMessage()));
                    }
                });
    }

    // Because of ES sandbox, this is only for demo.
    private String getDatabaseFileContent() {
        return "{\n" +
            "  \"hr\": {\n" +
            "    \"employees\": [\n" +
            "      {\n" +
            "        \"id\": 3,\n" +
            "        \"name\": \"Bob Smith\",\n" +
            "        \"title\": null\n" +
            "      },\n" +
            "      {\n" +
            "        \"id\": 4,\n" +
            "        \"name\": \"Susan Smith\",\n" +
            "        \"title\": \"Dev Mgr\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"id\": 6,\n" +
            "        \"name\": \"Jane Smith\",\n" +
            "        \"title\": \"Software Eng 2\"\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "}";
    }
}
