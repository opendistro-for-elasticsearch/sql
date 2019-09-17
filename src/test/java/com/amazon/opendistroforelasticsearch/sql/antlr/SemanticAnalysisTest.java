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

package com.amazon.opendistroforelasticsearch.sql.antlr;

import com.amazon.opendistroforelasticsearch.sql.antlr.semantic.SemanticAnalysisException;
import com.amazon.opendistroforelasticsearch.sql.antlr.syntax.SqlSyntaxAnalysisException;
import com.amazon.opendistroforelasticsearch.sql.esdomain.LocalClusterState;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.net.URL;

import static com.amazon.opendistroforelasticsearch.sql.util.CheckScriptContents.mockLocalClusterState;

/**
 * Test cases for semantic analysis focused on semantic check which was missing in the past.
 */
public class SemanticAnalysisTest {

    /** public accessor is required by @Rule annotation */
    @Rule
    public ExpectedException exception = ExpectedException.none();

    private static final String TEST_MAPPING_FILE = "mappings/semantic_test.json";

    private OpenDistroSqlAnalyzer analyzer = new OpenDistroSqlAnalyzer();

    @SuppressWarnings("UnstableApiUsage")
    @Before
    public void init() throws IOException {
        URL url = Resources.getResource(TEST_MAPPING_FILE);
        String mappings = Resources.toString(url, Charsets.UTF_8);
        mockLocalClusterState(mappings);
    }

    @After
    public void cleanUp() {
        LocalClusterState.state(null);
    }

    @Ignore("IndexNotFoundException should be thrown from ES API directly")
    @Test
    public void nonExistingIndexNameShouldFail() {
        expectValidationFailWithErrorMessage(
            "SELECT * FROM semantics1",
            ""
        );
    }

    @Test
    public void invalidIndexNameAliasInFromClauseShouldFail() {
        expectValidationFailWithErrorMessage(
            "SELECT * FROM semantics s, a.projects p",
            "Field [a.projects] cannot be found or used here"
        );
    }

    @Test
    public void invalidIndexNameAliasInWhereClauseShouldFail() {
        expectValidationFailWithErrorMessage(
            "SELECT * FROM semantics s WHERE a.balance = 10000",
            "Field [a.balance] cannot be found or used here"
        );
    }

    @Test
    public void nonNestedFieldInFromClauseShouldFail() {
        expectValidationFailWithErrorMessage(
            "SELECT * FROM semantics s, s.manager m",
            "Field [s.manager] is [OBJECT] type but nested type is required"
        );
    }

    @Test
    public void duplicateIndexNameAliasInFromClauseShouldFail() {
        expectValidationFailWithErrorMessage(
            "SELECT * FROM semantics s, s.projects s",
            "Field [s] is conflicting with field of same name defined by other index"
        );
    }

    @Ignore("To be implemented")
    @Test
    public void duplicateFieldAliasInSelectClauseShouldFail() {
        expectValidationFailWithErrorMessage(
            "SELECT age a, COUNT(*) a FROM semantics s, a.projects p",
            "Field [a.projects] cannot be found or used here"
        );
    }

    @Test
    public void validIndexNameAliasShouldPass() {
        validate("SELECT * FROM semantics s, s.projects p");
        validate("SELECT * FROM semantics s WHERE s.balance = 10000");
    }

    @Test
    public void regularJoinShouldPass() {
        validate("SELECT * FROM semantics s1, semantics s2");
        validate("SELECT * FROM semantics s1 JOIN semantics s2");
        validate("SELECT * FROM semantics s1 LEFT JOIN semantics s2 ON s1.balance = s2.balance");
    }

    @Test
    public void deepNestedFieldInFromClauseShouldPass() {
        validate("SELECT * FROM semantics s, s.projects p, p.members m");
    }

    @Ignore("To be implemented")
    @Test(expected = SemanticAnalysisException.class)
    public void nonExistingFieldNameShouldFail() {
    }

    @Ignore("To be implemented")
    @Test(expected = SemanticAnalysisException.class)
    public void unsupportedFunctionInSelectClauseShouldFail() {
        validate("SELECT NOW() FROM semantics");
    }

    @Ignore("To be implemented")
    @Test(expected = SemanticAnalysisException.class)
    public void unsupportedFunctionInWhereClauseShouldFail() {
        //analyze("SELECT * FROM semantics WHERE ");
    }

    @Ignore("To be implemented")
    @Test(expected = SemanticAnalysisException.class)
    public void useAggregateFunctionInWhereClauseShouldFail() {
        validate("SELECT * FROM semantics WHERE AVG(balance) > 10000");
    }

    private void expectValidationFailWithErrorMessage(String query, String message) {
        exception.expect(SemanticAnalysisException.class);
        exception.expectMessage(Matchers.containsString(message));
        validate(query);
    }

    private void validate(String sql) {
        analyzer.analyzeSemantic(analyzer.analyzeSyntax(sql), LocalClusterState.state());
    }
}
