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

/**
 * Generic type for more precise type expression
 */
public class Generic implements Type {

    private enum Name { T }
    public static final Function<Type[], Type> T = types -> findSameGenericType(Name.T, types);

    private final Name name;
    private final Type binding;

    public Generic(Name name, Type type) {
        this.name = name;
        this.binding = type;
    }

    public static Type T(Type type) {
        return new Generic(Name.T, type);
    }

    public static Function<Type[], Type> generify(Function<Type[], Type> func, Type[] types) {
        if (func != T) {
            return func;
        }

        Type genericType = func.apply(types);
        int genericTypeIndex = Arrays.asList(types).indexOf(genericType);
        return actualTypes -> actualTypes[genericTypeIndex];
    }

    // TODO: this is wrong. we need to know position in T() so we can return actualTypes[pos] here.
    private static Type findSameGenericType(Name name, Type[] types) {
        return Arrays.stream(types).
                      filter(type -> type instanceof Generic).
                      filter(type -> ((Generic) type).name == name).
                      findFirst().
                      orElseThrow(() -> new IllegalStateException(StringUtils.format(
                          "Type definition is wrong. Could not unbind generic type [%s] in type list %s.",
                            name, types))
                      );
    }

    @Override
    public boolean isCompatible(Type other) {
        return binding.isCompatible(other);
    }

    @Override
    public Type construct(List<Type> others) {
        return binding.construct(others);
    }

    @Override
    public String usage() {
        return binding.usage() + " " + name;
    }
}
