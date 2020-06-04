/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.legacy.domain;

import com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.Type;
import com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType;
import com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.special.Product;
import com.amazon.opendistroforelasticsearch.sql.legacy.executor.format.Schema;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The definition of column type provider
 */
public class ColumnTypeProvider {
    private final List<Schema.Type> typeList;

    private static final Map<ESDataType, Schema.Type> TYPE_MAP =
            new ImmutableMap.Builder<ESDataType, Schema.Type>()
                    .put(ESDataType.SHORT, Schema.Type.SHORT)
                    .put(ESDataType.LONG, Schema.Type.LONG)
                    .put(ESDataType.INTEGER, Schema.Type.INTEGER)
                    .put(ESDataType.FLOAT, Schema.Type.FLOAT)
                    .put(ESDataType.DOUBLE, Schema.Type.DOUBLE)
                    .put(ESDataType.KEYWORD, Schema.Type.KEYWORD)
                    .put(ESDataType.TEXT, Schema.Type.TEXT)
                    .put(ESDataType.STRING, Schema.Type.TEXT)
                    .put(ESDataType.DATE, Schema.Type.DATE)
                    .put(ESDataType.BOOLEAN, Schema.Type.BOOLEAN)
                    .put(ESDataType.UNKNOWN, Schema.Type.DOUBLE)
                    .build();
    public static final Schema.Type COLUMN_DEFAULT_TYPE = Schema.Type.DOUBLE;

    public ColumnTypeProvider(Type type) {
        this.typeList = convertOutputColumnType(type);
    }

    public ColumnTypeProvider() {
        this.typeList = new ArrayList<>();
    }

    /**
     * Get the type of column by index.
     *
     * @param index column index.
     * @return column type.
     */
    public Schema.Type get(int index) {
        if (typeList.isEmpty()) {
            return COLUMN_DEFAULT_TYPE;
        } else {
            return typeList.get(index);
        }
    }

    private List<Schema.Type> convertOutputColumnType(Type type) {
        if (type instanceof Product) {
            List<Type> types = ((Product) type).getTypes();
            return types.stream().map(t -> convertType(t)).collect(Collectors.toList());
        } else if (type instanceof ESDataType) {
            return ImmutableList.of(convertType(type));
        } else {
            return ImmutableList.of(COLUMN_DEFAULT_TYPE);
        }
    }

    private Schema.Type convertType(Type type) {
        try {
            return TYPE_MAP.getOrDefault(type, COLUMN_DEFAULT_TYPE);
        } catch (Exception e) {
            return COLUMN_DEFAULT_TYPE;
        }
    }
}
