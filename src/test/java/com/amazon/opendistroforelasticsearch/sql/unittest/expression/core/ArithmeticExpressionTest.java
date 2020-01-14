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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static com.amazon.opendistroforelasticsearch.sql.expression.core.ExpressionFactory.add;
import static com.amazon.opendistroforelasticsearch.sql.expression.core.ExpressionFactory.ref;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class ArithmeticExpressionTest extends ExpressionTest {
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void addIntegerValueShouldPass() {
        assertEquals(2, add(ref("intValue"), ref("intValue")).valueOf(bindingTuple()).numberValue());
    }

    @Test
    public void multipleAddIntegerValueShouldPass() {
        assertEquals(3,
                     add(ref("intValue"), add(ref("intValue"), ref("intValue"))).valueOf(bindingTuple()).numberValue());
    }

    @Test
    public void addDoubleValueShouldPass() {
        assertEquals(4d, add(ref("doubleValue"), ref("doubleValue")).valueOf(bindingTuple()).numberValue());
    }

    @Test
    public void addDoubleAndIntegerShouldThrowException() {
        exceptionRule.expect(RuntimeException.class);
        exceptionRule.expectMessage("operation with different type is unsupported: ADD(DOUBLE_VALUE, INTEGER_VALUE)");

        add(ref("doubleValue"), ref("intValue")).valueOf(bindingTuple());
    }
}
