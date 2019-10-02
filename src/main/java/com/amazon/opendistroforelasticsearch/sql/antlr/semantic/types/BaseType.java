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

package com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types;

import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;

import static com.amazon.opendistroforelasticsearch.sql.utils.StringUtils.toUpper;

/**
 * Base type hierarchy based on Elasticsearch data type
 */
public enum BaseType implements Type {

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

    //BINARY,
    //RANGE,

    OBJECT, NESTED,
    COMPLEX(OBJECT, NESTED),

    GEO_POINT,

    ES_TYPE(NUMBER, /*STRING,*/ DATE, BOOLEAN, COMPLEX, GEO_POINT);


    /**
     * Java Enum's valueOf() may thrown "enum constant not found" exception.
     * And Java doesn't provide a contains method.
     * So this static map is necessary for check and efficiency.
     */
    private static final Map<String, BaseType> ALL_BASE_TYPES;
    static {
        ImmutableMap.Builder<String, BaseType> builder = new ImmutableMap.Builder<>();
        for (BaseType type : BaseType.values()) {
            builder.put(type.name(), type);
        }
        ALL_BASE_TYPES = builder.build();
    }

    public static BaseType typeOf(String str) {
        return ALL_BASE_TYPES.getOrDefault(toUpper(str), UNKNOWN);
    }

    /** Parent of current base type */
    private BaseType parent;

    BaseType(BaseType... compatibleTypes) {
        for (BaseType subType : compatibleTypes) {
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

        if (!(other instanceof BaseType)) {
            return false;
        }

        // One way compatibility: parent base type is compatible with children
        BaseType cur = (BaseType) other;
        while (cur != null && cur != this) {
            cur = cur.parent;
        }
        return cur != null;
    }

    @Override
    public Type construct(List<Type> others) {
        return this;
    }

    @Override
    public String usage() {
        return name();
    }

}
