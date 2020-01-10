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

import static com.amazon.opendistroforelasticsearch.sql.correctness.report.TestCaseReport.TestResult.FAILURE;

/**
 * Report for test case that ends with an error.
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
public class ErrorTestCase extends TestCaseReport {

    /** Root cause of the error */
    private final String reason;

    public ErrorTestCase(int id, String sql, String reason) {
        super(id, sql, FAILURE);
        this.reason = reason;
    }

}
