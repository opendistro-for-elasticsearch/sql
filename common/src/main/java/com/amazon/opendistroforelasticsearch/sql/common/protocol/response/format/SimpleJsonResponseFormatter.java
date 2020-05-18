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

package com.amazon.opendistroforelasticsearch.sql.common.protocol.response.format;

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
 *              "name": "name"
 *          }
 *      ],
 *      "datarows": [
 *          ["John"],
 *          ["Smith"]
 *      ]
 *  }
 * </pre>
 */
public class SimpleJsonResponseFormatter extends JsonResponseFormatter<List<Object>> {

    public SimpleJsonResponseFormatter(boolean isPretty) {
        super(isPretty);
    }

    @Override
    public Object buildJsonObject(List<Object> objects) {
        return JsonResponse.builder().
                            column(new Column("firstname")).
                            row(new DataRow(new Object[]{"John"})).
                            build();
    }

    @Override
    public Object buildJsonObject(Throwable t) {
        return new JsonError(t.getClass().getSimpleName(), t.getMessage());
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
    }

    @RequiredArgsConstructor
    @Getter
    public static class Column {
        private final String name;
    }

    @RequiredArgsConstructor
    @Getter
    public static class DataRow {
        private final Object[] row;
    }

    @RequiredArgsConstructor
    @Getter
    public static class JsonError {
        private final String type;
        private final String reason;
    }

}
