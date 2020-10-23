package com.amazon.opendistroforelasticsearch.sql.benchmark.utils.query;

import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.load.DataGenerator;
import java.io.IOException;
import org.junit.jupiter.api.Test;

public class QueryGeneratorTest {

  @Test
  public void generateTest() throws IOException, InterruptedException {
    DataGenerator.generateData("/Users/rupalmahajan/Projects/GitHub/sql/benchmark/", 0.1);
    QueryGenerator.generateQueries("/Users/rupalmahajan/Projects/GitHub/sql/benchmark/", 0.1);
  }

  @Test
  public void cleanupTest() throws IOException, InterruptedException {
    DataGenerator.cleanupData("/Users/rupalmahajan/Projects/GitHub/sql/benchmark/");
    QueryGenerator.cleanupQueries("/Users/rupalmahajan/Projects/GitHub/sql/benchmark/");
  }

}
