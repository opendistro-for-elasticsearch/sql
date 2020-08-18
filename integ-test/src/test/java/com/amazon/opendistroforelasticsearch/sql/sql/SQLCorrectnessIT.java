/*
 *    Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License").
 *    You may not use this file except in compliance with the License.
 *    A copy of the License is located at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    or in the "license" file accompanying this file. This file is distributed
 *    on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *    express or implied. See the License for the specific language governing
 *    permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.sql.sql;

import com.amazon.opendistroforelasticsearch.sql.util.TestUtils;
import com.google.common.io.Resources;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;
import org.junit.Test;

/**
 * SQL integration test automated by comparison test framework.
 */
public class SQLCorrectnessIT extends CorrectnessTestBase {

  private static final String ROOT_DIR = "correctness/";
  private static final String[] EXPR_TEST_DIR = { "expressions" };
  private static final String[] QUERY_TEST_DIR = { "queries", "bugfixes" };

  @Override
  protected void init() throws Exception {
    super.init();
    TestUtils.enableNewQueryEngine(client());
  }

  @Test
  public void runAllTests() throws Exception {
    verifyQueries(EXPR_TEST_DIR, expr -> "SELECT " + expr);
    verifyQueries(QUERY_TEST_DIR, Function.identity());
  }

  /**
   * Verify queries in files in directories with a converter to preprocess query.
   * For example, for expressions it is converted to a SELECT clause before testing.
   */
  @SuppressWarnings("UnstableApiUsage")
  private void verifyQueries(String[] dirs, Function<String, String> converter) throws Exception {
    for (String dir : dirs) {
      Path dirPath = Paths.get(Resources.getResource(ROOT_DIR + dir).toURI());
      Files.walk(dirPath)
           .filter(Files::isRegularFile)
           .forEach(file -> verifyQueries(file, converter));
    }
  }

  private void verifyQueries(Path file, Function<String, String> converter) {
    try {
      String[] queries = Files.lines(file)
                              .map(converter)
                              .toArray(String[]::new);
      verify(queries);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to read file: " + file, e);
    }
  }

}
