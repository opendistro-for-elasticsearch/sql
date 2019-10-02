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

import java.util.List;

import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.BaseType.TYPE_ERROR;

/**
 * Set operator between queries.
 */
public enum SetOperator implements Type {
    UNION;

    @Override
    public String getName() {
        return name();
    }

    @Override
    public Type construct(List<Type> others) {
        if (others.size() < 2) {
            throw new IllegalStateException("");
        }

        for (int i = 0; i < others.size() - 1; i++) {
            if (!others.get(i).isCompatible(others.get(i + 1))) {
                return TYPE_ERROR;
            }
        }
        return others.get(0);
    }

    @Override
    public String usage() {
        return "SELECT field_list1 FROM ... UNION [ALL] SELECT field_list2 FROM ... "
            + "(each field in field list 1 and 2 is compatible)";
    }

}
