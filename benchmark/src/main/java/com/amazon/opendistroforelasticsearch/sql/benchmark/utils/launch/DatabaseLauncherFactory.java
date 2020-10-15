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
