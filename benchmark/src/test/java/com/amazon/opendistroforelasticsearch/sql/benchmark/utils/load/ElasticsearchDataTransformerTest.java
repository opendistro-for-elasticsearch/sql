package com.amazon.opendistroforelasticsearch.sql.benchmark.utils.load;

import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.load.elasticsearch.ElasticsearchDataTransformer;
import org.junit.jupiter.api.Test;

public class ElasticsearchDataTransformerTest {

  @Test
  public void transformDataTest() throws Exception {
    DataTransformer transformer = new ElasticsearchDataTransformer();
    transformer.transformData("/Users/rupalmahajan/Projects/GitHub/sql/benchmark/data/");
  }
}
