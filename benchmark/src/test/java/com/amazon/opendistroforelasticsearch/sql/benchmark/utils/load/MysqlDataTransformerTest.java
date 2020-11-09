package com.amazon.opendistroforelasticsearch.sql.benchmark.utils.load;

import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.load.mysql.MysqlDataLoader;
import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.load.mysql.MysqlDataTransformer;
import org.junit.jupiter.api.Test;

public class MysqlDataTransformerTest {

  @Test
  public void checkFiles() throws Exception {
    MysqlDataTransformer transformer = new MysqlDataTransformer();
    DataFormat format = transformer
        .transformData("/home/rupal/Downloads/sql-benchmarking-mysql/benchmark/data/");
    MysqlDataLoader loader = new MysqlDataLoader();
    loader.loadData(format);
  }
}
