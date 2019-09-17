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

import org.junit.Ignore;
import org.junit.Test;

/**
 * Semantic analyzer tests for identifier
 */
public class SemanticAnalyzerIdentifierTest extends SemanticAnalyzerTestBase {

    @Ignore("To be implemented")
    @Test
    public void duplicateFieldAliasInSelectClauseShouldFail() {
        expectValidationFailWithErrorMessage(
            "SELECT age a, COUNT(*) a FROM semantics s, a.projects p",
            "Field [a.projects] cannot be found or used here"
        );
    }

    @Test
    public void nonExistingFieldNameShouldFail() {
    }
}
