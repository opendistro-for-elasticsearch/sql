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
import com.amazon.opendistroforelasticsearch.sql.expression.config.FunctionConfig;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.booleanValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {FunctionConfig.class})
class BinaryPredicateFunctionTest {
    @Autowired
    private DSL dsl;

    private static Stream<Arguments> binaryPredicateArguments() {
        List<Boolean> booleans = Arrays.asList(true, false);
        return Lists.cartesianProduct(booleans, booleans).stream()
                .map(list -> Arguments.of(list.get(0), list.get(1)));
    }

    @ParameterizedTest(name = "and({0}, {1})")
    @MethodSource("binaryPredicateArguments")
    public void test_and(Boolean v1, Boolean v2) {
        FunctionExpression and = dsl.and(DSL.literal(booleanValue(v1)), DSL.literal(booleanValue(v2)));
        assertEquals(ExprType.BOOLEAN, and.type());
        assertEquals(v1 && v2, ExprValueUtils.getBooleanValue(and.valueOf()));
    }

    @ParameterizedTest(name = "or({0}, {1})")
    @MethodSource("binaryPredicateArguments")
    public void test_or(Boolean v1, Boolean v2) {
        FunctionExpression and = dsl.or(DSL.literal(booleanValue(v1)), DSL.literal(booleanValue(v2)));
        assertEquals(ExprType.BOOLEAN, and.type());
        assertEquals(v1 || v2, ExprValueUtils.getBooleanValue(and.valueOf()));
    }

    @ParameterizedTest(name = "xor({0}, {1})")
    @MethodSource("binaryPredicateArguments")
    public void test_xor(Boolean v1, Boolean v2) {
        FunctionExpression and = dsl.xor(DSL.literal(booleanValue(v1)), DSL.literal(booleanValue(v2)));
        assertEquals(ExprType.BOOLEAN, and.type());
        assertEquals(v1 ^ v2, ExprValueUtils.getBooleanValue(and.valueOf()));
    }
}