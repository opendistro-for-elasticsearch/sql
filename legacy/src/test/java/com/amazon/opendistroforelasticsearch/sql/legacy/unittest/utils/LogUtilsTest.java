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

package com.amazon.opendistroforelasticsearch.sql.legacy.unittest.utils;

import com.amazon.opendistroforelasticsearch.sql.legacy.utils.LogUtils;
import org.apache.logging.log4j.ThreadContext;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

public class LogUtilsTest {

    private static final String REQUEST_ID_KEY = "request_id";

    @After
    public void cleanUpContext() {

        ThreadContext.clearMap();
    }

    @Test
    public void addRequestId() {

        Assert.assertNull(ThreadContext.get(REQUEST_ID_KEY));
        LogUtils.addRequestId();
        final String requestId = ThreadContext.get(REQUEST_ID_KEY);
        Assert.assertNotNull(requestId);
    }

    @Test
    public void addRequestId_alreadyExists() {

        LogUtils.addRequestId();
        final String requestId = ThreadContext.get(REQUEST_ID_KEY);
        LogUtils.addRequestId();
        final String requestId2 = ThreadContext.get(REQUEST_ID_KEY);
        Assert.assertThat(requestId2, not(equalTo(requestId)));
    }

    @Test(expected = IllegalStateException.class)
    public void getRequestId_doesNotExist() {

        LogUtils.getRequestId();
    }

    @Test
    public void getRequestId() {

        final String test_request_id = "test_id_111";
        ThreadContext.put(REQUEST_ID_KEY, test_request_id);
        final String requestId = LogUtils.getRequestId();
        Assert.assertThat(requestId, equalTo(test_request_id));
    }

    @Test
    public void withCurrentContext() throws InterruptedException {

        Runnable task = () -> {
            Assert.assertTrue(ThreadContext.containsKey("test11"));
            Assert.assertTrue(ThreadContext.containsKey("test22"));
        };
        ThreadContext.put("test11", "value11");
        ThreadContext.put("test22", "value11");
        new Thread(LogUtils.withCurrentContext(task)).join();
    }
}
