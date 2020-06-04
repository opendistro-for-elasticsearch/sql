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

import com.amazon.opendistroforelasticsearch.sql.legacy.expression.core.operator.ScalarOperation;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.core.ExpressionFactory.literal;
import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.model.ExprValueFactory.doubleValue;
import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.model.ExprValueFactory.stringValue;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class UnaryExpressionTest extends ExpressionTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void absShouldPass() {
        assertEquals(2.0d, apply(ScalarOperation.ABS, literal(doubleValue(-2d))));
    }

    @Test
    public void asinShouldPass() {
        assertEquals(0.1001674211615598d, apply(ScalarOperation.ASIN, literal(doubleValue(0.1d))));
    }

    @Test
    public void atanShouldPass() {
        assertEquals(1.1071487177940904d, apply(ScalarOperation.ATAN, literal(doubleValue(2d))));
    }

    @Test
    public void tanShouldPass() {
        assertEquals(-2.185039863261519, apply(ScalarOperation.TAN, literal(doubleValue(2d))));
    }

    @Test
    public void atan2ShouldPass() {
        assertEquals(1.1071487177940904d,
                     apply(ScalarOperation.ATAN2, literal(doubleValue(2d)), literal(doubleValue(1d))));
    }

    @Test
    public void cbrtShouldPass() {
        assertEquals(1.2599210498948732d,
                     apply(ScalarOperation.CBRT, literal(doubleValue(2d))));
    }

    @Test
    public void ceilShouldPass() {
        assertEquals(3.0d,
                     apply(ScalarOperation.CEIL, literal(doubleValue(2.1d))));
    }

    @Test
    public void floorShouldPass() {
        assertEquals(2.0d,
                     apply(ScalarOperation.FLOOR, literal(doubleValue(2.1d))));
    }

    @Test
    public void cosShouldPass() {
        assertEquals(-0.4161468365471424d,
                     apply(ScalarOperation.COS, literal(doubleValue(2d))));
    }

    @Test
    public void coshShouldPass() {
        assertEquals(3.7621956910836314d,
                     apply(ScalarOperation.COSH, literal(doubleValue(2d))));
    }

    @Test
    public void expShouldPass() {
        assertEquals(7.38905609893065d,
                     apply(ScalarOperation.EXP, literal(doubleValue(2d))));
    }

    @Test
    public void lnShouldPass() {
        assertEquals(0.6931471805599453d,
                     apply(ScalarOperation.LN, literal(doubleValue(2d))));
    }

    @Test
    public void logShouldPass() {
        assertEquals(0.6931471805599453d,
                     apply(ScalarOperation.LOG, literal(doubleValue(2d))));
    }

    @Test
    public void log2ShouldPass() {
        assertEquals(1.0d,
                     apply(ScalarOperation.LOG2, literal(doubleValue(2d))));
    }

    @Test
    public void log10ShouldPass() {
        assertEquals(0.3010299956639812,
                     apply(ScalarOperation.LOG10, literal(doubleValue(2d))));
    }

    @Test
    public void absWithStringShouldThrowException() {
        exceptionRule.expect(RuntimeException.class);
        exceptionRule.expectMessage("unexpected operation type: ABS(STRING_VALUE)");

        apply(ScalarOperation.ABS, literal(stringValue("stringValue")));
    }

    @Test
    public void atan2WithStringShouldThrowException() {
        exceptionRule.expect(RuntimeException.class);
        exceptionRule.expectMessage("unexpected operation type: ATAN2(DOUBLE_VALUE,STRING_VALUE)");

        apply(ScalarOperation.ATAN2, literal(doubleValue(2d)), literal(stringValue("stringValue")));
    }
}
