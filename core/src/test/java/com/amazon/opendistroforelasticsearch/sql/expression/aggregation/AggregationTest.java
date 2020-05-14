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

package com.amazon.opendistroforelasticsearch.sql.expression.aggregation;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.expression.ExpressionTestBase;
import com.amazon.opendistroforelasticsearch.sql.storage.bindingtuple.BindingTuple;
import com.google.common.collect.ImmutableMap;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AggregationTest extends ExpressionTestBase {
    protected static List<BindingTuple> tuples = Arrays.asList(
            BindingTuple.from(ImmutableMap.of("integer_value", 2, "string_value", "m", "double_value", 3d)),
            BindingTuple.from(ImmutableMap.of("integer_value", 1, "string_value", "f", "double_value", 4d)),
            BindingTuple.from(ImmutableMap.of("integer_value", 3, "string_value", "m", "double_value", 5d)),
            BindingTuple.from(ImmutableMap.of("integer_value", 4, "string_value", "f", "double_value", 3d)));

    protected static List<BindingTuple> tuples_with_null_and_missing = Arrays.asList(
            BindingTuple.from(ImmutableMap.of("integer_value", 2, "string_value", "m", "double_value", 3d)),
            BindingTuple.from(ImmutableMap.of("integer_value", 1, "string_value", "f", "double_value", 4d)),
            BindingTuple.from(Collections.singletonMap("double_value", null)));

    protected ExprValue aggregation(Aggregator aggregator, List<BindingTuple> tuples) {
        AggregationState state = aggregator.create();
        for (BindingTuple tuple : tuples) {
            aggregator.iterate(tuple, state);
        }
        return state.result();
    }
}
