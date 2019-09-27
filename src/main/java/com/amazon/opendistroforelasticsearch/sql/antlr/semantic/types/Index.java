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

import java.util.List;

/**
 * Index type
 */
public enum Index implements Type {
    INDEX {
        @Override
        public Type construct(List<Type> others) {
            Index otherIndex = ((Index) super.construct(others));
            return otherIndex == INDEX_PATTERN ? INDEX_PATTERN : this;
        }
    },
    INDEX_PATTERN {
        @Override
        public Type construct(List<Type> others) {
            return INDEX_PATTERN;
        }
    },
    NESTED_INDEX {
        @Override
        public Type construct(List<Type> others) {
            Index otherIndex = ((Index) super.construct(others));
            return otherIndex == INDEX_PATTERN ? INDEX_PATTERN : this; // TODO: if possible Nested join Index?
        }
    };

    @Override
    public boolean isCompatible(Type other) {
        return false;
    }

    @Override
    public Type construct(List<Type> others) {
        if (others.size() != 1 || !(others.get(0) instanceof Index)) {
            throw new IllegalStateException(StringUtils.format(
                "Index type [%s] is joining type(s) %s", this, others));
        }
        return others.get(0);
    }

    @Override
    public String usage() {
        return name();
    }

}
