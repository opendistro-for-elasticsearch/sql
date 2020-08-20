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

/**
 * Expression serializer that (de-)serializes expression object.
 */
public interface ExpressionSerializer {

  /**
   * Serialize an expression.
   * @param expr  expression
   * @return      serialized string
   */
  String serialize(Expression expr);

  /**
   * Deserialize an expression.
   * @param code  serialized code
   * @return      original expression object
   */
  Expression deserialize(String code);

}
