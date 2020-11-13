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

import com.amazon.opendistroforelasticsearch.sql.data.type.ExprType;
import com.amazon.opendistroforelasticsearch.sql.protocol.response.QueryResult;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Singular;

/**
 * JDBC formatter that formats response exactly same way as legacy code
 * so as to avoid impact on client side.
 * The major differences include:
 *  1. Version
 *  2. Alias in schema
 *  3. Convert type name in schema
 *  3. Status
 */
public class JdbcResponseFormatter extends JsonResponseFormatter<QueryResult> {

  @Getter
  @RequiredArgsConstructor
  private enum Version {
    V_2_0("v2.0");

    private final String name;
  }

  public JdbcResponseFormatter(Style style) {
    super(style);
  }

  @Override
  protected Object buildJsonObject(QueryResult response) {
    JdbcResponse.JdbcResponseBuilder json = JdbcResponse.builder();

    json.version(Version.V_2_0.getName())
        .total(response.size())
        .size(response.size())
        .status(200);

    response.getSchema().getColumns().forEach(col ->
        json.column(
            new Column(
                col.getName(),
                col.getAlias(),
                convertType(col.getExprType()))));

    json.datarows(fetchDataRows(response));
    return json.build();
  }

  private String convertType(ExprType type) {
    String typeName = type.typeName();
    if ("string".equalsIgnoreCase(typeName)) {
      return "text";
    }
    return typeName;
  }

  private Object[][] fetchDataRows(QueryResult response) {
    Object[][] rows = new Object[response.size()][];
    int i = 0;
    for (Object[] values : response) {
      rows[i++] = values;
    }
    return rows;
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

    private String version;
    private long total;
    private long size;
    private int status;
  }

  @RequiredArgsConstructor
  @Getter
  public static class Column {
    private final String name;
    private final String alias;
    private final String type;
  }

}
