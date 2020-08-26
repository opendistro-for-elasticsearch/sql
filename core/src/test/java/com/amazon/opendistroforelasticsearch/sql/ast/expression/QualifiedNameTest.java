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

package com.amazon.opendistroforelasticsearch.sql.ast.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class QualifiedNameTest {

  @Test
  void can_return_prefix_and_suffix() {
    QualifiedName name = QualifiedName.of("first", "second", "third");
    assertTrue(name.getPrefix().isPresent());
    assertEquals(QualifiedName.of("first", "second"), name.getPrefix().get());
    assertEquals("third", name.getSuffix());
  }

  @Test
  void can_return_first_and_rest() {
    QualifiedName name = QualifiedName.of("first", "second", "third");
    assertTrue(name.first().isPresent());
    assertEquals("first", name.first().get());
    assertEquals(QualifiedName.of("second", "third"), name.rest());
  }

  @Test
  void should_return_empty_if_only_single_part() {
    QualifiedName name = QualifiedName.of("test");
    assertFalse(name.first().isPresent());
    assertFalse(name.getPrefix().isPresent());
  }

}