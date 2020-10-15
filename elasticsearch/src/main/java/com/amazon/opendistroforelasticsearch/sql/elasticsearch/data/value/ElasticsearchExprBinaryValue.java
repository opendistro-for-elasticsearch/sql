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

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.value;

import com.amazon.opendistroforelasticsearch.sql.data.model.AbstractExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprType;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.type.ElasticsearchDataType;
import lombok.EqualsAndHashCode;

/**
 * Elasticsearch BinaryValue.
 * Todo, add this to avoid the unknown value type exception, the implementation will be changed.
 */
@EqualsAndHashCode(callSuper = false)
public class ElasticsearchExprBinaryValue extends AbstractExprValue {
  private final String encodedString;

  public ElasticsearchExprBinaryValue(String encodedString) {
    this.encodedString = encodedString;
  }

  @Override
  public int compare(ExprValue other) {
    return encodedString.compareTo((String) other.value());
  }

  @Override
  public boolean equal(ExprValue other) {
    return encodedString.equals(other.value());
  }

  @Override
  public Object value() {
    return encodedString;
  }

  @Override
  public ExprType type() {
    return ElasticsearchDataType.ES_BINARY;
  }
}
