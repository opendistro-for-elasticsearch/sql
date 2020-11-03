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

package com.amazon.opendistroforelasticsearch.sql.benchmark.utils.load.mysql;

import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.load.DataFormat;
import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.load.DataTransformer;

/**
 * Data transformer for MySQL database.
 */
public class MysqlDataTransformer implements DataTransformer {

  /**
   * Data transforming function for MySQL.
   *
   * @param dataPath Directory for data to transform.
   * @return Path to transformed data.
   * @throws Exception Throws and exception if file read fails.
   */
  @Override
  public DataFormat transformData(String dataPath) throws Exception {
    return null;
  }
}
