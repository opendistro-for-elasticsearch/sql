/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic;

import org.junit.Before;
import org.junit.Test;

import static com.amazon.opendistroforelasticsearch.sql.legacy.util.MultipleIndexClusterUtils.mockMultipleIndexEnv;

public class SemanticAnalyzerFieldTypeTest extends SemanticAnalyzerTestBase {
    @Before
    public void setup() {
        mockMultipleIndexEnv();
    }

    /**
     * id has same type in account1 and account2.
     */
    @Test
    public void accessFieldTypeNotInQueryPassSemanticCheck() {
        validate("SELECT id FROM account* WHERE id = 1");
    }

    /**
     * address doesn't exist in account1.
     */
    @Test
    public void accessFieldTypeOnlyInOneIndexPassSemanticCheck() {
        validate("SELECT address FROM account* WHERE id = 30");
    }

    /**
     * age has different type in account1 and account2.
     */
    @Test
    public void accessConflictFieldTypeShouldFailSemanticCheck() {
        expectValidationFailWithErrorMessages("SELECT age FROM account* WHERE age = 30",
                "Field [age] have conflict type");
    }

    /**
     * age has different type in account1 and account2.
     */
    @Test
    public void mixNonConflictTypeAndConflictFieldTypeShouldFailSemanticCheck() {
        expectValidationFailWithErrorMessages("SELECT id, age FROM account* WHERE id = 1",
                "Field [age] have conflict type");
    }

    /**
     * age has different type in account1 and account2.
     */
    @Test
    public void conflictFieldTypeWithAliasShouldFailSemanticCheck() {
        expectValidationFailWithErrorMessages("SELECT a.age FROM account* as a",
                "Field [a.age] have conflict type");
    }

    /**
     * age has different type in account1 and account2.
     * Todo, the error message is not accurate.
     */
    @Test
    public void selectAllFieldTypeShouldFailSemanticCheck() {
        expectValidationFailWithErrorMessages("SELECT * FROM account*",
                "Field [account*.age] have conflict type");
    }

    /**
     * age has different type in account1 and account2.
     */
    @Test
    public void selectAllFieldTypeWithAliasShouldFailSemanticCheck() {
        expectValidationFailWithErrorMessages("SELECT a.* FROM account* as a",
                "Field [a.age] have conflict type");
    }

    /**
     * a.projects.name has same type in account1 and account2.
     */
    @Test
    public void selectNestedNoneConflictTypeShouldPassSemanticCheck() {
        validate("SELECT a.projects.name FROM account* as a");
    }

    /**
     * a.projects.started_year has conflict type in account1 and account2.
     */
    @Test
    public void selectNestedConflictTypeShouldFailSemanticCheck() {
        expectValidationFailWithErrorMessages("SELECT a.projects.started_year FROM account* as a",
                "Field [a.projects.started_year] have conflict type");
    }
}
