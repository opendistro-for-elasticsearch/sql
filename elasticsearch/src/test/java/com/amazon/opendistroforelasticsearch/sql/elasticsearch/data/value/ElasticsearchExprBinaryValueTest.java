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

import static com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.type.ElasticsearchDataType.ES_BINARY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ElasticsearchExprBinaryValueTest {

  @Test
  public void compare() {
    assertEquals(
        0,
        new ElasticsearchExprBinaryValue("U29tZSBiaW5hcnkgYmxvYg==")
            .compare(new ElasticsearchExprBinaryValue("U29tZSBiaW5hcnkgYmxvYg==")));
  }

  @Test
  public void equal() {
    ElasticsearchExprBinaryValue value =
        new ElasticsearchExprBinaryValue("U29tZSBiaW5hcnkgYmxvYg==");
    assertTrue(value.equal(new ElasticsearchExprBinaryValue("U29tZSBiaW5hcnkgYmxvYg==")));
  }

  @Test
  public void value() {
    ElasticsearchExprBinaryValue value =
        new ElasticsearchExprBinaryValue("U29tZSBiaW5hcnkgYmxvYg==");
    assertEquals("U29tZSBiaW5hcnkgYmxvYg==", value.value());
  }

  @Test
  public void type() {
    ElasticsearchExprBinaryValue value =
        new ElasticsearchExprBinaryValue("U29tZSBiaW5hcnkgYmxvYg==");
    assertEquals(ES_BINARY, value.type());
  }

}
