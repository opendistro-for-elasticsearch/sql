/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.legacy.unittest;

import com.amazon.opendistroforelasticsearch.sql.legacy.esdomain.LocalClusterState;
import com.amazon.opendistroforelasticsearch.sql.legacy.executor.Format;
import com.amazon.opendistroforelasticsearch.sql.legacy.plugin.SqlSettings;
import com.amazon.opendistroforelasticsearch.sql.legacy.request.SqlRequestParam;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.amazon.opendistroforelasticsearch.sql.legacy.request.SqlRequestParam.QUERY_PARAMS_FORMAT;
import static com.amazon.opendistroforelasticsearch.sql.legacy.request.SqlRequestParam.QUERY_PARAMS_PRETTY;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class SqlRequestParamTest {
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Before
    public void setup() {
        SqlSettings settings = spy(new SqlSettings());
        // Force return empty list to avoid ClusterSettings be invoked which is a final class and hard to mock.
        // In this case, default value in Setting will be returned all the time.
        doReturn(emptyList()).when(settings).getSettings();
        LocalClusterState.state().setSqlSettings(settings);
    }

    @Test
    public void shouldReturnTrueIfPrettyParamsIsTrue() {
        assertTrue(SqlRequestParam.isPrettyFormat(ImmutableMap.of(QUERY_PARAMS_PRETTY, "true")));
    }

    @Test
    public void shouldReturnTrueIfPrettyParamsIsEmpty() {
        assertTrue(SqlRequestParam.isPrettyFormat(ImmutableMap.of(QUERY_PARAMS_PRETTY, "")));
    }

    @Test
    public void shouldReturnFalseIfNoPrettyParams() {
        assertFalse(SqlRequestParam.isPrettyFormat(ImmutableMap.of()));
    }

    @Test
    public void shouldReturnFalseIfPrettyParamsIsUnknownValue() {
        assertFalse(SqlRequestParam.isPrettyFormat(ImmutableMap.of(QUERY_PARAMS_PRETTY, "unknown")));
    }

    @Test
    public void shouldReturnJSONIfFormatParamsIsJSON() {
        assertEquals(Format.JSON, SqlRequestParam.getFormat(ImmutableMap.of(QUERY_PARAMS_FORMAT, "json")));
    }

    @Test
    public void shouldReturnDefaultFormatIfNoFormatParams() {
        assertEquals(Format.JDBC, SqlRequestParam.getFormat(ImmutableMap.of()));
    }

    @Test
    public void shouldThrowExceptionIfFormatParamsIsEmpty() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("Failed to create executor due to unknown response format: ");

        assertEquals(Format.JDBC, SqlRequestParam.getFormat(ImmutableMap.of(QUERY_PARAMS_FORMAT, "")));
    }

    @Test
    public void shouldThrowExceptionIfFormatParamsIsNotSupported() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("Failed to create executor due to unknown response format: xml");

        SqlRequestParam.getFormat(ImmutableMap.of(QUERY_PARAMS_FORMAT, "xml"));
    }
}