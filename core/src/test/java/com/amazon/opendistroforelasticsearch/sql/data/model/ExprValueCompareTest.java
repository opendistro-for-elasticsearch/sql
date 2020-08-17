/*
 *
 *    Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License").
 *    You may not use this file except in compliance with the License.
 *    A copy of the License is located at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    or in the "license" file accompanying this file. This file is distributed
 *    on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *    express or implied. See the License for the specific language governing
 *    permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.sql.data.model;

import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.LITERAL_FALSE;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.LITERAL_MISSING;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.LITERAL_NULL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class ExprValueCompareTest {

  @Test
  public void timeValueCompare() {
    assertEquals(0, new ExprTimeValue("18:00:00").compareTo(new ExprTimeValue("18:00:00")));
    assertEquals(1, new ExprTimeValue("19:00:00").compareTo(new ExprTimeValue("18:00:00")));
    assertEquals(-1, new ExprTimeValue("18:00:00").compareTo(new ExprTimeValue("19:00:00")));
  }

  @Test
  public void dateValueCompare() {
    assertEquals(0, new ExprDateValue("2012-08-07").compareTo(new ExprDateValue("2012-08-07")));
    assertEquals(1, new ExprDateValue("2012-08-08").compareTo(new ExprDateValue("2012-08-07")));
    assertEquals(-1, new ExprDateValue("2012-08-07").compareTo(new ExprDateValue("2012-08-08")));
  }

  @Test
  public void timestampValueCompare() {
    assertEquals(0,
        new ExprTimestampValue("2012-08-07 18:00:00")
            .compareTo(new ExprTimestampValue("2012-08-07 18:00:00")));
    assertEquals(1,
        new ExprTimestampValue("2012-08-07 19:00:00")
            .compareTo(new ExprTimestampValue("2012-08-07 18:00:00")));
    assertEquals(-1,
        new ExprTimestampValue("2012-08-07 18:00:00")
            .compareTo(new ExprTimestampValue("2012-08-07 19:00:00")));
  }

  @Test
  public void missingCompareToMethodShouldNotBeenCalledDirectly() {
    IllegalStateException exception = assertThrows(IllegalStateException.class,
        () -> LITERAL_MISSING.compareTo(LITERAL_FALSE));
    assertEquals("[BUG] Unreachable, Comparing with NULL or MISSING is undefined",
        exception.getMessage());

    exception = assertThrows(IllegalStateException.class,
        () -> LITERAL_FALSE.compareTo(LITERAL_MISSING));
    assertEquals("[BUG] Unreachable, Comparing with NULL or MISSING is undefined",
        exception.getMessage());

    exception = assertThrows(IllegalStateException.class,
        () -> ExprMissingValue.of().compare(LITERAL_MISSING));
    assertEquals("[BUG] Unreachable, Comparing with MISSING is undefined",
        exception.getMessage());
  }

  @Test
  public void nullCompareToMethodShouldNotBeenCalledDirectly() {
    IllegalStateException exception = assertThrows(IllegalStateException.class,
        () -> LITERAL_NULL.compareTo(LITERAL_FALSE));
    assertEquals("[BUG] Unreachable, Comparing with NULL or MISSING is undefined",
        exception.getMessage());

    exception = assertThrows(IllegalStateException.class,
        () -> LITERAL_FALSE.compareTo(LITERAL_NULL));
    assertEquals("[BUG] Unreachable, Comparing with NULL or MISSING is undefined",
        exception.getMessage());

    exception = assertThrows(IllegalStateException.class,
        () -> ExprNullValue.of().compare(LITERAL_MISSING));
    assertEquals("[BUG] Unreachable, Comparing with NULL is undefined",
        exception.getMessage());
  }
}
