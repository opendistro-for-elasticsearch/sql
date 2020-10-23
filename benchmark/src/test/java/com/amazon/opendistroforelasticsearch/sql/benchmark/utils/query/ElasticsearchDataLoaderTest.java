package com.amazon.opendistroforelasticsearch.sql.benchmark.utils.query;

import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.load.DataFormat;
import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.load.elasticsearch.ElasticsearchDataLoader;
import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.load.elasticsearch.ElasticsearchDataTransformer;
import org.junit.jupiter.api.Test;

public class ElasticsearchDataLoaderTest {

  @Test
  public void dataUploaderTest() throws Exception {
    ElasticsearchDataTransformer dataTransformer = new ElasticsearchDataTransformer();
    DataFormat dataFormat = dataTransformer.transformData("/Users/rupalmahajan/Projects/GitHub/sql/benchmark/data/");
    ElasticsearchDataLoader dataLoader = new ElasticsearchDataLoader();
    dataLoader.loadData(dataFormat);
  }
}
