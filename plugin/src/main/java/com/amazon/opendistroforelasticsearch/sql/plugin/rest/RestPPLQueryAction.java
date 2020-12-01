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

import static com.amazon.opendistroforelasticsearch.sql.protocol.response.format.JsonResponseFormatter.Style.PRETTY;
import static org.elasticsearch.rest.RestStatus.BAD_REQUEST;
import static org.elasticsearch.rest.RestStatus.INTERNAL_SERVER_ERROR;
import static org.elasticsearch.rest.RestStatus.OK;
import static org.elasticsearch.rest.RestStatus.SERVICE_UNAVAILABLE;

import com.amazon.opendistroforelasticsearch.sql.common.antlr.SyntaxCheckException;
import com.amazon.opendistroforelasticsearch.sql.common.response.ResponseListener;
import com.amazon.opendistroforelasticsearch.sql.common.setting.Settings;
import com.amazon.opendistroforelasticsearch.sql.common.utils.LogUtils;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.response.error.ErrorMessageFactory;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.security.SecurityAccess;
import com.amazon.opendistroforelasticsearch.sql.exception.ExpressionEvaluationException;
import com.amazon.opendistroforelasticsearch.sql.exception.QueryEngineException;
import com.amazon.opendistroforelasticsearch.sql.exception.SemanticCheckException;
import com.amazon.opendistroforelasticsearch.sql.executor.ExecutionEngine.ExplainResponse;
import com.amazon.opendistroforelasticsearch.sql.executor.ExecutionEngine.QueryResponse;
import com.amazon.opendistroforelasticsearch.sql.legacy.metrics.MetricName;
import com.amazon.opendistroforelasticsearch.sql.legacy.metrics.Metrics;
import com.amazon.opendistroforelasticsearch.sql.plugin.request.PPLQueryRequestFactory;
import com.amazon.opendistroforelasticsearch.sql.ppl.PPLService;
import com.amazon.opendistroforelasticsearch.sql.ppl.config.PPLServiceConfig;
import com.amazon.opendistroforelasticsearch.sql.ppl.domain.PPLQueryRequest;
import com.amazon.opendistroforelasticsearch.sql.protocol.response.QueryResult;
import com.amazon.opendistroforelasticsearch.sql.protocol.response.format.JsonResponseFormatter;
import com.amazon.opendistroforelasticsearch.sql.protocol.response.format.SimpleJsonResponseFormatter;
import java.io.IOException;
import java.security.PrivilegedExceptionAction;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class RestPPLQueryAction extends BaseRestHandler {
  public static final String QUERY_API_ENDPOINT = "/_opendistro/_ppl";
  public static final String EXPLAIN_API_ENDPOINT = "/_opendistro/_ppl/_explain";

  private static final Logger LOG = LogManager.getLogger();

  /**
   * Cluster service required by bean initialization.
   */
  private final ClusterService clusterService;

  /**
   * Settings required by been initialization.
   */
  private final Settings pluginSettings;

  private final Supplier<Boolean> pplEnabled;

  /**
   * Constructor of RestPPLQueryAction.
   */
  public RestPPLQueryAction(RestController restController, ClusterService clusterService,
                            Settings pluginSettings,
                            org.elasticsearch.common.settings.Settings clusterSettings) {
    super();
    this.clusterService = clusterService;
    this.pluginSettings = pluginSettings;
    this.pplEnabled =
        () -> MULTI_ALLOW_EXPLICIT_INDEX.get(clusterSettings)
            && (Boolean) pluginSettings.getSettingValue(Settings.Key.PPL_ENABLED);
  }

  @Override
  public List<Route> routes() {
    return Arrays.asList(
        new Route(RestRequest.Method.POST, QUERY_API_ENDPOINT),
        new Route(RestRequest.Method.POST, EXPLAIN_API_ENDPOINT)
    );
  }

  @Override
  public String getName() {
    return "ppl_query_action";
  }

  @Override
  protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient nodeClient) {
    Metrics.getInstance().getNumericalMetric(MetricName.PPL_REQ_TOTAL).increment();
    Metrics.getInstance().getNumericalMetric(MetricName.PPL_REQ_COUNT_TOTAL).increment();

    LogUtils.addRequestId();

    if (!pplEnabled.get()) {
      return channel -> reportError(channel, new IllegalAccessException(
          "Either opendistro.ppl.enabled or rest.action.multi.allow_explicit_index setting is false"
      ), BAD_REQUEST);
    }

    PPLService pplService = createPPLService(nodeClient);
    PPLQueryRequest pplRequest = PPLQueryRequestFactory.getPPLRequest(request);
    if (pplRequest.isExplainRequest()) {
      return channel -> pplService.explain(pplRequest, createExplainResponseListener(channel));
    }
    return channel -> pplService.execute(pplRequest, createListener(channel));
  }

  /**
   * Ideally, the AnnotationConfigApplicationContext should be shared across Plugin. By default,
   * spring construct all the bean as singleton. Currently, there are no better solution to
   * create the bean in protocol scope. The limitations are
   * alt-1, add annotation for bean @Scope(value = SCOPE_PROTOTYPE, proxyMode = TARGET_CLASS), it
   * works by add the proxy,
   * but when running in Elasticsearch, all the operation need security permission whic is hard
   * to control.
   * alt-2, using ObjectFactory with @Autowired, it also works, but require add to all the
   * configuration.
   * We will revisit the current solution if any major issue found.
   */
  private PPLService createPPLService(NodeClient client) {
    return doPrivileged(() -> {
      AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
      context.registerBean(ClusterService.class, () -> clusterService);
      context.registerBean(NodeClient.class, () -> client);
      context.registerBean(Settings.class, () -> pluginSettings);
      context.register(ElasticsearchPluginConfig.class);
      context.register(PPLServiceConfig.class);
      context.refresh();
      return context.getBean(PPLService.class);
    });
  }

  /**
   * TODO: need to extract an interface for both SQL and PPL action handler and move these
   * common methods to the interface. This is not easy to do now because SQL action handler
   * is still in legacy module.
   */
  private ResponseListener<ExplainResponse> createExplainResponseListener(
      RestChannel channel) {
    return new ResponseListener<ExplainResponse>() {
      @Override
      public void onResponse(ExplainResponse response) {
        sendResponse(channel, OK, new JsonResponseFormatter<ExplainResponse>(PRETTY) {
          @Override
          protected Object buildJsonObject(ExplainResponse response) {
            return response;
          }
        }.format(response));
      }

      @Override
      public void onFailure(Exception e) {
        LOG.error("Error happened during explain", e);
        sendResponse(channel, INTERNAL_SERVER_ERROR,
            "Failed to explain the query due to error: " + e.getMessage());
      }
    };
  }

  private ResponseListener<QueryResponse> createListener(RestChannel channel) {
    SimpleJsonResponseFormatter formatter =
        new SimpleJsonResponseFormatter(PRETTY); // TODO: decide format and pretty from URL param
    return new ResponseListener<QueryResponse>() {
      @Override
      public void onResponse(QueryResponse response) {
        sendResponse(channel, OK, formatter.format(new QueryResult(response.getSchema(),
            response.getResults())));
      }

      @Override
      public void onFailure(Exception e) {
        LOG.error("Error happened during query handling", e);
        if (isClientError(e)) {
          Metrics.getInstance().getNumericalMetric(MetricName.PPL_FAILED_REQ_COUNT_CUS).increment();
          reportError(channel, e, BAD_REQUEST);
        } else {
          Metrics.getInstance().getNumericalMetric(MetricName.PPL_FAILED_REQ_COUNT_SYS).increment();
          reportError(channel, e, SERVICE_UNAVAILABLE);
        }
      }
    };
  }

  private <T> T doPrivileged(PrivilegedExceptionAction<T> action) {
    try {
      return SecurityAccess.doPrivileged(action);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to perform privileged action", e);
    }
  }

  private void sendResponse(RestChannel channel, RestStatus status, String content) {
    channel.sendResponse(
        new BytesRestResponse(status, "application/json; charset=UTF-8", content));
  }

  private void reportError(final RestChannel channel, final Exception e, final RestStatus status) {
    channel.sendResponse(new BytesRestResponse(status,
        ErrorMessageFactory.createErrorMessage(e, status.getStatus()).toString()));
  }

  private static boolean isClientError(Exception e) {
    return e instanceof NullPointerException
        // NPE is hard to differentiate but more likely caused by bad query
        || e instanceof IllegalArgumentException
        || e instanceof IndexNotFoundException
        || e instanceof SemanticCheckException
        || e instanceof ExpressionEvaluationException
        || e instanceof QueryEngineException
        || e instanceof SyntaxCheckException;
  }
}
