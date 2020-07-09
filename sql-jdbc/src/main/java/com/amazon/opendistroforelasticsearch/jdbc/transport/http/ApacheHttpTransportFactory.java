/*
 * Copyright <2019> Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.jdbc.transport.http;

import com.amazon.opendistroforelasticsearch.jdbc.config.ConnectionConfig;
import com.amazon.opendistroforelasticsearch.jdbc.logging.Logger;
import com.amazon.opendistroforelasticsearch.jdbc.transport.TransportException;
import com.amazon.opendistroforelasticsearch.jdbc.transport.TransportFactory;

public class ApacheHttpTransportFactory implements TransportFactory<ApacheHttpTransport> {

    public static ApacheHttpTransportFactory INSTANCE = new ApacheHttpTransportFactory();

    private ApacheHttpTransportFactory() {

    }

    @Override
    public ApacheHttpTransport getTransport(ConnectionConfig config, Logger log, String userAgent) throws TransportException {
        return new ApacheHttpTransport(config, log, userAgent);
    }
}
