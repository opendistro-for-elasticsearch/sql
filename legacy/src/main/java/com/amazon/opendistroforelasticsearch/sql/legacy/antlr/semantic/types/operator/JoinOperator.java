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
import com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESIndex;

import java.util.List;
import java.util.Optional;

import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.TYPE_ERROR;

/**
 * Join operator
 */
public enum JoinOperator implements Type {
    JOIN;

    @Override
    public String getName() {
        return name();
    }

    @Override
    public Type construct(List<Type> others) {
        Optional<Type> isAnyNonIndexType = others.stream().
                                                  filter(type -> !(type instanceof ESIndex)).
                                                  findAny();
        if (isAnyNonIndexType.isPresent()) {
            return TYPE_ERROR;
        }
        return others.get(0);
    }

    @Override
    public String usage() {
        return "Please join index with other index or its nested field.";
    }

    @Override
    public String toString() {
        return "Operator [" + getName() + "]";
    }
}
