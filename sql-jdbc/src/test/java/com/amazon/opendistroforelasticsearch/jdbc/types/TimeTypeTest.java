/*
 *   Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.jdbc.types;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazon.opendistroforelasticsearch.jdbc.test.UTCTimeZoneTestExtension;
import java.sql.Time;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(UTCTimeZoneTestExtension.class)
public class TimeTypeTest {

  @ParameterizedTest
  @CsvSource(value = {
      "00:00:00, 00:00:00",
      "01:01:01, 01:01:01",
      "23:59:59, 23:59:59"
  })
  void testTimeFromString(String inputString, String resultString) {
    Time time = Assertions.assertDoesNotThrow(
        () -> TimeType.INSTANCE.fromValue(inputString, null));
    assertEquals(resultString, time.toString());
  }
}
