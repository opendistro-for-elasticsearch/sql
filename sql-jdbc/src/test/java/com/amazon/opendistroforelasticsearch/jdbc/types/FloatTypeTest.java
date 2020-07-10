package com.amazon.opendistroforelasticsearch.jdbc.types;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.SQLDataException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FloatTypeTest {
    @ParameterizedTest
    @CsvSource(value = {
            "2147483647, 2147483647", // Integer.MAX_VALUE
            "-2147483648, -2147483648", // Integer.MIN_VALUE
            "9460800, 9460800",
            "100, 100",
            "-9460800, -9460800",
            "100.25, 100.25",
            "100.80, 100.80",
            "-100.80, -100.80",
            "0, 0",
            "3.4028234E38, 3.4028234E38"
    })
    void testIntegerFromValidString(String stringValue, float expectedValue) {
        float floatValue = Assertions.assertDoesNotThrow(
                () -> FloatType.INSTANCE.fromValue(stringValue, null));
        assertEquals(expectedValue, floatValue);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "3.4028235E38",
            "3.4028235E39",
            "-3.4028235E38",
            "-3.4028235E39"
    })
    void testIntegerFromOutOfRangeString(String stringValue) {
        assertThrows(
                SQLDataException.class,
                () -> FloatType.INSTANCE.fromValue(stringValue, null));
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
    void testIntegerFromValidRangeNumber(Number numberValue, float expectedValue) {
        float floatValue = Assertions.assertDoesNotThrow(
                () -> FloatType.INSTANCE.fromValue(numberValue, null));
        assertEquals(expectedValue, floatValue);
    }

    private static Stream<Arguments> outOfRangeNumberProvider() {
        return Stream.of(
                // floats
                Arguments.of(Float.MAX_VALUE),
                Arguments.of(-Float.MAX_VALUE)
        );
    }

    private static Stream<Arguments> validRangeNumberProvider() {
        return Stream.of(
                // floats
                Arguments.of(3.14f, 3.14f),
                Arguments.of(-3.14f, -3.14f),
                Arguments.of(0, 0),
                Arguments.of(0x1.fffffdP+127f, 0x1.fffffdP+127f),
                Arguments.of(-0x1.fffffdP+127f, -0x1.fffffdP+127f)
        );
    }

}