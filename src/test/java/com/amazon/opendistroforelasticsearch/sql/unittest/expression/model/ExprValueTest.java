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

package com.amazon.opendistroforelasticsearch.sql.unittest.expression.model;

import com.amazon.opendistroforelasticsearch.sql.expression.model.ExprIntegerValue;
import com.amazon.opendistroforelasticsearch.sql.expression.model.ExprValue;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class ExprValueTest {
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void interValueShouldReturnCorrectValue() {
        ExprValue value = new ExprIntegerValue(1);
        assertEquals(1, value.numberValue());
    }

    @Test
    public void stringValueOnInterValueShouldThrowException() {
        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("invalid stringValue operation on INTEGER_VALUE");

        ExprValue value = new ExprIntegerValue(1);
        value.stringValue();
    }
}
