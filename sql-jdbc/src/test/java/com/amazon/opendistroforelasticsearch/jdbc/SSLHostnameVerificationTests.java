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

import com.amazon.opendistroforelasticsearch.jdbc.config.HostnameVerificationConnectionProperty;
import com.amazon.opendistroforelasticsearch.jdbc.config.TrustSelfSignedConnectionProperty;
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
import java.sql.SQLException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(TempDirectory.class)
public class SSLHostnameVerificationTests {

    // WireMockServer has a problem initializing server on TLS from
    // a password protected JKS keystore. If the issue gets fixed,
    // these tests can use WireMockServer instead.

    static Server jettyServer;
    static String connectURL;

    @BeforeAll
    static void beforeAll(@TempDirectory.TempDir Path tempDir) throws Exception {

        // Start server with SSL enabled
        Path keyStoreFile = tempDir.resolve("keystore");
        TestResources.copyResourceToPath(TLSServer.SERVER_KEY_JKS_RESOURCE_NON_LOCALHOST, keyStoreFile);
        System.out.println("Copied keystore to: " + keyStoreFile.toAbsolutePath().toString());

        String host = "localhost";
        jettyServer = TLSServer.startSecureServer(host,
                keyStoreFile.toAbsolutePath().toString(),
                "changeit",
                "JKS",
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
    void testTrustSelfSignedEnabledHostnameVerificationDisabled() throws Exception {
        Properties props = new Properties();
        props.setProperty(TrustSelfSignedConnectionProperty.KEY, "true");
        props.setProperty(HostnameVerificationConnectionProperty.KEY, "false");

        Connection con = Assertions.assertDoesNotThrow(() -> new Driver().connect(connectURL, props));

        MockES.INSTANCE.assertMockESConnectionResponse((ElasticsearchConnection) con);
    }

    @Test
    void testTrustSelfSignedEnabledHostnameVerificationEnabled() throws Exception {
        Properties props = new Properties();
        props.setProperty(TrustSelfSignedConnectionProperty.KEY, "true");
        props.setProperty(HostnameVerificationConnectionProperty.KEY, "true");

        SQLException sqe = Assertions.assertThrows(SQLException.class, () -> new Driver().connect(connectURL, props));
        assertTrue(sqe.getMessage().contains("javax.net.ssl.SSLPeerUnverifiedException"));
    }

    @Test
    void testTrustSelfSignedEnabledHostnameVerificationDefault() throws Exception {
        Properties props = new Properties();
        props.setProperty(TrustSelfSignedConnectionProperty.KEY, "true");

        SQLException sqe = Assertions.assertThrows(SQLException.class, () -> new Driver().connect(connectURL, props));
        assertTrue(sqe.getMessage().contains("javax.net.ssl.SSLPeerUnverifiedException"));
    }

}

