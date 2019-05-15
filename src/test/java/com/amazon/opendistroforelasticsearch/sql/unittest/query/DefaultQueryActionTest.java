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

package com.amazon.opendistroforelasticsearch.sql.unittest.query;

import com.amazon.opendistroforelasticsearch.sql.domain.Field;
import com.amazon.opendistroforelasticsearch.sql.domain.KVValue;
import com.amazon.opendistroforelasticsearch.sql.domain.MethodField;
import com.amazon.opendistroforelasticsearch.sql.domain.Select;
import com.amazon.opendistroforelasticsearch.sql.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.query.DefaultQueryAction;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.script.Script;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

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
