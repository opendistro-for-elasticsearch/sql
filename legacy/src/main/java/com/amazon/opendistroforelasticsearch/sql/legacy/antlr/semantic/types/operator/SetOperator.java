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

import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.TYPE_ERROR;

/**
 * Set operator between queries.
 */
public enum SetOperator implements Type {
    UNION,
    MINUS,
    IN;

    @Override
    public String getName() {
        return name();
    }

    @Override
    public Type construct(List<Type> others) {
        if (others.size() < 2) {
            throw new IllegalStateException("");
        }

        // Compare each type and return anyone for now if pass
        for (int i = 0; i < others.size() - 1; i++) {
            Type type1 = others.get(i);
            Type type2 = others.get(i + 1);

            // Do it again as in Product because single base type won't be wrapped in Product
            if (!type1.isCompatible(type2) && !type2.isCompatible(type1)) {
                return TYPE_ERROR;
            }
        }
        return others.get(0);
    }

    @Override
    public String usage() {
        return "Please return field(s) of compatible type from each query.";
    }

    @Override
    public String toString() {
        return "Operator [" + getName() + "]";
    }
}
