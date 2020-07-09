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

/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package com.amazon.opendistroforelasticsearch.jdbc.transport.http;

import org.apache.commons.logging.Log;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.config.MessageConstraints;
import org.apache.http.entity.ContentLengthStrategy;
import org.apache.http.impl.conn.DefaultManagedHttpClientConnection;
import org.apache.http.impl.conn.Wire;
import org.apache.http.io.HttpMessageParserFactory;
import org.apache.http.io.HttpMessageWriterFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

/**
 * Adapted from Apache HttpClient to offer per connection HTTP wire
 * logging.
 */
public class LoggingManagedHttpClientConnection extends DefaultManagedHttpClientConnection {

        private final Log log;
        private final Wire wire;

        public LoggingManagedHttpClientConnection(
                final String id,
                final Log log,
                final int buffersize,
                final int fragmentSizeHint,
                final CharsetDecoder chardecoder,
                final CharsetEncoder charencoder,
                final MessageConstraints constraints,
                final ContentLengthStrategy incomingContentStrategy,
                final ContentLengthStrategy outgoingContentStrategy,
                final HttpMessageWriterFactory<HttpRequest> requestWriterFactory,
                final HttpMessageParserFactory<HttpResponse> responseParserFactory) {
            super(id, buffersize, fragmentSizeHint, chardecoder, charencoder,
                    constraints, incomingContentStrategy, outgoingContentStrategy,
                    requestWriterFactory, responseParserFactory);
            this.log = log;
            this.wire = new Wire(log, id);
        }

        @Override
        public void close() throws IOException {

            if (super.isOpen()) {
                if (this.log.isDebugEnabled()) {
                    this.log.debug(getId() + ": Close connection");
                }
                super.close();
            }
        }

        @Override
        public void setSocketTimeout(final int timeout) {
            if (this.log.isDebugEnabled()) {
                this.log.debug(getId() + ": set socket timeout to " + timeout);
            }
            super.setSocketTimeout(timeout);
        }

        @Override
        public void shutdown() throws IOException {
            if (this.log.isDebugEnabled()) {
                this.log.debug(getId() + ": Shutdown connection");
            }
            super.shutdown();
        }

        @Override
        protected InputStream getSocketInputStream(final Socket socket) throws IOException {
            InputStream in = super.getSocketInputStream(socket);
            if (this.wire.enabled()) {
                in = new LoggingInputStream(in, this.wire);
            }
            return in;
        }

        @Override
        protected OutputStream getSocketOutputStream(final Socket socket) throws IOException {
            OutputStream out = super.getSocketOutputStream(socket);
            if (this.wire.enabled()) {
                out = new LoggingOutputStream(out, this.wire);
            }
            return out;
        }

    }
