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
import com.amazon.opendistroforelasticsearch.sql.legacy.plugin.SqlSettings;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.QueryAction;
import com.amazon.opendistroforelasticsearch.sql.legacy.request.SqlRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.threadpool.ThreadPool;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Map;
import java.util.function.Predicate;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.elasticsearch.transport.TcpTransport.TRANSPORT_WORKER_THREAD_NAME_PREFIX;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test AsyncRestExecutor behavior.
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class AsyncRestExecutorTest {

    private static final boolean NON_BLOCKING = false;

    @Mock
    private RestExecutor executor;

    @Mock
    private Client client;

    private Map<String, String> params = emptyMap();

    @Mock
    private QueryAction action;

    @Mock
    private RestChannel channel;

    @Before
    public void setUp() {
        when(client.threadPool()).thenReturn(mock(ThreadPool.class));
        when(action.getSqlRequest()).thenReturn(SqlRequest.NULL);

        SqlSettings settings = spy(new SqlSettings());
        doReturn(emptyList()).when(settings).getSettings();
        LocalClusterState.state().setSqlSettings(settings);
    }

    @Test
    public void executeBlockingQuery() throws Exception {
        Thread.currentThread().setName(TRANSPORT_WORKER_THREAD_NAME_PREFIX);
        execute();
        verifyRunInWorkerThread();
    }

    @Test
    public void executeBlockingQueryButNotInTransport() throws Exception {
        execute();
        verifyRunInCurrentThread();
    }

    @Test
    public void executeNonBlockingQuery() throws Exception {
        execute(anyAction -> NON_BLOCKING);
        verifyRunInCurrentThread();
    }

    private void execute() throws Exception {
        AsyncRestExecutor asyncExecutor = new AsyncRestExecutor(executor);
        asyncExecutor.execute(client, params, action, channel);
    }

    private void execute(Predicate<QueryAction> isBlocking) throws Exception {
        AsyncRestExecutor asyncExecutor = new AsyncRestExecutor(executor, isBlocking);
        asyncExecutor.execute(client, params, action, channel);
    }

    private void verifyRunInCurrentThread() {
        verify(client, never()).threadPool();
    }

    private void verifyRunInWorkerThread() {
        verify(client, times(1)).threadPool();
    }

}
