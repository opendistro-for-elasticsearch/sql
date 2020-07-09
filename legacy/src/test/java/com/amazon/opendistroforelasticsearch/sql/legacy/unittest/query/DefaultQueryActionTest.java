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

package com.amazon.opendistroforelasticsearch.sql.legacy.unittest.query;

import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Field;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.KVValue;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.MethodField;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Select;
import com.amazon.opendistroforelasticsearch.sql.legacy.esdomain.LocalClusterState;
import com.amazon.opendistroforelasticsearch.sql.legacy.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.legacy.executor.Format;
import com.amazon.opendistroforelasticsearch.sql.legacy.metrics.Metrics;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.DefaultQueryAction;
import com.amazon.opendistroforelasticsearch.sql.legacy.request.SqlRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.script.Script;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static com.amazon.opendistroforelasticsearch.sql.legacy.plugin.SqlSettings.CURSOR_ENABLED;
import static com.amazon.opendistroforelasticsearch.sql.legacy.plugin.SqlSettings.CURSOR_FETCH_SIZE;
import static com.amazon.opendistroforelasticsearch.sql.legacy.plugin.SqlSettings.CURSOR_KEEPALIVE;
import static com.amazon.opendistroforelasticsearch.sql.legacy.plugin.SqlSettings.METRICS_ROLLING_WINDOW;
import static com.amazon.opendistroforelasticsearch.sql.legacy.plugin.SqlSettings.METRICS_ROLLING_INTERVAL;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

public class DefaultQueryActionTest {

    private DefaultQueryAction queryAction;

    private Client mockClient;

    private Select mockSelect;

    private SearchRequestBuilder mockRequestBuilder;

    @Before
    public void initDefaultQueryAction() {

        mockClient = mock(Client.class);
        mockSelect = mock(Select.class);
        mockRequestBuilder = mock(SearchRequestBuilder.class);

        List<Field> fields = new LinkedList<>();
        fields.add(new Field("balance", "bbb"));

        doReturn(fields).when(mockSelect).getFields();
        doReturn(null).when(mockRequestBuilder).setFetchSource(any(String[].class), any(String[].class));
        doReturn(null).when(mockRequestBuilder).addScriptField(anyString(), any(Script.class));

        queryAction = new DefaultQueryAction(mockClient, mockSelect);
        queryAction.initialize(mockRequestBuilder);
    }

    @After
    public void cleanup() {
        LocalClusterState.state(null);
    }

    @Test
    public void scriptFieldWithTwoParams() throws SqlParseException {

        List<Field> fields = new LinkedList<>();
        fields.add(createScriptField("script1", "doc['balance'] * 2",
                false, true, false));

        queryAction.setFields(fields);

        final Optional<List<String>> fieldNames = queryAction.getFieldNames();
        Assert.assertTrue("Field names have not been set", fieldNames.isPresent());
        Assert.assertThat(fieldNames.get().size(), equalTo(1));
        Assert.assertThat(fieldNames.get().get(0), equalTo("script1"));

        Mockito.verify(mockRequestBuilder).addScriptField(eq("script1"), any(Script.class));
    }

    @Test
    public void scriptFieldWithThreeParams() throws SqlParseException {

        List<Field> fields = new LinkedList<>();
        fields.add(createScriptField("script1", "doc['balance'] * 2",
                true, true, false));

        queryAction.setFields(fields);

        final Optional<List<String>> fieldNames = queryAction.getFieldNames();
        Assert.assertTrue("Field names have not been set", fieldNames.isPresent());
        Assert.assertThat(fieldNames.get().size(), equalTo(1));
        Assert.assertThat(fieldNames.get().get(0), equalTo("script1"));

        Mockito.verify(mockRequestBuilder).addScriptField(eq("script1"), any(Script.class));
    }

    @Test(expected = SqlParseException.class)
    public void scriptFieldWithLessThanTwoParams() throws SqlParseException {

        List<Field> fields = new LinkedList<>();
        fields.add(createScriptField("script1", "doc['balance'] * 2",
                false, false, false));

        queryAction.setFields(fields);
    }

    @Test
    public void scriptFieldWithMoreThanThreeParams() throws SqlParseException {

        List<Field> fields = new LinkedList<>();
        fields.add(createScriptField("script1", "doc['balance'] * 2",
                false, true, true));

        queryAction.setFields(fields);
    }

    @Test
    public void testIfScrollShouldBeOpenWithDifferentFormats() {
        int settingFetchSize = 500;
        TimeValue timeValue = new TimeValue(120000);
        int limit = 2300;
        mockLocalClusterStateAndInitializeMetrics(true, settingFetchSize, timeValue);

        doReturn(limit).when(mockSelect).getRowCount();
        doReturn(mockRequestBuilder).when(mockRequestBuilder).setSize(settingFetchSize);
        SqlRequest mockSqlRequest = mock(SqlRequest.class);
        doReturn(settingFetchSize).when(mockSqlRequest).fetchSize();
        queryAction.setSqlRequest(mockSqlRequest);

        Format[] formats = new Format[] {Format.CSV, Format.RAW, Format.JSON, Format.TABLE};
        for (Format format : formats) {
            queryAction.setFormat(format);
            queryAction.checkAndSetScroll();
        }

        Mockito.verify(mockRequestBuilder, times(4)).setSize(limit);
        Mockito.verify(mockRequestBuilder, never()).setScroll(any(TimeValue.class));

        queryAction.setFormat(Format.JDBC);
        queryAction.checkAndSetScroll();
        Mockito.verify(mockRequestBuilder).setSize(settingFetchSize);
        Mockito.verify(mockRequestBuilder).setScroll(timeValue);

    }

