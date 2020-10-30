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

package com.amazon.opendistroforelasticsearch.sql.benchmark.utils.load;

import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.BenchmarkConstants;
import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.load.elasticsearch.ElasticsearchDataFormat;
import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.load.elasticsearch.ElasticsearchDataLoader;
import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.load.elasticsearch.ElasticsearchDataTransformer;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

/**
 * Data utility holder factory, used to get a data utility holder.
 */
public class DataUtilHolderFactory {
  private static Map<String, DataUtilHolder> DATA_UTIL_HOLDER_MAP = new HashMap<>();

  static {
    try {
      DATA_UTIL_HOLDER_MAP.put(BenchmarkConstants.ELASTICSEARCH,
          new DataUtilHolder(BenchmarkConstants.ELASTICSEARCH));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Empty private constructor since this is a factory.
   */
  private DataUtilHolderFactory() {
  }

  /**
   * Returns DataUtilHolder for specified type.
   * @param type Type of database to get DataUtilHolder for.
   * @return DataUtilHolder for specified type.
   */
  public static DataUtilHolder getDataUtilHolder(final String type) {
    return DATA_UTIL_HOLDER_MAP.get(type);
  }

  /**
   * Data utility holder holds the data formatter, loader, and transformer for a specified database.
   */
  @Getter
  public static class DataUtilHolder {
    final DataFormat dataFormat;
    final DataLoader dataLoader;
    final DataTransformer dataTransformer;

    /**
     * Private constructor to setup data for specific type.
     */
    private DataUtilHolder(final String type) throws Exception {
      if (type.equals(BenchmarkConstants.ELASTICSEARCH)) {
        dataFormat = new ElasticsearchDataFormat();
        dataLoader = new ElasticsearchDataLoader();
        dataTransformer = new ElasticsearchDataTransformer();
      } else {
        throw new Exception("TODO: Proper exceptions.");
      }
    }
  }
}
