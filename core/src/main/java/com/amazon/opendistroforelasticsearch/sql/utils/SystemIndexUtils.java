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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

/**
 * System Index Utils.
 * Todo. Find the better name for this class.
 */
@UtilityClass
public class SystemIndexUtils {
  /**
   * The prefix of all the system tables.
   */
  private static final String SYS_TABLES_PREFIX = "_ODFE_SYS_TABLE";

  /**
   * The prefix of all the meta tables.
   */
  private static final String SYS_META_PREFIX = SYS_TABLES_PREFIX + "_META";

  /**
   * The prefix of all the table mappings.
   */
  private static final String SYS_MAPPINGS_PREFIX = SYS_TABLES_PREFIX + "_MAPPINGS";

  /**
   * The _ODFE_SYS_TABLE_META.ALL contain all the table info.
   */
  public static final String TABLE_INFO = SYS_META_PREFIX + ".ALL";


  public static Boolean isSystemIndex(String indexName) {
    return indexName.startsWith(SYS_TABLES_PREFIX);
  }

  /**
   * Compose system mapping table.
   *
   * @return system mapping table.
   */
  public static String mappingTable(String indexName) {
    return String.join(".", SYS_MAPPINGS_PREFIX, indexName);
  }

  /**
   * Build the {@link SystemTable}.
   *
   * @return {@link SystemTable}
   */
  public static SystemTable systemTable(String indexName) {
    final int lastDot = indexName.indexOf(".");
    String prefix = indexName.substring(0, lastDot);
    String tableName = indexName.substring(lastDot + 1)
        .replace("%", "*");

    if (prefix.equalsIgnoreCase(SYS_META_PREFIX)) {
      return new SystemInfoTable(tableName);
    } else if (prefix.equalsIgnoreCase(SYS_MAPPINGS_PREFIX)) {
      return new MetaInfoTable(tableName);
    } else {
      throw new IllegalStateException("Invalid system index name: " + indexName);
    }
  }

  /**
   * System Table.
   */
  public interface SystemTable {

    String getTableName();

    default boolean isSystemInfoTable() {
      return false;
    }

    default boolean isMetaInfoTable() {
      return false;
    }
  }

  /**
   * System Info Table.
   */
  @Getter
  @RequiredArgsConstructor
  public static class SystemInfoTable implements SystemTable {

    private final String tableName;

    public boolean isSystemInfoTable() {
      return true;
    }
  }

  /**
   * System Table.
   */
  @Getter
  @RequiredArgsConstructor
  public static class MetaInfoTable implements SystemTable {

    private final String tableName;

    public boolean isMetaInfoTable() {
      return true;
    }
  }
}