    @Test
    public void testIfScrollShouldBeOpenWithCursorEnabled() {
        int settingFetchSize = 500;
        TimeValue timeValue = new TimeValue(120000);
        int limit = 2300;

        doReturn(limit).when(mockSelect).getRowCount();
        doReturn(mockRequestBuilder).when(mockRequestBuilder).setSize(settingFetchSize);
        SqlRequest mockSqlRequest = mock(SqlRequest.class);
        doReturn(settingFetchSize).when(mockSqlRequest).fetchSize();
        queryAction.setSqlRequest(mockSqlRequest);
        queryAction.setFormat(Format.JDBC);

        mockLocalClusterStateAndInitializeMetrics(false, settingFetchSize, timeValue);
        queryAction.checkAndSetScroll();
        Mockito.verify(mockRequestBuilder).setSize(limit);
        Mockito.verify(mockRequestBuilder, never()).setScroll(any(TimeValue.class));

        mockLocalClusterStateAndInitializeMetrics(true, settingFetchSize, timeValue);
        queryAction.checkAndSetScroll();
        Mockito.verify(mockRequestBuilder).setSize(settingFetchSize);
        Mockito.verify(mockRequestBuilder).setScroll(timeValue);

    }

    @Test
    public void testIfScrollShouldBeOpenWithDifferentFetchSize() {
        int fetchSize = 500;
        TimeValue timeValue = new TimeValue(120000);
        int limit = 2300;
        mockLocalClusterStateAndInitializeMetrics(true, fetchSize, timeValue);

        doReturn(limit).when(mockSelect).getRowCount();
        SqlRequest mockSqlRequest = mock(SqlRequest.class);
        queryAction.setSqlRequest(mockSqlRequest);
        queryAction.setFormat(Format.JDBC);

        int[] fetchSizes = new int[] {0, -10};
        for (int fetch : fetchSizes) {
            doReturn(fetch).when(mockSqlRequest).fetchSize();
            queryAction.checkAndSetScroll();
        }
        Mockito.verify(mockRequestBuilder, times(2)).setSize(limit);
        Mockito.verify(mockRequestBuilder, never()).setScroll(timeValue);

        int userFetchSize = 20;
        doReturn(userFetchSize).when(mockSqlRequest).fetchSize();
        doReturn(mockRequestBuilder).when(mockRequestBuilder).setSize(userFetchSize);
        queryAction.checkAndSetScroll();
        Mockito.verify(mockRequestBuilder).setSize(20);
        Mockito.verify(mockRequestBuilder).setScroll(timeValue);
    }


    @Test
    public void testIfScrollShouldBeOpenWithDifferentValidFetchSizeAndLimit() {
        int fetchSize = 1000;
        TimeValue timeValue = new TimeValue(120000);
        mockLocalClusterStateAndInitializeMetrics(true, fetchSize, timeValue);

        int limit = 2300;
        doReturn(limit).when(mockSelect).getRowCount();
        SqlRequest mockSqlRequest = mock(SqlRequest.class);

        /** fetchSize <= LIMIT - open scroll*/
        int userFetchSize = 1500;
        doReturn(userFetchSize).when(mockSqlRequest).fetchSize();
        doReturn(mockRequestBuilder).when(mockRequestBuilder).setSize(userFetchSize);
        queryAction.setSqlRequest(mockSqlRequest);
        queryAction.setFormat(Format.JDBC);

        queryAction.checkAndSetScroll();
        Mockito.verify(mockRequestBuilder).setSize(userFetchSize);
        Mockito.verify(mockRequestBuilder).setScroll(timeValue);

        /** fetchSize > LIMIT - no scroll */
        userFetchSize = 5000;
        doReturn(userFetchSize).when(mockSqlRequest).fetchSize();
        mockRequestBuilder = mock(SearchRequestBuilder.class);
        queryAction.initialize(mockRequestBuilder);
        queryAction.checkAndSetScroll();
        Mockito.verify(mockRequestBuilder).setSize(limit);
        Mockito.verify(mockRequestBuilder, never()).setScroll(timeValue);
    }

    private void mockLocalClusterStateAndInitializeMetrics(boolean cursorEnabled, Integer fetchSize, TimeValue time) {
        LocalClusterState mockLocalClusterState = mock(LocalClusterState.class);
        LocalClusterState.state(mockLocalClusterState);
        doReturn(cursorEnabled).when(mockLocalClusterState).getSettingValue(CURSOR_ENABLED);
        doReturn(fetchSize).when(mockLocalClusterState).getSettingValue(CURSOR_FETCH_SIZE);
        doReturn(time).when(mockLocalClusterState).getSettingValue(CURSOR_KEEPALIVE);
        doReturn(3600L).when(mockLocalClusterState).getSettingValue(METRICS_ROLLING_WINDOW);
        doReturn(2L).when(mockLocalClusterState).getSettingValue(METRICS_ROLLING_INTERVAL);

        Metrics.getInstance().registerDefaultMetrics();

    }

    private Field createScriptField(final String name, final String script, final boolean addScriptLanguage,
                                    final boolean addScriptParam, final boolean addRedundantParam) {

        final List<KVValue> params = new ArrayList<>();

        params.add(new KVValue("alias", name));
        if (addScriptLanguage) {
            params.add(new KVValue("painless"));
        }
        if (addScriptParam) {
            params.add(new KVValue(script));
        }
        if (addRedundantParam) {
            params.add(new KVValue("Fail the test"));
        }

        return new MethodField("script", params, null, null);
    }
}
