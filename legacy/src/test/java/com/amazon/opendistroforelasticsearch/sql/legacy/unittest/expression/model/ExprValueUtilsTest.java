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

package com.amazon.opendistroforelasticsearch.sql.legacy.unittest.expression.model;

import com.amazon.opendistroforelasticsearch.sql.legacy.expression.model.ExprValueFactory;
import com.amazon.opendistroforelasticsearch.sql.legacy.expression.model.ExprValueUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ExprValueUtilsTest {
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void getIntegerValueWithIntegerExprValueShouldPass() {
        assertThat(ExprValueUtils.getIntegerValue(ExprValueFactory.integerValue(1)), equalTo(1));
    }

    @Test
    public void getDoubleValueWithIntegerExprValueShouldPass() {
        assertThat(ExprValueUtils.getDoubleValue(ExprValueFactory.integerValue(1)), equalTo(1d));
    }

    @Test
    public void getIntegerWithDoubleExprValueShouldPass() {
        assertThat(ExprValueUtils.getIntegerValue(ExprValueFactory.doubleValue(1d)), equalTo(1));
    }

    @Test
    public void getLongValueFromLongExprValueShouldPass() {
        assertThat(ExprValueUtils.getLongValue(ExprValueFactory.from(1L)), equalTo(1L));
    }

    @Test
    public void getIntegerValueFromStringExprValueShouldThrowException() {
        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("invalid to get NUMBER_VALUE from expr type of STRING_VALUE");

        ExprValueUtils.getIntegerValue(ExprValueFactory.stringValue("string"));
    }

    @Test
    public void getStringValueFromIntegerExprValueShouldThrowException() {
        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("invalid to get STRING_VALUE from expr type of INTEGER_VALUE");

        ExprValueUtils.getStringValue(ExprValueFactory.integerValue(1));
    }
}
