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

package com.amazon.opendistroforelasticsearch.jdbc.config;

import com.amazon.opendistroforelasticsearch.jdbc.auth.AuthenticationType;
import com.amazon.opendistroforelasticsearch.jdbc.internal.util.UrlParser;
import com.amazon.opendistroforelasticsearch.jdbc.logging.LogLevel;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class ConnectionConfigTests {

    @Test
    void testConnectionConfigBuilderDefaults() {
        ConnectionConfig.Builder conConfigBuilder = ConnectionConfig.builder();

        ConnectionProperty[] conProps = conConfigBuilder.getConnectionProperties();

        // properties start out un-parsed
        assertTrue(Arrays.stream(conProps).noneMatch(ConnectionProperty::isParsed));

        // no exception with all properties set to default
        ConnectionConfig conConfig = assertDoesNotThrow(conConfigBuilder::build);

        // verify defaults
        verifyDefaults(conConfig);
    }

    @Test
    void testConnectionConfigBuilderDefaultsWithEmptyProps() {
        ConnectionConfig.Builder conConfigBuilder = ConnectionConfig.builder()
                .setPropertyMap(new HashMap<>());

        ConnectionConfig conConfig = assertDoesNotThrow(conConfigBuilder::build);

        // verify defaults
        verifyDefaults(conConfig);
    }

    @Test
    void testConnectionConfigHost() {
        Map<String, Object> props = new HashMap<>();

        // exception with invalid values
        assertPropertyRejects(HostConnectionProperty.KEY,
                42,
                true,
                false);

        // valid value
        assertPropertyAccepts(HostConnectionProperty.KEY, ConnectionConfig::getHost,
                "hostvalue",
                "host.1234567890+&-$-");
    }

    @Test
    void testPortConfig() {
        // exception with invalid values
        assertPropertyRejects(PortConnectionProperty.KEY,
                "invalidValue",
                -1,
                "-1",
                65536,
                "65536");

        // valid values
        assertPropertyAccepts(PortConnectionProperty.KEY, ConnectionConfig::getPort,
                9400,
                65535);

        assertPropertyAcceptsParsedValue(
                PortConnectionProperty.KEY, ConnectionConfig::getPort, "9400", 9400);
    }

    @Test
    void testFetchSizeConfig() {
        // exception with invalid values
        assertPropertyRejects(FetchSizeProperty.KEY,
                "invalidValue",
                -1,
                "-1",
                "3.14");

        // valid values
        assertPropertyAccepts(FetchSizeProperty.KEY, ConnectionConfig::getFetchSize,
                500,
                0);

        assertPropertyAcceptsParsedValue(
                FetchSizeProperty.KEY, ConnectionConfig::getFetchSize, "25", 25);
    }

    @Test
    void testPathConfig() {
        // exception with invalid values
        assertPropertyRejects(PathConnectionProperty.KEY, 42, -1, true, false);

        // valid values
        assertPropertyAccepts(
                PathConnectionProperty.KEY,
                ConnectionConfig::getPath,
                "somepath",
                "path/value",
                "long/path/value/here/1234567890+&-$-"
        );

        // ignore a single trailing '/' in the user specified Path
        assertPropertyAcceptsValue(PathConnectionProperty.KEY, ConnectionConfig::getPath, "/context/path/", "/context/path");
    }

    @Test
    void testLogOutputConfig() {
        // exception with invalid values
        assertPropertyRejects(LogOutputConnectionProperty.KEY, 42);

        // valid values
        assertPropertyAccepts(
                LogOutputConnectionProperty.KEY,
                ConnectionConfig::getLogOutput,
                "AGeneralPlainString",
                "some/path/value",
                "long/nix/path/value/here/1234567890+&-$-.log",
                "c:\\long\\windows-path\\here\\1234567890+&-$-.log"
        );
    }

    @Test
    void testLogLevelConfig() {
        // exception with invalid values
        assertPropertyRejects(LogLevelConnectionProperty.KEY, 42, "unknown", "true", true, false);

        // valid values
        Arrays.stream(LogLevel.values()).forEach(
                logLevel -> assertPropertyAcceptsValue(
                        LogLevelConnectionProperty.KEY,
                        ConnectionConfig::getLogLevel,
                        logLevel.name().toLowerCase(), logLevel));
    }

    @Test
    void testLoginTimeoutConfig() {
        // exception with invalid values
        assertPropertyRejects(LoginTimeoutConnectionProperty.KEY, -1, "invalid", "9999999.5");

        // valid values
        assertPropertyAccepts(LoginTimeoutConnectionProperty.KEY, ConnectionConfig::getLoginTimeout,
                6000, 0, 9999999);
        assertPropertyAcceptsParsedValue(LoginTimeoutConnectionProperty.KEY, ConnectionConfig::getLoginTimeout,
                "0", 0);
        assertPropertyAcceptsParsedValue(LoginTimeoutConnectionProperty.KEY, ConnectionConfig::getLoginTimeout,
                "30", 30);
        assertPropertyAcceptsParsedValue(LoginTimeoutConnectionProperty.KEY, ConnectionConfig::getLoginTimeout,
                "6000", 6000);
        assertPropertyAcceptsParsedValue(LoginTimeoutConnectionProperty.KEY, ConnectionConfig::getLoginTimeout,
                "9999999", 9999999);
    }

    @Test
    void testUseSSLConfig() {
        assertCommonBooleanPropertyTests(UseSSLConnectionProperty.KEY, ConnectionConfig::isUseSSL);
    }

    @Test
    void testRequestCompressionConfig() {
        assertCommonBooleanPropertyTests(RequestCompressionConnectionProperty.KEY, ConnectionConfig::requestCompression);
    }

    @Test
    void testAuthConfig() {
        // exception with invalid values
        assertPropertyRejects(AuthConnectionProperty.KEY, 42, "unknown", "true", true, false);
    }

    @Test
    void testBasicAuthConfigMissingUsername() {
        ConnectionConfig.Builder builder = ConnectionConfig.builder();
        Map<String, Object> props = new HashMap<>();

        props.put(AuthConnectionProperty.KEY, "basic");

        builder.setPropertyMap(props);
        ConnectionPropertyException ex = assertThrows(ConnectionPropertyException.class, builder::build);
        assertEquals(AuthConnectionProperty.KEY, ex.getPropertyKey());
        assertTrue(ex.getMessage() != null && ex.getMessage().contains("requires a valid username"));
    }

    @Test
    void testBasicAuthConfigWithUsername() {
        ConnectionConfig.Builder builder = ConnectionConfig.builder();
        Map<String, Object> props = new HashMap<>();

        props.put(AuthConnectionProperty.KEY, "basic");
        props.put(UserConnectionProperty.KEY, "user");

        builder.setPropertyMap(props);
        ConnectionConfig connectionConfig = assertDoesNotThrow(builder::build);
        Assertions.assertEquals(AuthenticationType.BASIC, connectionConfig.getAuthenticationType());
    }

    @Test
    void testDefaultAuthConfigWithUsername() {
        ConnectionConfig.Builder builder = ConnectionConfig.builder();
        Map<String, Object> props = new HashMap<>();

        props.put(UserConnectionProperty.KEY, "some_user");

        builder.setPropertyMap(props);
        ConnectionConfig connectionConfig = assertDoesNotThrow(builder::build);
        Assertions.assertEquals(AuthenticationType.BASIC, connectionConfig.getAuthenticationType());
        assertEquals("some_user", connectionConfig.getUser());
    }

    @Test
    void testAwsSigV4AuthConfigWithRegion() {
        ConnectionConfig.Builder builder = ConnectionConfig.builder();
        Map<String, Object> props = new HashMap<>();

        props.put(AuthConnectionProperty.KEY, "aws_sigv4");
        props.put(RegionConnectionProperty.KEY, "us-west-2");

        builder.setPropertyMap(props);
        ConnectionConfig connectionConfig = assertDoesNotThrow(builder::build);
        Assertions.assertEquals(AuthenticationType.AWS_SIGV4, connectionConfig.getAuthenticationType());
    }

    @Test
    void testAwsSigV4AuthConfigWithoutRegion() {
        ConnectionConfig.Builder builder = ConnectionConfig.builder();
        Map<String, Object> props = new HashMap<>();

        props.put(AuthConnectionProperty.KEY, "aws_sigv4");

        builder.setPropertyMap(props);
        ConnectionPropertyException ex = assertThrows(ConnectionPropertyException.class, builder::build);
        assertEquals(AuthConnectionProperty.KEY, ex.getPropertyKey());
        assertTrue(ex.getMessage() != null && ex.getMessage().contains("requires a region"));
    }

    @Test
    void testAwsSigV4AuthConfigWithDetectedRegion() {
        ConnectionConfig.Builder builder = ConnectionConfig.builder();
        Map<String, Object> props = new HashMap<>();

        props.put(AuthConnectionProperty.KEY, "aws_sigv4");
        props.put(HostConnectionProperty.KEY, "some-hostname.us-west-1.es.amazonaws.com");

        builder.setPropertyMap(props);
        ConnectionConfig connectionConfig = assertDoesNotThrow(builder::build);
        Assertions.assertEquals(AuthenticationType.AWS_SIGV4, connectionConfig.getAuthenticationType());
        assertEquals("us-west-1", connectionConfig.getRegion());
    }

    @Test
    void testRegionConfig() {
        Map<String, Object> props = new HashMap<>();

        // exception with invalid values
        assertPropertyRejects(RegionConnectionProperty.KEY,
                42,
                true,
                false);

        // valid values
        assertPropertyAccepts(RegionConnectionProperty.KEY, ConnectionConfig::getRegion,
                "region-value",
                "us-gov-west-1",
                "ap-southeast-2");
    }

    @Test
    void testAwsCredentialsProviderConfig() {
        assertPropertyRejectsValue(AwsCredentialsProviderProperty.KEY, "Invalid AWS Credentials Provider");

        // The property accepts null and valid AWSCredentialProvider
        assertPropertyAcceptsValue(AwsCredentialsProviderProperty.KEY, ConnectionConfig::getAwsCredentialsProvider,
                null);
        assertPropertyAcceptsValue(AwsCredentialsProviderProperty.KEY, ConnectionConfig::getAwsCredentialsProvider,
                new EnvironmentVariableCredentialsProvider());
    }

    @Test
    void testHostnameVerificationConfig() {
        assertCommonBooleanPropertyTests(HostnameVerificationConnectionProperty.KEY, ConnectionConfig::hostnameVerification);
    }

    @Test
    void testConnectionConfigMultipleProps() {
        ConnectionConfig.Builder conConfigBuilder = ConnectionConfig.builder();

        Map<String, Object> props = new HashMap<>();

        // exception when any property invalid
        props.put(HostConnectionProperty.KEY, "es-host");
        props.put(PortConnectionProperty.KEY, "9300");
        props.put(UseSSLConnectionProperty.KEY, "True");
        props.put(PathConnectionProperty.KEY, "valid/path");
        props.put(LoginTimeoutConnectionProperty.KEY, -1); // invalid
        conConfigBuilder.setPropertyMap(props);
        ConnectionPropertyException ex = assertThrows(ConnectionPropertyException.class, conConfigBuilder::build);
        assertEquals(LoginTimeoutConnectionProperty.KEY, ex.getPropertyKey());

        props.put(HostConnectionProperty.KEY, "es-host");
        props.put(PortConnectionProperty.KEY, "9300");
        props.put(UseSSLConnectionProperty.KEY, "True");
        props.put(PathConnectionProperty.KEY, 100); // invalid
        props.put(LoginTimeoutConnectionProperty.KEY, "60");
        conConfigBuilder.setPropertyMap(props);
        ex = assertThrows(ConnectionPropertyException.class, conConfigBuilder::build);
        assertEquals(PathConnectionProperty.KEY, ex.getPropertyKey());

        props.put(HostConnectionProperty.KEY, "es-host");
        props.put(PortConnectionProperty.KEY, "9300");
        props.put(UseSSLConnectionProperty.KEY, 5); // invalid
        props.put(PathConnectionProperty.KEY, "path/value");
        props.put(LoginTimeoutConnectionProperty.KEY, "60");
        conConfigBuilder.setPropertyMap(props);
        ex = assertThrows(ConnectionPropertyException.class, conConfigBuilder::build);
        assertEquals(UseSSLConnectionProperty.KEY, ex.getPropertyKey());

        props.put(HostConnectionProperty.KEY, "es-host");
        props.put(PortConnectionProperty.KEY, -5); // invalid
        props.put(UseSSLConnectionProperty.KEY, "true");
        props.put(PathConnectionProperty.KEY, "path/value");
        props.put(LoginTimeoutConnectionProperty.KEY, "60");
        conConfigBuilder.setPropertyMap(props);
        ex = assertThrows(ConnectionPropertyException.class, conConfigBuilder::build);
        assertEquals(PortConnectionProperty.KEY, ex.getPropertyKey());

        props.put(HostConnectionProperty.KEY, new Object()); // invalid
        props.put(PortConnectionProperty.KEY, "9300");
        props.put(UseSSLConnectionProperty.KEY, "true");
        props.put(PathConnectionProperty.KEY, "path/value");
        props.put(LoginTimeoutConnectionProperty.KEY, "60");
        conConfigBuilder.setPropertyMap(props);
        ex = assertThrows(ConnectionPropertyException.class, conConfigBuilder::build);
        assertEquals(HostConnectionProperty.KEY, ex.getPropertyKey());

        // all valid
        props.put(HostConnectionProperty.KEY, "es-hostname");
        props.put(PortConnectionProperty.KEY, "9400");
        props.put(UseSSLConnectionProperty.KEY, "true");
        props.put(PathConnectionProperty.KEY, "path/value/1");
        props.put(LoginTimeoutConnectionProperty.KEY, "90");
        conConfigBuilder.setPropertyMap(props);
        ConnectionConfig conConfig = assertDoesNotThrow(conConfigBuilder::build);
        assertEquals("es-hostname", conConfig.getHost());
        assertEquals(9400, conConfig.getPort());
        assertTrue(conConfig.isUseSSL());
        assertEquals("path/value/1", conConfig.getPath());
        assertEquals(90, conConfig.getLoginTimeout());
    }

    @Test
    void testBuildWithProperties() {
        Properties properties = new Properties();

        properties.setProperty(HostConnectionProperty.KEY, "prop-host");
        properties.setProperty(LogOutputConnectionProperty.KEY, "prop-log-file");
        properties.setProperty(PathConnectionProperty.KEY, "prop-path");
        properties.setProperty(LoginTimeoutConnectionProperty.KEY, "3000");
        properties.setProperty(UseSSLConnectionProperty.KEY, "true");

        ConnectionConfig.Builder conConfigBuilder = ConnectionConfig.builder().setProperties(properties);
        ConnectionConfig conConfig = assertDoesNotThrow(conConfigBuilder::build);

        assertEquals(conConfig.getHost(), "prop-host");
        assertEquals(conConfig.getLogOutput(), "prop-log-file");
        assertEquals(conConfig.getPath(), "prop-path");
        assertEquals(conConfig.getLoginTimeout(), 3000);
        assertTrue(conConfig.isUseSSL());

        // verify unset properties carry expected defaults
        assertEquals(conConfig.getPort(), 443); // default with useSSL
        assertFalse(conConfig.requestCompression());
    }

    @Test
    void testBuildWithPropertiesWithDefaults() {
        Properties defaults = new Properties();
        defaults.setProperty(PortConnectionProperty.KEY, "1080");
        defaults.setProperty(LogOutputConnectionProperty.KEY, "default-log-file");
        defaults.setProperty(LoginTimeoutConnectionProperty.KEY, "1000");
        defaults.setProperty(UseSSLConnectionProperty.KEY, "true");

        Properties properties = new Properties(defaults);
        properties.setProperty(HostConnectionProperty.KEY, "prop-host");
        properties.setProperty(LogOutputConnectionProperty.KEY, "prop-log-file");
        properties.setProperty(PathConnectionProperty.KEY, "prop-path");
        properties.setProperty(LoginTimeoutConnectionProperty.KEY, "3000");


        ConnectionConfig.Builder conConfigBuilder = ConnectionConfig.builder().setProperties(properties);
        ConnectionConfig conConfig = assertDoesNotThrow(conConfigBuilder::build);

        assertEquals(conConfig.getHost(), "prop-host");
        assertEquals(conConfig.getPort(), 1080); // set from defaults
        assertEquals(conConfig.getLogOutput(), "prop-log-file"); // default overridden
        assertEquals(conConfig.getPath(), "prop-path"); // no default
        assertEquals(conConfig.getLoginTimeout(), 3000); // default overridden
        assertTrue(conConfig.isUseSSL()); // set from defaults
    }

    @Test
    void testBuildWithUrl() {
        final String url = UrlParser.URL_PREFIX + "https://url-host/?logOutput=url-log-file&loginTimeout=2000";

        ConnectionConfig.Builder conConfigBuilder = ConnectionConfig.builder()
                .setUrl(url);

        ConnectionConfig conConfig = assertDoesNotThrow(conConfigBuilder::build);

        assertTrue(conConfig.isUseSSL());
        assertEquals(conConfig.getHost(), "url-host");
        assertEquals(conConfig.getPath(), "/");
        assertEquals(conConfig.getLogOutput(), "url-log-file");
        assertEquals(conConfig.getLoginTimeout(), 2000);

        // verify unset properties carry expected defaults
        assertEquals(conConfig.getPort(), 443); // default with useSSL
        assertFalse(conConfig.requestCompression());
    }

    @Test
    void testBuildWithInvalidUrl() {
        final String url = "https://url-host/?logOutput=url-log-file&loginTimeout=2000";

        ConnectionConfig.Builder conConfigBuilder = ConnectionConfig.builder()
                .setUrl(url);

        ConnectionPropertyException ex = assertThrows(ConnectionPropertyException.class, conConfigBuilder::build);
        assertNotNull(ex.getCause());
        assertEquals(URISyntaxException.class, ex.getCause().getClass());
    }

    @Test
    void testBuildWithUrlAndProperties() {
        final String url = UrlParser.URL_PREFIX + "https://url-host/?logOutput=url-log-file&loginTimeout=2000";
        Properties properties = new Properties();

        properties.setProperty(HostConnectionProperty.KEY, "prop-host");
        properties.setProperty(PathConnectionProperty.KEY, "prop-path");
        properties.setProperty(LoginTimeoutConnectionProperty.KEY, "3000");
        properties.setProperty(UseSSLConnectionProperty.KEY, "false");

        ConnectionConfig.Builder conConfigBuilder = ConnectionConfig.builder()
                .setUrl(url)
                .setProperties(properties);

        ConnectionConfig conConfig = assertDoesNotThrow(conConfigBuilder::build);

        // properties overridden by builder.setProperties( )
        assertFalse(conConfig.isUseSSL());
        assertEquals(conConfig.getHost(), "prop-host");
        assertEquals(conConfig.getPath(), "prop-path");
        assertEquals(conConfig.getLoginTimeout(), 3000);

        // properties from url
        assertEquals(conConfig.getLogOutput(), "url-log-file");

        // verify unset properties carry expected defaults
        assertEquals(conConfig.getPort(), 9200);
    }

    @Test
    void testBuildWithUrlAndPropertiesWithDefaults() {
        final String url = UrlParser.URL_PREFIX + "http://url-host/?logOutput=url-log-file&loginTimeout=2000&user=url-user";

        Properties defaults = new Properties();
        defaults.setProperty(PortConnectionProperty.KEY, "1080");
        defaults.setProperty(LogOutputConnectionProperty.KEY, "default-log-file");
        defaults.setProperty(LoginTimeoutConnectionProperty.KEY, "1000");
        defaults.setProperty(UseSSLConnectionProperty.KEY, "true");

        Properties properties = new Properties(defaults);
        properties.setProperty(HostConnectionProperty.KEY, "prop-host");
        properties.setProperty(LogOutputConnectionProperty.KEY, "prop-log-file");
        properties.setProperty(PathConnectionProperty.KEY, "prop-path");
        properties.setProperty(LoginTimeoutConnectionProperty.KEY, "3000");


        ConnectionConfig.Builder conConfigBuilder = ConnectionConfig.builder()
                .setUrl(url)
                .setProperties(properties);

        ConnectionConfig conConfig = assertDoesNotThrow(conConfigBuilder::build);

        assertTrue(conConfig.isUseSSL()); // set from defaults
        assertEquals(conConfig.getHost(), "prop-host");
        assertEquals(conConfig.getPort(), 1080); // set from defaults
        assertEquals(conConfig.getLogOutput(), "prop-log-file"); // default overridden
        assertEquals(conConfig.getPath(), "prop-path"); // no default
        assertEquals(conConfig.getLoginTimeout(), 3000); // default overridden

        assertEquals(conConfig.getUser(), "url-user"); // from url
    }

    @Test
    void testBuildWithPropertyMap() {
        Map<String, Object> propertyMap = new HashMap<>();

        propertyMap.put(RequestCompressionConnectionProperty.KEY, true);
        propertyMap.put(HostConnectionProperty.KEY, "prop-host");
        propertyMap.put(LogOutputConnectionProperty.KEY, "prop-log-file");
        propertyMap.put(PathConnectionProperty.KEY, "prop-path");
        propertyMap.put(LoginTimeoutConnectionProperty.KEY, 3000);

        ConnectionConfig.Builder conConfigBuilder = ConnectionConfig.builder()
                .setPropertyMap(propertyMap);

        ConnectionConfig conConfig = assertDoesNotThrow(conConfigBuilder::build);

        assertTrue(conConfig.requestCompression());
        assertEquals(conConfig.getHost(), "prop-host");
        assertEquals(conConfig.getPort(), 9200); // default
        assertEquals(conConfig.getLogOutput(), "prop-log-file");
        assertEquals(conConfig.getPath(), "prop-path");
        assertEquals(conConfig.getLoginTimeout(), 3000);
    }

    @Test
    void testBuildWithUrlAndPropertyMap() {
        final String url = UrlParser.URL_PREFIX + "http://url-host/?logOutput=url-log-file&loginTimeout=2000&user=url-user";

        Map<String, Object> propertyMap = new HashMap<>();

        propertyMap.put(RequestCompressionConnectionProperty.KEY, true);
        propertyMap.put(HostConnectionProperty.KEY, "prop-host");
        propertyMap.put(LogOutputConnectionProperty.KEY, "prop-log-file");
        propertyMap.put(PathConnectionProperty.KEY, "prop-path");
        propertyMap.put(LoginTimeoutConnectionProperty.KEY, 3000);

        ConnectionConfig.Builder conConfigBuilder = ConnectionConfig.builder()
                .setUrl(url)
                .setPropertyMap(propertyMap);

        ConnectionConfig conConfig = assertDoesNotThrow(conConfigBuilder::build);

        assertTrue(conConfig.requestCompression());
        assertEquals("prop-host", conConfig.getHost());
        assertEquals(9200, conConfig.getPort()); // default
        assertEquals("prop-log-file", conConfig.getLogOutput());
        assertEquals("prop-path", conConfig.getPath());
        assertEquals(3000, conConfig.getLoginTimeout());
        assertEquals(true, conConfig.requestCompression());

        assertEquals("url-user", conConfig.getUser()); // from url
    }

    @Test
    void testBuildWithUrlAndOverrideMap() {
        final String url = UrlParser.URL_PREFIX + "http://url-host/?logOutput=url-log-file&loginTimeout=2000&user=url-user";

        Map<String, Object> propertyMap = new HashMap<>();

        propertyMap.put(UseSSLConnectionProperty.KEY, true);
        propertyMap.put(HostConnectionProperty.KEY, "prop-host");
        propertyMap.put(LogOutputConnectionProperty.KEY, "prop-log-file");
        propertyMap.put(PathConnectionProperty.KEY, "prop-path");
        propertyMap.put(LoginTimeoutConnectionProperty.KEY, 3000);

        ConnectionConfig.Builder conConfigBuilder = ConnectionConfig.builder()
                .setUrl(url)
                .overrideProperties(propertyMap);

        ConnectionConfig conConfig = assertDoesNotThrow(conConfigBuilder::build);

        assertTrue(conConfig.isUseSSL());
        assertEquals("prop-host", conConfig.getHost());
        assertEquals(443, conConfig.getPort()); // default with useSSL
        assertEquals("prop-log-file", conConfig.getLogOutput());
        assertEquals("prop-path", conConfig.getPath());
        assertEquals(3000, conConfig.getLoginTimeout());

        assertEquals("url-user", conConfig.getUser()); // from url
    }

    @Test
    void testBuildWithUrlAndPropertyMapAndOverrides() {
        final String url = UrlParser.URL_PREFIX + "http://url-host/?logOutput=url-log-file" +
                "&loginTimeout=2000&user=url-user&password=url-password";

        Map<String, Object> propertyMap = new HashMap<>();

        propertyMap.put(UseSSLConnectionProperty.KEY, true);
        propertyMap.put(HostConnectionProperty.KEY, "prop-host");
        propertyMap.put(LogOutputConnectionProperty.KEY, "prop-log-file");
        propertyMap.put(PathConnectionProperty.KEY, "prop-path");
        propertyMap.put(LoginTimeoutConnectionProperty.KEY, 3000);

        Map<String, Object> overrideMap = new HashMap<>();
        overrideMap.put(UseSSLConnectionProperty.KEY, false);
        overrideMap.put(LoginTimeoutConnectionProperty.KEY, 5000);
        overrideMap.put(UserConnectionProperty.KEY, "override-user");

        ConnectionConfig.Builder conConfigBuilder = ConnectionConfig.builder()
                .setUrl(url)
                .setPropertyMap(propertyMap)
                .overrideProperties(overrideMap);

        ConnectionConfig conConfig = assertDoesNotThrow(conConfigBuilder::build);

        assertFalse(conConfig.isUseSSL()); // override
        assertEquals("prop-host", conConfig.getHost());
        assertEquals(9200, conConfig.getPort()); // default
        assertEquals("prop-log-file", conConfig.getLogOutput());
        assertEquals("prop-path", conConfig.getPath());
        assertEquals(5000, conConfig.getLoginTimeout()); // override

        assertEquals("override-user", conConfig.getUser()); // override
        assertEquals("url-password", conConfig.getPassword()); // url
    }

    @Test
    void testBuildWithUrlAndPropertyMapAndMultipleOverrides() {
        final String url = UrlParser.URL_PREFIX + "http://url-host/?logOutput=url-log-file" +
                "&loginTimeout=2000&user=url-user&password=url-password";

        Map<String, Object> propertyMap = new HashMap<>();

        propertyMap.put(UseSSLConnectionProperty.KEY, true);
        propertyMap.put(HostConnectionProperty.KEY, "prop-host");
        propertyMap.put(LogOutputConnectionProperty.KEY, "prop-log-file");
        propertyMap.put(PathConnectionProperty.KEY, "prop-path");
        propertyMap.put(LoginTimeoutConnectionProperty.KEY, 3000);

        Map<String, Object> overrideMap1 = new HashMap<>();
        overrideMap1.put(UseSSLConnectionProperty.KEY, false);
        overrideMap1.put(LoginTimeoutConnectionProperty.KEY, 5000);

        Map<String, Object> overrideMap2 = new HashMap<>();
        overrideMap2.put(UseSSLConnectionProperty.KEY, true);
        overrideMap2.put(UserConnectionProperty.KEY, "override-user");

        ConnectionConfig.Builder conConfigBuilder = ConnectionConfig.builder()
                .setUrl(url)
                .setPropertyMap(propertyMap)
                .overrideProperties(overrideMap1)
                .overrideProperties(overrideMap2);

        ConnectionConfig conConfig = assertDoesNotThrow(conConfigBuilder::build);

        assertTrue(conConfig.isUseSSL()); // override 2
        assertEquals("prop-host", conConfig.getHost());
        assertEquals(443, conConfig.getPort()); // default with useSSL
        assertEquals("prop-log-file", conConfig.getLogOutput());
        assertEquals("prop-path", conConfig.getPath());
        assertEquals(5000, conConfig.getLoginTimeout()); // override 1

        assertEquals("override-user", conConfig.getUser()); // override 2
        assertEquals("url-password", conConfig.getPassword()); // url
    }

    @ParameterizedTest
    @CsvSource(value = {
            "0, 1, 2", "0, 2, 1",
            "1, 0, 2", "1, 2, 0",
            "2, 0, 1", "2, 1, 0"
    })
    void testBuildWithUrlAndPropertyMapAndOverridesCombinations(int f, int s, int t) {
        // Verify that order of invoking ConnectionConfig.Builder methods
        // does not change the effective behavior
        final String url = UrlParser.URL_PREFIX + "http://url-host/?logOutput=url-log-file" +
                "&loginTimeout=2000&user=url-user&password=url-password";

        Map<String, Object> propertyMap = new HashMap<>();

        propertyMap.put(UseSSLConnectionProperty.KEY, true);
        propertyMap.put(HostConnectionProperty.KEY, "prop-host");
        propertyMap.put(LogOutputConnectionProperty.KEY, "prop-log-file");
        propertyMap.put(PathConnectionProperty.KEY, "prop-path");
        propertyMap.put(LoginTimeoutConnectionProperty.KEY, 3000);

        Map<String, Object> overrideMap = new HashMap<>();
        overrideMap.put(UseSSLConnectionProperty.KEY, false);
        overrideMap.put(LoginTimeoutConnectionProperty.KEY, 5000);
        overrideMap.put(UserConnectionProperty.KEY, "override-user");

        ConnectionConfig.Builder conConfigBuilder = ConnectionConfig.builder();
        Integer[] order = new Integer[]{f, s, t};

        for (int m : order) {
            switch (m) {
                case 0:
                    conConfigBuilder.setUrl(url);
                    break;
                case 1:
                    conConfigBuilder.setPropertyMap(propertyMap);
                    break;
                case 2:
                    conConfigBuilder.overrideProperties(overrideMap);
                    break;
            }
        }

        String message = "order: " + Arrays.toString(order);

        ConnectionConfig conConfig = assertDoesNotThrow(conConfigBuilder::build, message);

        assertFalse(conConfig.isUseSSL(), message); // override
        assertEquals("prop-host", conConfig.getHost(), message);
        assertEquals(9200, conConfig.getPort(), message); // default
        assertEquals("prop-log-file", conConfig.getLogOutput(), message);
        assertEquals("prop-path", conConfig.getPath(), message);
        assertEquals(5000, conConfig.getLoginTimeout(), message); // override

        assertEquals("override-user", conConfig.getUser(), message); // override
        assertEquals("url-password", conConfig.getPassword(), message); // url
    }

    @Test
    void testBuildWithOverrideMap() {
        Map<String, Object> propertyMap = new HashMap<>();

        propertyMap.put(UseSSLConnectionProperty.KEY, true);
        propertyMap.put(HostConnectionProperty.KEY, "prop-host");
        propertyMap.put(LogOutputConnectionProperty.KEY, "prop-log-file");
        propertyMap.put(PathConnectionProperty.KEY, "prop-path");
        propertyMap.put(LoginTimeoutConnectionProperty.KEY, 3000);

        ConnectionConfig.Builder conConfigBuilder = ConnectionConfig.builder()
                .overrideProperties(propertyMap);

        ConnectionConfig conConfig = assertDoesNotThrow(conConfigBuilder::build);

        assertTrue(conConfig.isUseSSL());
        assertEquals("prop-host", conConfig.getHost());
        assertEquals(443, conConfig.getPort()); // default with useSSL
        assertEquals("prop-log-file", conConfig.getLogOutput());
        assertEquals("prop-path", conConfig.getPath());
        assertEquals(3000, conConfig.getLoginTimeout());
    }

    /**
     * Common assertions that should pass with any Boolean property
     *
     * @param propertyKey property key
     * @param propertyGetter getter function to retrieve the parsed value
     *         of the property
     */
    private void assertCommonBooleanPropertyTests(String propertyKey, Function<ConnectionConfig, Object> propertyGetter) {
        // exception with invalid values
        assertPropertyRejects(propertyKey, -1, 0, 100.5);

        // valid values
        assertPropertyAccepts(propertyKey, propertyGetter, true, false);

        assertPropertyAcceptsParsedValue(propertyKey, propertyGetter, "true", true);
        assertPropertyAcceptsParsedValue(propertyKey, propertyGetter, "false", false);
        assertPropertyAcceptsParsedValue(propertyKey, propertyGetter, "any", false);
    }

    /**
     * Helper to assert a {@link ConnectionConfig} accepts specified
     * objects as values for a property and the actual value of the
     * property is set exactly same as the specified objects.
     *
     * @param key key associated with property
     * @param propertyGetter getter function to retrieve the parsed value of the property
     * @param values value objects to set the property value with
     */
    private void assertPropertyAccepts(
            final String key, Function<ConnectionConfig, Object> propertyGetter, final Object... values) {
        Arrays.stream(values).forEach((value) -> assertPropertyAcceptsValue(key, propertyGetter, value));
    }

    /**
     * Helper to assert {@link ConnectionConfig} rejects specified
     * objects as values for a property.
     *
     * @param key key associated with property
     * @param values value objects to set the property value with
     */
    private void assertPropertyRejects(final String key, final Object... values) {
        Arrays.stream(values).forEach((value) -> assertPropertyRejectsValue(key, value));
    }

    /**
     * Helper to assert {@link ConnectionConfig} accepts the specified
     * object as a value for a property.
     *
     * @param key
     * @param propertyGetter
     * @param specifiedValue
     */
    private void assertPropertyAcceptsValue(
            final String key, Function<ConnectionConfig, Object> propertyGetter, final Object specifiedValue) {
        assertPropertyAcceptsValue(key, propertyGetter, specifiedValue, specifiedValue);
    }

    /**
     * Helper to assert {@link ConnectionConfig} accepts a specified
     * object as a value and that the resulting value retrieved from
     * ConnectionConfig matches the expected value.
     *
     * @param key Property key to set
     * @param propertyGetter Function to retrieve property value from ConnectionConfig
     * @param specifiedValue Property value to specify when building ConnectionConfig
     * @param expectedValue Expected value returned from ConnectionConfig
     */
    private void assertPropertyAcceptsValue(
            final String key, Function<ConnectionConfig, Object> propertyGetter,
            final Object specifiedValue, final Object expectedValue) {
        ConnectionConfig conConfig = assertConnectionConfigIsBuilt(key, specifiedValue);
        assertEquals(expectedValue, propertyGetter.apply(conConfig));
    }

    /**
     * Helper to assert {@link ConnectionConfig} accepts a specified
     * object as a value for a property and that the actual value set
     * on the property matches a specific value.
     *
     * @param key key associated with property
     * @param propertyGetter getter function to retrieve the parsed value of the property
     * @param specifiedValue value object to set the property with
     * @param parsedValue expected parsed (actual) value of the property
     */
    private void assertPropertyAcceptsParsedValue(
            final String key, Function<ConnectionConfig, Object> propertyGetter,
            final Object specifiedValue, final Object parsedValue) {
        ConnectionConfig conConfig = assertConnectionConfigIsBuilt(key, specifiedValue);
        assertEquals(parsedValue, propertyGetter.apply(conConfig));
    }

    /**
     * Helper to assert a {@link ConnectionConfig} can be built successfully
     * when a specific property is assigned a certain value.
     *
     * @param key key associated with a property
     * @param specifiedValue value to assign the property
     *
     * @return {@link ConnectionConfig} object built with specified property
     */
    private ConnectionConfig assertConnectionConfigIsBuilt(final String key, final Object specifiedValue) {
        ConnectionConfig.Builder builder = ConnectionConfig.builder();
        Map<String, Object> props = new HashMap<>();

        // exception with invalid values
        props.put(key, specifiedValue);
        builder.setPropertyMap(props);
        return assertDoesNotThrow(builder::build);
    }

    /**
     * Helper to assert that building a {@link ConnectionConfig} fails with
     * an exception if the specified property is assigned a certain value.
     *
     * @param key key associated with the property
     * @param value value to assign to the property
     */
    private void assertPropertyRejectsValue(final String key, final Object value) {
        ConnectionConfig.Builder builder = ConnectionConfig.builder();
        Map<String, Object> props = new HashMap<>();

        // exception with invalid value
        props.put(key, value);
        builder.setPropertyMap(props);
        ConnectionPropertyException ex = assertThrows(ConnectionPropertyException.class, builder::build);
        assertEquals(key, ex.getPropertyKey());
    }

    /**
     * Verifies property values in a {@link ConnectionConfig} instance match
     * their expected defaults.
     *
     * @param connectionConfig {@link ConnectionConfig} instance to inspect
     */
    private void verifyDefaults(ConnectionConfig connectionConfig) {
        // verify defaults
        assertEquals(9200, connectionConfig.getPort());
        assertEquals("", connectionConfig.getPath());
        assertEquals(0, connectionConfig.getFetchSize());
        assertEquals("localhost", connectionConfig.getHost());
        assertEquals(0, connectionConfig.getLoginTimeout());
        assertFalse(connectionConfig.isUseSSL());
        assertFalse(connectionConfig.requestCompression());
        assertEquals(AuthenticationType.NONE, connectionConfig.getAuthenticationType());
        assertNull(connectionConfig.getRegion());
        assertEquals(LogLevel.OFF, connectionConfig.getLogLevel());
        assertTrue(connectionConfig.hostnameVerification());
    }

}
