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

package com.amazon.opendistroforelasticsearch.sql.data.model;

import static com.amazon.opendistroforelasticsearch.sql.utils.ComparisonUtil.compare;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazon.opendistroforelasticsearch.sql.exception.ExpressionEvaluationException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class ExprCollectionValueTest {
  @Test
  public void equal_to_itself() {
    ExprValue value = ExprValueUtils.collectionValue(ImmutableList.of(1));
    assertTrue(value.equals(value));
  }

  @Test
  public void collection_compare_int() {
    ExprValue intValue = ExprValueUtils.integerValue(10);
    ExprValue value = ExprValueUtils.collectionValue(ImmutableList.of(1));
    assertFalse(value.equals(intValue));
  }

  @Test
  public void compare_collection_with_different_size() {
    ExprValue value1 = ExprValueUtils.collectionValue(ImmutableList.of(1));
    ExprValue value2 = ExprValueUtils.collectionValue(ImmutableList.of(1, 2));
    assertFalse(value1.equals(value2));
    assertFalse(value2.equals(value1));
  }

  @Test
  public void compare_collection_with_int_object() {
    ExprValue value = ExprValueUtils.collectionValue(ImmutableList.of(1));
    assertFalse(value.equals(1));
  }

  @Test
  public void comparabilityTest() {
    ExprValue collectionValue = ExprValueUtils.collectionValue(Arrays.asList(0, 1));
    ExpressionEvaluationException exception = assertThrows(ExpressionEvaluationException.class,
        () -> compare(collectionValue, collectionValue));
    assertEquals("ExprCollectionValue instances are not comparable", exception.getMessage());
  }
}
