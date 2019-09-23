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

import com.amazon.opendistroforelasticsearch.sql.utils.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.BaseType.TYPE_ERROR;
import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.BaseType.UNKNOWN;

/**
 * Type expression by constructor such as function, operator etc.
 */
public interface TypeExpression extends Type {

    @Override
    default boolean isCompatible(Type other) {
        if (other == UNKNOWN) { // this == UNKNOWN ?
            return true;
        }

        if (!(other instanceof TypeExpression)) {
            return false;
        }

        TypeExpression otherExpr = (TypeExpression) other;
        Type[] expectArgTypes = spec().argTypes;
        Type[] actualArgTypes = otherExpr.spec().argTypes;

        // Arg numbers exactly match
        if (expectArgTypes.length != actualArgTypes.length) {
            return false;
        }

        // Arg types are compatible
        for (int i = 0; i < expectArgTypes.length; i++) {
            if (!expectArgTypes[i].isCompatible(actualArgTypes[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    default Type construct(List<Type> actualArgs) {
        // Create a temp expr without return type for compatibility check
        TypeExpression otherExpr = () -> {
            TypeExpressionSpec spec = new TypeExpressionSpec();
            spec.argTypes = actualArgs.toArray(new Type[0]);
            return spec;
        };

        if (isCompatible(otherExpr)) {
            return spec().constructFunc.apply(actualArgs.toArray(new Type[0]));
        }
        return TYPE_ERROR;
    }

    @Override
    default String usage() {
        TypeExpressionSpec spec = spec();
        String argTypesStr = Arrays.stream(spec.argTypes).
                                    map(Type::usage).
                                    collect(Collectors.joining());
        String returnTypeStr = spec.constructFunc.apply(spec.argTypes).usage();

        return StringUtils.format("%s(%s) -> %s", this, argTypesStr, returnTypeStr);
    }

    TypeExpressionSpec spec();

    class TypeExpressionSpec {
        Type[] argTypes;
        Function<Type[], Type> constructFunc;

        public TypeExpressionSpec map(Type... args) {
            this.argTypes = args;
            return this;
        }

        public TypeExpressionSpec to(Function<Type[], Type> constructFunc) {
            this.constructFunc = Generic.generify(constructFunc, argTypes);
            return this;
        }
    }

}
