package com.amazon.opendistroforelasticsearch.sql.benchmark.utils;

import com.amazon.opendistroforelasticsearch.sql.benchmark.BenchmarkService;
import org.junit.jupiter.api.Test;

public class BenchmarkServiceTest {

  String configFilePath = "/Users/rupalmahajan/Projects/GitHub/sql/benchmark/config.json";

  @Test
  public void runbenchmarkService() {
    BenchmarkService.main(new String[]{configFilePath});
  }
}
