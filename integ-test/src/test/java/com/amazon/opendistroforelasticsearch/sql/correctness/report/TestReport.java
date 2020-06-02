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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * Test report class to generate JSON report.
 */
@EqualsAndHashCode
@ToString
@Getter
public class TestReport {

    private final TestSummary summary = new TestSummary();

    private final List<TestCaseReport> tests = new ArrayList<>();

    /**
     * Add a test case report to the whole report.
     * @param testCase  report for a single test case
     */
    public void addTestCase(TestCaseReport testCase) {
        tests.add(testCase);
        if ("Success".equals(testCase.getResult())) {
            summary.addSuccess();
        } else {
            summary.addFailure();
        }
    }

}
