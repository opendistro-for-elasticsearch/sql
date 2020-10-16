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

package com.amazon.opendistroforelasticsearch.sql.benchmark.utils.launch;

import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.BenchmarkConstants;
import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.launch.elasticsearch.ElasticsearchDatabaseLauncher;

/**
 * Factory for getting DatabaseLauncher Objects.
 */
public class DatabaseLauncherFactory {

  /**
   * Empty private constructor since this is a factory.
   */
  private DatabaseLauncherFactory() {
  }

  /**
   * Function to get DatabaseLauncher for type.
   * @param type Type of DatabaseLauncher to get.
   * @return DatabaseLauncher that matches specified type.
   * @throws Exception If DatabaseLauncher for specified type cannot be found.
   */
  public static DatabaseLauncher getDatabaseLauncher(String type) throws Exception {
    if (type == null) {
      throw new Exception("TODO: Proper exceptions.");
    }

    if (type.equals(BenchmarkConstants.ELASTICSEARCH)) {
      return new ElasticsearchDatabaseLauncher();
    } else {
      throw new Exception("TODO: Proper exceptions.");
    }
  }
}
