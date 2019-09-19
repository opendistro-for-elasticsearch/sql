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

import com.amazon.opendistroforelasticsearch.sql.antlr.semantic.SemanticAnalysisException;
import com.amazon.opendistroforelasticsearch.sql.antlr.visitor.Reducible;
import com.amazon.opendistroforelasticsearch.sql.utils.StringUtils;

import java.util.List;

import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.BaseType.NESTED;

/**
 * Join
 */
public enum Join implements Type {

    COMMA, CROSS, INNER, LEFT_OUTER, RIGHT_OUTER, FULL_OUTER;

    @Override
    public boolean isCompatible(Type other) {
        return false;
    }

    @Override
    public <T extends Reducible> T reduce(List<T> types) {
        Type result = new Index(); // TODO: singleton?
        for (T type : types) {
            if (type == NESTED) {
                result = NESTED;
            } else if (type instanceof Index) {
            } else {
                throw new SemanticAnalysisException(StringUtils.format(
                    "Found [%s] type but nested type is required", type));
            }
        }
        return (T) result;
    }

    @Override
    public Type construct(List<Type> others) {
        return null;
    }

    @Override
    public String usage() {
        return null;
    }

}
