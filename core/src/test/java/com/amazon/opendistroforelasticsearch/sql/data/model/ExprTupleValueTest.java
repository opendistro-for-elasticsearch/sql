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

package com.amazon.opendistroforelasticsearch.sql.data.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

class ExprTupleValueTest {
    @Test
    public void equal_to_itself() {
        ExprValue tupleValue = ExprValueUtils.tupleValue(ImmutableMap.of("integer_value", 2));
        assertTrue(tupleValue.equals(tupleValue));
    }

    @Test
    public void tuple_compare_int() {
        ExprValue tupleValue = ExprValueUtils.tupleValue(ImmutableMap.of("integer_value", 2));
        ExprValue intValue = ExprValueUtils.integerValue(10);
        assertFalse(tupleValue.equals(intValue));
    }

    @Test
    public void compre_tuple_with_different_size() {
        ExprValue tupleValue1 = ExprValueUtils.tupleValue(ImmutableMap.of("integer_value", 2));
        ExprValue tupleValue2 = ExprValueUtils.tupleValue(ImmutableMap.of("integer_value", 2, "float_value", 1f));
        assertFalse(tupleValue1.equals(tupleValue2));
        assertFalse(tupleValue2.equals(tupleValue1));
    }
}