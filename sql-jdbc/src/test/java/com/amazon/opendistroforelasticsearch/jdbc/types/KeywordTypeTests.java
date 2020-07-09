package com.amazon.opendistroforelasticsearch.jdbc.types;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class KeywordTypeTests {

    @ParameterizedTest
    @MethodSource("validIpStringProvider")
    void testIpFromValidIpString(String stringValue, String expectedValue) {
        String result = Assertions.assertDoesNotThrow(
                () -> StringType.INSTANCE.fromValue(stringValue, null));
        assertEquals(expectedValue, result);
    }

    private static Stream<Arguments> validIpStringProvider() {
        return Stream.of(
                Arguments.of("199.72.81.55", "199.72.81.55"),
                Arguments.of("205.212.115.106", "205.212.115.106"),
                Arguments.of("255.255.255.255", "255.255.255.255"),
                Arguments.of("255.0.0.0", "255.0.0.0")
        );
    }
}
