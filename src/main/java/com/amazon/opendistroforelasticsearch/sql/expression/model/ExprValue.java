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

package com.amazon.opendistroforelasticsearch.sql.expression.model;

import java.util.List;
import java.util.Map;

/**
 * The definition of the Value used in the Expression
 */
public interface ExprValue {
    default Object value() {
        throw new IllegalStateException("invalid value operation on " + kind());
    }

    default List<ExprValue> collectionValue() {
        throw new IllegalStateException("invalid collectionValue operation on " + kind());
    }

    default Map<String, ExprValue> tupleValue() {
        throw new IllegalStateException("invalid tupleValue on " + kind());
    }

    default Number numberValue() {
        throw new IllegalStateException("invalid numberValue operation on " + kind());
    }

    default Boolean booleanValue() {
        throw new IllegalStateException("invalid booleanValue operation on " + kind());
    }

    default String stringValue() {
        throw new IllegalStateException("invalid stringValue operation on " + kind());
    }

    default ExprValueKind kind() {
        throw new IllegalStateException("invalid kind operation");
    }

    enum ExprValueKind {
        TUPLE_VALUE,
        COLLECTION_VALUE,
        MISSING_VALUE,

        BOOLEAN_VALUE,
        INTEGER_VALUE,
        DOUBLE_VALUE,
        STRING_VALUE
    }
}
