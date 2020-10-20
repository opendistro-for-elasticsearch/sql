package com.amazon.opendistroforelasticsearch.sql.benchmark.utils.load;

import java.io.IOException;
import org.junit.jupiter.api.Test;

public class DataGeneratorTest {

  @Test
  public void generateDataTest() throws IOException, InterruptedException {
    DataGenerator.generateData("/home/rupal/Downloads/sql-benchmarking/benchmark/", 0.1);
  }

  @Test
  public void cleanupDataTest() throws IOException, InterruptedException {
    DataGenerator.cleanupData("/home/rupal/Downloads/sql-benchmarking/benchmark/");
  }
}

