package com.amazon.opendistroforelasticsearch.sql.benchmark.utils.query;

import static com.amazon.opendistroforelasticsearch.sql.benchmark.utils.CommandExecution.executeCommand;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

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
          + " && ./qgen -v -c -d -s " + scaleFactor + " > tpch-queries.sql"
          + " && mv ./tpch-queries.sql ../queries";
      executeCommand(commands);
    } else {
      throw new FileNotFoundException("Invalid Directory");
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
