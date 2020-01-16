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

import com.amazon.opendistroforelasticsearch.sql.expression.core.ScalarOperation;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static com.amazon.opendistroforelasticsearch.sql.expression.core.ExpressionFactory.constant;
import static com.amazon.opendistroforelasticsearch.sql.expression.core.ExpressionFactory.ref;
import static com.amazon.opendistroforelasticsearch.sql.expression.model.ExprValueFactory.integerValue;
import static com.amazon.opendistroforelasticsearch.sql.expression.model.ExprValueFactory.stringValue;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class BinaryExpressionTest extends ExpressionTest {
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void addIntegerValueShouldPass() {
        assertEquals(2,
                     of(ScalarOperation.ADD, ref("intValue"), ref("intValue")).valueOf(bindingTuple()).numberValue());
    }

    @Test
    public void multipleAddIntegerValueShouldPass() {
        assertEquals(3,
                     of(ScalarOperation.ADD, ref("intValue"), of(ScalarOperation.ADD, ref("intValue"), ref("intValue")))
                             .valueOf(bindingTuple()).numberValue());
    }

    @Test
    public void addDoubleValueShouldPass() {
        assertEquals(4d,
                     of(ScalarOperation.ADD, ref("doubleValue"), ref("doubleValue")).valueOf(bindingTuple())
                             .numberValue());
    }

    @Test
    public void addDoubleAndIntegerShouldPass() {
        assertEquals(3d,
                     of(ScalarOperation.ADD, ref("doubleValue"), ref("intValue")).valueOf(bindingTuple())
                             .numberValue());
    }

    @Test
    public void divideIntegerValueShouldPass() {
        assertEquals(0,
                     of(ScalarOperation.DIVIDE, ref("intValue"), ref("intValue2")).valueOf(bindingTuple())
                             .numberValue());
    }

    @Test
    public void divideIntegerAndDoubleShouldPass() {
        assertEquals(0.5d,
                     of(ScalarOperation.DIVIDE, ref("intValue"), ref("doubleValue")).valueOf(bindingTuple())
                             .numberValue());
    }

    @Test
    public void subtractIntAndDoubleShouldPass() {
        assertEquals(-1d,
                     of(ScalarOperation.SUBTRACT, ref("intValue"), ref("doubleValue")).valueOf(bindingTuple())
                             .numberValue());
    }

    @Test
    public void multiplyIntAndDoubleShouldPass() {
        assertEquals(2d,
                     of(ScalarOperation.MULTIPLY, ref("intValue"), ref("doubleValue")).valueOf(bindingTuple())
                             .numberValue());
    }

    @Test
    public void modulesIntAndDoubleShouldPass() {
        assertEquals(1d,
                     of(ScalarOperation.MODULES, ref("intValue"), ref("doubleValue")).valueOf(bindingTuple())
                             .numberValue());
    }

    @Test
    public void addIntAndStringShouldPass() {
        exceptionRule.expect(RuntimeException.class);
        exceptionRule.expectMessage("unexpected operation type: ADD(INTEGER_VALUE, STRING_VALUE)");

        assertEquals(2, apply(ScalarOperation.ADD, constant(integerValue(1)), constant(stringValue("stringValue"))));
    }
}
