package com.amazon.opendistroforelasticsearch.sql.benchmark.utils;

import com.amazon.opendistroforelasticsearch.sql.benchmark.BenchmarkService;
import org.junit.jupiter.api.Test;

public class BenchmarkServiceTest {

  String configFilePath = "config.json";

  @Test
  public void runbenchmarkService() {
    try {
      BenchmarkService.main(new String[]{configFilePath});
    } catch(Exception e) {
      System.out.println("Exception: " + e);
    }
  }
}
