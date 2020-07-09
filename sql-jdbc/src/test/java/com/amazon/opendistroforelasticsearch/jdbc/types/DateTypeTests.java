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
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(UTCTimeZoneTestExtension.class)
public class DateTypeTests {

    @ParameterizedTest
    @CsvSource(value = {
            "2015-01-01, 1420070400000",
            "1972-12-31, 94608000000",
            "1950-01-01, -631152000000"
    })
    void testDateFromStringDefaultTZ(String stringValue, long longValue) {
        Date date = Assertions.assertDoesNotThrow(
                () -> DateType.INSTANCE.fromValue(stringValue, null));
        assertEquals(longValue, date.getTime());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "2015-01-01, PST, 1420099200000",
            "1972-12-31, PST, 94636800000",
            "1950-01-01, PST, -631123200000"
    })
    void testDateFromStringCustomTZ(String stringValue, String timezone, long longValue) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(timezone));
        Map<String, Object> conversionParams = new HashMap<>();
        conversionParams.put("calendar", calendar);

        Date date = Assertions.assertDoesNotThrow(
                () -> DateType.INSTANCE.fromValue(stringValue, conversionParams));
        assertEquals(longValue, date.getTime());
    }

    @ParameterizedTest
    @MethodSource("numberProvider")
    void testDateFromNumber(Number numericValue) {
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

}
