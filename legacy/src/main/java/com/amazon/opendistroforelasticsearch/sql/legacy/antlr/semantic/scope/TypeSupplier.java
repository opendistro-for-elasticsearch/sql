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

package com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.scope;

import com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.SemanticAnalysisException;
import com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.Type;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * The TypeSupplier is construct by the symbolName and symbolType.
 * The TypeSupplier implement the {@link Supplier<Type>} interface to provide the {@link Type}.
 * The TypeSupplier maintain types to track different {@link Type} definition for the same symbolName.
 */
public class TypeSupplier implements Supplier<Type> {
    private final String symbolName;
    private final Type symbolType;
    private final Set<Type> types;

    public TypeSupplier(String symbolName, Type symbolType) {
        this.symbolName = symbolName;
        this.symbolType = symbolType;
        this.types = new HashSet<>();
        this.types.add(symbolType);
    }

    public TypeSupplier add(Type type) {
        types.add(type);
        return this;
    }

    /**
     * Get the {@link Type}
     * Throw {@link SemanticAnalysisException} if conflict found.
     * Currently, if the two types not equal, they are treated as conflicting.
     */
    @Override
    public Type get() {
        if (types.size() > 1) {
            throw new SemanticAnalysisException(
                    String.format("Field [%s] have conflict type [%s]", symbolName, types));
        } else {
            return symbolType;
        }
    }
}
