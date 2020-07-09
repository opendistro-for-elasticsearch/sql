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

package com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic;

import com.amazon.opendistroforelasticsearch.sql.legacy.antlr.OpenDistroSqlAnalyzer;
import com.amazon.opendistroforelasticsearch.sql.legacy.antlr.SqlAnalysisConfig;
import com.amazon.opendistroforelasticsearch.sql.legacy.esdomain.LocalClusterState;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

/**
 * Test cases for semantic analysis configuration
 */
public class SemanticAnalyzerConfigTest extends SemanticAnalyzerTestBase {

    @Rule
    public ExpectedException exceptionWithoutSuggestion = ExpectedException.none();

    @Test
    public void noAnalysisShouldPerformForNonSelectStatement() {
        String sql = "DELETE FROM semantics WHERE age12 = 123";
        expectValidationPassWithConfig(sql, new SqlAnalysisConfig(true, true, 1000));
    }

    @Test
    public void noAnalysisShouldPerformIfDisabledAnalysis() {
        String sql = "SELECT * FROM semantics WHERE age12 = 123";
        expectValidationFailWithErrorMessages(sql, "Field [age12] cannot be found or used here.");
        expectValidationPassWithConfig(sql, new SqlAnalysisConfig(false, true, 1000));
    }

    @Test
    public void noFieldNameSuggestionIfDisabledSuggestion() {
        String sql = "SELECT * FROM semantics WHERE age12 = 123";
        expectValidationFailWithErrorMessages(sql,
            "Field [age12] cannot be found or used here.",
            "Did you mean [age]?");

        exceptionWithoutSuggestion.expect(SemanticAnalysisException.class);
        exceptionWithoutSuggestion.expectMessage(
            allOf(
                containsString("Field [age12] cannot be found or used here"),
                not(containsString("Did you mean"))
            )
        );
        new OpenDistroSqlAnalyzer(new SqlAnalysisConfig(true, false, 1000)).
            analyze(sql, LocalClusterState.state());
    }

    @Test
    public void noAnalysisShouldPerformIfIndexMappingIsLargerThanThreshold() {
        String sql = "SELECT * FROM semantics WHERE test = 123";
        expectValidationFailWithErrorMessages(sql, "Field [test] cannot be found or used here.");
        expectValidationPassWithConfig(sql, new SqlAnalysisConfig(true, true, 1));
    }

    private void expectValidationPassWithConfig(String sql, SqlAnalysisConfig config) {
        new OpenDistroSqlAnalyzer(config).analyze(sql, LocalClusterState.state());
    }

}
