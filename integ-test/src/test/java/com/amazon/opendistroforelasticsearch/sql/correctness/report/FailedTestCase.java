/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazon.opendistroforelasticsearch.sql.correctness.report;

import static com.amazon.opendistroforelasticsearch.sql.correctness.report.TestCaseReport.TestResult.FAILURE;

import com.amazon.opendistroforelasticsearch.sql.correctness.runner.resultset.DBResult;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Report for test case that fails due to inconsistent result set.
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
public class FailedTestCase extends TestCaseReport {

  /**
   * Inconsistent result sets for reporting
   */
  private final List<DBResult> resultSets;

  /**
   * Explain where the difference is caused the test failure.
   */
  private final String explain;

  /**
   * Errors occurred for partial other databases.
   */
  private final String errors;


  public FailedTestCase(int id, String sql, List<DBResult> resultSets, String errors) {
    super(id, sql, FAILURE);
    this.resultSets = resultSets;
    this.resultSets.sort(Comparator.comparing(DBResult::getDatabaseName));
    this.errors = errors;

    // Generate explanation by diff the first result with remaining
    this.explain = resultSets.subList(1, resultSets.size())
                             .stream()
                             .map(result -> resultSets.get(0).diff(result))
                             .collect(Collectors.joining(", "));
  }

}
