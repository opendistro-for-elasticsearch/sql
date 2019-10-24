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
            "Did you mean [LOG10]?"
        );
    }

    @Test
    public void scalarFunctionCallWithLessArgumentsInWhereClauseShouldFail() {
        expectValidationFailWithErrorMessages(
            "SELECT * FROM semantics WHERE LOG() = 1",
            "Function [LOG] cannot work with [<None>].",
            "Usage: LOG(NUMBER T) -> T"
        );
    }

    @Test
    public void scalarFunctionCallWithMoreArgumentsInWhereClauseShouldFail() {
        expectValidationFailWithErrorMessages(
            "SELECT * FROM semantics WHERE LOG(age, city) = 1",
            "Function [LOG] cannot work with [INTEGER, KEYWORD].",
            "Usage: LOG(NUMBER T) -> T"
        );
    }

    @Test
    public void logFunctionCallWithOneNestedInSelectClauseShouldFail() {
        expectValidationFailWithErrorMessages(
            "SELECT LOG(projects) FROM semantics",
            "Function [LOG] cannot work with [NESTED_FIELD].",
            "Usage: LOG(NUMBER T) -> T"
        );
    }

    @Test
    public void logFunctionCallWithOneTextInWhereClauseShouldFail() {
        expectValidationFailWithErrorMessages(
            "SELECT * FROM semantics WHERE LOG(city) = 1",
            "Function [LOG] cannot work with [KEYWORD].",
            "Usage: LOG(NUMBER T) -> T"
        );
    }

    @Test
    public void logFunctionCallWithOneNumberShouldPass() {
        validate("SELECT LOG(age) FROM semantics");
        validate("SELECT * FROM semantics s WHERE LOG(s.balance) = 1000");
        validate("SELECT LOG(s.manager.salary) FROM semantics s");
    }

    @Test
    public void logFunctionCallInDifferentCaseShouldPass() {
        validate("SELECT log(age) FROM semantics");
        validate("SELECT Log(age) FROM semantics");
        validate("SELECT loG(age) FROM semantics");
    }

    @Test
    public void logFunctionCallWithUnknownFieldShouldPass() {
        validate("SELECT LOG(new_field) FROM semantics");
    }

    @Test
    public void substringWithLogFunctionCallWithUnknownFieldShouldPass() {
        validate("SELECT SUBSTRING(LOG(new_field), 0, 1) FROM semantics");
    }

    @Test
    public void logFunctionCallWithResultOfAbsFunctionCallWithOneNumberShouldPass() {
        validate("SELECT LOG(ABS(age)) FROM semantics");
    }

    @Test
    public void logFunctionCallWithMoreNestedFunctionCallWithOneNumberShouldPass() {
        validate("SELECT LOG(ABS(SQRT(balance))) FROM semantics");
    }

    @Test
    public void substringFunctionCallWithResultOfAnotherSubstringAndAbsFunctionCallShouldPass() {
        validate("SELECT SUBSTRING(SUBSTRING(city, ABS(age), 1), 2, ABS(1)) FROM semantics");
    }

    @Test
    public void substringFunctionCallWithResultOfMathFunctionCallShouldFail() {
        expectValidationFailWithErrorMessages(
            "SELECT SUBSTRING(LOG(balance), 2, 3) FROM semantics",
            "Function [SUBSTRING] cannot work with [DOUBLE, INTEGER, INTEGER].",
            "Usage: SUBSTRING(STRING T, INTEGER, INTEGER) -> T"
        );
    }

    @Test
    public void logFunctionCallWithResultOfSubstringFunctionCallShouldFail() {
        expectValidationFailWithErrorMessages(
            "SELECT LOG(SUBSTRING(address, 0, 1)) FROM semantics",
            "Function [LOG] cannot work with [TEXT].",
            "Usage: LOG(NUMBER T) -> T or LOG(NUMBER T, NUMBER) -> T"
        );
    }

    @Test
    public void allSupportedMathFunctionCallInSelectClauseShouldPass() {
        validate(
            "SELECT" +
            " ABS(age), " +
            " ASIN(age), " +
            " ATAN(age), " +
            " ATAN2(age, age), " +
            " CBRT(age), " +
            " CEIL(age), " +
            " COS(age), " +
            " COSH(age), " +
            " DEGREES(age), " +
            " EXP(age), " +
            " EXPM1(age), " +
            " FLOOR(age), " +
            " LOG(age), " +
            " LOG2(age), " +
            " LOG10(age), " +
            " POW(age), " +
            " RADIANS(age), " +
            " RINT(age), " +
            " ROUND(age), " +
            " SIN(age), " +
            " SINH(age), " +
            " SQRT(age), " +
            " TAN(age) " +
            "FROM semantics"
        );
    }

    @Test
    public void allSupportedMathFunctionCallInWhereClauseShouldPass() {
        validate(
            "SELECT * FROM semantics WHERE " +
            " ABS(age) = 1 AND " +
            " ASIN(age) = 1 AND " +
            " ATAN(age) = 1 AND " +
            " ATAN2(age, age) = 1 AND " +
            " CBRT(age) = 1 AND " +
            " CEIL(age) = 1 AND " +
            " COS(age) = 1 AND " +
            " COSH(age) = 1 AND " +
            " DEGREES(age) = 1 AND " +
            " EXP(age) = 1 AND " +
            " EXPM1(age) = 1 AND " +
            " FLOOR(age) = 1 AND " +
            " LOG(age) = 1 AND " +
            " LOG2(age) = 1 AND " +
            " LOG10(age) = 1 AND " +
            " POW(age) = 1 AND " +
            " RADIANS(age) = 1 AND " +
            " RINT(age) = 1 AND " +
            " ROUND(age) = 1 AND " +
            " SIN(age) = 1 AND " +
            " SINH(age) = 1 AND " +
            " SQRT(age) = 1 AND " +
            " TAN(age) = 1 "
        );
    }

    @Test
    public void allSupportedConstantsUseInSelectClauseShouldPass() {
        validate(
            "SELECT " +
            " E(), " +
            " PI() " +
            "FROM semantics"
        );
    }

    @Test
    public void allSupportedConstantsUseInWhereClauseShouldPass() {
        validate(
            "SELECT * FROM semantics WHERE " +
            " E() > 1 OR " +
            " PI() > 1"
        );
    }

    @Test
    public void allSupportedStringFunctionCallInSelectClauseShouldPass() {
        validate(
            "SELECT * FROM semantics WHERE " +
                " SUBSTRING(city, 0, 3) = 'Sea' AND " +
                " UPPER(city) = 'SEATTLE' AND " +
                " LOWER(city) = 'seattle'"
        );
    }

    @Test
    public void allSupportedStringFunctionCallInWhereClauseShouldPass() {
        validate(
            "SELECT" +
            " SUBSTRING(city, 0, 3), " +
            " UPPER(address), " +
            " LOWER(manager.name) " +
            "FROM semantics "
        );
    }

    @Test
    public void dateFormatFunctionCallWithNumberShouldFail() {
        expectValidationFailWithErrorMessages(
            "SELECT DATE_FORMAT(balance, 'yyyy-MM') FROM semantics",
            "Function [DATE_FORMAT] cannot work with [DOUBLE, STRING].",
            "Usage: DATE_FORMAT(DATE, STRING) -> STRING or DATE_FORMAT(DATE, STRING, STRING) -> STRING"
        );
    }

    @Test
    public void allSupportedDateFunctionCallShouldPass() {
        validate(
            "SELECT date_format(birthday, 'yyyy-MM') " +
            "FROM semantics " +
            "WHERE date_format(birthday, 'yyyy-MM') > '1980-01' " +
            "GROUP BY date_format(birthday, 'yyyy-MM') " +
            "ORDER BY date_format(birthday, 'yyyy-MM') DESC"
        );
    }

    @Test
    public void concatRequiresVarargSupportShouldPassAnyway() {
        validate("SELECT CONCAT('aaa') FROM semantics");
        validate("SELECT CONCAT('aaa', 'bbb') FROM semantics");
        validate("SELECT CONCAT('aaa', 'bbb', 123) FROM semantics");
    }

}
