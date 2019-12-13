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

import org.json.JSONObject;

import java.util.Objects;

/**
 * Report for test case that ends with an error.
 */
public class ErrorTestCase extends TestCaseReport {

    /** Root cause of the error */
    private final String reason;

    public ErrorTestCase(String sql, String reason) {
        super(false, sql);
        this.reason = reason;
    }

    @Override
    public JSONObject report() {
        JSONObject report = super.report();
        report.put("reason", reason);
        return report;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ErrorTestCase that = (ErrorTestCase) o;
        return reason.equals(that.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), reason);
    }

    @Override
    public String toString() {
        return "ErrorTestCase{" +
            "reason='" + reason + '\'' +
            '}';
    }
}
