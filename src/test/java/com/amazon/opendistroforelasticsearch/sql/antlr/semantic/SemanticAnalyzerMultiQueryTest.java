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

import org.junit.Test;

/**
 * Semantic analyzer tests for multi query like UNION and MINUS
 */
public class SemanticAnalyzerMultiQueryTest extends SemanticAnalyzerTestBase {

    @Test
    public void unionDifferentResultTypeOfTwoQueriesShouldFail() {
        expectValidationFailWithErrorMessages(
            "SELECT balance FROM semantics UNION SELECT address FROM semantics"
        );
    }

    @Test
    public void unionDifferentNumberOfResultTypeOfTwoQueriesShouldFail() {
        expectValidationFailWithErrorMessages(
            "SELECT balance FROM semantics UNION SELECT balance, age FROM semantics"
        );
    }

    @Test
    public void unionSameResultTypeOfTwoQueriesShouldPass() {
        validate("SELECT balance FROM semantics UNION SELECT balance FROM semantics");
    }

    @Test
    public void unionCompatibleResultTypeOfTwoQueriesShouldPass() {
        validate("SELECT balance FROM semantics UNION SELECT age FROM semantics");
        validate("SELECT address FROM semantics UNION ALL SELECT city FROM semantics");
    }

}
