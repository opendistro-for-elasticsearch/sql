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

package com.amazon.opendistro.sql.executor;

import com.amazon.opendistro.sql.query.QueryAction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.transport.Transports;

import java.util.Map;
import java.util.function.Predicate;

import static org.elasticsearch.rest.RestStatus.INTERNAL_SERVER_ERROR;

/**
 * A RestExecutor wrapper to execute request asynchronously to avoid blocking transport thread.
 */
public class AsyncRestExecutor implements RestExecutor {

    /** Custom thread pool name managed by ES */
    public static final String SQL_WORKER_THREAD_POOL_NAME = "sql-worker";

    private static final Logger LOG = LogManager.getLogger();

    /** Treat all actions as blocking which means async all actions, ex. execute() in csv executor or pretty format executor */
    private static final Predicate<QueryAction> ALL_ACTION_IS_BLOCKING = anyAction -> true;

    /** Delegated rest executor to async */
    private final RestExecutor executor;

    /** Request type that expect to async to avoid blocking */
    private final Predicate<QueryAction> isBlocking;


    AsyncRestExecutor(RestExecutor executor) {
        this(executor, ALL_ACTION_IS_BLOCKING);
    }

    AsyncRestExecutor(RestExecutor executor, Predicate<QueryAction> isBlocking) {
        this.executor = executor;
        this.isBlocking = isBlocking;
    }

    @Override
    public void execute(Client client, Map<String, String> params, QueryAction queryAction, RestChannel channel) throws Exception {
        if (isBlockingAction(queryAction) && isRunningInTransportThread()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Async blocking query action [{}] for executor [{}] in current thread [{}]",
                    name(executor), name(queryAction), Thread.currentThread().getName());
            }
            async(client, params, queryAction, channel);
        }
        else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Continue running query action [{}] for executor [{}] in current thread [{}]",
                    name(executor), name(queryAction), Thread.currentThread().getName());
            }
            executor.execute(client, params, queryAction, channel);
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

    private void async(Client client, Map<String, String> params, QueryAction queryAction, RestChannel channel) {
        // Run given task in thread pool asynchronously
        client.threadPool().schedule(
            new TimeValue(0L),
            SQL_WORKER_THREAD_POOL_NAME,
            () -> {
                try {
                    executor.execute(client, params, queryAction, channel);
                }
                catch (Exception e) {
                    LOG.error("Failed to execute async task", e);
                    channel.sendResponse(new BytesRestResponse(INTERNAL_SERVER_ERROR, e.getMessage()));
                }
            });
    }

    private String name(Object object) {
        return object.getClass().getSimpleName();
    }

}
