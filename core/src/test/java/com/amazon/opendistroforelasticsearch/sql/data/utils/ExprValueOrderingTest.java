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

package com.amazon.opendistroforelasticsearch.sql.data.utils;

import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.LITERAL_FALSE;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.LITERAL_MISSING;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.LITERAL_NULL;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.LITERAL_TRUE;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.collectionValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.doubleValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.floatValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.integerValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.longValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.stringValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.tupleValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType;
import com.amazon.opendistroforelasticsearch.sql.exception.ExpressionEvaluationException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExprValueOrderingTest {
  @Mock
  ExprValue left;
  @Mock
  ExprValue right;

  @Test
  public void natural() {
    ExprValueOrdering ordering = ExprValueOrdering.natural();
    assertEquals(1, ordering.compare(integerValue(5), integerValue(4)));
    assertEquals(0, ordering.compare(integerValue(5), integerValue(5)));
    assertEquals(-1, ordering.compare(integerValue(4), integerValue(5)));
  }

  @Test
  public void natural_reverse() {
    ExprValueOrdering ordering = ExprValueOrdering.natural().reverse();
    assertEquals(-1, ordering.compare(integerValue(5), integerValue(4)));
    assertEquals(0, ordering.compare(integerValue(5), integerValue(5)));
    assertEquals(1, ordering.compare(integerValue(4), integerValue(5)));
  }

  @Test
  public void natural_null_first() {
    ExprValueOrdering ordering = ExprValueOrdering.natural().nullsFirst();
    assertEquals(1, ordering.compare(integerValue(5), LITERAL_NULL));
    assertEquals(1, ordering.compare(integerValue(5), LITERAL_MISSING));
  }

  @Test
  public void natural_null_last() {
    ExprValueOrdering ordering = ExprValueOrdering.natural().nullsLast();
    assertEquals(-1, ordering.compare(integerValue(5), LITERAL_NULL));
    assertEquals(-1, ordering.compare(integerValue(5), LITERAL_MISSING));
    assertEquals(1, ordering.compare(LITERAL_NULL, integerValue(5)));
    assertEquals(1, ordering.compare(LITERAL_MISSING, integerValue(5)));
  }

  @Test
  public void natural_null_first_reverse() {
    ExprValueOrdering ordering = ExprValueOrdering.natural().reverse().nullsFirst();
    assertEquals(-1, ordering.compare(integerValue(5), integerValue(4)));
    assertEquals(0, ordering.compare(integerValue(5), integerValue(5)));
    assertEquals(1, ordering.compare(integerValue(4), integerValue(5)));
    assertEquals(1, ordering.compare(integerValue(5), LITERAL_NULL));
    assertEquals(1, ordering.compare(integerValue(5), LITERAL_MISSING));
  }

  @Test
  public void natural_reverse_null_first() {
    ExprValueOrdering ordering = ExprValueOrdering.natural().nullsFirst().reverse();
    assertEquals(-1, ordering.compare(integerValue(5), integerValue(4)));
    assertEquals(0, ordering.compare(integerValue(5), integerValue(5)));
    assertEquals(1, ordering.compare(integerValue(4), integerValue(5)));
    assertEquals(-1, ordering.compare(integerValue(5), LITERAL_NULL));
    assertEquals(-1, ordering.compare(integerValue(5), LITERAL_MISSING));
  }

  @Test
  public void natural_null_last_reverse() {
    ExprValueOrdering ordering = ExprValueOrdering.natural().reverse().nullsLast();
    assertEquals(-1, ordering.compare(integerValue(5), integerValue(4)));
    assertEquals(0, ordering.compare(integerValue(5), integerValue(5)));
    assertEquals(1, ordering.compare(integerValue(4), integerValue(5)));
    assertEquals(-1, ordering.compare(integerValue(5), LITERAL_NULL));
    assertEquals(-1, ordering.compare(integerValue(5), LITERAL_MISSING));
  }

  @Test
  public void natural_reverse_null_last() {
    ExprValueOrdering ordering = ExprValueOrdering.natural().nullsLast().reverse();
    assertEquals(-1, ordering.compare(integerValue(5), integerValue(4)));
    assertEquals(0, ordering.compare(integerValue(5), integerValue(5)));
    assertEquals(1, ordering.compare(integerValue(4), integerValue(5)));
    assertEquals(1, ordering.compare(integerValue(5), LITERAL_NULL));
    assertEquals(1, ordering.compare(integerValue(5), LITERAL_MISSING));
  }

  @Test
  public void natural_order_double_value() {
    ExprValueOrdering ordering = ExprValueOrdering.natural();
    assertEquals(1, ordering.compare(doubleValue(5d), doubleValue(4d)));
    assertEquals(0, ordering.compare(doubleValue(5d), doubleValue(5d)));
    assertEquals(-1, ordering.compare(doubleValue(4d), doubleValue(5d)));
  }

  @Test
  public void natural_order_float_value() {
    ExprValueOrdering ordering = ExprValueOrdering.natural();
    assertEquals(1, ordering.compare(floatValue(5f), floatValue(4f)));
    assertEquals(0, ordering.compare(floatValue(5f), floatValue(5f)));
    assertEquals(-1, ordering.compare(floatValue(4f), floatValue(5f)));
  }

  @Test
  public void natural_order_long_value() {
    ExprValueOrdering ordering = ExprValueOrdering.natural();
    assertEquals(1, ordering.compare(longValue(5L), longValue(4L)));
    assertEquals(0, ordering.compare(longValue(5L), longValue(5L)));
    assertEquals(-1, ordering.compare(longValue(4L), longValue(5L)));
  }

  @Test
  public void natural_order_boolean_value() {
    ExprValueOrdering ordering = ExprValueOrdering.natural();
    assertEquals(1, ordering.compare(LITERAL_TRUE, LITERAL_FALSE));
    assertEquals(0, ordering.compare(LITERAL_TRUE, LITERAL_TRUE));
    assertEquals(-1, ordering.compare(LITERAL_FALSE, LITERAL_TRUE));
  }

  @Test
  public void natural_order_string_value() {
    ExprValueOrdering ordering = ExprValueOrdering.natural();
    assertEquals(1, ordering.compare(stringValue("abd"), stringValue("abc")));
    assertEquals(0, ordering.compare(stringValue("abc"), stringValue("abc")));
    assertEquals(-1, ordering.compare(stringValue("abc"), stringValue("abd")));
  }

  @Test
  public void natural_order_collection_value() {
    ExprValueOrdering ordering = ExprValueOrdering.natural();
    assertEquals(
        1,
        ordering.compare(
            collectionValue(ImmutableList.of(1, 2)), collectionValue(ImmutableList.of(3))));
    assertEquals(
        0,
        ordering.compare(
            collectionValue(ImmutableList.of(1, 2)), collectionValue(ImmutableList.of(3, 4))));
    assertEquals(
        -1,
        ordering.compare(
            collectionValue(ImmutableList.of(3)), collectionValue(ImmutableList.of(1, 2))));
  }

  @Test
  public void natural_order_tuple_value() {
    ExprValueOrdering ordering = ExprValueOrdering.natural();
    assertEquals(
        1,
        ordering.compare(
            tupleValue(ImmutableMap.of("v1", 1, "v2", 2)), tupleValue(ImmutableMap.of("v1", 3))));
    assertEquals(
        0,
        ordering.compare(
            tupleValue(ImmutableMap.of("v1", 1, "v2", 2)),
            tupleValue(ImmutableMap.of("v1", 3, "v2", 4))));
    assertEquals(
        -1,
        ordering.compare(
            tupleValue(ImmutableMap.of("v1", 3)), tupleValue(ImmutableMap.of("v1", 1, "v2", 2))));
  }

  @Test
  public void order_compare_value_with_different_type() {
    ExprValueOrdering ordering = ExprValueOrdering.natural();
    ExpressionEvaluationException exception =
        assertThrows(
            ExpressionEvaluationException.class,
            () -> ordering.compare(integerValue(1), doubleValue(2d)));
    assertEquals(
        "compare expected value have same type, but with [INTEGER, DOUBLE]",
        exception.getMessage());
  }
}
