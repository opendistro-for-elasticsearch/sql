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

package com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base;

import com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.Type;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESIndex.IndexType.NESTED_FIELD;
import static com.amazon.opendistroforelasticsearch.sql.legacy.utils.StringUtils.toUpper;

/**
 * Base type hierarchy based on Elasticsearch data type
 */
public enum ESDataType implements BaseType {

    TYPE_ERROR,
    UNKNOWN,

    SHORT, LONG,
    INTEGER(SHORT, LONG),
    FLOAT(INTEGER),
    DOUBLE(FLOAT),
    NUMBER(DOUBLE),

    KEYWORD,
    TEXT(KEYWORD),
    STRING(TEXT),

    DATE_NANOS,
    DATE(DATE_NANOS, STRING),

    BOOLEAN,

    OBJECT, NESTED,
    COMPLEX(OBJECT, NESTED),

    GEO_POINT,

    ES_TYPE(
        NUMBER,
        //STRING, move to under DATE because DATE is compatible
        DATE,
        BOOLEAN,
        COMPLEX,
        GEO_POINT
    );


    /**
     * Java Enum's valueOf() may thrown "enum constant not found" exception.
     * And Java doesn't provide a contains method.
     * So this static map is necessary for check and efficiency.
     */
    private static final Map<String, ESDataType> ALL_BASE_TYPES;
    static {
        ImmutableMap.Builder<String, ESDataType> builder = new ImmutableMap.Builder<>();
        for (ESDataType type : ESDataType.values()) {
            builder.put(type.name(), type);
        }
        ALL_BASE_TYPES = builder.build();
    }

    public static ESDataType typeOf(String str) {
        return ALL_BASE_TYPES.getOrDefault(toUpper(str), UNKNOWN);
    }

    /** Parent of current base type */
    private ESDataType parent;

    ESDataType(ESDataType... compatibleTypes) {
        for (ESDataType subType : compatibleTypes) {
            subType.parent = this;
        }
    }

    @Override
    public String getName() {
        return name();
    }

    /**
     * For base type, compatibility means this (current type) is ancestor of other
     * in the base type hierarchy.
     */
    @Override
    public boolean isCompatible(Type other) {
        // Skip compatibility check if type is unknown
        if (this == UNKNOWN || other == UNKNOWN) {
            return true;
        }

        if (!(other instanceof ESDataType)) {
            // Nested data type is compatible with nested index type for type expression use
            if (other instanceof ESIndex && ((ESIndex) other).type() == NESTED_FIELD) {
                return isCompatible(NESTED);
            }
            return false;
        }

        // One way compatibility: parent base type is compatible with children
        ESDataType cur = (ESDataType) other;
        while (cur != null && cur != this) {
            cur = cur.parent;
        }
        return cur != null;
    }

    @Override
    public String toString() {
        return "ES Data Type [" + getName() + "]";
    }
}
