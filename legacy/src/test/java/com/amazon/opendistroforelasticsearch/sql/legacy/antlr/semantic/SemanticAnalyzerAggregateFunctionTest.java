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
            "Usage: AVG(NUMBER T) -> DOUBLE"
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

    @Test
    public void useAvgFunctionCallAliasInHavingClauseShouldPass() {
        validate("SELECT city, AVG(age) AS avg FROM semantics GROUP BY city HAVING avg > 10");
    }

    @Test
    public void useAvgAndMaxFunctionCallAliasInHavingClauseShouldPass() {
        validate(
            "SELECT city, AVG(age) AS avg, MAX(balance) AS bal FROM semantics " +
            "GROUP BY city HAVING avg > 10 AND bal > 10000"
        );
    }

    @Test
    public void useAvgFunctionCallWithoutAliasInHavingShouldPass() {
        validate("SELECT city, AVG(age) FROM semantics GROUP BY city HAVING AVG(age) > 10");
    }

    @Test
    public void useDifferentAggregateFunctionInHavingClauseShouldPass() {
        validate("SELECT city, AVG(age) FROM semantics GROUP BY city HAVING COUNT(*) > 10 AND SUM(balance) <= 10000");
    }

    @Test
    public void useAvgFunctionCallAliasInOrderByClauseShouldPass() {
        validate("SELECT city, AVG(age) AS avg FROM semantics GROUP BY city ORDER BY avg");
    }

    @Test
    public void useAvgFunctionCallAliasInGroupByAndOrderByClauseShouldPass() {
        validate("SELECT SUBSTRING(address, 0, 3) AS add FROM semantics GROUP BY add ORDER BY add");
    }

    @Test
    public void useColumnNameAliasInOrderByClauseShouldPass() {
        validate("SELECT age AS a, AVG(balance) FROM semantics GROUP BY age ORDER BY a");
    }

    @Test
    public void useExpressionAliasInOrderByClauseShouldPass() {
        validate("SELECT age + 1 AS a FROM semantics GROUP BY age ORDER BY a");
    }

    @Test
    public void useAvgFunctionCallWithTextFieldInHavingClauseShouldFail() {
        expectValidationFailWithErrorMessages(
            "SELECT city FROM semantics GROUP BY city HAVING AVG(address) > 10",
            "Function [AVG] cannot work with [TEXT].",
            "Usage: AVG(NUMBER T) -> DOUBLE"
        );
    }

    @Test
    public void useCountFunctionCallWithNestedFieldShouldPass() {
        validate("SELECT * FROM semantics s, s.projects p GROUP BY city HAVING COUNT(p) > 1");
        validate("SELECT * FROM semantics s, s.projects p, p.members m GROUP BY city HAVING COUNT(m) > 1");
    }

}
