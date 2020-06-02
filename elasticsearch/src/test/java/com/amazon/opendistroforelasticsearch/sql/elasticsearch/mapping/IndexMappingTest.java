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

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.mapping;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class IndexMappingTest {

  @Test
  public void getFieldType() {
    IndexMapping indexMapping = new IndexMapping(ImmutableMap.of("name", "text"));
    assertEquals("text", indexMapping.getFieldType("name"));
    assertNull(indexMapping.getFieldType("not_exist"));
  }

  @Test
  public void getAllFieldTypes() {
    IndexMapping indexMapping = new IndexMapping(ImmutableMap.of("name", "text", "age", "int"));
    Map<String, String> fieldTypes = indexMapping.getAllFieldTypes(type -> "our_type");
    assertThat(
        fieldTypes,
        allOf(aMapWithSize(2), hasEntry("name", "our_type"), hasEntry("age", "our_type")));
  }
}
