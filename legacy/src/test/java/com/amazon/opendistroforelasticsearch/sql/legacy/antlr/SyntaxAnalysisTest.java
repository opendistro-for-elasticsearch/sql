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

package com.amazon.opendistroforelasticsearch.sql.legacy.antlr;

import com.amazon.opendistroforelasticsearch.sql.legacy.antlr.syntax.SyntaxAnalysisException;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.allOf;

/**
 * Test cases focused on illegal syntax testing (blacklist) along with a few normal cases not covered previously.
 * All other normal cases should be covered in existing unit test and IT.
 */
public class SyntaxAnalysisTest {

    /** public accessor is required by @Rule annotation */
    @Rule
    public ExpectedException exception = ExpectedException.none();

    private OpenDistroSqlAnalyzer analyzer = new OpenDistroSqlAnalyzer(new SqlAnalysisConfig(true, true, 1000));

    /** In reality exception occurs before reaching new parser for now */
    @Test
    public void unsupportedKeywordShouldThrowException() {
        expectValidationFailWithErrorMessage(
            "INSERT INTO accounts VALUES ('a')",
            "offending symbol [INSERT]"
        );
    }

    /**
     * Why we need to let it go and verify in semantic analyzer?
     *  Parser treats LOG123 a valid column and stops at '(' which gives wrong location and expected token
     *  In this case it's hard for parser to figure out if this is a wrong function name indeed or not.
     *  So we let it pass as an UDF and fail in semantic analyzer with meaningful message.
     */
    @Test //(expected = SyntaxAnalysisException.class)
    public void unsupportedFunctionShouldThrowException() {
        validate("SELECT * FROM accounts WHERE LOG123(balance) = 1");
    }

    @Test
    public void unsupportedOperatorShouldPassSyntaxCheck() {
        expectValidationFailWithErrorMessage(
            "SELECT * FROM accounts WHERE age <=> 1",
            "offending symbol [>]"
        );
    }

    @Test
    public void missingFromClauseShouldThrowException() {
        expectValidationFailWithErrorMessage(
            "SELECT 1",
            "offending symbol [<EOF>]" // parsing was unable to terminate normally
        );
    }

    @Test
    public void missingWhereKeywordShouldThrowException() {
        expectValidationFailWithErrorMessage(
            "SELECT * FROM accounts age = 1",
            "offending symbol [=]", // parser thought 'age' is alias of 'accounts' and failed at '='
            "Expecting", ";"        // "Expecting tokens in {<EOF>, ';'}"
        );
    }

    @Test
    public void someKeywordsShouldBeAbleToUseAsIdentifier() {
        validate("SELECT AVG(balance) AS avg FROM accounts");
    }

    @Test
    public void specialIndexNameShouldPass() {
        validate("SELECT * FROM accounts/temp");
        validate("SELECT * FROM account*");
        validate("SELECT * FROM es-accounts");
        validate("SELECT * FROM es-account*");
    }

    @Test
    public void typeNamePatternShouldThrowException() {
        expectValidationFailWithErrorMessage(
            "SELECT * FROM accounts/tem*",
            "offending symbol [*]"
        );
    }

    @Test
    public void systemIndexNameShouldPass() {
        validate("SELECT * FROM .kibana");
    }

    @Test
    public void useMetadataFieldShouldPass() {
        validate("SELECT @timestamp FROM accounts");
    }

    @Test
    public void leftJoinOnNestedFieldWithoutOnClauseShouldPass() {
        validate("SELECT * FROM accounts a LEFT JOIN a.projects p");
    }

    @Test
    public void useDeepNestedFieldShouldPass() {
        validate("SELECT a.projects.name FROM accounts a");
    }

    /** As the translation is not supported for now, check this in semantic analyzer */
    @Test
    public void arithmeticExpressionInWhereClauseShouldPass() {
        validate("SELECT * FROM accounts WHERE age + 1 = 10");
    }

    @Test
    public void queryEndWithSemiColonShouldPass() {
        validate("SELECT * FROM accounts;");
    }

    private void expectValidationFailWithErrorMessage(String query, String... messages) {
        exception.expect(SyntaxAnalysisException.class);
        exception.expectMessage(allOf(Arrays.stream(messages).
                                      map(Matchers::containsString).
                                      collect(toList())));
        validate(query);
    }

    private void validate(String sql) {
        analyzer.analyzeSyntax(sql);
    }
}
