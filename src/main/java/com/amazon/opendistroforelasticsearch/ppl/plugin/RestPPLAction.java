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
import com.amazon.opendistroforelasticsearch.sql.plugin.RestSqlAction;
import com.amazon.opendistroforelasticsearch.sql.query.planner.physical.PhysicalOperator;
import com.amazon.opendistroforelasticsearch.sql.utils.LogUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        PPLRequest pplRequest = PPLRequestFactory.getPPLRequest(request);

        LOG.info("[{}] Incoming request {}: {}", LogUtils.getRequestId(), request.uri(), pplRequest.getPpl());

        return channel -> {
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
            channel.sendResponse(pretty(execute(physicalOperator)));
        };
    }

    public BytesRestResponse pretty(List<BindingTuple> bindingTuples) {
        return null;
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
