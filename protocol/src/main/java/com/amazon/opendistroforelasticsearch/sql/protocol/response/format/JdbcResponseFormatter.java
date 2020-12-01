/*
 *    Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License").
 *    You may not use this file except in compliance with the License.
 *    A copy of the License is located at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    or in the "license" file accompanying this file. This file is distributed
 *    on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *    express or implied. See the License for the specific language governing
 *    permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.sql.protocol.response.format;

import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.ARRAY;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRUCT;
import static com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.type.ElasticsearchDataType.ES_TEXT;
import static com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.type.ElasticsearchDataType.ES_TEXT_KEYWORD;

import com.amazon.opendistroforelasticsearch.sql.common.antlr.SyntaxCheckException;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprType;
import com.amazon.opendistroforelasticsearch.sql.exception.QueryEngineException;
import com.amazon.opendistroforelasticsearch.sql.executor.ExecutionEngine.Schema;
import com.amazon.opendistroforelasticsearch.sql.protocol.response.QueryResult;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Singular;

/**
 * JDBC formatter that formats both normal or error response exactly same way as legacy code to
 * avoid impact on client side. The only difference is a new "version" that indicates the response
 * was produced by new query engine.
 */
public class JdbcResponseFormatter extends JsonResponseFormatter<QueryResult> {

  public JdbcResponseFormatter(Style style) {
    super(style);
  }

  @Override
  protected Object buildJsonObject(QueryResult response) {
    JdbcResponse.JdbcResponseBuilder json = JdbcResponse.builder();

    // Fetch schema and data rows
    response.getSchema().getColumns().forEach(col -> json.column(fetchColumn(col)));
    json.datarows(fetchDataRows(response));

    // Populate other fields
    json.total(response.size())
        .size(response.size())
        .status(200);

    return json.build();
  }

  @Override
  public String format(Throwable t) {
    Error error = new Error(
        t.getClass().getSimpleName(),
        t.getMessage(),
        t.getMessage());
    return jsonify(new JdbcErrorResponse(error, getStatus(t)));
  }

  private Column fetchColumn(Schema.Column col) {
    return new Column(col.getName(), col.getAlias(), convertToLegacyType(col.getExprType()));
  }

  /**
   * Convert type that exists in both legacy and new engine but has different name.
   * Return old type name to avoid breaking impact on client-side.
   */
  private String convertToLegacyType(ExprType type) {
    if (type == ES_TEXT || type == ES_TEXT_KEYWORD) {
      return "text";
    } else if (type == STRING) {
      return "keyword";
    } else if (type == STRUCT) {
      return "object";
    } else if (type == ARRAY) {
      return "nested";
    } else {
      return type.typeName().toLowerCase();
    }
  }

  private Object[][] fetchDataRows(QueryResult response) {
    Object[][] rows = new Object[response.size()][];
    int i = 0;
    for (Object[] values : response) {
      rows[i++] = values;
    }
    return rows;
  }

  private int getStatus(Throwable t) {
    return (t instanceof SyntaxCheckException
        || t instanceof QueryEngineException) ? 400 : 503;
  }

  /**
   * org.json requires these inner data classes be public (and static)
   */
  @Builder
  @Getter
  public static class JdbcResponse {
    @Singular("column")
    private final List<Column> schema;
    private final Object[][] datarows;
    private final long total;
    private final long size;
    private final int status;
  }

  @RequiredArgsConstructor
  @Getter
  public static class Column {
    private final String name;
    private final String alias;
    private final String type;
  }

  @RequiredArgsConstructor
  @Getter
  public static class JdbcErrorResponse {
    private final Error error;
    private final int status;
  }

  @RequiredArgsConstructor
  @Getter
  public static class Error {
    private final String type;
    private final String reason;
    private final String details;
  }

}
