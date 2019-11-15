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

package com.amazon.opendistroforelasticsearch.sql.doctest.core.response;

import com.amazon.opendistroforelasticsearch.sql.doctest.core.markup.DataTable;
import com.amazon.opendistroforelasticsearch.sql.utils.JsonPrettyFormatter;
import com.amazon.opendistroforelasticsearch.sql.utils.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Response formatter
 */
public enum ResponseFormat {

    NO_RESPONSE {
        @Override
        public String format(SqlResponse str) {
            throw new UnsupportedOperationException();
        }
    },
    ORIGINAL_RESPONSE {
        @Override
        public String format(SqlResponse sqlResponse) {
            return sqlResponse.body();
        }
    },
    PRETTY_JSON_RESPONSE {
        @Override
        public String format(SqlResponse sqlResponse) {
            String body = sqlResponse.body();
            try {
                return JsonPrettyFormatter.format(body);
            } catch (IOException e) {
                throw new IllegalStateException(
                    StringUtils.format("Failed to pretty format response: %s", body), e);
            }
        }
    },
    TABLE_RESPONSE {
        @Override
        public String format(SqlResponse sqlResponse) {
            JSONObject body = new JSONObject(sqlResponse.body());
            if (body.isNull("schema")) {
                throw new IllegalStateException(
                    "Only JDBC response can be formatted to table: " + sqlResponse.body());
            }

            JSONArray schema = body.getJSONArray("schema");
            JSONArray rows = body.getJSONArray("datarows");

            Object[] header = new Object[schema.length()];
            for (int i = 0; i < header.length; i++) {
                JSONObject nameType = schema.getJSONObject(i);
                header[i] = StringUtils.format("%s (%s)", nameType.get("name"), nameType.get("type"));
            }

            DataTable table = new DataTable(header);
            for (Object row : rows) {
                table.addRow(((JSONArray) row).toList().toArray());
            }
            return table.toString();
        }
    };

    public abstract String format(SqlResponse str);

}
