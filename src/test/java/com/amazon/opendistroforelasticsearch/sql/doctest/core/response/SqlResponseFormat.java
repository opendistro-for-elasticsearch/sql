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

import com.amazon.opendistroforelasticsearch.sql.utils.JsonPrettyFormatter;
import com.amazon.opendistroforelasticsearch.sql.utils.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Different SQL response formats
 */
public enum SqlResponseFormat {

    IGNORE_RESPONSE {
        @Override
        public String format(SqlResponse sqlResponse) {
            return "";
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
            return format(sqlResponse, true);
        }
    },
    TABLE_UNSORTED_RESPONSE {
        @Override
        public String format(SqlResponse sqlResponse) {
            return format(sqlResponse, false);
        }
    };

    /**
     * Format SQL response to specific format for documentation
     * @param sqlResponse   sql response
     * @return              string in specific format
     */
    public abstract String format(SqlResponse sqlResponse);

    /**
     * Note that we put this format() here because it's shared by two format enums.
     * @param sqlResponse   original response from plugin
     * @param isSorted      true to sort the result or just leave it as is
     */
    protected String format(SqlResponse sqlResponse, boolean isSorted) {
        JSONObject body = new JSONObject(sqlResponse.body());
        if (body.isNull("schema")) {
            throw new IllegalStateException(
                "Only JDBC response can be formatted to table: " + sqlResponse.body());
        }

        Object[] header = parseHeader(body.getJSONArray("schema"));
        List<Object[]> rows = parseDataRows(body.getJSONArray("datarows"), isSorted);

        DataTable table = new DataTable(header);
        for (Object[] row : rows) {
            table.addRow(row);
        }
        return table.toString();
    }

    private Object[] parseHeader(JSONArray schema) {
        Object[] header = new Object[schema.length()];
        for (int i = 0; i < header.length; i++) {
            JSONObject nameType = schema.getJSONObject(i);
            header[i] = nameType.optString("alias", nameType.getString("name"));
        }
        return header;
    }

    private List<Object[]> parseDataRows(JSONArray rows, boolean isSorted) {
        List<Object[]> rowsToSort = new ArrayList<>();
        for (Object row : rows) {
            rowsToSort.add(((JSONArray) row).toList().toArray());
        }

        if (isSorted) {
            sort(rowsToSort);
        }
        return rowsToSort;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void sort(List<Object[]> lists) {
        lists.sort((list1, list2) -> {
            if (list1 == null || list2 == null) {
                return compareNullable(list1, list2);
            }

            // Assume 2 lists are of same length and all elements are comparable
            for (int i = 0; i < list1.length; i++) {
                Comparable obj1 = (Comparable) list1[i];
                Comparable obj2 = (Comparable) list2[i];

                if (obj1 == null || obj2 == null) {
                    return compareNullable(obj1, obj2);
                }

                int result = obj1.compareTo(obj2);
                if (result != 0) {
                    return result;
                }
            }
            return 0;
        });
    }

    /** Put NULL first (as smaller element) */
    private static int compareNullable(Object obj1, Object obj2) {
        if (obj1 == null && obj2 == null) {
            return 0;
        } else if (obj1 == null) {
            return -1;
        } else { // obj2 == null
            return 1;
        }
    }

}
