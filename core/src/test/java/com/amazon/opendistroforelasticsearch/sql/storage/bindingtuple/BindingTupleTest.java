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

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.exception.ExpressionEvaluationException;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BindingTupleTest {
    @Test
    public void resolve_ref_expression() {
        BindingTuple bindingTuple = BindingTuple.from(ImmutableMap.of("ip", "209.160.24.63"));
        assertEquals(ExprValueUtils.stringValue("209.160.24.63"), bindingTuple.resolve(DSL.ref("ip")));
    }

    @Test
    public void resolve_missing_expression() {
        BindingTuple bindingTuple = BindingTuple.from(ImmutableMap.of("ip", "209.160.24.63"));
        assertEquals(ExprValueUtils.LITERAL_MISSING, bindingTuple.resolve(DSL.ref("ip_missing")));
    }

    @Test
    public void resolve_literal_expression_throw_exception() {
        BindingTuple bindingTuple = BindingTuple.from(ImmutableMap.of("ip", "209.160.24.63"));
        ExpressionEvaluationException exception = assertThrows
                (ExpressionEvaluationException.class, () -> bindingTuple.resolve(DSL.literal(1)));
        assertEquals("can resolve expression: 1", exception.getMessage());
    }

    @Test
    public void string_value() {
        BindingTuple bindingTuple = BindingTuple.from(ImmutableMap.of("ip", "209.160.24.63"));
        assertEquals("<ip:\"209.160.24.63\">", bindingTuple.toString());
    }
}