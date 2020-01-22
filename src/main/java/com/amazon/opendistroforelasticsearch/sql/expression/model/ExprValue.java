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

/**
 * The definition of the Expression Value.
 */
public interface ExprValue {
    default Object value() {
        throw new IllegalStateException("invalid value operation on " + kind());
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
        LONG_VALUE,
        FLOAT_VALUE,
        STRING_VALUE
    }
}
