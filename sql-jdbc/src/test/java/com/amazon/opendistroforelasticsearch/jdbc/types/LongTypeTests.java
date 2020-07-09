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

public class LongTypeTests {

    @ParameterizedTest
    @CsvSource(value = {
            "9223372036854775807, 9223372036854775807", // Long.MAX_VALUE
            "-9223372036854775808, -9223372036854775808", // Long.MIN_VALUE
            "9460800, 9460800",
            "100, 100",
            "-9460800, -9460800",
            "100.25, 100",
            "100.80, 101",
            "-100000123456.80, -100000123457",
            "-100000123456.30, -100000123456"
    })
    void testLongFromValidString(String stringValue, long expectedValue) {
        long longValue = Assertions.assertDoesNotThrow(
                () -> LongType.INSTANCE.fromValue(stringValue, null));
        assertEquals(expectedValue, longValue);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "9223372036854775809",
            "9223372036854775807.8",
            "-9223372036854775809",
            "-9223372036854775808.6"
    })
    void testLongFromOutOfRangeString(String stringValue) {
        assertThrows(SQLDataException.class,
                () -> LongType.INSTANCE.fromValue(stringValue, null));
    }

    @ParameterizedTest
    @MethodSource("validRangeNumberProvider")
    void testLongFromValidRangeNumber(Number numberValue, long expectedValue) {
        long longValue = Assertions.assertDoesNotThrow(
                () -> LongType.INSTANCE.fromValue(numberValue, null));
        assertEquals(expectedValue, longValue);
    }

    private static Stream<Arguments> validRangeNumberProvider() {
        return Stream.of(

                // doubles
                Arguments.of(2147483647.0D, 2147483647L),
                Arguments.of(-2147483648.0D, -2147483648L),
                Arguments.of(2147483647.21D, 2147483647L),
                Arguments.of(2147483646.81D, 2147483647L),

                // ints
                Arguments.of(2147483647, 2147483647L),
                Arguments.of(-2147483648, -2147483648L),
                Arguments.of(9999, 9999L),

                // shorts
                Arguments.of((short) 32767, 32767),
                Arguments.of((short) -32768, -32768),
                Arguments.of((short) 250, 250),

                // floats
                Arguments.of(32767.8f, 32768L),
                Arguments.of(-32768.2f, -32768L),
                Arguments.of(250.1f, 250L)
        );
    }
}
