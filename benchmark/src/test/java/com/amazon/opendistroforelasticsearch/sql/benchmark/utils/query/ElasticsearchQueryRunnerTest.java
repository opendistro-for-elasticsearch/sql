package com.amazon.opendistroforelasticsearch.sql.benchmark.utils.query;

import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.query.elasticsearch.ElasticsearchQueryRunner;
import org.junit.jupiter.api.Test;

public class ElasticsearchQueryRunnerTest {

  @Test
  public void runQueryTest() throws Exception {
    String query = "Select count(*) from customer1";
    QueryRunner queryRunner = new ElasticsearchQueryRunner();
    queryRunner.prepareQueryRunner(query);
    queryRunner.runQuery();
    queryRunner.checkQueryExecutionStatus("/Users/rupalmahajan/Projects/GitHub/sql/benchmark/");
  }
}
