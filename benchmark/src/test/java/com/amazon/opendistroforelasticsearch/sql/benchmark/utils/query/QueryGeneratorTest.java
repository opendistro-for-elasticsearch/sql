package com.amazon.opendistroforelasticsearch.sql.benchmark.utils.query;

import java.io.IOException;
import org.junit.jupiter.api.Test;

public class QueryGeneratorTest {

  @Test
  public void generateDataTest() throws IOException, InterruptedException {
    QueryGenerator.generateQueries("/Users/rupalmahajan/Projects/GitHub/sql/benchmark/", 0.1);
  }

  @Test
  public void cleanupDataTest() throws IOException, InterruptedException {
    QueryGenerator.cleanupQueries("/Users/rupalmahajan/Projects/GitHub/sql/benchmark/");
  }

}
