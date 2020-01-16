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
import static com.amazon.opendistroforelasticsearch.sql.expression.model.ExprValueFactory.doubleValue;
import static com.amazon.opendistroforelasticsearch.sql.expression.model.ExprValueFactory.stringValue;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class UnaryExpressionTest extends ExpressionTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void absShouldPass() {
        assertEquals(2.0d, apply(ScalarOperation.ABS, constant(doubleValue(-2d))));
    }

    @Test
    public void asinShouldPass() {
        assertEquals(0.1001674211615598d, apply(ScalarOperation.ASIN, constant(doubleValue(0.1d))));
    }

    @Test
    public void atanShouldPass() {
        assertEquals(1.1071487177940904d, apply(ScalarOperation.ATAN, constant(doubleValue(2d))));
    }

    @Test
    public void atan2ShouldPass() {
        assertEquals(1.1071487177940904d,
                     apply(ScalarOperation.ATAN2, constant(doubleValue(2d)), constant(doubleValue(1d))));
    }

    @Test
    public void cbrtShouldPass() {
        assertEquals(1.2599210498948732d,
                     apply(ScalarOperation.CBRT, constant(doubleValue(2d))));
    }

    @Test
    public void ceilShouldPass() {
        assertEquals(3.0d,
                     apply(ScalarOperation.CEIL, constant(doubleValue(2.1d))));
    }

    @Test
    public void floorShouldPass() {
        assertEquals(2.0d,
                     apply(ScalarOperation.FLOOR, constant(doubleValue(2.1d))));
    }

    @Test
    public void cosShouldPass() {
        assertEquals(-0.4161468365471424d,
                     apply(ScalarOperation.COS, constant(doubleValue(2d))));
    }

    @Test
    public void coshShouldPass() {
        assertEquals(3.7621956910836314d,
                     apply(ScalarOperation.COSH, constant(doubleValue(2d))));
    }

    @Test
    public void expShouldPass() {
        assertEquals(7.38905609893065d,
                     apply(ScalarOperation.EXP, constant(doubleValue(2d))));
    }

    @Test
    public void lnShouldPass() {
        assertEquals(0.6931471805599453d,
                     apply(ScalarOperation.LN, constant(doubleValue(2d))));
    }

    @Test
    public void logShouldPass() {
        assertEquals(0.6931471805599453d,
                     apply(ScalarOperation.LOG, constant(doubleValue(2d))));
    }

    @Test
    public void log2ShouldPass() {
        assertEquals(1.0d,
                     apply(ScalarOperation.LOG2, constant(doubleValue(2d))));
    }

    @Test
    public void log10ShouldPass() {
        assertEquals(0.3010299956639812,
                     apply(ScalarOperation.LOG10, constant(doubleValue(2d))));
    }

    @Test
    public void absWithStringShouldThrowException() {
        exceptionRule.expect(RuntimeException.class);
        exceptionRule.expectMessage("unexpected operation type: ABS(STRING_VALUE)");

        apply(ScalarOperation.ABS, constant(stringValue("stringValue")));
    }

    @Test
    public void atan2WithStringShouldThrowException() {
        exceptionRule.expect(RuntimeException.class);
        exceptionRule.expectMessage("unexpected operation type: ATAN2(DOUBLE_VALUE,STRING_VALUE)");

        apply(ScalarOperation.ATAN2, constant(doubleValue(2d)), constant(stringValue("stringValue")));
    }
}
