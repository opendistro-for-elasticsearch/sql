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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;

/**
 * Default serializer that (de-)serialize expressions by JDK serialization.
 */
public class DefaultExpressionSerializer implements ExpressionSerializer {

  @Override
  public String serialize(Expression expr) {
    try {
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      ObjectOutputStream objectOutput = new ObjectOutputStream(output);
      objectOutput.writeObject(expr);
      objectOutput.flush();
      return Base64.getEncoder().encodeToString(output.toByteArray());
    } catch (IOException e) {
      throw new IllegalStateException("Failed to serialize expression: " + expr, e);
    }
  }

  @Override
  public Expression deserialize(String code) {
    try {
      ByteArrayInputStream input = new ByteArrayInputStream(Base64.getDecoder().decode(code));
      ObjectInputStream objectInput = new ObjectInputStream(input);
      return (Expression) objectInput.readObject();
    } catch (Exception e) {
      throw new IllegalStateException("Failed to deserialize expression code: " + code, e);
    }
  }

}
