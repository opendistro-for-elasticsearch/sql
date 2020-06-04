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

package com.amazon.opendistroforelasticsearch.sql.legacy.cursor;

import com.amazon.opendistroforelasticsearch.sql.legacy.executor.format.Schema;
import com.google.common.base.Strings;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * Minimum metdata that will be serialized for generating cursorId for
 * SELECT .... FROM .. ORDER BY .... queries
 */
@Getter
@Setter
@NoArgsConstructor
public class DefaultCursor implements Cursor {

    /** Make sure all keys are unique to prevent overriding
     * and as small as possible to make cursor compact
     */
    private static final String FETCH_SIZE = "f";
    private static final String ROWS_LEFT = "l";
    private static final String INDEX_PATTERN = "i";
    private static final String SCROLL_ID = "s";
    private static final String SCHEMA_COLUMNS = "c";
    private static final String FIELD_ALIAS_MAP = "a";

    /** To get mappings for index to check if type is date needed for
     * @see com.amazon.opendistroforelasticsearch.sql.legacy.executor.format.DateFieldFormatter */
    @NonNull
    private String indexPattern;

    /** List of Schema.Column for maintaining field order and generating null values of missing fields */
    @NonNull
    private List<Schema.Column> columns;

    /** To delegate to correct cursor handler to get next page*/
    private final CursorType type = CursorType.DEFAULT;

    /**
     * Truncate the @see DataRows to respect LIMIT clause and/or to identify last page to close scroll context.
     * docsLeft is decremented by fetch_size for call to get page of result.
     */
    private long rowsLeft;

    /** @see com.amazon.opendistroforelasticsearch.sql.legacy.executor.format.SelectResultSet */
    @NonNull
    private Map<String, String> fieldAliasMap;

    /** To get next batch of result */
    private String scrollId;

    /** To reduce the number of rows left by fetchSize */
    @NonNull
    private Integer fetchSize;

    private Integer limit;

    @Override
    public CursorType getType() {
        return type;
    }

    @Override
    public String generateCursorId() {
        if (rowsLeft <=0 || Strings.isNullOrEmpty(scrollId)) {
            return null;
        }
        JSONObject json = new JSONObject();
        json.put(FETCH_SIZE, fetchSize);
        json.put(ROWS_LEFT, rowsLeft);
        json.put(INDEX_PATTERN, indexPattern);
        json.put(SCROLL_ID, scrollId);
        json.put(SCHEMA_COLUMNS, getSchemaAsJson());
        json.put(FIELD_ALIAS_MAP, fieldAliasMap);
        return String.format("%s:%s", type.getId(), encodeCursor(json));
    }

    public static DefaultCursor from(String cursorId) {
        /**
         * It is assumed that cursorId here is the second part of the original cursor passed
         * by the client after removing first part which identifies cursor type
         */
         JSONObject json = decodeCursor(cursorId);
         DefaultCursor cursor = new DefaultCursor();
         cursor.setFetchSize(json.getInt(FETCH_SIZE));
         cursor.setRowsLeft(json.getLong(ROWS_LEFT));
         cursor.setIndexPattern(json.getString(INDEX_PATTERN));
         cursor.setScrollId(json.getString(SCROLL_ID));
         cursor.setColumns(getColumnsFromSchema(json.getJSONArray(SCHEMA_COLUMNS)));
         cursor.setFieldAliasMap(fieldAliasMap(json.getJSONObject(FIELD_ALIAS_MAP)));

         return cursor;
    }

    private JSONArray getSchemaAsJson() {
        JSONArray schemaJson = new JSONArray();

        for (Schema.Column column : columns) {
            schemaJson.put(schemaEntry(column.getName(), column.getAlias(), column.getType()));
        }

        return schemaJson;
    }

    private JSONObject schemaEntry(String name, String alias, String type) {
        JSONObject entry = new JSONObject();
        entry.put("name", name);
        if (alias != null) {
            entry.put("alias", alias);
        }
        entry.put("type", type);
        return entry;
    }

    private static String encodeCursor(JSONObject cursorJson) {
        return Base64.getEncoder().encodeToString(cursorJson.toString().getBytes());
    }

    private static JSONObject decodeCursor(String cursorId) {
        return new JSONObject(new String(Base64.getDecoder().decode(cursorId)));
    }

    private static Map<String, String> fieldAliasMap(JSONObject json) {
        Map<String, String> fieldToAliasMap = new HashMap<>();
        json.keySet().forEach(key -> fieldToAliasMap.put(key, json.get(key).toString()));
        return fieldToAliasMap;
    }

    private static List<Schema.Column> getColumnsFromSchema(JSONArray schema) {
        List<Schema.Column> columns = IntStream.
                range(0, schema.length()).
                mapToObj(i -> {
                            JSONObject jsonColumn = schema.getJSONObject(i);
                            return new Schema.Column(
                                    jsonColumn.getString("name"),
                                    jsonColumn.optString("alias", null),
                                    Schema.Type.valueOf(jsonColumn.getString("type").toUpperCase())
                            );
                        }
                ).collect(Collectors.toList());
        return columns;
    }
}
