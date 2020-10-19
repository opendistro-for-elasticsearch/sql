package com.amazon.opendistroforelasticsearch.sql.benchmark.utils.load;

import org.junit.Test;

public class DataGeneratorTest {

  @Test
  public void generateDataTest() {
    DataGenerator.generateData("/Users/rupalmahajan/Projects/GitHub/sql/benchmark/", 0.1);
  }

  @Test
  public void cleanupDataTest() {
    DataGenerator.cleanupData("/Users/rupalmahajan/Projects/GitHub/sql/benchmark/");
  }
}

