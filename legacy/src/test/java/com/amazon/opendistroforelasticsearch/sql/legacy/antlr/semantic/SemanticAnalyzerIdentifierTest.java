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

import org.junit.Ignore;
import org.junit.Test;

/**
 * Semantic analyzer tests for identifier
 */
public class SemanticAnalyzerIdentifierTest extends SemanticAnalyzerTestBase {

    @Ignore("To be implemented")
    @Test
    public void duplicateFieldAliasInSelectClauseShouldFail() {
        expectValidationFailWithErrorMessages(
            "SELECT age a, COUNT(*) a FROM semantics s, a.projects p",
            "Field [a.projects] cannot be found or used here"
        );
    }

    @Test
    public void fieldWithDifferentCaseInSelectClauseShouldFail() {
        expectValidationFailWithErrorMessages(
            "SELECT Age a FROM semantics",
            "Field [Age] cannot be found or used here",
            "Did you mean [age]?"
        );
    }

    @Test
    public void useHiddenFieldShouldPass() {
        validate("SELECT _score FROM semantics WHERE _id = 1 AND _type = '_doc'");
    }

    @Ignore("Need to remove single quote or back ticks")
    @Test
    public void useFieldNameWithSpaceShouldPass() {
        validate("SELECT ['field with spaces'] FROM semantics");
        validate("SELECT `field with spaces` FROM semantics");
    }

    @Test
    public void nonExistingFieldNameInSelectClauseShouldFail() {
        expectValidationFailWithErrorMessages(
            "SELECT age1 FROM semantics s",
            "Field [age1] cannot be found or used here.",
            "Did you mean [age]?"
        );
    }

    @Test
    public void invalidIndexAliasInFromClauseShouldFail() {
        expectValidationFailWithErrorMessages(
            "SELECT * FROM semantics s, a.projects p",
            "Field [a.projects] cannot be found or used here.",
            "Did you mean [s.projects]?"
        );
    }

    @Test
    public void nonExistingFieldNameInWhereClauseShouldFail() {
        expectValidationFailWithErrorMessages(
            "SELECT * FROM semantics s WHERE s.balce = 10000",
            "Field [s.balce] cannot be found or used here.",
            "Did you mean [s.balance]?"
        );
    }

    @Test
    public void nonExistingFieldNameInGroupByClauseShouldFail() {
        expectValidationFailWithErrorMessages(
            "SELECT * FROM semantics s GROUP BY s.balce",
            "Field [s.balce] cannot be found or used here.",
            "Did you mean [s.balance]?"
        );
    }

    @Test
    public void nonExistingFieldNameInHavingClauseShouldFail() {
        expectValidationFailWithErrorMessages(
            "SELECT * FROM semantics s HAVING COUNT(s.balce) > 5",
            "Field [s.balce] cannot be found or used here.",
            "Did you mean [s.balance]?"
        );
    }

    @Test
    public void nonExistingFieldNameInOrderByClauseShouldFail() {
        expectValidationFailWithErrorMessages(
            "SELECT * FROM semantics s ORDER BY s.balce",
            "Field [s.balce] cannot be found or used here.",
            "Did you mean [s.balance]?"
        );
    }

    @Test
    public void nonExistingFieldNameInFunctionShouldFail() {
        expectValidationFailWithErrorMessages(
            "SELECT * FROM semantics s WHERE LOG(s.balce) = 1",
            "Field [s.balce] cannot be found or used here.",
            "Did you mean [s.balance]?"
        );
    }

    @Test
    public void nonExistingNestedFieldNameInWhereClauseShouldFail() {
        expectValidationFailWithErrorMessages(
            "SELECT * FROM semantics s, s.projects p, p.members m WHERE m.nam = 'John'",
            "Field [m.nam] cannot be found or used here.",
            "Did you mean [m.name]?"
        );
    }

    @Test
    public void nonExistingNestedFieldNameInFunctionShouldFail() {
        expectValidationFailWithErrorMessages(
            "SELECT * FROM semantics WHERE nested(projects.actives) = TRUE",
            "Field [projects.actives] cannot be found or used here.",
            "Did you mean [projects.active]?"
        );
    }

    @Test
    public void useKeywordInMultiFieldShouldPass() {
        validate("SELECT employer.keyword FROM semantics WHERE employer.keyword LIKE 'AWS' GROUP BY employer.keyword");
        validate("SELECT * FROM semantics s WHERE s.manager.name.keyword LIKE 'John'");
    }

    @Test
    public void useDeepNestedFieldNameShouldPass() {
        validate("SELECT p.* FROM semantics s, s.projects p WHERE p IS NULL");
        validate("SELECT p.active FROM semantics s, s.projects p WHERE p.active = TRUE");
        validate("SELECT m.name FROM semantics s, s.projects p, p.members m WHERE m.name = 'John'");
    }

    @Test
    public void useConstantLiteralInSelectClauseShouldPass() {
        validate("SELECT 1 FROM semantics");
        validate("SELECT 2.0 FROM semantics");
        //validate("SELECT 'test' FROM semantics"); TODO: why 'test' goes to fullColumnName that can be string literal
        validate("SELECT TRUE FROM semantics");
    }

    @Test
    public void queryWithBackticksQuotedIndexShouldPass() {
        validate("SELECT age FROM `semantics`");
    }

    @Test
    public void queryWithBackticksQuotedIndexAliasShouldPass() {
        validate("SELECT `s`.age FROM semantics AS `s`");
        validate("SELECT `s t`.age FROM semantics AS `s t`");
    }

    @Test
    public void queryWithBackticksQuotedFieldNameShouldPass() {
        validate("SELECT `age` FROM semantics");
        validate("SELECT s.`age` FROM semantics AS s");
        validate("SELECT `s`.`age` FROM semantics AS `s`");
    }

    @Test
    public void queryWithBackticksQuotedFieldNameInFunctionShouldPass() {
        validate("SELECT SUM(`age`) FROM semantics");
    }
}
