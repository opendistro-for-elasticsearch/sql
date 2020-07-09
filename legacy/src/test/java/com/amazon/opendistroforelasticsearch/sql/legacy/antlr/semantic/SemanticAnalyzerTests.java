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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Semantic analyzer test suite to prepare mapping and avoid load from file every time.
 * But Gradle seems not work well with suite. So move common logic to test base class
 * and keep this for quick testing in IDE.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    SemanticAnalyzerBasicTest.class,
    SemanticAnalyzerConfigTest.class,
    SemanticAnalyzerFromClauseTest.class,
    SemanticAnalyzerIdentifierTest.class,
    SemanticAnalyzerScalarFunctionTest.class,
    SemanticAnalyzerESScalarFunctionTest.class,
    SemanticAnalyzerAggregateFunctionTest.class,
    SemanticAnalyzerOperatorTest.class,
    SemanticAnalyzerSubqueryTest.class,
    SemanticAnalyzerMultiQueryTest.class,
})
public class SemanticAnalyzerTests {
}
