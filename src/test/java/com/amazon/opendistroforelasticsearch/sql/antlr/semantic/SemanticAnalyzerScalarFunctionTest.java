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
 * Semantic analysis tests for scalar function.
 */
public class SemanticAnalyzerScalarFunctionTest extends SemanticAnalyzerTestBase {

    @Test
    public void unsupportedScalarFunctionCallInSelectClauseShouldFail() {
        expectValidationFailWithErrorMessages(
            "SELECT NOW() FROM semantics",
            "Function [NOW] cannot be found or used here."
        );
    }

    @Test
    public void unsupportedScalarFunctionCallInWhereClauseShouldFail() {
        expectValidationFailWithErrorMessages(
            "SELECT * FROM semantics WHERE LOG100(balance) = 1",
            "Function [LOG100] cannot be found or used here.",
            "Did you mean [LOG]?"
        );
    }

    @Test
    public void scalarFunctionCallWithLessArgumentsInWhereClauseShouldFail() {
        expectValidationFailWithErrorMessages(
            "SELECT * FROM semantics WHERE LOG() = 1",
            "Function [LOG] cannot work with [<None>].",
            "Usage: LOG(NUMBER) -> NUMBER"
        );
    }

    @Ignore
    @Test
    public void scalarFunctionCallWithWrongMoreArgumentsInWhereClauseShouldFail() {
        expectValidationFailWithErrorMessages(
            "SELECT * FROM semantics WHERE LOG(age, city) = 1",
            "Function [LOG] cannot work with [INTEGER, KEYWORD].",
            "Usage: LOG(NUMBER) -> NUMBER"
        );
    }

    @Test
    public void logFunctionCallShouldPass() {
        validate("SELECT LOG(age) FROM semantics WHERE LOG(balance) = 1000");
        validate("SELECT LOG(s.age) FROM semantics s WHERE LOG(s.balance) = 1000");
    }

    @Ignore
    @Test
    public void allSupportedScalarFunctionCallShouldPass() {

    }

}
