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

package com.amazon.opendistroforelasticsearch.sql.legacy.unittest;

import com.amazon.opendistroforelasticsearch.sql.legacy.executor.format.ElasticsearchErrorMessage;
import com.amazon.opendistroforelasticsearch.sql.legacy.executor.format.ErrorMessage;
import com.amazon.opendistroforelasticsearch.sql.legacy.executor.format.ErrorMessageFactory;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.rest.RestStatus;
import org.junit.Assert;
import org.junit.Test;

public class ErrorMessageFactoryTest {

    private Throwable nonEsThrowable = new Throwable();
    private Throwable esThrowable = new ElasticsearchException(nonEsThrowable);

    @Test
    public void esExceptionShouldCreateEsErrorMessage() {
        Exception exception = new ElasticsearchException(nonEsThrowable);
        ErrorMessage msg = ErrorMessageFactory.createErrorMessage(exception, RestStatus.BAD_REQUEST.getStatus());
        Assert.assertTrue(msg instanceof ElasticsearchErrorMessage);
    }

    @Test
    public void nonEsExceptionShouldCreateGenericErrorMessage() {
        Exception exception = new Exception(nonEsThrowable);
        ErrorMessage msg = ErrorMessageFactory.createErrorMessage(exception, RestStatus.BAD_REQUEST.getStatus());
        Assert.assertFalse(msg instanceof ElasticsearchErrorMessage);
    }

    @Test
    public void nonEsExceptionWithWrappedEsExceptionCauseShouldCreateEsErrorMessage() {
        Exception exception = (Exception) esThrowable;
        ErrorMessage msg = ErrorMessageFactory.createErrorMessage(exception, RestStatus.BAD_REQUEST.getStatus());
        Assert.assertTrue(msg instanceof ElasticsearchErrorMessage);
    }

    @Test
    public void nonEsExceptionWithMultiLayerWrappedEsExceptionCauseShouldCreateEsErrorMessage() {
        Exception exception = new Exception(new Throwable(new Throwable(esThrowable)));
        ErrorMessage msg = ErrorMessageFactory.createErrorMessage(exception, RestStatus.BAD_REQUEST.getStatus());
        Assert.assertTrue(msg instanceof ElasticsearchErrorMessage);
    }

}
