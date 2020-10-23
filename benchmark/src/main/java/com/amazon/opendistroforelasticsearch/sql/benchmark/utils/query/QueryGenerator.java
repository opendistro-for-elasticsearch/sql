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

package com.amazon.opendistroforelasticsearch.sql.benchmark.utils.query;

import static com.amazon.opendistroforelasticsearch.sql.benchmark.utils.CommandExecution.executeCommand;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Class to generate and cleanup benchmarking queries.
 */
public class QueryGenerator {

  /**
   * Private constructor since no state information is required.
   */
  private QueryGenerator() {
  }

  /**
   * Function to generate queries for benchmarking.
   *
   * @param benchmarkPath Path to benchmark.
   * @param scaleFactor   Scale factor to use.
   */
  public static void generateQueries(final String benchmarkPath, final double scaleFactor)
      throws IndexOutOfBoundsException, IOException, InterruptedException {
    File benchmarkRootDirectory = new File(benchmarkPath);
    if (benchmarkRootDirectory.exists() && benchmarkRootDirectory.isDirectory()) {
      String commands = "cd " + benchmarkPath
          + " && mkdir queries "
          + " && cd ./tpch-dbgen/"
          + " && export DSS_QUERY=" + benchmarkPath + "tpch-dbgen/queries/"
          + " && for ((i=1;i<=22;i++)); do"
          + "  ./qgen -s " + scaleFactor + " ${i} > tpch-query-${i}.sql;"
          + " done"
          + " && mv ./*.sql ../queries";
      executeCommand(commands);
      populateTpchQueries(benchmarkPath + "/queries/");
    } else {
      throw new FileNotFoundException("Invalid Directory");
    }
  }

  /**
   * Function to read queries from files and populate to variable
   *
   * @param queriesPath Path to folder containing queries.
   */
  private static void populateTpchQueries(final String queriesPath) throws IOException {
    File path = new File(queriesPath);
    if (path.exists() && path.isDirectory()) {
      for (int i = 1; i <= TpchQueries.tpchQueriesCountMax; i++) {
        String query = new String(
            Files.readAllBytes(Paths.get(queriesPath + "tpch-query-" + i + ".sql")));
        // Remove comment at the start of query file
        query = query.substring(40);
        query = query.replace("\r\n","");
        query.trim();
        TpchQueries.tpchQueries.add(query);
      }
    }
  }

  /**
   * Function to delete benchmarking queries.
   *
   * @param benchmarkPath Path to benchmark.
   */
  public static void cleanupQueries(final String benchmarkPath)
      throws IndexOutOfBoundsException, IOException, InterruptedException {
    File benchmarkRootDirectory = new File(benchmarkPath);
    if (benchmarkRootDirectory.exists() && benchmarkRootDirectory.isDirectory()) {
      String commands = "cd " + benchmarkPath
          + "&& rm -r ./queries ";
      executeCommand(commands);
    } else {
      throw new FileNotFoundException("Invalid Directory");
    }
  }
}
