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

package com.amazon.opendistroforelasticsearch.sql.expression.scalar.predicate;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprType;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.FunctionExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.scalar.FunctionTestBase;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.booleanValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

class UnaryPredicateFunctionTest extends FunctionTestBase {
    @ParameterizedTest(name = "not({0})")
    @ValueSource(booleans = {true, false})
    public void test_not(Boolean v) {
        FunctionExpression and = dsl.not(DSL.literal(booleanValue(v)));
        assertEquals(ExprType.BOOLEAN, and.type(emptyTypeEnv()));
        assertEquals(!v, ExprValueUtils.getBooleanValue(and.valueOf(emptyValueEnv())));
    }
}