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


import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.lang.reflect.Field;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;


/**
 * JUnit extension to inject a WireMockServer instance into a
 * {@link WireMockServer} parameter for a Test.
 *
 * Use this extension to create a new {@link WireMockServer}
 * instance for each Test in a class.
 *
 * The extension ensures:
 * <p>
 *     <li>
 *         Before each test, mock server is started and made available to the Test as a parameter.
 *         Note: if a test does not declare a {@link WireMockServer} parameter, no mock server
 *         instance is created.
 *     </li>
 *     <li>
 *         After the test execution, the mock server is stopped.
 *     </li>
 * </p>
 *
 *
 */
public class PerTestWireMockServerExtension implements AfterEachCallback, ParameterResolver {

    private WireMockServer mockServer;

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
       cleanupMockServer(context);
    }

    private WireMockServer createAndStartMockServer() {
        System.out.println("Creating mock server");
        mockServer = new WireMockServer(options()
                .dynamicPort()
                .notifier(new ConsoleNotifier(true)
                ));

        mockServer.start();
        return mockServer;
    }

    private void cleanupMockServer(ExtensionContext context) {
        if (mockServer != null) {
            System.out.println("Cleaning up mock server");
            mockServer.stop();
            mockServer = null;
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return parameterContext.getParameter().getType() == WireMockServer.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return createAndStartMockServer();
    }
}
