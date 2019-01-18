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

package com.amazon.opendistro.sql.util;

import com.alibaba.druid.sql.parser.ParserException;
import com.amazon.opendistro.sql.SearchDao;
import com.amazon.opendistro.sql.domain.QueryStatement;
import com.amazon.opendistro.sql.exception.SqlParseException;
import com.amazon.opendistro.sql.intgtest.MainTestSuite;
import com.amazon.opendistro.sql.query.QueryAction;
import org.elasticsearch.client.Client;
import com.amazon.opendistro.sql.executor.format.DataRows;
import com.amazon.opendistro.sql.executor.format.Protocol;
import com.amazon.opendistro.sql.executor.QueryActionElasticExecutor;
import com.amazon.opendistro.sql.executor.format.Schema;
import com.amazon.opendistro.sql.executor.format.Schema.Column;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertTrue;

public class CheckPrettyFormatContents {

    public static JSONObject getJdbcResponse(String query) {
        Protocol protocol = execute(query, "jdbc");
        return new JSONObject(protocol.format());
    }

    public static JSONArray getJdbcDataRows(JSONObject jdbcResponse) {
        return jdbcResponse.getJSONArray("datarows");
    }

    public static Schema getSchema(Protocol protocol) {
        return protocol.getResultSet().getSchema();
    }

    public static DataRows getDataRows(Protocol protocol) {
        return protocol.getResultSet().getDataRows();
    }

    public static void containsColumnsInAnyOrder(Schema schema, Set<String> fields) {
        Set<String> columnNames = new HashSet<>();
        for (Column column : schema) {
            columnNames.add(column.getName());
        }

        assertThat(columnNames, equalTo(fields));
    }

    public static void containsColumns(Schema schema, List<String> fields) {
        List<String> columnNames = new ArrayList<>();
        for (Column column : schema) {
            columnNames.add(column.getName());
        }

        assertThat(columnNames, equalTo(fields));
    }

    public static void containsAliases(Schema schema, Map<String, String> aliases) {
        for (Column column : schema) {
            assertThat(
                    column.getAlias(),
                    equalTo(
                            aliases.get(
                                    column.getName()
                            )
                    )
            );
        }
    }

    public static void containsData(DataRows dataRows, Collection<String> fields) {
        DataRows.Row row = dataRows.iterator().next();
        for (String field : fields) {
            assertTrue(row.hasField(field));
        }
    }

    private static QueryAction getQueryAction(String sql) {
        try {
            SearchDao searchDao = MainTestSuite.getSearchDao();
            return searchDao.explain(sql);
        } catch (SqlParseException | SQLFeatureNotSupportedException e) {
            throw new ParserException("Illegal sql expr in request: " + sql);
        }
    }

    public static Protocol execute(String sql, String format) {
        try {
            QueryAction queryAction = getQueryAction(sql);
            Client client = queryAction.getClient();
            QueryStatement queryStatement = queryAction.getQueryStatement();
            Object queryResult = QueryActionElasticExecutor.executeAnyAction(client, queryAction);

            return new Protocol(client, queryStatement, queryResult, format);
        }
        catch (IOException | SqlParseException e) {
            throw new ParserException("Unsupported query: " + sql);
        }
    }
}
