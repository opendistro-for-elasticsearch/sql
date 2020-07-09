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

package com.amazon.opendistroforelasticsearch.jdbc.test;

import com.amazon.opendistroforelasticsearch.jdbc.internal.util.UrlParser;
import com.amazon.opendistroforelasticsearch.jdbc.test.mocks.MockES;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.NetworkTrafficServerConnector;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class TLSServer {

    public static final String TRUST_SERVER_JKS_RESOURCE = "mock/jks/truststore_with_server_cert.jks";
    public static final String TRUST_CLIENT_JKS_RESOURCE = "mock/jks/truststore_with_client_cert.jks";

    public static final String SERVER_KEY_JKS_RESOURCE = "mock/jks/keystore_with_server_key.jks";
    public static final String SERVER_KEY_JKS_RESOURCE_NON_LOCALHOST = "mock/jks/keystore_with_non_localhost_server_key.jks";
    public static final String CLIENT_KEY_JKS_RESOURCE = "mock/jks/keystore_with_client_key.jks";

    public static Server startSecureServer(
            String host,
            String keyStorePath,
            String keyStorePassword,
            String keyStoreType,
            Handler handler) throws Exception {

        return startSecureServer(
                host,
                keyStorePath,
                keyStorePassword,
                keyStoreType,
                null,
                null,
                null,
                false,
                handler
        );
    }

    public static Server startSecureServer(
            String host,
            String keyStorePath,
            String keyStorePassword,
            String keyStoreType,
            String trustStorePath,
            String trustStorePassword,
            String trustStoreType,
            boolean needClientAuth,
            Handler handler) throws Exception {
        Server jettyServer = new Server();
        jettyServer.setStopTimeout(0);

        ServerConnector httpsConnector = null;

        // setup ssl
        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setKeyStorePath(keyStorePath);
        sslContextFactory.setKeyStorePassword(keyStorePassword);
        sslContextFactory.setKeyStoreType(keyStoreType);

        if (trustStorePath != null) {
            sslContextFactory.setTrustStorePath(trustStorePath);
            sslContextFactory.setTrustStorePassword(trustStorePassword);
            sslContextFactory.setTrustStoreType(trustStoreType);
        }
        sslContextFactory.setNeedClientAuth(needClientAuth);

        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.addCustomizer(new SecureRequestCustomizer());

        httpsConnector = createServerConnector(
                jettyServer,
                host,
                0,
                new org.eclipse.jetty.server.SslConnectionFactory(
                        sslContextFactory,
                        "http/1.1"
                ),
                new org.eclipse.jetty.server.HttpConnectionFactory(httpConfig)
        );

        jettyServer.addConnector(httpsConnector);
        jettyServer.setHandler(handler);
        jettyServer.start();

        return jettyServer;
    }

    public static class MockESConnectionHandler extends AbstractHandler {
        @Override
        public void handle(
                String target,
                Request baseRequest,
                HttpServletRequest request,
                HttpServletResponse response) throws IOException, ServletException {
            response.setContentType("application/json");
            response.setStatus(200);
            baseRequest.setHandled(true);
            response.getWriter().write(MockES.INSTANCE.getConnectionResponse());
        }
    }

    private static ServerConnector createServerConnector(
            Server jettyServer,
            String bindAddress,
            int port,
            ConnectionFactory... connectionFactories) {
        NetworkTrafficServerConnector connector = new NetworkTrafficServerConnector(
                jettyServer,
                null,
                null,
                null,
                2,
                2,
                connectionFactories
        );
        connector.setPort(port);
        connector.setStopTimeout(0);
        connector.getSelectorManager().setStopTimeout(0);
        connector.setHost(bindAddress);

        return connector;
    }

    public static String getBaseURLForConnect(Server jettyServer) {
        int port = -1;
        String host = null;

        for (Connector c : jettyServer.getConnectors()) {
            if (c instanceof ServerConnector) {
                port = ((ServerConnector) c).getLocalPort();
                host = ((ServerConnector) c).getHost();
            }
        }

        return UrlParser.URL_PREFIX + "https://" + host + ":" + port;
    }
}
