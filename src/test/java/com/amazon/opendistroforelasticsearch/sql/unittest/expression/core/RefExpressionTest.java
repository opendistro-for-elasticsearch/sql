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

package com.amazon.opendistroforelasticsearch.sql.unittest.expression.core;

import org.junit.Test;

import static com.amazon.opendistroforelasticsearch.sql.expression.core.ExpressionFactory.ref;
import static com.amazon.opendistroforelasticsearch.sql.expression.model.ExprValueFactory.doubleValue;
import static com.amazon.opendistroforelasticsearch.sql.expression.model.ExprValueFactory.integerValue;
import static com.amazon.opendistroforelasticsearch.sql.expression.model.ExprValueFactory.stringValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertEquals;

public class RefExpressionTest extends ExpressionTest {
    @Test
    public void refIntegerValueShouldPass() {
        assertEquals(1, ref("intValue").valueOf(bindingTuple()).numberValue());
    }

    @Test
    public void refDoubleValueShouldPass() {
        assertEquals(2.0d, ref("doubleValue").valueOf(bindingTuple()).numberValue());
    }

    @Test
    public void refStringValueShouldPass() {
        assertEquals("string", ref("stringValue").valueOf(bindingTuple()).stringValue());
    }

    @Test
    public void refBooleanValueShouldPass() {
        assertEquals(true, ref("booleanValue").valueOf(bindingTuple()).booleanValue());
    }

    @Test
    public void refTupleValueShouldPass() {
        assertThat(ref("tupleValue").valueOf(bindingTuple()).tupleValue(),
                   allOf(hasEntry("intValue", integerValue(1)), hasEntry("doubleValue", doubleValue(2d)),
                         hasEntry("stringValue", stringValue("string"))));
    }

    @Test
    public void refCollectValueShouldPass() {
        assertThat(ref("collectValue").valueOf(bindingTuple()).collectionValue(),
                   contains(integerValue(1), integerValue(2), integerValue(3)));
    }
}
