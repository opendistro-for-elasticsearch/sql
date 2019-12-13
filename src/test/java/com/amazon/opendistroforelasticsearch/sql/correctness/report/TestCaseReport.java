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

public abstract class TestCaseReport {
    private final boolean isSuccess;
    private final String sql;

    public TestCaseReport(boolean isSuccess, String sql) {
        this.isSuccess = isSuccess;
        this.sql = sql;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public JSONObject report() {
        JSONObject report = new JSONObject();
        report.put("result", (isSuccess ? "Success" : "Failed"));
        report.put("sql", sql);
        return report;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestCaseReport that = (TestCaseReport) o;
        return isSuccess == that.isSuccess &&
            sql.equals(that.sql);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isSuccess, sql);
    }

    @Override
    public String toString() {
        return "TestCaseReport{" +
            "isSuccess=" + isSuccess +
            ", sql='" + sql + '\'' +
            '}';
    }
}
