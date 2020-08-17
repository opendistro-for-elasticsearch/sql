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

package com.amazon.opendistroforelasticsearch.sql.common.setting;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Setting.
 */
public abstract class Settings {
  @RequiredArgsConstructor
  public enum Key {
    /**
     * PPL Setting.
     */
    PPL_QUERY_MEMORY_LIMIT("opendistro.ppl.query.memory_limit"),

    PPL_ENABLED("opendistro.ppl.enabled"),

    /**
     * Common Setting for SQL and PPL.
     */
    QUERY_SIZE_LIMIT("opendistro.query.size_limit");

    @Getter
    private final String keyValue;
  }

  /**
   * Get Setting Value.
   */
  public abstract <T> T getSettingValue(Key key);
}
