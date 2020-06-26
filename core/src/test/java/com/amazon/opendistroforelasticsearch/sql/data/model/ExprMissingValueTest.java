/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.data.model;

import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.LITERAL_MISSING;
import static com.amazon.opendistroforelasticsearch.sql.utils.ComparisonUtil.compare;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazon.opendistroforelasticsearch.sql.exception.ExpressionEvaluationException;
import org.junit.jupiter.api.Test;


class ExprMissingValueTest {

  @Test
  public void test_is_missing() {
    assertTrue(LITERAL_MISSING.isMissing());
  }

  @Test
  public void getValue() {
    ExpressionEvaluationException exception = assertThrows(ExpressionEvaluationException.class,
        () -> LITERAL_MISSING.value());
    assertEquals("invalid to call value operation on missing value", exception.getMessage());
  }

  @Test
  public void getType() {
    ExpressionEvaluationException exception = assertThrows(ExpressionEvaluationException.class,
        () -> LITERAL_MISSING.type());
    assertEquals("invalid to call type operation on missing value", exception.getMessage());
  }

  @Test
  public void comparabilityTest() {
    ExpressionEvaluationException exception = assertThrows(ExpressionEvaluationException.class,
        () -> compare(LITERAL_MISSING, LITERAL_MISSING));
    assertEquals("invalid to call compare operation on missing value", exception.getMessage());
  }
}