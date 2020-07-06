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

import com.amazon.opendistroforelasticsearch.jdbc.test.UTCTimeZoneTestExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(UTCTimeZoneTestExtension.class)
public class TimestampTypeTests {

    // Test inputs here assume default JVM TimeZone of UTC.
    // The UTCTimeZoneTestExtension applied to this class ensures
    // the tests see behavior consistent with a JVM running under
    // a UTC TimeZone.

    @ParameterizedTest
    @CsvSource(value = {
            "2009-06-16T07:28:52.333, 1245137332333",
            "2015-01-01 00:34:46,     1420072486000",
            "2015-01-01 00:34:46.778, 1420072486778",
            "2015-01-01 00:34:46.778+00:00, 1420072486778",
            "2015-01-01 00:34:46.778Z, 1420072486778",
            "2015-01-01T00:34:46.778+01:00, 1420068886778",
            "2015-01-01 00:34:46.778-02, 1420079686778",
    })
    void testTimestampFromStringDefaultTZ(String stringValue, long longValue) {
        Timestamp timestamp = Assertions.assertDoesNotThrow(
                () -> TimestampType.INSTANCE.fromValue(stringValue, null));
        assertEquals(longValue, timestamp.getTime());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "2009-06-16T07:28:52.333, UTC, 1245137332333",
            "2015-01-01 00:34:46,     PST, 1420101286000",
            "2015-01-01 00:34:46.778, PST, 1420101286778"
    })
    void testTimestampFromStringCustomTZ(String stringValue, String timezone, long longValue) {
        Map<String, Object> conversionParams = new HashMap<>();
        conversionParams.put("calendar", Calendar.getInstance(TimeZone.getTimeZone(timezone)));
        Timestamp timestamp = Assertions.assertDoesNotThrow(
                () -> TimestampType.INSTANCE.fromValue(stringValue, conversionParams));
        assertEquals(longValue, timestamp.getTime());
    }

    @ParameterizedTest
    @MethodSource("numberProvider")
    void testTimestampFromNumber(Number numericValue) {
        Timestamp timestamp = Assertions.assertDoesNotThrow(
                () -> TimestampType.INSTANCE.fromValue(numericValue, null));
        assertEquals(numericValue.longValue(), timestamp.getTime());

        // timestamp does not matter when converting from numeric value
        Map<String, Object> conversionParams = new HashMap<>();
        conversionParams.put("calendar", Calendar.getInstance(TimeZone.getTimeZone("PST")));
        timestamp = Assertions.assertDoesNotThrow(
                () -> TimestampType.INSTANCE.fromValue(numericValue, conversionParams));
        assertEquals(numericValue.longValue(), timestamp.getTime());
    }

    private static Stream<Arguments> numberProvider() {
        return Stream.of(
                // longs
                Arguments.of(1245137332333L),
                Arguments.of(1420101286778L),
                Arguments.of(1L),
                Arguments.of(0L),
                Arguments.of(-10023456L),

                // ints
                Arguments.of(1245137332),
                Arguments.of(1420101286),
                Arguments.of(1),
                Arguments.of(0),
                Arguments.of(-10023456)
        );
    }

    @Test
    void testTimestampFromNull() {
        Timestamp timestamp = Assertions.assertDoesNotThrow(
                () -> TimestampType.INSTANCE.fromValue(null, null));
        assertNull(timestamp);

        // timestamp does not matter when converting from null value
        Map<String, Object> conversionParams = new HashMap<>();
        conversionParams.put("calendar", Calendar.getInstance(TimeZone.getTimeZone("PST")));
        timestamp = Assertions.assertDoesNotThrow(
                () -> TimestampType.INSTANCE.fromValue(null, conversionParams));
        assertNull(timestamp);
    }


}
