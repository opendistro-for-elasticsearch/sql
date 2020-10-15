package com.amazon.opendistroforelasticsearch.sql.benchmark.utils.launch;

/**
 * Interface for launching and shutting down databases.
 */
public interface DatabaseLauncher {

  /**
   * Function interface for launching databases.
   */
  void launchDatabase();

  /**
   * Function interface for shutting down databases.
   */
  void shutdownDatabase();
}
