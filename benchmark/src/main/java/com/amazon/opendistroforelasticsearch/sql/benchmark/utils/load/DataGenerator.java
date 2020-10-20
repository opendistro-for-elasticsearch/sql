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

import static com.amazon.opendistroforelasticsearch.sql.benchmark.utils.CommandExecution.executeCommand;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Class to generate and cleanup benchmarking data.
 */
public class DataGenerator {

  /**
   * Private constructor since no state information is required.
   */
  private DataGenerator() {
  }

  /**
   * Function to generate data for benchmarking.
   *
   * @param benchmarkPath Path to benchmark.
   * @param scaleFactor   Scale factor to use.
   */
  public static void generateData(final String benchmarkPath, final double scaleFactor)
      throws IndexOutOfBoundsException, IOException, InterruptedException {
    File benchmarkRootDirectory = new File(benchmarkPath);
    if (benchmarkRootDirectory.exists() && benchmarkRootDirectory.isDirectory()) {
      String commands = "cd " + benchmarkPath
          + " && mkdir data "
          + " && cd ./tpch-dbgen/"
          + " && ./dbgen -s " + scaleFactor
          + " && mv ./*.tbl ../data";
      executeCommand(commands);
    } else {
      throw new FileNotFoundException("Invalid Directory");
    }
  }

  /**
   * Function to delete benchmarking data.
   *
   * @param benchmarkPath Path to benchmark.
   */
  public static void cleanupData(final String benchmarkPath)
      throws IndexOutOfBoundsException, IOException, InterruptedException {
    File benchmarkRootDirectory = new File(benchmarkPath);
    if (benchmarkRootDirectory.exists() && benchmarkRootDirectory.isDirectory()) {
      String commands = "cd " + benchmarkPath
          + "&& rm -r ./data ";
      executeCommand(commands);
    } else {
      throw new FileNotFoundException("Invalid Directory");
    }
  }
}

