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

public class ShortTypeTests {
    @ParameterizedTest
    @CsvSource(value = {
            "32767, 32767", // Short.MAX_VALUE
            "-32768, -32768", // Short.MIN_VALUE
            "100, 100",
            "-9460, -9460",
            "45.40, 45",
            "100.95, 101"
    })
    void testShortFromValidString(String stringValue, short expectedValue) {
        short shortValue = Assertions.assertDoesNotThrow(
                () -> ShortType.INSTANCE.fromValue(stringValue, null));
        assertEquals(expectedValue, shortValue);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "32768",
            "21474836470",
            "-32769"
    })
    void testShortFromOutOfRangeString(String stringValue) {
        assertThrows(SQLDataException.class,
                () -> ShortType.INSTANCE.fromValue(stringValue, null));
    }

    @ParameterizedTest
    @MethodSource("outOfRangeNumberProvider")
    void testShortFromOutOfRangeNumber(Number numberValue) {
        SQLDataException ex = assertThrows(SQLDataException.class,
                () -> ShortType.INSTANCE.fromValue(numberValue, null));
        assertTrue(ex.getMessage().contains("out of range"));
    }


    @ParameterizedTest
    @MethodSource("validRangeNumberProvider")
    void testShortFromValidRangeNumber(Number numberValue, short expectedValue) {
        short shortValue = Assertions.assertDoesNotThrow(
                () -> ShortType.INSTANCE.fromValue(numberValue, null));
        assertEquals(expectedValue, shortValue);
    }

    private static Stream<Arguments> outOfRangeNumberProvider() {
        return Stream.of(
                // ints
                Arguments.of(32768),
                Arguments.of(-32769),

                // longs
                Arguments.of(2147483648L),
                Arguments.of(-2147483649L),

                // doubles
                Arguments.of(21474836400D),
                Arguments.of(-21474836400D)
        );
    }

    private static Stream<Arguments> validRangeNumberProvider() {
        return Stream.of(
                // ints
                Arguments.of(32767, (short) 32767),
                Arguments.of(-32768, (short) -32768),

                // longs
                Arguments.of(32767L, (short) 32767),
                Arguments.of(250L, (short) 250),
                Arguments.of(-32768L, (short) -32768),

                // doubles
                Arguments.of(32767.20D, (short) 32767),
                Arguments.of(-32768.20D, (short) -32768),
                Arguments.of(250D, (short) 250)
        );
    }
}
