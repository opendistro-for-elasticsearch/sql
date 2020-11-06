package com.amazon.opendistroforelasticsearch.sql.benchmark.utils.load;

import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.load.mysql.MysqlDataTransformer;
import org.junit.jupiter.api.Test;

public class MysqlDataTransformerTest {

  @Test
  public void checkFiles() throws Exception {
    MysqlDataTransformer transformer = new MysqlDataTransformer();
    transformer.transformData("/Users/rupalmahajan/Projects/GitHub/sql/benchmark/data/");
  }
}
