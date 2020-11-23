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

import com.amazon.opendistroforelasticsearch.sql.common.antlr.SyntaxCheckException;
import com.amazon.opendistroforelasticsearch.sql.common.utils.StringUtils;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprType;
import com.amazon.opendistroforelasticsearch.sql.exception.QueryEngineException;
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

    json.total(response.size())
        .size(response.size())
        .status(200);

    response.getSchema().getColumns().forEach(col ->
        json.column(
            new Column(
                col.getName(),
                col.getAlias(),
                convertToLegacyType(col.getExprType()))));

    json.datarows(fetchDataRows(response));
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

  private String convertToLegacyType(ExprType type) {
    String typeName = StringUtils.toLower(type.typeName());
    switch (typeName) {
      case "string":
        return "text";
      default:
        return typeName;
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
        || t instanceof QueryEngineException) ? 400 : 500;
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
