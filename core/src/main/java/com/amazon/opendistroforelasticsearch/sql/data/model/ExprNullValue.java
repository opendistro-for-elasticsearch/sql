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

package com.amazon.opendistroforelasticsearch.sql.data.model;

import com.amazon.opendistroforelasticsearch.sql.exception.ExpressionEvaluationException;
import lombok.EqualsAndHashCode;

/**
 * The definition of the expression null value.
 */
@EqualsAndHashCode
public class ExprNullValue implements ExprValue {
    private static final ExprValue instance = new ExprNullValue();

    private ExprNullValue() {
    }

    public static ExprValue of() {
        return instance;
    }

    @Override
    public Object value() {
        return null;
    }

    @Override
    public ExprType type() {
        throw new ExpressionEvaluationException("invalid to call type operation on null value");
    }

    @Override
    public boolean isNull() {
        return true;
    }

    @Override
    public String toString() {
        return "null";
    }
}
