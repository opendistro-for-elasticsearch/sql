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

package com.amazon.opendistroforelasticsearch.sql.legacy.unittest.expression.core;

import org.junit.Test;

import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.core.ExpressionFactory.ref;
import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.model.ExprValueFactory.doubleValue;
import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.model.ExprValueFactory.integerValue;
import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.model.ExprValueFactory.stringValue;
import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.model.ExprValueUtils.getBooleanValue;
import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.model.ExprValueUtils.getCollectionValue;
import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.model.ExprValueUtils.getDoubleValue;
import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.model.ExprValueUtils.getIntegerValue;
import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.model.ExprValueUtils.getStringValue;
import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.model.ExprValueUtils.getTupleValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertEquals;

public class RefExpressionTest extends ExpressionTest {
    @Test
    public void refIntegerValueShouldPass() {
        assertEquals(Integer.valueOf(1), getIntegerValue(ref("intValue").valueOf(bindingTuple())));
    }

    @Test
    public void refDoubleValueShouldPass() {
        assertEquals(Double.valueOf(2d), getDoubleValue(ref("doubleValue").valueOf(bindingTuple())));
    }

    @Test
    public void refStringValueShouldPass() {
        assertEquals("string", getStringValue(ref("stringValue").valueOf(bindingTuple())));
    }

    @Test
    public void refBooleanValueShouldPass() {
        assertEquals(true, getBooleanValue(ref("booleanValue").valueOf(bindingTuple())));
    }

    @Test
    public void refTupleValueShouldPass() {
        assertThat(getTupleValue(ref("tupleValue").valueOf(bindingTuple())),
                   allOf(hasEntry("intValue", integerValue(1)), hasEntry("doubleValue", doubleValue(2d)),
                         hasEntry("stringValue", stringValue("string"))));
    }

    @Test
    public void refCollectValueShouldPass() {
        assertThat(getCollectionValue(ref("collectValue").valueOf(bindingTuple())),
                   contains(integerValue(1), integerValue(2), integerValue(3)));
    }
}
