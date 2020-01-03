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

import com.amazon.opendistroforelasticsearch.sql.correctness.runner.resultset.DBResult;
import com.amazon.opendistroforelasticsearch.sql.correctness.runner.resultset.Row;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Report for test case that fails due to inconsistent result set.
 */
public class FailedTestCase extends TestCaseReport {

    /** Inconsistent result set for reporting */
    private final List<DBResult> results;

    public FailedTestCase(String sql, List<DBResult> results) {
        super(false, sql);
        this.results = results;
        this.results.sort(Comparator.comparing(DBResult::getDatabaseName));
    }

    @Override
    public JSONObject report() {
        JSONObject report = super.report();
        JSONArray resultSets = new JSONArray();
        report.put("resultSets", resultSets);

        for (DBResult result : results) {
            JSONObject json = new JSONObject();
            json.put("database", result.getDatabaseName());
            resultSets.put(json);

            JSONObject resultSet = new JSONObject();
            json.put("resultSet", resultSet);

            resultSet.put("schema", new JSONArray());
            result.getColumnNameAndTypes().forEach((name, type) -> {
                JSONObject nameAndType = new JSONObject();
                nameAndType.put("name", name);
                nameAndType.put("type", type);
                resultSet.getJSONArray("schema").put(nameAndType);
            });

            resultSet.put("dataRows", new JSONArray());
            for (Row row : result.getDataRows()) {
                resultSet.getJSONArray("dataRows").put(row.getValues());
            }
        }
        return report;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        FailedTestCase that = (FailedTestCase) o;
        return results.equals(that.results);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), results);
    }

    @Override
    public String toString() {
        return "FailedTestCase{" +
            "results=" + results +
            '}';
    }
}
