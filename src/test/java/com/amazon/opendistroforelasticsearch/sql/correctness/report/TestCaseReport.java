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

import static com.amazon.opendistroforelasticsearch.sql.correctness.report.TestCaseReport.TestResult.SUCCESS;

/**
 * Base class for different test result.
 */
@EqualsAndHashCode
@ToString
public abstract class TestCaseReport {

    public enum TestResult {
        SUCCESS, FAILURE;
    }

    @Getter
    private final int id;

    @Getter
    private final String sql;

    private final TestResult result;

    public TestCaseReport(int id, String sql, TestResult result) {
        this.id = id;
        this.sql = sql;
        this.result = result;
    }

    public String getResult() {
        return result == SUCCESS ? "Success" : "Failed";
    }

}
