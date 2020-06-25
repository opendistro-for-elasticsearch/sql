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

import com.google.common.io.Resources;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import org.junit.Test;

/**
 * SQL integration test automated by comparison test framework.
 */
public class SQLCorrectnessIT extends SQLIntegTestCase {

  private static final String ROOT_DIR = "correctness/";
  private static final String[] EXPR_TEST_DIR = { "expressions" };
  private static final String[] QUERY_TEST_DIR = { "queries"/*, "bugfixes"*/ };

  @Test
  public void runAllTests() throws Exception {
    iterateAllFiles(EXPR_TEST_DIR, expr -> verify("SELECT " + expr));
    iterateAllFiles(QUERY_TEST_DIR, this::verify);
  }

  @SuppressWarnings("UnstableApiUsage")
  private void iterateAllFiles(String[] dirs, Consumer<String> verify) throws Exception {
    for (String dir : dirs) {
      Path dirPath = Paths.get(Resources.getResource(ROOT_DIR + dir).toURI());
      Files.walk(dirPath).filter(Files::isRegularFile).forEach(file -> {
        try {
          Files.lines(file).forEach(verify);
        } catch (IOException e) {
          throw new IllegalStateException("Failed to read file: " + file, e);
        }
      });
    }
  }

}
