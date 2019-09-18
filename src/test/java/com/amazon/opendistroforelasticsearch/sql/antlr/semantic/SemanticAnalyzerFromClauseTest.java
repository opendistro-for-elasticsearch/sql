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
 * Semantic analyzer tests for FROM clause, including parse single index, multiple indices,
 * index + (deep) nested field and multiple statements like UNION/MINUS etc. Basically, we
 * need to make sure the environment be set up properly so that semantic analysis followed
 * can be performed correctly.
 */
public class SemanticAnalyzerFromClauseTest extends SemanticAnalyzerTestBase {

    @Ignore("IndexNotFoundException should be thrown from ES API directly")
    @Test
    public void nonExistingIndexNameShouldFail() {
        expectValidationFailWithErrorMessages(
            "SELECT * FROM semantics1",
            ""
        );
    }

    @Test
    public void invalidIndexNameAliasInFromClauseShouldFail() {
        expectValidationFailWithErrorMessages(
            "SELECT * FROM semantics s, a.projects p",
            "Field [a.projects] cannot be found or used here",
            "Did you mean [s.projects]?"
        );
    }

    @Test
    public void invalidIndexNameAliasInWhereClauseShouldFail() {
        expectValidationFailWithErrorMessages(
            "SELECT * FROM semantics s WHERE a.balance = 10000",
            "Field [a.balance] cannot be found or used here",
            "Did you mean [s.balance]?"
        );
    }

    @Test
    public void invalidIndexNameAliasInGroupByClauseShouldFail() {
        expectValidationFailWithErrorMessages(
            "SELECT * FROM semantics s GROUP BY a.balance",
            "Field [a.balance] cannot be found or used here",
            "Did you mean [s.balance]?"
        );
    }

    @Ignore("To be implemented")
    @Test
    public void invalidIndexNameAliasInHavingClauseShouldFail() {
        expectValidationFailWithErrorMessages(
            "SELECT * FROM semantics s HAVING COUNT(a.balance) > 5",
            "Field [a.balance] cannot be found or used here",
            "Did you mean [s.balance]?"
        );
    }

    @Test
    public void invalidIndexNameAliasInOrderByClauseShouldFail() {
        expectValidationFailWithErrorMessages(
            "SELECT * FROM semantics s ORDER BY a.balance",
            "Field [a.balance] cannot be found or used here",
            "Did you mean [s.balance]?"
        );
    }

    @Test
    public void invalidIndexNameAliasInOnClauseShouldFail() {
        expectValidationFailWithErrorMessages(
            "SELECT * FROM semantics sem JOIN semantic tic ON sem.age = t.age",
            "Field [t.age] cannot be found or used here",
            "Did you mean [tic.age]?"
        );
    }

    @Test
    public void nonNestedFieldInFromClauseShouldFail() {
        expectValidationFailWithErrorMessages(
            "SELECT * FROM semantics s, s.manager m",
            "Field [s.manager] is [OBJECT] type but nested type is required"
        );
    }

    @Test
    public void nonExistingNestedFieldInFromClauseShouldFail() {
        expectValidationFailWithErrorMessages(
            "SELECT * FROM semantics s, s.project p",
            "Field [s.project] cannot be found or used here",
            "Did you mean [s.projects]?"
        );
    }

    @Test
    public void duplicateIndexNameAliasInFromClauseShouldFail() {
        expectValidationFailWithErrorMessages(
            "SELECT * FROM semantics s, s.projects s",
            "Field [s] is conflicting with field of same name defined by other index"
        );
    }

    @Test
    public void duplicateFieldNameFromDifferentIndexShouldFail() {
        expectValidationFailWithErrorMessages(
            "SELECT * FROM semantics INNER JOIN semantics",
            "is conflicting with field of same name defined by other index"
        );
    }

    @Test
    public void validIndexNameAliasShouldPass() {
        validate("SELECT * FROM semantics s, s.projects p");
        validate("SELECT * FROM semantics s WHERE s.balance = 10000");
    }

    @Test
    public void noIndexAliasShouldPass() {
        validate("SELECT * FROM semantics");
        //validate("SELECT * FROM semantics, projects"); //TODO: alias should be required for nested field
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

    @Test
    public void duplicateFieldNameFromDifferentStatementShouldPass() {
        validate("SELECT age FROM semantics UNION SELECT age FROM semantic");
        validate("SELECT s.age FROM semantics s UNION SELECT s.age FROM semantic s");
    }

}
