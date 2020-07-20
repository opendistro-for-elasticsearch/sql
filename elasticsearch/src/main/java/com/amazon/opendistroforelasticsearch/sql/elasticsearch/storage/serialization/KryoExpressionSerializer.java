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

import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Expression serializer powered by Kryo library.
 */
public class KryoExpressionSerializer implements ExpressionSerializer {

  private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

  private Kryo kryo = new Kryo();

  /**
   * Initialize and configure Kryo serializer internally.
   */
  public KryoExpressionSerializer() {
    kryo.setRegistrationRequired(false);
  }

  @Override
  public String serialize(Expression expr) {
    ByteArrayOutputStream array = new ByteArrayOutputStream();
    Output output = new Output(array);
    kryo.writeClassAndObject(output, expr);
    output.close();
    return new String(array.toByteArray(), DEFAULT_CHARSET);
  }

  @Override
  public Expression deserialize(String code) {
    return (Expression)
        kryo.readClassAndObject(
            new Input(new ByteArrayInputStream(
                code.getBytes(DEFAULT_CHARSET))));
  }

}
