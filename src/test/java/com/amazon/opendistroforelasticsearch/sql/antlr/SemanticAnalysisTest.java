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

import com.amazon.opendistroforelasticsearch.sql.antlr.semantic.SqlSemanticAnalysisException;
import com.amazon.opendistroforelasticsearch.sql.esdomain.LocalClusterState;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static com.amazon.opendistroforelasticsearch.sql.util.CheckScriptContents.mockLocalClusterState;

/**
 * Test cases for semantic analysis focused on semantic check which was missing in the past.
 */
public class SemanticAnalysisTest {

    private OpenDistroSqlAnalyzer analyzer = new OpenDistroSqlAnalyzer();

    @SuppressWarnings("UnstableApiUsage")
    @Before
    public void init() throws IOException {
        //String mappings = Files.toString(new File("mappings/semantic_test.json"), Charsets.UTF_8);
        URL url = Resources.getResource("mappings/semantic_test.json");
        String mappings = Resources.toString(url, Charsets.UTF_8);
        mockLocalClusterState(mappings);
    }

    @After
    public void cleanUp() {
        LocalClusterState.state(null);
    }

    @Ignore("To be implemented")
    @Test(expected = SqlSemanticAnalysisException.class)
    public void nonExistingIndexNameShouldFail() {
        analyze("SELECT * FROM semantics1");
    }

    @Ignore("To be implemented")
    @Test(expected = SqlSemanticAnalysisException.class)
    public void nonNestedFieldInFromClauseShouldFail() {
    }

    @Test
    public void regularTwoIndexJoinShouldPass() {
    }

    @Test
    public void deepNestedFieldInFromClauseShouldPass() {
    }

    @Ignore("To be implemented")
    @Test(expected = SqlSemanticAnalysisException.class)
    public void nonExistingFieldNameShouldFail() {
    }

    @Ignore("To be implemented")
    @Test(expected = SqlSemanticAnalysisException.class)
    public void unsupportedFunctionInSelectClauseShouldFail() {
        analyze("SELECT NOW() FROM semantics");
    }

    @Ignore("To be implemented")
    @Test(expected = SqlSemanticAnalysisException.class)
    public void unsupportedFunctionInWhereClauseShouldFail() {
        //analyze("SELECT * FROM semantics WHERE ");
    }

    @Ignore("To be implemented")
    @Test(expected = SqlSemanticAnalysisException.class)
    public void useAggregateFunctionInWhereClauseShouldFail() {
        analyze("SELECT * FROM semantics WHERE AVG(balance) > 10000");
    }

    private void analyze(String sql) {
        analyzer.analyze(sql);
    }
}
