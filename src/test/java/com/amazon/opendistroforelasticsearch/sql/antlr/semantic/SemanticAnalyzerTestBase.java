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

package com.amazon.opendistroforelasticsearch.sql.antlr.semantic;

import com.amazon.opendistroforelasticsearch.sql.antlr.OpenDistroSqlAnalyzer;
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
public abstract class SemanticAnalyzerTestBase {

    /** public accessor is required by @Rule annotation */
    @Rule
    public ExpectedException exception = ExpectedException.none();

    private OpenDistroSqlAnalyzer analyzer = new OpenDistroSqlAnalyzer();

    protected void expectValidationFailWithErrorMessage(String query, String message) {
        exception.expect(SemanticAnalysisException.class);
        exception.expectMessage(Matchers.containsString(message));
        validate(query);
    }

    protected void validate(String sql) {
        analyzer.analyzeSemantic(analyzer.analyzeSyntax(sql), LocalClusterState.state());
    }
}
