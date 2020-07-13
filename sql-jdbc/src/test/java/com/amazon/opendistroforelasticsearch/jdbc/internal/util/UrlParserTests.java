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

package com.amazon.opendistroforelasticsearch.jdbc.internal.util;

import static org.junit.jupiter.api.Assertions.*;

import com.amazon.opendistroforelasticsearch.jdbc.config.HostConnectionProperty;
import com.amazon.opendistroforelasticsearch.jdbc.config.PasswordConnectionProperty;
import com.amazon.opendistroforelasticsearch.jdbc.config.PathConnectionProperty;
import com.amazon.opendistroforelasticsearch.jdbc.config.PortConnectionProperty;
import com.amazon.opendistroforelasticsearch.jdbc.config.UseSSLConnectionProperty;
import com.amazon.opendistroforelasticsearch.jdbc.config.UserConnectionProperty;
import com.amazon.opendistroforelasticsearch.jdbc.test.KeyValuePairs;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URISyntaxException;
import java.util.Properties;

class UrlParserTests {

    @ParameterizedTest
    @ValueSource(strings = {
            "jdbc:elasticsearch://host:9200",
            "jdbc:elasticsearch://host:9200/path",
            "jdbc:elasticsearch://host:9200/path/",
            "jdbc:elasticsearch://host:9200/path?option=value",
            "jdbc:elasticsearch://host:9200/path?option=value&option2=value2",
            "jdbc:elasticsearch://host/path",
            "jdbc:elasticsearch://host/path/",
            "jdbc:elasticsearch://host/path?option=value&option2=value2",
    })
    void testIsAcceptable(String url) {
        assertTrue(UrlParser.isAcceptable(url), () -> url + " was not accepted");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "jdbc:elasticsearch:/",
            "elasticsearch://host:9200/path",
            "jdbc:elasticsearch:",
            "jdbc:elasticsearch",
            "jdbc://host:9200/"
    })
    void testIsNotAcceptable(String url) {
        assertFalse(UrlParser.isAcceptable(url), () -> url + " was accepted");
    }

    @Test
    void testNullNotAcceptable() {
        assertFalse(UrlParser.isAcceptable(null), "null was accepted");
    }

    @Test
    void testPropertiesFromURL() throws URISyntaxException {

        propertiesFromUrl("jdbc:elasticsearch://")
                .match(); // empty properties

        propertiesFromUrl("jdbc:elasticsearch://https://localhost:9200/")
                .match(
                        KeyValuePairs.skvp(HostConnectionProperty.KEY, "localhost"),
                        KeyValuePairs.skvp(PortConnectionProperty.KEY, "9200"),
                        KeyValuePairs.skvp(UseSSLConnectionProperty.KEY, "true"),
                        KeyValuePairs.skvp(PathConnectionProperty.KEY, "/"));

        propertiesFromUrl("jdbc:elasticsearch://localhost:9200")
                .match(
                        KeyValuePairs.skvp(HostConnectionProperty.KEY, "localhost"),
                        KeyValuePairs.skvp(PortConnectionProperty.KEY, "9200"),
                        KeyValuePairs.skvp(UseSSLConnectionProperty.KEY, "false"));

        propertiesFromUrl("jdbc:elasticsearch://es-domain-name.sub.hostname.com:1080")
                .match(
                        KeyValuePairs.skvp(HostConnectionProperty.KEY, "es-domain-name.sub.hostname.com"),
                        KeyValuePairs.skvp(PortConnectionProperty.KEY, "1080"),
                        KeyValuePairs.skvp(UseSSLConnectionProperty.KEY, "false"));

        propertiesFromUrl("jdbc:elasticsearch://es-domain-name.sub.hostname.com:1090/")
                .match(
                        KeyValuePairs.skvp(HostConnectionProperty.KEY, "es-domain-name.sub.hostname.com"),
                        KeyValuePairs.skvp(PortConnectionProperty.KEY, "1090"),
                        KeyValuePairs.skvp(UseSSLConnectionProperty.KEY, "false"),
                        KeyValuePairs.skvp(PathConnectionProperty.KEY, "/"));

    }

    @Test
    public void testPropertiesFromLongUrl() {
        propertiesFromUrl(
                "jdbc:elasticsearch://search-elasticsearch-es23-dedm-za-1-edmwao5g64rlo3hcohapy2jpru.us-east-1.es.a9.com")
                .match(
                        KeyValuePairs.skvp(HostConnectionProperty.KEY,
                                "search-elasticsearch-es23-dedm-za-1-edmwao5g64rlo3hcohapy2jpru.us-east-1.es.a9.com"),
                        KeyValuePairs.skvp(UseSSLConnectionProperty.KEY, "false"));
    }

    @Test
    public void testPropertiesFromUrlInvalidPrefix() {
        String url = "jdbc:unknown://https://localhost:9200/";

        URISyntaxException ex = assertThrows(URISyntaxException.class, () -> UrlParser.parseProperties(url));
        assertTrue(ex.getMessage().contains(UrlParser.URL_PREFIX));
    }

    @Test
    public void testPropertiesFromUrlInvalidScheme() {
        String url = "jdbc:elasticsearch://tcp://domain-name.sub-domain.com:9023";

        URISyntaxException ex = assertThrows(URISyntaxException.class, () -> UrlParser.parseProperties(url));
        assertTrue(ex.getMessage().contains("Invalid scheme:tcp"));
    }

    @Test
    public void testPropertiesFromUrlHttpsScheme() {
        String url = "jdbc:elasticsearch://https://domain-name.sub-domain.com:9023";

        propertiesFromUrl("jdbc:elasticsearch://https://domain-name.sub-domain.com:9023")
                .match(
                        KeyValuePairs.skvp(HostConnectionProperty.KEY, "domain-name.sub-domain.com"),
                        KeyValuePairs.skvp(PortConnectionProperty.KEY, "9023"),
                        KeyValuePairs.skvp(UseSSLConnectionProperty.KEY, "true"));
    }

    @Test
    public void testPropertiesFromUrlHttpsSchemeAndPath() {
        propertiesFromUrl("jdbc:elasticsearch://https://domain-name.sub-domain.com:9023/context/path")
                .match(
                        KeyValuePairs.skvp(HostConnectionProperty.KEY, "domain-name.sub-domain.com"),
                        KeyValuePairs.skvp(PortConnectionProperty.KEY, "9023"),
                        KeyValuePairs.skvp(UseSSLConnectionProperty.KEY, "true"),
                        KeyValuePairs.skvp(PathConnectionProperty.KEY, "/context/path"));
    }

    @Test
    public void testPropertiesFromUrlAndQueryString() {
        propertiesFromUrl("jdbc:elasticsearch://https://domain-name.sub-domain.com:9023/context/path?" +
                "user=username123&password=pass@$!w0rd")
                .match(
                        KeyValuePairs.skvp(HostConnectionProperty.KEY, "domain-name.sub-domain.com"),
                        KeyValuePairs.skvp(PortConnectionProperty.KEY, "9023"),
                        KeyValuePairs.skvp(UseSSLConnectionProperty.KEY, "true"),
                        KeyValuePairs.skvp(PathConnectionProperty.KEY, "/context/path"),
                        KeyValuePairs.skvp(UserConnectionProperty.KEY, "username123"),
                        KeyValuePairs.skvp(PasswordConnectionProperty.KEY, "pass@$!w0rd"));
    }

    @Test
    public void testPropertiesFromUrlWithInvalidQueryString() {
        final String url = "jdbc:elasticsearch://https://domain-name.sub-domain.com:9023/context/path?prop=value=3";

        URISyntaxException ex = assertThrows(URISyntaxException.class, () -> UrlParser.parseProperties(url));
        assertTrue(ex.getMessage().contains("Expected key=value pairs"));
    }

    private ConnectionPropertyMatcher propertiesFromUrl(String url) {
        Properties props = Assertions.assertDoesNotThrow(() -> UrlParser.parseProperties(url),
                () -> "Exception occurred when parsing URL: " + url);
        return new ConnectionPropertyMatcher(props);
    }

    private class ConnectionPropertyMatcher {
        Properties properties;

        public ConnectionPropertyMatcher(Properties props) {
            this.properties = props;
        }

        public void match(KeyValuePairs.StringKvp... keyValuePairs) {
            assertEquals(KeyValuePairs.toProperties(keyValuePairs), properties);
        }
    }
}
