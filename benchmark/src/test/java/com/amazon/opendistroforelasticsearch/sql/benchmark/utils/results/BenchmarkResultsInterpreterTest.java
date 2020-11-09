package com.amazon.opendistroforelasticsearch.sql.benchmark.utils.results;

import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.BenchmarkConstants;
import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.query.ResultGrabber;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class BenchmarkResultsInterpreterTest {

  private static final List<String> QUERIES = ImmutableList.of("query 1", "query 2", "query 3");

  @Test
  public void testResultsGrabGeneratePlots() throws Exception {
    try {
      final List<BenchmarkResults> benchmarkResultsList = new ArrayList<>();
      final ResultGrabber resultGrabber1 = new ResultGrabber(BenchmarkConstants.MOCK1, 10.0);
      final ResultGrabber resultGrabber2 = new ResultGrabber(BenchmarkConstants.MOCK2, 10.0);
      benchmarkResultsList.add(resultGrabber1.runQueries(QUERIES, ""));
      benchmarkResultsList.add(resultGrabber2.runQueries(QUERIES, ""));
      final BenchmarkResultsInterpreter benchmarkResultsInterpreter =
          new BenchmarkResultsInterpreter();
      benchmarkResultsInterpreter.interpretResults(benchmarkResultsList);
    } catch (Exception e) {
      e.printStackTrace();
      throw new Exception(e);
    }
  }
}
