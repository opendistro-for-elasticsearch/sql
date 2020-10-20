package com.amazon.opendistroforelasticsearch.sql.benchmark.utils.launch;

import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.launch.elasticsearch.ElasticsearchDatabaseLauncher;

import java.io.IOException;

import org.junit.jupiter.api.Test;


public class ElasticsearchDatabaseLauncherTest {
  @Test
  public void launchDatabaseTest() throws IOException, InterruptedException {
    DatabaseLauncher es = new ElasticsearchDatabaseLauncher();
    es.launchDatabase("test_password");
  }

  @Test
  public void shutdownDatabaseTest() throws IOException, InterruptedException {
    DatabaseLauncher es = new ElasticsearchDatabaseLauncher();
    es.shutdownDatabase("test_password");
  }
}
