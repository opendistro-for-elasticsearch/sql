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

package com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types;

import com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.SemanticAnalysisException;
import com.amazon.opendistroforelasticsearch.sql.legacy.antlr.visitor.Reducible;
import com.amazon.opendistroforelasticsearch.sql.legacy.utils.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.TYPE_ERROR;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.UNKNOWN;

/**
 * Type interface which represents any type of symbol in the SQL.
 */
public interface Type extends Reducible {

    /**
     * Hide generic type ugliness and error check here in one place.
     */
    @SuppressWarnings("unchecked")
    @Override
    default <T extends Reducible> T reduce(List<T> others) {
        List<Type> actualArgTypes = (List<Type>) others;
        Type result = construct(actualArgTypes);
        if (result != TYPE_ERROR) {
            return (T) result;
        }

        // Generate error message by current type name, argument types and usage of current type
        // For example, 'Function [LOG] cannot work with [TEXT, INTEGER]. Usage: LOG(NUMBER) -> NUMBER
        String actualArgTypesStr;
        if (actualArgTypes.isEmpty()) {
            actualArgTypesStr = "<None>";
        } else {
            actualArgTypesStr = actualArgTypes.stream().
                                               map(Type::usage).
                                               collect(Collectors.joining(", "));
        }

        throw new SemanticAnalysisException(
            StringUtils.format("%s cannot work with [%s]. Usage: %s",
                this, actualArgTypesStr, usage()));
    }

    /**
     * Type descriptive name
     * @return  name
     */
    String getName();

    /**
     * Check if current type is compatible with other of same type.
     * @param other     other type
     * @return          true if compatible
     */
    default boolean isCompatible(Type other) {
        return other == UNKNOWN || this == other;
    }

    /**
     * Construct a new type by applying current constructor on other types.
     * Constructor is a generic conception that could be function, operator, join etc.
     *
     * @param others other types
     * @return  a new type as result
     */
    Type construct(List<Type> others);

    /**
     * Return typical usage of current type
     * @return  usage string
     */
    String usage();
}
