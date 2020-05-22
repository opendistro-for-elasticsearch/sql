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

import com.amazon.opendistroforelasticsearch.sql.protocol.response.QueryResult;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Singular;

import java.util.List;

/**
 * JSON response format with schema header and data rows. For example,
 *
 * <pre>
 *  {
 *      "schema": [
 *          {
 *              "name": "name",
 *              "type": "string"
 *          }
 *      ],
 *      "datarows": [
 *          ["John"],
 *          ["Smith"]
 *      ],
 *      "total": 2,
 *      "size": 2
 *  }
 * </pre>
 */
public class SimpleJsonResponseFormatter extends JsonResponseFormatter<QueryResult> {

    public SimpleJsonResponseFormatter(Style style) {
        super(style);
    }

    @Override
    public Object buildJsonObject(QueryResult response) {
        JsonResponse.JsonResponseBuilder json = JsonResponse.builder();

        json.total(response.size()).
             size(response.size());

        response.columnNameTypes().forEach((name, type) -> json.column(new Column(name, type)));

        for (Object[] values : response) {
            json.row(new DataRow(values));
        }
        return json.build();
    }

    /**
     * org.json requires these inner data classes be public (and static)
     */
    @Builder
    @Getter
    public static class JsonResponse {
        @Singular("column")
        private final List<Column> schema;

        @Singular("row")
        private final List<DataRow> datarows;

        private long total;
        private long size;
    }

    @RequiredArgsConstructor
    @Getter
    public static class Column {
        private final String name;
        private final String type;
    }

    @RequiredArgsConstructor
    @Getter
    public static class DataRow {
        private final Object[] row;
    }

}
