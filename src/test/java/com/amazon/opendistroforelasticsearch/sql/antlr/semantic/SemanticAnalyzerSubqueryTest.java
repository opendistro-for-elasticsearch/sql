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
 * Semantic analysis test for subquery
 */
public class SemanticAnalyzerSubqueryTest extends SemanticAnalyzerTestBase {

    @Test
    public void useExistClauseOnNestedFieldShouldPass() {
        validate(
            "SELECT * FROM semantics AS s WHERE EXISTS " +
            " ( SELECT * FROM s.projects AS p WHERE p.active IS TRUE ) " +
            " AND s.age > 10"
        );
    }

    @Test
    public void useNotExistClauseOnNestedFieldShouldPass() {
        validate(
            "SELECT * FROM semantics AS s WHERE NOT EXISTS " +
            " ( SELECT * FROM s.projects AS p WHERE p.active IS TRUE ) " +
            " AND s.age > 10"
        );
    }

    @Test
    public void useInClauseOnAgeWithIntegerLiteralListShouldPass() {
        validate("SELECT * FROM semantics WHERE age IN (30, 40)");
    }

    @Test
    public void useAliasInSubqueryShouldFail() {
        expectValidationFailWithErrorMessages(
            "SELECT * FROM semantics s WHERE EXISTS (SELECT * FROM s.projects p) AND p.active IS TRUE",
            "Field [p.active] cannot be found or used here.",
            "Did you mean [projects.active]?"
        );
    }

    @Test
    public void useInClauseWithIncompatibleFieldTypesShouldFail() {
        expectValidationFailWithErrorMessages(
            "SELECT * FROM semantics s WHERE age IN (SELECT p.active FROM s.projects p)",
            "Operator [IN] cannot work with [INTEGER, BOOLEAN]."
        );
    }

    @Test
    public void useInClauseWithCompatibleFieldTypesShouldPass() {
        validate("SELECT * FROM semantics s WHERE address IN (SELECT city FROM s.projects p)");
    }

    @Test
    public void useInClauseWithSelectStarShouldFail() {
        expectValidationFailWithErrorMessages(
            "SELECT * FROM semantics s WHERE address IN (SELECT * FROM s.projects p)",
            "Operator [IN] cannot work with [TEXT, (*)]"
        );
    }

}
