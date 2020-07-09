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

public class ByteTypeTests {
    @ParameterizedTest
    @CsvSource(value = {
            "127, 127", // Byte.MAX_VALUE
            "-128, -128", // Byte.MIN_VALUE
            "100, 100",
            "-94, -94",
            "45.40, 45",
            "-100.95, -101",
            "127.2, 127",
            "-128.41, -128"
    })
    void testByteFromValidString(String stringValue, byte expectedValue) {
        byte byteValue = Assertions.assertDoesNotThrow(
                () -> ByteType.INSTANCE.fromValue(stringValue, null));
        assertEquals(expectedValue, byteValue);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "128",
            "21474836470",
            "-129",
            "127.6",
            "-128.6"
    })
    void testByteFromOutOfRangeString(String stringValue) {
        assertThrows(SQLDataException.class,
                () -> ByteType.INSTANCE.fromValue(stringValue, null));
    }

    @ParameterizedTest
    @MethodSource("validRangeNumberProvider")
    void testByteFromValidRangeNumber(Number numberValue, byte expectedValue) {
        byte byteValue = Assertions.assertDoesNotThrow(
                () -> ByteType.INSTANCE.fromValue(numberValue, null));
        assertEquals(expectedValue, byteValue);
    }


    @ParameterizedTest
    @MethodSource("outOfRangeNumberProvider")
    void testByteFromOutOfRangeNumber(Number numberValue) {
        SQLDataException ex = assertThrows(SQLDataException.class,
                () -> ByteType.INSTANCE.fromValue(numberValue, null));
        assertTrue(ex.getMessage().contains("out of range"));
    }

    private static Stream<Arguments> outOfRangeNumberProvider() {
        return Stream.of(
                // ints
                Arguments.of(128),
                Arguments.of(-129),

                // longs
                Arguments.of(128L),
                Arguments.of(-129L),

                // doubles
                Arguments.of(127.6D),
                Arguments.of(-128.55D)
        );
    }


    private static Stream<Arguments> validRangeNumberProvider() {
        return Stream.of(
                // ints
                Arguments.of(127, (byte) 127),
                Arguments.of(-128, (byte) -128),

                // longs
                Arguments.of(127L, (byte) 127),
                Arguments.of(125L, (byte) 125),
                Arguments.of(-128L, (byte) -128),

                // doubles
                Arguments.of(127.20D, (byte) 127),
                Arguments.of(-128.20D, (byte) -128),
                Arguments.of(125D, (byte) 125),

                // floats
                Arguments.of(127.20f, (byte) 127),
                Arguments.of(-128.20f, (byte) -128),
                Arguments.of(125f, (byte) 125)
        );
    }
}
