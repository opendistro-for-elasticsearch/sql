/*
 *    Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License").
 *    You may not use this file except in compliance with the License.
 *    A copy of the License is located at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    or in the "license" file accompanying this file. This file is distributed
 *    on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *    express or implied. See the License for the specific language governing
 *    permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.serialization;

import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;
import static com.amazon.opendistroforelasticsearch.sql.expression.DSL.literal;
import static com.amazon.opendistroforelasticsearch.sql.expression.DSL.ref;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.config.ExpressionConfig;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class DefaultExpressionSerializerTest {

  /**
   * Initialize function repository manually to avoid dependency on Spring container.
   */
  private final DSL dsl = new ExpressionConfig().dsl(new ExpressionConfig().functionRepository());

  private final ExpressionSerializer serializer = new DefaultExpressionSerializer();

  @Test
  public void can_serialize_and_deserialize_literals() {
    Expression original = literal(10);
    Expression actual = serializer.deserialize(serializer.serialize(original));
    assertEquals(original, actual);
  }

  @Test
  public void can_serialize_and_deserialize_references() {
    Expression original = ref("name", STRING);
    Expression actual = serializer.deserialize(serializer.serialize(original));
    assertEquals(original, actual);
  }

  @Disabled("Bypass until binary operator refactored as well")
  @Test
  public void can_serialize_and_deserialize_predicates() {
    Expression original = dsl.or(literal(true), dsl.less(literal(1), literal(2)));
    Expression actual = serializer.deserialize(serializer.serialize(original));
    assertEquals(original, actual);
  }

  @Disabled("Bypass until function refactored as well")
  @Test
  public void can_serialize_and_deserialize_functions() {
    Expression original = dsl.abs(literal(30.0));
    Expression actual = serializer.deserialize(serializer.serialize(original));
    assertEquals(original, actual);
  }

}