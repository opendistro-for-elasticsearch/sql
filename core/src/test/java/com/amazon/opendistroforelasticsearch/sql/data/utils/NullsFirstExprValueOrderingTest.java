/*
 *   Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.LITERAL_MISSING;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.LITERAL_NULL;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.integerValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import org.junit.jupiter.api.Test;

class NullsFirstExprValueOrderingTest {
  @Test
  public void natural_null_first_null_first() {
    ExprValueOrdering ordering = ExprValueOrdering.natural().nullsFirst().nullsFirst();
    assertEquals(1, ordering.compare(integerValue(5), LITERAL_NULL));
    assertEquals(1, ordering.compare(integerValue(5), LITERAL_MISSING));
  }

  @Test
  public void natural_null_first_null_Last() {
    ExprValueOrdering ordering = ExprValueOrdering.natural().nullsFirst().nullsLast();
    assertEquals(-1, ordering.compare(integerValue(5), LITERAL_NULL));
    assertEquals(-1, ordering.compare(integerValue(5), LITERAL_MISSING));
  }

  @Test
  public void natural_null_first_compare_same_object() {
    ExprValueOrdering ordering = ExprValueOrdering.natural().nullsFirst();
    ExprValue exprValue = integerValue(5);
    assertEquals(0, ordering.compare(exprValue, exprValue));
  }
}