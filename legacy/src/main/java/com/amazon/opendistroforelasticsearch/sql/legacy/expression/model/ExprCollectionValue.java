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

package com.amazon.opendistroforelasticsearch.sql.legacy.expression.model;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.model.ExprValue.ExprValueKind.COLLECTION_VALUE;

@EqualsAndHashCode
@RequiredArgsConstructor
public class ExprCollectionValue implements ExprValue {
    private final List<ExprValue> valueList;

    @Override
    public Object value() {
        return valueList;
    }

    @Override
    public ExprValueKind kind() {
        return COLLECTION_VALUE;
    }

    @Override
    public String toString() {
        return valueList.stream()
                .map(Object::toString)
                .collect(Collectors.joining(",", "[", "]"));
    }
}
