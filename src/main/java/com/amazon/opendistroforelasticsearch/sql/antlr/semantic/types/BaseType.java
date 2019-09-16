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

import com.amazon.opendistroforelasticsearch.sql.antlr.visitor.Aggregator;
import com.google.common.collect.ImmutableMap;

import java.util.Collection;
import java.util.Map;

/**
 * Base type hierarchy based on Elasticsearch data type
 */
public enum BaseType implements Type {

    TYPE_ERROR,
    UNKNOWN,

    SHORT, LONG,
    INTEGER(SHORT, LONG),
    DOUBLE,
    FLOAT(DOUBLE),
    NUMBER(INTEGER, FLOAT),

    TEXT, KEYWORD,
    STRING(TEXT, KEYWORD),

    DATE_NANOS,
    DATE(DATE_NANOS),

    BOOLEAN,

    //BINARY,
    //RANGE,

    OBJECT, NESTED,
    COMPLEX(OBJECT, NESTED),

    ES_TYPE(NUMBER, STRING, DATE, BOOLEAN, COMPLEX);


    private static final Map<String, BaseType> ALL_BASE_TYPES;
    static {
        ImmutableMap.Builder<String, BaseType> builder = new ImmutableMap.Builder<>();
        for (BaseType type : BaseType.values()) {
            builder.put(type.name(), type);
        }
        ALL_BASE_TYPES = builder.build();
    }

    private BaseType parent;
    private final BaseType[] subTypes;


    BaseType(BaseType... subTypes) {
        this.subTypes = subTypes;
        for (BaseType subType : subTypes) {
            subType.parent = this;
        }
    }

    /*
    @Override
    public String getName() {
        return name();
    }

    @Override
    public Type apply(Type... actualTypes) {
        if (actualTypes.length != 1) {
            return TYPE_ERROR;
        }
        return isCompatible(actualTypes[0]) ? actualTypes[0] : TYPE_ERROR;
    }
    */

    public boolean isCompatible(Type other) {
        // Skip compatibility check if type is unknown
        if (this == UNKNOWN || other == UNKNOWN) {
            return true;
        }

        if (!(other instanceof BaseType)) {
            return false;
        }

        // One way compatibility
        BaseType cur = (BaseType) other;
        while (cur != null && cur != this) {
            cur = cur.parent;
        }
        return cur != null;
    }

    @Override
    public <T extends Aggregator> T aggregate(Collection<T> args) {
        return null;
    }

    /*
    @Override
    public String toString() {
        return getName();
    }
     */

}
