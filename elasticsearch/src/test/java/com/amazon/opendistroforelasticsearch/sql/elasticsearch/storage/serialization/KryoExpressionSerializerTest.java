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

import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.expression.DSL.literal;
import static com.amazon.opendistroforelasticsearch.sql.expression.DSL.ref;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.config.ExpressionConfig;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class KryoExpressionSerializerTest {

  private final ExpressionSerializer serializer = new KryoExpressionSerializer();

  private final DSL dsl = new DSL(new ExpressionConfig().functionRepository());

  @Test
  void shouldSerializeLiteralExpressionBackAsItWas() {
    Expression expr = literal(5);
    assertEquals(expr, serializer.deserialize(serializer.serialize(expr)));
  }

  @Disabled("Need to refactor some lambda and inner class")
  @Test
  void shouldSerializeFunctionExpressionBackAsItWas() {
    Expression expr =
        dsl.abs(
            dsl.add(
                ref("age", INTEGER),
                literal(5)));
    assertEquals(expr, serializer.deserialize(serializer.serialize(expr)));
  }

}