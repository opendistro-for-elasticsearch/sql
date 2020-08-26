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

package com.amazon.opendistroforelasticsearch.sql.ast.tree;

import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.qualifiedName;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RelationTest {

  @Test
  void should_return_table_name_if_no_alias() {
    Relation relation = new Relation(qualifiedName("test"));
    assertEquals("test", relation.getTableName());
    assertEquals("test", relation.getTableNameOrAlias());
  }

  @Test
  void should_return_alias_if_aliased() {
    Relation relation = new Relation(qualifiedName("test"), "t");
    assertEquals("t", relation.getTableNameOrAlias());
  }

}