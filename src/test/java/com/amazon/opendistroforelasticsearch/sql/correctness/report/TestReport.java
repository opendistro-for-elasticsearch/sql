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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Test report class to generate JSON report.
 */
public class TestReport {

    private final List<TestCaseReport> testCases = new ArrayList<>();

    public void addTestCase(TestCaseReport testCase) {
        testCases.add(testCase);
    }

    public String report() {
        JSONObject report = new JSONObject();
        JSONArray tests = new JSONArray();
        report.put("tests", tests);

        int success = 0, failure = 0;
        for (int i = 0; i < testCases.size(); i++) {
            TestCaseReport testCase = testCases.get(i);
            if (testCase.isSuccess()) {
                success++;
            } else {
                failure++;
            }

            JSONObject test = testCase.report();
            test.put("id", i + 1);
            tests.put(test);
        }

        JSONObject summary = new JSONObject();
        summary.put("total", testCases.size());
        summary.put("success", success);
        summary.put("failure", failure);
        report.put("summary", summary);
        return report.toString(2);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestReport that = (TestReport) o;
        return testCases.equals(that.testCases);
    }

    @Override
    public int hashCode() {
        return Objects.hash(testCases);
    }

    @Override
    public String toString() {
        return "TestReport{" +
            "testCases=" + testCases +
            '}';
    }
}
