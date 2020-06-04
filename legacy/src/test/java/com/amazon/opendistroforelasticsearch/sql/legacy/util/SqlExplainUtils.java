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

package com.amazon.opendistroforelasticsearch.sql.legacy.util;

import com.alibaba.druid.sql.parser.ParserException;
import com.amazon.opendistroforelasticsearch.sql.legacy.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.ESActionFactory;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.QueryAction;
import org.elasticsearch.client.Client;
import org.mockito.Mockito;

import java.sql.SQLFeatureNotSupportedException;

/**
 * Test utils class that explains a query
 */
public class SqlExplainUtils {

    public static String explain(String query) {
        try {
            Client mockClient = Mockito.mock(Client.class);
            CheckScriptContents.stubMockClient(mockClient);
            QueryAction queryAction = ESActionFactory.create(mockClient, query);

            return queryAction.explain().explain();
        } catch (SqlParseException | SQLFeatureNotSupportedException e) {
            throw new ParserException("Illegal sql expr in: " + query);
        }
    }

    private SqlExplainUtils() {}
}
