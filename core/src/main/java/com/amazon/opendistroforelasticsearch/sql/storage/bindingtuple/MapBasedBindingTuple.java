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

package com.amazon.opendistroforelasticsearch.sql.storage.bindingtuple;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprMissingValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.expression.ReferenceExpression;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Singular;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * The {@link BindingTuple} backed with Map.
 */
@Builder
@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class MapBasedBindingTuple extends BindingTuple {
    @Singular("binding")
    private final Map<String, ExprValue> bindingMap;

    /**
     * Resolve the {@link ReferenceExpression} in BindingTuple context.
     */
    public ExprValue resolve(ReferenceExpression ref) {
        return bindingMap.getOrDefault(ref.getAttr(), ExprMissingValue.of());
    }

    @Override
    public String toString() {
        return bindingMap.entrySet()
                .stream()
                .map(entry -> String.format("%s:%s", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(",", "<", ">"));
    }
}
