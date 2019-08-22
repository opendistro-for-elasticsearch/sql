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

package com.amazon.opendistroforelasticsearch.sql.unittest;

import com.alibaba.druid.sql.parser.ParserException;
import com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants;
import com.amazon.opendistroforelasticsearch.sql.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.query.ESActionFactory;
import com.amazon.opendistroforelasticsearch.sql.query.QueryAction;
import com.amazon.opendistroforelasticsearch.sql.util.CheckScriptContents;
import com.google.common.io.Files;
import org.elasticsearch.client.Client;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLFeatureNotSupportedException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


public class WhereWithBoolConditionTest {

    private static final String SELECT_ALL = "SELECT * ";
    private static final String FROM_BANK = "FROM " + TestsConstants.TEST_INDEX_BANK;
    private static final String WHERE_CONDITION_TRUE = "WHERE male = true";     // 4 hits
    private static final String WHERE_CONDITION_FALSE = "WHERE male = false";   // 3 hits

    @Test
    public void whereWithBoolCompilationTest() throws SQLFeatureNotSupportedException, SqlParseException {
       query(FROM_BANK, WHERE_CONDITION_FALSE);
    }

    @Test
    public void selectAllTest() throws SQLFeatureNotSupportedException, SqlParseException, IOException {
        String expectedOutput = Files.toString(
                new File(getResourcePath() + "src/test/resources/expectedOutput/select_where_true.json"), StandardCharsets.UTF_8)
                .replaceAll("\r", "");

        assertThat(removeSpaces(query(FROM_BANK, WHERE_CONDITION_TRUE)), equalTo(removeSpaces(expectedOutput)));
    }

    private String query(String from, String... statements) throws SQLFeatureNotSupportedException, SqlParseException {
        return explain(SELECT_ALL + from + " " + String.join(" ", statements));
    }

    private String explain(String sql) throws ParserException, SqlParseException, SQLFeatureNotSupportedException {
        Client mockClient = Mockito.mock(Client.class);
        CheckScriptContents.stubMockClient(mockClient);
        QueryAction queryAction = ESActionFactory.create(mockClient, sql);
        return queryAction.explain().explain();
    }

    private String removeSpaces(String s) {
        return s.replaceAll("\\s+", "");
    }

    private String getResourcePath() {
        String projectRoot = System.getProperty("project.root");
        if ( projectRoot!= null && projectRoot.trim().length() > 0) {
            return projectRoot.trim() + "/";
        } else {
            return "";
        }
    }
}
