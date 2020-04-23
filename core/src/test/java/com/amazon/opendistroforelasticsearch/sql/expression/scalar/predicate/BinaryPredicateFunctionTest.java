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
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.ExpressionTestBase;
import com.amazon.opendistroforelasticsearch.sql.expression.FunctionExpression;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.LITERAL_FALSE;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.LITERAL_MISSING;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.LITERAL_NULL;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.LITERAL_TRUE;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.booleanValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.fromObjectValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BinaryPredicateFunctionTest extends ExpressionTestBase {

    private static Stream<Arguments> binaryPredicateArguments() {
        List<Boolean> booleans = Arrays.asList(true, false);
        return Lists.cartesianProduct(booleans, booleans).stream()
                .map(list -> Arguments.of(list.get(0), list.get(1)));
    }

    private static Stream<Arguments> testEqualArguments() {
        List<Object> arguments = Arrays.asList(1, 1L, 1F, 1D, "str", true, ImmutableList.of(1),
                ImmutableMap.of("str", 1));
        Stream.Builder<Arguments> builder = Stream.builder();
        for (Object argument : arguments) {
            builder.add(Arguments.of(fromObjectValue(argument), fromObjectValue(argument)));
        }
        return builder.build();
    }

    @ParameterizedTest(name = "and({0}, {1})")
    @MethodSource("binaryPredicateArguments")
    public void test_and(Boolean v1, Boolean v2) {
        FunctionExpression and = dsl.and(typeEnv(), DSL.literal(booleanValue(v1)), DSL.literal(booleanValue(v2)));
        assertEquals(ExprType.BOOLEAN, and.type(typeEnv()));
        assertEquals(v1 && v2, ExprValueUtils.getBooleanValue(and.valueOf(valueEnv())));
    }

    @Test
    public void test_boolean_and_null() {
        FunctionExpression and = dsl.and(typeEnv(), DSL.literal(LITERAL_TRUE), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD));
        assertEquals(ExprType.BOOLEAN, and.type(typeEnv()));
        assertEquals(LITERAL_NULL, and.valueOf(valueEnv()));

        and = dsl.and(typeEnv(), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD), DSL.literal(LITERAL_TRUE));
        assertEquals(ExprType.BOOLEAN, and.type(typeEnv()));
        assertEquals(LITERAL_NULL, and.valueOf(valueEnv()));

        and = dsl.and(typeEnv(), DSL.literal(LITERAL_FALSE), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD));
        assertEquals(ExprType.BOOLEAN, and.type(typeEnv()));
        assertEquals(LITERAL_FALSE, and.valueOf(valueEnv()));

        and = dsl.and(typeEnv(), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD), DSL.literal(LITERAL_FALSE));
        assertEquals(ExprType.BOOLEAN, and.type(typeEnv()));
        assertEquals(LITERAL_FALSE, and.valueOf(valueEnv()));
    }

    @Test
    public void test_boolean_and_missing() {
        FunctionExpression and = dsl.and(typeEnv(), DSL.literal(LITERAL_TRUE), DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD));
        assertEquals(ExprType.BOOLEAN, and.type(typeEnv()));
        assertEquals(LITERAL_MISSING, and.valueOf(valueEnv()));

        and = dsl.and(typeEnv(), DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD), DSL.literal(LITERAL_TRUE));
        assertEquals(ExprType.BOOLEAN, and.type(typeEnv()));
        assertEquals(LITERAL_MISSING, and.valueOf(valueEnv()));

        and = dsl.and(typeEnv(), DSL.literal(LITERAL_FALSE), DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD));
        assertEquals(ExprType.BOOLEAN, and.type(typeEnv()));
        assertEquals(LITERAL_FALSE, and.valueOf(valueEnv()));

        and = dsl.and(typeEnv(), DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD), DSL.literal(LITERAL_FALSE));
        assertEquals(ExprType.BOOLEAN, and.type(typeEnv()));
        assertEquals(LITERAL_FALSE, and.valueOf(valueEnv()));
    }

    @Test
    public void test_null_and_missing() {
        FunctionExpression and = dsl.and(typeEnv(), DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD),
                DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD));
        assertEquals(ExprType.BOOLEAN, and.type(typeEnv()));
        assertEquals(LITERAL_MISSING, and.valueOf(valueEnv()));

        and = dsl.and(typeEnv(), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD));
        assertEquals(ExprType.BOOLEAN, and.type(typeEnv()));
        assertEquals(LITERAL_NULL, and.valueOf(valueEnv()));

        and = dsl.and(typeEnv(), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD), DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD));
        assertEquals(ExprType.BOOLEAN, and.type(typeEnv()));
        assertEquals(LITERAL_MISSING, and.valueOf(valueEnv()));

        and = dsl.and(typeEnv(), DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD));
        assertEquals(ExprType.BOOLEAN, and.type(typeEnv()));
        assertEquals(LITERAL_MISSING, and.valueOf(valueEnv()));
    }

    @ParameterizedTest(name = "or({0}, {1})")
    @MethodSource("binaryPredicateArguments")
    public void test_or(Boolean v1, Boolean v2) {
        FunctionExpression and = dsl.or(typeEnv(), DSL.literal(booleanValue(v1)), DSL.literal(booleanValue(v2)));
        assertEquals(ExprType.BOOLEAN, and.type(typeEnv()));
        assertEquals(v1 || v2, ExprValueUtils.getBooleanValue(and.valueOf(valueEnv())));
    }

    @Test
    public void test_boolean_or_null() {
        FunctionExpression or = dsl.or(typeEnv(), DSL.literal(LITERAL_TRUE), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD));
        assertEquals(ExprType.BOOLEAN, or.type(typeEnv()));
        assertEquals(LITERAL_TRUE, or.valueOf(valueEnv()));

        or = dsl.or(typeEnv(), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD), DSL.literal(LITERAL_TRUE));
        assertEquals(ExprType.BOOLEAN, or.type(typeEnv()));
        assertEquals(LITERAL_TRUE, or.valueOf(valueEnv()));

        or = dsl.or(typeEnv(), DSL.literal(LITERAL_FALSE), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD));
        assertEquals(ExprType.BOOLEAN, or.type(typeEnv()));
        assertEquals(LITERAL_NULL, or.valueOf(valueEnv()));

        or = dsl.or(typeEnv(), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD), DSL.literal(LITERAL_FALSE));
        assertEquals(ExprType.BOOLEAN, or.type(typeEnv()));
        assertEquals(LITERAL_NULL, or.valueOf(valueEnv()));
    }

    @Test
    public void test_boolean_or_missing() {
        FunctionExpression or = dsl.or(typeEnv(), DSL.literal(LITERAL_TRUE), DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD));
        assertEquals(ExprType.BOOLEAN, or.type(typeEnv()));
        assertEquals(LITERAL_TRUE, or.valueOf(valueEnv()));

        or = dsl.or(typeEnv(), DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD), DSL.literal(LITERAL_TRUE));
        assertEquals(ExprType.BOOLEAN, or.type(typeEnv()));
        assertEquals(LITERAL_TRUE, or.valueOf(valueEnv()));

        or = dsl.or(typeEnv(), DSL.literal(LITERAL_FALSE), DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD));
        assertEquals(ExprType.BOOLEAN, or.type(typeEnv()));
        assertEquals(LITERAL_MISSING, or.valueOf(valueEnv()));

        or = dsl.or(typeEnv(), DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD), DSL.literal(LITERAL_FALSE));
        assertEquals(ExprType.BOOLEAN, or.type(typeEnv()));
        assertEquals(LITERAL_MISSING, or.valueOf(valueEnv()));
    }

    @Test
    public void test_null_or_missing() {
        FunctionExpression or = dsl.or(typeEnv(), DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD),
                DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD));
        assertEquals(ExprType.BOOLEAN, or.type(typeEnv()));
        assertEquals(LITERAL_MISSING, or.valueOf(valueEnv()));

        or = dsl.or(typeEnv(), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD));
        assertEquals(ExprType.BOOLEAN, or.type(typeEnv()));
        assertEquals(LITERAL_NULL, or.valueOf(valueEnv()));

        or = dsl.or(typeEnv(), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD), DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD));
        assertEquals(ExprType.BOOLEAN, or.type(typeEnv()));
        assertEquals(LITERAL_NULL, or.valueOf(valueEnv()));

        or = dsl.or(typeEnv(), DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD));
        assertEquals(ExprType.BOOLEAN, or.type(typeEnv()));
        assertEquals(LITERAL_NULL, or.valueOf(valueEnv()));
    }


    @ParameterizedTest(name = "xor({0}, {1})")
    @MethodSource("binaryPredicateArguments")
    public void test_xor(Boolean v1, Boolean v2) {
        FunctionExpression and = dsl.xor(typeEnv(), DSL.literal(booleanValue(v1)), DSL.literal(booleanValue(v2)));
        assertEquals(ExprType.BOOLEAN, and.type(typeEnv()));
        assertEquals(v1 ^ v2, ExprValueUtils.getBooleanValue(and.valueOf(valueEnv())));
    }

    @Test
    public void test_boolean_xor_null() {
        FunctionExpression xor = dsl.xor(typeEnv(), DSL.literal(LITERAL_TRUE), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD));
        assertEquals(ExprType.BOOLEAN, xor.type(typeEnv()));
        assertEquals(LITERAL_TRUE, xor.valueOf(valueEnv()));

        xor = dsl.xor(typeEnv(), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD), DSL.literal(LITERAL_TRUE));
        assertEquals(ExprType.BOOLEAN, xor.type(typeEnv()));
        assertEquals(LITERAL_TRUE, xor.valueOf(valueEnv()));

        xor = dsl.xor(typeEnv(), DSL.literal(LITERAL_FALSE), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD));
        assertEquals(ExprType.BOOLEAN, xor.type(typeEnv()));
        assertEquals(LITERAL_NULL, xor.valueOf(valueEnv()));

        xor = dsl.xor(typeEnv(), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD), DSL.literal(LITERAL_FALSE));
        assertEquals(ExprType.BOOLEAN, xor.type(typeEnv()));
        assertEquals(LITERAL_NULL, xor.valueOf(valueEnv()));
    }

    @Test
    public void test_boolean_xor_missing() {
        FunctionExpression xor = dsl.xor(typeEnv(), DSL.literal(LITERAL_TRUE), DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD));
        assertEquals(ExprType.BOOLEAN, xor.type(typeEnv()));
        assertEquals(LITERAL_TRUE, xor.valueOf(valueEnv()));

        xor = dsl.xor(typeEnv(), DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD), DSL.literal(LITERAL_TRUE));
        assertEquals(ExprType.BOOLEAN, xor.type(typeEnv()));
        assertEquals(LITERAL_TRUE, xor.valueOf(valueEnv()));

        xor = dsl.xor(typeEnv(), DSL.literal(LITERAL_FALSE), DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD));
        assertEquals(ExprType.BOOLEAN, xor.type(typeEnv()));
        assertEquals(LITERAL_MISSING, xor.valueOf(valueEnv()));

        xor = dsl.xor(typeEnv(), DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD), DSL.literal(LITERAL_FALSE));
        assertEquals(ExprType.BOOLEAN, xor.type(typeEnv()));
        assertEquals(LITERAL_MISSING, xor.valueOf(valueEnv()));
    }

    @Test
    public void test_null_xor_missing() {
        FunctionExpression xor = dsl.xor(typeEnv(), DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD),
                DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD));
        assertEquals(ExprType.BOOLEAN, xor.type(typeEnv()));
        assertEquals(LITERAL_MISSING, xor.valueOf(valueEnv()));

        xor = dsl.xor(typeEnv(), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD));
        assertEquals(ExprType.BOOLEAN, xor.type(typeEnv()));
        assertEquals(LITERAL_NULL, xor.valueOf(valueEnv()));

        xor = dsl.xor(typeEnv(), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD), DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD));
        assertEquals(ExprType.BOOLEAN, xor.type(typeEnv()));
        assertEquals(LITERAL_NULL, xor.valueOf(valueEnv()));

        xor = dsl.xor(typeEnv(), DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD));
        assertEquals(ExprType.BOOLEAN, xor.type(typeEnv()));
        assertEquals(LITERAL_NULL, xor.valueOf(valueEnv()));
    }

    @ParameterizedTest(name = "equal({0}, {1})")
    @MethodSource("testEqualArguments")
    public void test_equal(ExprValue v1, ExprValue v2) {
        FunctionExpression equal = dsl.equal(typeEnv(), DSL.literal(v1), DSL.literal(v2));
        assertEquals(ExprType.BOOLEAN, equal.type(typeEnv()));
        assertEquals(v1.value().equals(v2.value()), ExprValueUtils.getBooleanValue(equal.valueOf(valueEnv())));
    }

    @Test
    public void test_null_equal_missing() {
        FunctionExpression equal = dsl.equal(typeEnv(), DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD),
                DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD));
        assertEquals(ExprType.BOOLEAN, equal.type(typeEnv()));
        assertEquals(LITERAL_TRUE, equal.valueOf(valueEnv()));

        equal = dsl.equal(typeEnv(), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD));
        assertEquals(ExprType.BOOLEAN, equal.type(typeEnv()));
        assertEquals(LITERAL_TRUE, equal.valueOf(valueEnv()));

        equal = dsl.equal(typeEnv(), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD), DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD));
        assertEquals(ExprType.BOOLEAN, equal.type(typeEnv()));
        assertEquals(LITERAL_FALSE, equal.valueOf(valueEnv()));

        equal = dsl.equal(typeEnv(), DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD));
        assertEquals(ExprType.BOOLEAN, equal.type(typeEnv()));
        assertEquals(LITERAL_FALSE, equal.valueOf(valueEnv()));

        equal = dsl.equal(typeEnv(), DSL.literal(LITERAL_TRUE), DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD));
        assertEquals(ExprType.BOOLEAN, equal.type(typeEnv()));
        assertEquals(LITERAL_FALSE, equal.valueOf(valueEnv()));

        equal = dsl.equal(typeEnv(), DSL.literal(LITERAL_TRUE), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD));
        assertEquals(ExprType.BOOLEAN, equal.type(typeEnv()));
        assertEquals(LITERAL_FALSE, equal.valueOf(valueEnv()));

        equal = dsl.equal(typeEnv(), DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD), DSL.literal(LITERAL_TRUE));
        assertEquals(ExprType.BOOLEAN, equal.type(typeEnv()));
        assertEquals(LITERAL_FALSE, equal.valueOf(valueEnv()));

        equal = dsl.equal(typeEnv(), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD), DSL.literal(LITERAL_TRUE));
        assertEquals(ExprType.BOOLEAN, equal.type(typeEnv()));
        assertEquals(LITERAL_FALSE, equal.valueOf(valueEnv()));
    }
}