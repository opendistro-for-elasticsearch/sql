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

package com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.operator;

import com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.Type;

import java.util.List;

import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.BOOLEAN;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.TYPE_ERROR;

/**
 * Type for comparison operator
 */
public enum ComparisonOperator implements Type {

    EQUAL("="),
    NOT_EQUAL("<>"),
    NOT_EQUAL2("!="),
    GREATER_THAN(">"),
    GREATER_THAN_OR_EQUAL_TO(">="),
    SMALLER_THAN("<"),
    SMALLER_THAN_OR_EQUAL_TO("<="),
    IS("IS");

    /** Actual name representing the operator */
    private final String name;

    ComparisonOperator(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Type construct(List<Type> actualArgs) {
        if (actualArgs.size() != 2) {
            return TYPE_ERROR;
        }

        Type leftType = actualArgs.get(0);
        Type rightType = actualArgs.get(1);
        if (leftType.isCompatible(rightType) || rightType.isCompatible(leftType)) {
            return BOOLEAN;
        }
        return TYPE_ERROR;
    }

    @Override
    public String usage() {
        return "Please use compatible types from each side.";
    }

    @Override
    public String toString() {
        return "Operator [" + getName() + "]";
    }
}
