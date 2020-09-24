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

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazon.opendistroforelasticsearch.sql.exception.ExpressionEvaluationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ExprNumberValueTest {
  @Test
  public void getShortValueFromIncompatibleExprValue() {
    ExprBooleanValue booleanValue = ExprBooleanValue.of(true);
    ExpressionEvaluationException exception = Assertions
        .assertThrows(ExpressionEvaluationException.class, () -> booleanValue.shortValue());
    assertEquals("invalid to get shortValue from value of type BOOLEAN", exception.getMessage());
  }
}
