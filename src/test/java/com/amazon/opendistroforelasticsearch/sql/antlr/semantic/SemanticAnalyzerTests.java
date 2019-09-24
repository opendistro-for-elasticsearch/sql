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

import com.amazon.opendistroforelasticsearch.sql.esdomain.LocalClusterState;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.io.IOException;
import java.net.URL;

import static com.amazon.opendistroforelasticsearch.sql.util.CheckScriptContents.mockLocalClusterState;

/**
 * Semantic analyzer test suite to prepare mapping and avoid load from file every time.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    SemanticAnalyzerFromClauseTest.class,
    SemanticAnalyzerIdentifierTest.class,
    SemanticAnalyzerScalarFunctionTest.class,
    SemanticAnalyzerESScalarFunctionTest.class,
    //SemanticAnalyzerAggregateFunctionTest.class,
    //SemanticAnalyzerOperatorTest.class,
    //SemanticAnalyzerSubqueryTest.class,
    //SemanticAnalyzerMultiQueryTest.class,
})
public class SemanticAnalyzerTests {

    private static final String TEST_MAPPING_FILE = "mappings/semantics.json";

    @SuppressWarnings("UnstableApiUsage")
    @BeforeClass
    public static void init() throws IOException {
        URL url = Resources.getResource(TEST_MAPPING_FILE);
        String mappings = Resources.toString(url, Charsets.UTF_8);
        mockLocalClusterState(mappings);
    }

    @AfterClass
    public static void cleanUp() {
        LocalClusterState.state(null);
    }

}
