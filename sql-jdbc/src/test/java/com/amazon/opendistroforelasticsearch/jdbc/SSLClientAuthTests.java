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

package com.amazon.opendistroforelasticsearch.jdbc;

import com.amazon.opendistroforelasticsearch.jdbc.config.KeyStoreLocationConnectionProperty;
import com.amazon.opendistroforelasticsearch.jdbc.config.KeyStorePasswordConnectionProperty;
import com.amazon.opendistroforelasticsearch.jdbc.config.TrustStoreLocationConnectionProperty;
import com.amazon.opendistroforelasticsearch.jdbc.config.TrustStorePasswordConnectionProperty;
import com.amazon.opendistroforelasticsearch.jdbc.test.TLSServer;
import com.amazon.opendistroforelasticsearch.jdbc.test.TestResources;
import com.amazon.opendistroforelasticsearch.jdbc.test.mocks.MockES;
import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.Properties;

@ExtendWith(TempDirectory.class)
public class SSLClientAuthTests {

    // WireMockServer has a problem initializing server on TLS from
    // a password protected JKS keystore. If the issue gets fixed,
    // these tests can use WireMockServer instead.

    static Server jettyServer;
    static String connectURL;

    @BeforeAll
    static void beforeAll(@TempDirectory.TempDir Path tempDir) throws Exception {

        // Start server with SSL enabled and requiring client cert auth
        Path keyStoreFile = tempDir.resolve("server_keystore");
        Path trustStoreFile = tempDir.resolve("server_truststore");
        TestResources.copyResourceToPath(TLSServer.SERVER_KEY_JKS_RESOURCE, keyStoreFile);
        TestResources.copyResourceToPath(TLSServer.TRUST_CLIENT_JKS_RESOURCE, trustStoreFile);
        System.out.println("Copied server keystore to: " + keyStoreFile.toAbsolutePath().toString());
        System.out.println("Copied server truststore to: " + trustStoreFile.toAbsolutePath().toString());

        String host = "localhost";
        jettyServer = TLSServer.startSecureServer(host,
                keyStoreFile.toAbsolutePath().toString(),
                "changeit",
                "JKS",
                trustStoreFile.toAbsolutePath().toString(),
                "changeit",
                "JKS",
                true,
                new TLSServer.MockESConnectionHandler());

        connectURL = TLSServer.getBaseURLForConnect(jettyServer);
        System.out.println("Started on: " + connectURL);
    }

    @AfterAll
    static void afterAll() throws Exception {
        System.out.println("Stopping jetty");
        jettyServer.stop();
    }

    @Test
    void testTLSClientAuth(@TempDirectory.TempDir Path tempDir) throws Exception {
        Path keyStoreFile = tempDir.resolve("client_keystore");
        TestResources.copyResourceToPath(TLSServer.CLIENT_KEY_JKS_RESOURCE, keyStoreFile);

        Path trustStoreFile = tempDir.resolve("client_truststore");
        TestResources.copyResourceToPath(TLSServer.TRUST_SERVER_JKS_RESOURCE, trustStoreFile);

        Properties props = new Properties();
        props.setProperty(TrustStoreLocationConnectionProperty.KEY, trustStoreFile.toAbsolutePath().toString());
        props.setProperty(TrustStorePasswordConnectionProperty.KEY, "changeit");

        props.setProperty(KeyStoreLocationConnectionProperty.KEY, keyStoreFile.toAbsolutePath().toString());
        props.setProperty(KeyStorePasswordConnectionProperty.KEY, "changeit");

        Connection con = Assertions.assertDoesNotThrow(() -> new Driver().connect(connectURL, props));
        MockES.INSTANCE.assertMockESConnectionResponse((ElasticsearchConnection) con);
    }


}

