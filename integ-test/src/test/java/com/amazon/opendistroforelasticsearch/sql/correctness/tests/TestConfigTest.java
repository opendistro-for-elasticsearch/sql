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

package com.amazon.opendistroforelasticsearch.sql.correctness.tests;

import com.amazon.opendistroforelasticsearch.sql.correctness.TestConfig;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link TestConfig}
 */
public class TestConfigTest {

    @Test
    public void testDefaultConfig() {
        TestConfig config = new TestConfig(emptyMap());
        assertThat(config.getESHostUrl(), is(emptyString()));
        assertThat(
            config.getOtherDbConnectionNameAndUrls(),
            allOf(
                hasEntry("H2", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"),
                hasEntry("SQLite", "jdbc:sqlite::memory:")
            )
        );
    }

    @Test
    public void testCustomESUrls() {
        Map<String, String> args = ImmutableMap.of("esHost", "localhost:9200");
        TestConfig config = new TestConfig(args);
        assertThat(config.getESHostUrl(), is("localhost:9200"));
    }

    @Test
    public void testCustomDbUrls() {
        Map<String, String> args = ImmutableMap.of("otherDbUrls",
                                                   "H2=jdbc:h2:mem:test;DB_CLOSE_DELAY=-1,"
                                                   + "Derby=jdbc:derby:memory:myDb;create=true");

        TestConfig config = new TestConfig(args);
        assertThat(
            config.getOtherDbConnectionNameAndUrls(),
            allOf(
                hasEntry("H2", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"),
                hasEntry("Derby", "jdbc:derby:memory:myDb;create=true")
            )
        );
    }

}
