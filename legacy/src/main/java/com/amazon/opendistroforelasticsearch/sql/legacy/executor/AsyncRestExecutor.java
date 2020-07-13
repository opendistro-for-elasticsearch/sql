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

package com.amazon.opendistroforelasticsearch.sql.legacy.executor;

import com.amazon.opendistroforelasticsearch.sql.legacy.esdomain.LocalClusterState;
import com.amazon.opendistroforelasticsearch.sql.legacy.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.legacy.metrics.MetricName;
import com.amazon.opendistroforelasticsearch.sql.legacy.metrics.Metrics;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.QueryAction;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.join.BackOffRetryStrategy;
import com.amazon.opendistroforelasticsearch.sql.legacy.utils.LogUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.Transports;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.function.Predicate;

import static com.amazon.opendistroforelasticsearch.sql.legacy.plugin.SqlSettings.QUERY_SLOWLOG;

/**
 * A RestExecutor wrapper to execute request asynchronously to avoid blocking transport thread.
 */
public class AsyncRestExecutor implements RestExecutor {

    /**
     * Custom thread pool name managed by ES
     */
    public static final String SQL_WORKER_THREAD_POOL_NAME = "sql-worker";

    private static final Logger LOG = LogManager.getLogger(AsyncRestExecutor.class);

    /**
     * Treat all actions as blocking which means async all actions,
     * ex. execute() in csv executor or pretty format executor
     */
    private static final Predicate<QueryAction> ALL_ACTION_IS_BLOCKING = anyAction -> true;

    /**
     * Delegated rest executor to async
     */
    private final RestExecutor executor;

    /**
     * Request type that expect to async to avoid blocking
     */
    private final Predicate<QueryAction> isBlocking;


    AsyncRestExecutor(RestExecutor executor) {
        this(executor, ALL_ACTION_IS_BLOCKING);
    }

    AsyncRestExecutor(RestExecutor executor, Predicate<QueryAction> isBlocking) {
        this.executor = executor;
        this.isBlocking = isBlocking;
    }

    @Override
    public void execute(Client client, Map<String, String> params, QueryAction queryAction, RestChannel channel)
            throws Exception {
        if (isBlockingAction(queryAction) && isRunningInTransportThread()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("[{}] Async blocking query action [{}] for executor [{}] in current thread [{}]",
                        LogUtils.getRequestId(), name(executor), name(queryAction), Thread.currentThread().getName());
            }
            async(client, params, queryAction, channel);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("[{}] Continue running query action [{}] for executor [{}] in current thread [{}]",
                        LogUtils.getRequestId(), name(executor), name(queryAction), Thread.currentThread().getName());
            }
            doExecuteWithTimeMeasured(client, params, queryAction, channel);
        }
    }

    @Override
    public String execute(Client client, Map<String, String> params, QueryAction queryAction) throws Exception {
        // Result is always required and no easy way to async it here.
        return executor.execute(client, params, queryAction);
    }

    private boolean isBlockingAction(QueryAction queryAction) {
        return isBlocking.test(queryAction);
    }

    private boolean isRunningInTransportThread() {
        return Transports.isTransportThread(Thread.currentThread());
    }

    /**
     * Run given task in thread pool asynchronously
     */
    private void async(Client client, Map<String, String> params, QueryAction queryAction, RestChannel channel) {

        ThreadPool threadPool = client.threadPool();
        Runnable runnable = () -> {
            try {
                doExecuteWithTimeMeasured(client, params, queryAction, channel);
            } catch (IOException | SqlParseException | ElasticsearchException e) {
                Metrics.getInstance().getNumericalMetric(MetricName.FAILED_REQ_COUNT_SYS).increment();
                LOG.warn("[{}] [MCB] async task got an IO/SQL exception: {}", LogUtils.getRequestId(),
                        e.getMessage());
                channel.sendResponse(new BytesRestResponse(RestStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
            } catch (IllegalStateException e) {
                Metrics.getInstance().getNumericalMetric(MetricName.FAILED_REQ_COUNT_SYS).increment();
                LOG.warn("[{}] [MCB] async task got a runtime exception: {}", LogUtils.getRequestId(),
                        e.getMessage());
                channel.sendResponse(new BytesRestResponse(RestStatus.INSUFFICIENT_STORAGE,
                        "Memory circuit is broken."));
            } catch (Throwable t) {
                Metrics.getInstance().getNumericalMetric(MetricName.FAILED_REQ_COUNT_SYS).increment();
                LOG.warn("[{}] [MCB] async task got an unknown throwable: {}", LogUtils.getRequestId(),
                        t.getMessage());
                channel.sendResponse(new BytesRestResponse(RestStatus.INTERNAL_SERVER_ERROR,
                        String.valueOf(t.getMessage())));
            } finally {
                BackOffRetryStrategy.releaseMem(executor);
            }
        };

        // Preserve context of calling thread to ensure headers of requests are forwarded when running blocking actions
        threadPool.schedule(
                LogUtils.withCurrentContext(runnable),
                new TimeValue(0L),
                SQL_WORKER_THREAD_POOL_NAME
        );
    }

    /**
     * Time the real execution of Executor and log slow query for troubleshooting
     */
    private void doExecuteWithTimeMeasured(Client client,
                                           Map<String, String> params,
                                           QueryAction action,
                                           RestChannel channel) throws Exception {
        long startTime = System.nanoTime();
        try {
            executor.execute(client, params, action, channel);
        } finally {
            Duration elapsed = Duration.ofNanos(System.nanoTime() - startTime);
            int slowLogThreshold = LocalClusterState.state().getSettingValue(QUERY_SLOWLOG);
            if (elapsed.getSeconds() >= slowLogThreshold) {
                LOG.warn("[{}] Slow query: elapsed={} (ms)", LogUtils.getRequestId(), elapsed.toMillis());
            }
        }
    }

    private String name(Object object) {
        return object.getClass().getSimpleName();
    }
}
