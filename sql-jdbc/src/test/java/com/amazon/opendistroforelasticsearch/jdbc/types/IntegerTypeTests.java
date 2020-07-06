/*
 * Copyright <2019> Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.jdbc.types;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;


import java.sql.SQLDataException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class IntegerTypeTests {

    @ParameterizedTest
    @CsvSource(value = {
            "2147483647, 2147483647", // Integer.MAX_VALUE
            "-2147483648, -2147483648", // Integer.MIN_VALUE
            "9460800, 9460800",
            "100, 100",
            "-9460800, -9460800",
            "100.25, 100",
            "100.80, 101",
            "-100.80, -101"
    })
    void testIntegerFromValidString(String stringValue, int expectedValue) {
        int integerValue = Assertions.assertDoesNotThrow(
                () -> IntegerType.INSTANCE.fromValue(stringValue, null));
        assertEquals(expectedValue, integerValue);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "2147483648",
            "21474836470",
            "-2147483649",
            "2147483647.6"
    })
    void testIntegerFromOutOfRangeString(String stringValue) {
        assertThrows(SQLDataException.class,
                () -> IntegerType.INSTANCE.fromValue(stringValue, null));
    }

    @ParameterizedTest
    @MethodSource("outOfRangeNumberProvider")
    void testIntegerFromOutOfRangeNumber(Number numberValue) {
        SQLDataException ex = assertThrows(SQLDataException.class,
                () -> IntegerType.INSTANCE.fromValue(numberValue, null));
        assertTrue(ex.getMessage().contains("out of range"));
    }

    @ParameterizedTest
    @MethodSource("validRangeNumberProvider")
    void testIntegerFromValidRangeNumber(Number numberValue, int expectedValue) {
        int intValue = Assertions.assertDoesNotThrow(
                () -> IntegerType.INSTANCE.fromValue(numberValue, null));
        assertEquals(expectedValue, intValue);
    }

    private static Stream<Arguments> outOfRangeNumberProvider() {
        return Stream.of(
                // longs
                Arguments.of(2147483648L),
                Arguments.of(-2147483649L),

                // doubles
                Arguments.of(21474836400D),
                Arguments.of(-21474836400D),
                Arguments.of(2147483647.61D)
        );
    }

    private static Stream<Arguments> validRangeNumberProvider() {
        return Stream.of(
                // longs
                Arguments.of(2147483647L, 2147483647),
                Arguments.of(-2147483648L, -2147483648),

                // doubles
                Arguments.of(2147483647.0D, 2147483647),
                Arguments.of(-2147483648.0D, -2147483648),
                Arguments.of(2147483647.21D, 2147483647),
                Arguments.of(2147483646.81D, 2147483647),

                // shorts
                Arguments.of((short) 32767, 32767),
                Arguments.of((short) -32768, -32768),
                Arguments.of((short) 250, 250)
        );
    }
}
