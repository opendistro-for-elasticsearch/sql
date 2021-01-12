/*
 *
 *    Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License").
 *    You may not use this file except in compliance with the License.
 *    A copy of the License is located at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    or in the "license" file accompanying this file. This file is distributed
 *    on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *    express or implied. See the License for the specific language governing
 *    permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.sql.utils;

import static com.amazon.opendistroforelasticsearch.sql.utils.SystemIndexUtils.isSystemIndex;
import static com.amazon.opendistroforelasticsearch.sql.utils.SystemIndexUtils.mappingTable;
import static com.amazon.opendistroforelasticsearch.sql.utils.SystemIndexUtils.systemTable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SystemIndexUtilsTest {

  @Test
  void test_system_index() {
    assertTrue(isSystemIndex("_ODFE_SYS_TABLE_META.ALL"));
    assertFalse(isSystemIndex(".kibana"));
  }

  @Test
  void test_compose_mapping_table() {
    assertEquals("_ODFE_SYS_TABLE_MAPPINGS.employee", mappingTable("employee"));
  }

  @Test
  void test_system_info_table() {
    final SystemIndexUtils.SystemTable table = systemTable("_ODFE_SYS_TABLE_META.ALL");

    assertTrue(table.isSystemInfoTable());
    assertFalse(table.isMetaInfoTable());
    assertEquals("ALL", table.getTableName());
  }

  @Test
  void test_mapping_info_table() {
    final SystemIndexUtils.SystemTable table = systemTable("_ODFE_SYS_TABLE_MAPPINGS.employee");

    assertTrue(table.isMetaInfoTable());
    assertFalse(table.isSystemInfoTable());
    assertEquals("employee", table.getTableName());
  }

  @Test
  void throw_exception_for_invalid_index() {
    final IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> systemTable("_ODFE_SYS_TABLE.employee"));
    assertEquals("Invalid system index name: _ODFE_SYS_TABLE.employee", exception.getMessage());
  }
}