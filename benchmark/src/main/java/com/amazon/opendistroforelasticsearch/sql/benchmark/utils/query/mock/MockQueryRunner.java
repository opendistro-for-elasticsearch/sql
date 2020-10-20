/*
 *   Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.benchmark.utils.query.mock;

import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.query.QueryRunner;

/**
 * Query runner for testing purposes.
 */
public class MockQueryRunner extends QueryRunner {
  private static final long WAIT_TIME_MILLISECONDS = 1000L;

  /**
   * Function mock running query.
   * @param query Query to run.
   */
  @Override
  public void runQuery(final String query) {
    try {
      Thread.sleep(WAIT_TIME_MILLISECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}