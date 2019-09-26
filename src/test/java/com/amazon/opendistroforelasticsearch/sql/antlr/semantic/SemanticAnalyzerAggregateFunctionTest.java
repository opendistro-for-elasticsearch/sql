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
 * Semantic analysis test for aggregate functions.
 */
public class SemanticAnalyzerAggregateFunctionTest extends SemanticAnalyzerTestBase {

    @Ignore("To be implemented")
    @Test(expected = SemanticAnalysisException.class)
    public void useAggregateFunctionInWhereClauseShouldFail() {
        validate("SELECT * FROM semantics WHERE AVG(balance) > 10000");
    }

    @Test
    public void useAggregateFunctionInSelectClauseShouldPass() {
        validate(
            "SELECT" +
            " city," +
            " COUNT(*)," +
            " MAX(age)," +
            " MIN(balance)," +
            " AVG(manager.salary)," +
            " SUM(balance)" +
            "FROM semantics " +
            "GROUP BY city");
    }

    @Test
    public void useAggregateFunctionInSelectClauseWithoutGroupByShouldPass() {
        validate(
            "SELECT" +
            " COUNT(*)," +
            " MAX(age)," +
            " MIN(balance)," +
            " AVG(manager.salary)," +
            " SUM(balance)" +
            "FROM semantics");
    }

    @Test
    public void countFunctionCallOnAnyFieldShouldPass() {
        validate(
            "SELECT" +
            " COUNT(address)," +
            " COUNT(age)," +
            " COUNT(birthday)," +
            " COUNT(location)," +
            " COUNT(manager.address)," +
            " COUNT(employer)" +
            "FROM semantics");
    }

    @Test
    public void maxFunctionCallOnTextFieldShouldFail() {
        expectValidationFailWithErrorMessages(
            "SELECT MAX(address) FROM semantics",
            "Function [MAX] cannot work with [TEXT].",
            "Usage: MAX(NUMBER T) -> T"
        );
    }

    @Test
    public void minFunctionCallOnDateFieldShouldFail() {
        expectValidationFailWithErrorMessages(
            "SELECT MIN(birthday) FROM semantics",
            "Function [MIN] cannot work with [DATE].",
            "Usage: MIN(NUMBER T) -> T"
        );
    }

    @Test
    public void avgFunctionCallOnBooleanFieldShouldFail() {
        expectValidationFailWithErrorMessages(
            "SELECT AVG(p.active) FROM semantics s, s.projects p",
            "Function [AVG] cannot work with [BOOLEAN].",
            "Usage: AVG(NUMBER T) -> T"
        );
    }

    @Test
    public void sumFunctionCallOnBooleanFieldShouldFail() {
        expectValidationFailWithErrorMessages(
            "SELECT SUM(city) FROM semantics",
            "Function [SUM] cannot work with [KEYWORD].",
            "Usage: SUM(NUMBER T) -> T"
        );
    }

}
