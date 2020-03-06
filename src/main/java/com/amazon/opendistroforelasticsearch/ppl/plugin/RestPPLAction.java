/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.ppl.plugin;

import com.amazon.opendistroforelasticsearch.ppl.analysis.Analyzer;
import com.amazon.opendistroforelasticsearch.ppl.antlr.PPLSyntaxParser;
import com.amazon.opendistroforelasticsearch.ppl.parser.AstBuilder;
import com.amazon.opendistroforelasticsearch.ppl.parser.AstExpressionBuilder;
import com.amazon.opendistroforelasticsearch.ppl.planner.Planner;
import com.amazon.opendistroforelasticsearch.ppl.request.PPLRequest;
import com.amazon.opendistroforelasticsearch.ppl.request.PPLRequestFactory;
import com.amazon.opendistroforelasticsearch.ppl.spec.scope.Context;
import com.amazon.opendistroforelasticsearch.sql.esdomain.LocalClusterState;
import com.amazon.opendistroforelasticsearch.sql.expression.domain.BindingTuple;
import com.amazon.opendistroforelasticsearch.sql.expression.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.plugin.RestSqlAction;
import com.amazon.opendistroforelasticsearch.sql.query.planner.physical.PhysicalOperator;
import com.amazon.opendistroforelasticsearch.sql.utils.LogUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.threadpool.ThreadPool;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.elasticsearch.rest.RestStatus.OK;

public class RestPPLAction extends BaseRestHandler {
    private static final Logger LOG = LogManager.getLogger(RestSqlAction.class);

    /**
     * API endpoint path
     */
    public static final String QUERY_API_ENDPOINT = "/_opendistro/_ppl";
    public static final String EXPLAIN_API_ENDPOINT = QUERY_API_ENDPOINT + "/_explain";

    public RestPPLAction(Settings settings, RestController restController) {
        super();
        LOG.info("REGISTER PPL");
        restController.registerHandler(RestRequest.Method.POST, QUERY_API_ENDPOINT, this);
        restController.registerHandler(RestRequest.Method.GET, QUERY_API_ENDPOINT, this);
        restController.registerHandler(RestRequest.Method.POST, EXPLAIN_API_ENDPOINT, this);
        restController.registerHandler(RestRequest.Method.GET, EXPLAIN_API_ENDPOINT, this);
    }

    @Override
    public String getName() {
        return "ppl_action";
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) {
        LogUtils.addRequestId();

        PPLRequest pplRequest = PPLRequestFactory.getPPLRequest(request);
        LOG.info("[{}] Incoming request {}: {}", LogUtils.getRequestId(), request.uri(), pplRequest.getPpl());

        PPLSyntaxParser pplSyntaxParser = new PPLSyntaxParser();
        AstBuilder astBuilder = new AstBuilder(new AstExpressionBuilder());
        Analyzer analyzer = new Analyzer(new Context<>(), LocalClusterState.state(), 1000);
        Planner planner = new Planner(client);

        PhysicalOperator<BindingTuple> physicalOperator = Optional.of(pplRequest.getPpl())
                .map(pplSyntaxParser::analyzeSyntax)
                .map(astBuilder::visit)
                .map(analyzer::analyze)
                .map(planner::plan)
                .get();

        return channel -> execute(request, client, channel, physicalOperator);
    }

    public void execute(RestRequest request,
                        NodeClient client,
                        RestChannel channel,
                        PhysicalOperator<BindingTuple> op) {
        Map<String, String> additionalParams = new HashMap<>();
        for (String paramName : responseParams()) {
            if (request.hasParam(paramName)) {
                additionalParams.put(paramName, request.param(paramName));
            }
        }
        new AsyncRestExecutor().execute(client, channel, op);
    }

    public static final String SQL_WORKER_THREAD_POOL_NAME = "sql-worker";

    private static class AsyncRestExecutor {
        public void execute(NodeClient client,
                            RestChannel channel,
                            PhysicalOperator<BindingTuple> op) {
            ThreadPool threadPool = client.threadPool();
            Runnable runnable = () -> channel.sendResponse(pretty(execute(op)));

            threadPool.schedule(
                    threadPool.preserveContext(LogUtils.withCurrentContext(runnable)),
                    new TimeValue(0L),
                    SQL_WORKER_THREAD_POOL_NAME
            );
        }

        public BytesRestResponse pretty(List<BindingTuple> bindingTuples) {
            List<Map<String, Object>> rowList = bindingTuples.stream().map(tuple -> {
                Map<String, ExprValue> bindingMap = tuple.getBindingMap();
                Map<String, Object> rowMap = new HashMap<>();
                for (String s : bindingMap.keySet()) {
                    rowMap.put(s, bindingMap.get(s).value());
                }
                return rowMap;
            }).collect(Collectors.toList());
            return new BytesRestResponse(OK, "application/json; charset=UTF-8", new JSONArray(rowList).toString());
        }

        public List<BindingTuple> execute(PhysicalOperator<BindingTuple> op) {
            List<BindingTuple> tuples = new ArrayList<>();
            try {
                op.open(null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            while (op.hasNext()) {
                tuples.add(op.next().data());
            }
            return tuples;
        }
    }
}
