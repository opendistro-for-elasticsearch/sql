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

package com.amazon.opendistroforelasticsearch.sql.legacy.unittest;

import com.amazon.opendistroforelasticsearch.sql.legacy.parser.ScriptFilter;
import com.amazon.opendistroforelasticsearch.sql.legacy.parser.SqlParser;
import com.amazon.opendistroforelasticsearch.sql.legacy.util.CheckScriptContents;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.elasticsearch.search.builder.SearchSourceBuilder.ScriptField;
import static org.junit.Assert.assertTrue;

public class MathFunctionsTest {

    private static SqlParser parser;

    @BeforeClass
    public static void init() { parser = new SqlParser(); }

    /** Tests for case insensitivity when calling SQL functions */
    @Test
    public void lowerCaseInSelect() {
        String query = "SELECT abs(age) " +
                       "FROM bank";
        ScriptField scriptField = CheckScriptContents.getScriptFieldFromQuery(query);
        assertTrue(
                CheckScriptContents.scriptContainsString(
                        scriptField,
                        "Math.abs(doc['age'].value)"));
    }

    @Test
    public void upperCaseInSelect() {
        String query = "SELECT ABS(age) " +
                       "FROM bank";
        ScriptField scriptField = CheckScriptContents.getScriptFieldFromQuery(query);
        assertTrue(
                CheckScriptContents.scriptContainsString(
                        scriptField,
                        "Math.abs(doc['age'].value)"));
    }

    @Test
    public void lowerCaseInWhere() {
        String query = "SELECT * " +
                       "FROM bank " +
                       "WHERE sqrt(age) > 5";
        ScriptFilter scriptFilter = CheckScriptContents.getScriptFilterFromQuery(query, parser);
        assertTrue(
                CheckScriptContents.scriptContainsString(
                        scriptFilter,
                        "Math.sqrt(doc['age'].value)"));
        assertTrue(
                CheckScriptContents.scriptHasPattern(
                        scriptFilter,
                        "sqrt_\\d+ > 5"));
    }

    @Test
    public void upperCaseInWhere() {
        String query = "SELECT * " +
                       "FROM bank " +
                       "WHERE SQRT(age) > 5";
        ScriptFilter scriptFilter = CheckScriptContents.getScriptFilterFromQuery(query, parser);
        assertTrue(
                CheckScriptContents.scriptContainsString(
                        scriptFilter,
                        "Math.sqrt(doc['age'].value)"));
        assertTrue(
                CheckScriptContents.scriptHasPattern(
                        scriptFilter,
                        "sqrt_\\d+ > 5"));
    }

    /** Tests for constant functions */
    @Test
    public void eulersNumberInSelect() {
        String query = "SELECT E() " +
                       "FROM bank";
        ScriptField scriptField = CheckScriptContents.getScriptFieldFromQuery(query);
        assertTrue(
                CheckScriptContents.scriptContainsString(
                        scriptField,
                        "Math.E"));
    }

    @Test
    public void eulersNumberInWhere() {
        String query = "SELECT * " +
                       "FROM bank " +
                       "WHERE E() > 2";
        ScriptFilter scriptFilter = CheckScriptContents.getScriptFilterFromQuery(query, parser);
        assertTrue(
                CheckScriptContents.scriptContainsString(
                        scriptFilter,
                        "Math.E"));
        assertTrue(
                CheckScriptContents.scriptHasPattern(
                        scriptFilter,
                        "E_\\d+ > 2"));
    }

    @Test
    public void piInSelect() {
        String query = "SELECT PI() " +
                       "FROM bank";
        ScriptField scriptField = CheckScriptContents.getScriptFieldFromQuery(query);
        assertTrue(
                CheckScriptContents.scriptContainsString(
                        scriptField,
                        "Math.PI"));
    }

    @Test
    public void piInWhere() {
        String query = "SELECT * " +
                       "FROM bank " +
                       "WHERE PI() < 4";
        ScriptFilter scriptFilter = CheckScriptContents.getScriptFilterFromQuery(query, parser);
        assertTrue(
                CheckScriptContents.scriptContainsString(
                        scriptFilter,
                        "Math.PI"));
        assertTrue(
                CheckScriptContents.scriptHasPattern(
                        scriptFilter,
                        "PI_\\d+ < 4"));
    }

    /** Tests for general math functions */
    @Test
    public void expm1WithPropertyArgument() {
        String query = "SELECT * " +
                       "FROM bank " +
                       "WHERE expm1(age) > 10";
        ScriptFilter scriptFilter = CheckScriptContents.getScriptFilterFromQuery(query, parser);
        assertTrue(
                CheckScriptContents.scriptContainsString(
                        scriptFilter,
                        "Math.expm1(doc['age'].value)"));
        assertTrue(
                CheckScriptContents.scriptHasPattern(
                        scriptFilter,
                        "expm1_\\d+ > 10"));
    }

    @Test
    public void expm1WithValueArgument() {
        String query = "SELECT * " +
                       "FROM bank " +
                       "WHERE expm1(5) > 10";
        ScriptFilter scriptFilter = CheckScriptContents.getScriptFilterFromQuery(query, parser);
        assertTrue(
                CheckScriptContents.scriptContainsString(
                        scriptFilter,
                        "Math.expm1(5)"));
        assertTrue(
                CheckScriptContents.scriptHasPattern(
                        scriptFilter,
                        "expm1_\\d+ > 10"));
    }


    /** Tests for trigonometric functions */
    @Test
    public void degreesWithPropertyArgument() {
        String query = "SELECT degrees(age) " +
                       "FROM bank";
        ScriptField scriptField = CheckScriptContents.getScriptFieldFromQuery(query);
        assertTrue(
                CheckScriptContents.scriptContainsString(
                        scriptField,
                        "Math.toDegrees(doc['age'].value)"));
    }

    @Test
    public void degreesWithValueArgument() {
        String query = "SELECT degrees(10) " +
                       "FROM bank";
        ScriptField scriptField = CheckScriptContents.getScriptFieldFromQuery(query);
        assertTrue(
                CheckScriptContents.scriptContainsString(
                        scriptField,
                        "Math.toDegrees(10)"));
    }

    @Test
    public void radiansWithPropertyArgument() {
        String query = "SELECT radians(age) " +
                       "FROM bank";
        ScriptField scriptField = CheckScriptContents.getScriptFieldFromQuery(query);
        assertTrue(
                CheckScriptContents.scriptContainsString(
                        scriptField,
                        "Math.toRadians(doc['age'].value)"));
    }

    @Test
    public void radiansWithValueArgument() {
        String query = "SELECT radians(180) " +
                       "FROM bank";
        ScriptField scriptField = CheckScriptContents.getScriptFieldFromQuery(query);
        assertTrue(
                CheckScriptContents.scriptContainsString(
                        scriptField,
                        "Math.toRadians(180)"));
    }

    @Test
    public void sinWithPropertyArgument() {
        String query = "SELECT sin(radians(age)) " +
                       "FROM bank";
        ScriptField scriptField = CheckScriptContents.getScriptFieldFromQuery(query);
        assertTrue(
                CheckScriptContents.scriptContainsString(
                        scriptField,
                        "Math.toRadians(doc['age'].value)"));
        assertTrue(
                CheckScriptContents.scriptHasPattern(
                        scriptField,
                        "Math.sin\\(radians_\\d+\\)"));
    }

    @Test
    public void sinWithValueArgument() {
        String query = "SELECT sin(radians(180)) " +
                       "FROM bank";
        ScriptField scriptField = CheckScriptContents.getScriptFieldFromQuery(query);
        assertTrue(
                CheckScriptContents.scriptContainsString(
                        scriptField,
                        "Math.toRadians(180)"));
        assertTrue(
                CheckScriptContents.scriptHasPattern(
                        scriptField,
                        "Math.sin\\(radians_\\d+\\)"));
    }

    @Test
    public void atanWithPropertyArgument() {
        String query = "SELECT atan(age) " +
                       "FROM bank";
        ScriptField scriptField = CheckScriptContents.getScriptFieldFromQuery(query);
        assertTrue(
                CheckScriptContents.scriptContainsString(
                        scriptField,
                        "Math.atan(doc['age'].value)"));
    }

    @Test
    public void atanWithValueArgument() {
        String query = "SELECT atan(1) " +
                       "FROM bank";
        ScriptField scriptField = CheckScriptContents.getScriptFieldFromQuery(query);
        assertTrue(
                CheckScriptContents.scriptContainsString(
                        scriptField,
                        "Math.atan(1)"));
    }

    @Test
    public void atanWithFunctionArgument() {
        String query = "SELECT atan(PI() / 2) " +
                       "FROM bank";
        ScriptField scriptField = CheckScriptContents.getScriptFieldFromQuery(query);
        assertTrue(
                CheckScriptContents.scriptContainsString(
                        scriptField,
                        "Math.PI"));
        assertTrue(
                CheckScriptContents.scriptHasPattern(
                        scriptField,
                        "PI_\\d+ / 2"));
        assertTrue(
                CheckScriptContents.scriptHasPattern(
                        scriptField,
                        "Math.atan\\(divide_\\d+\\)"));
    }

    @Test
    public void coshWithPropertyArgument() {
        String query = "SELECT cosh(age) " +
                       "FROM bank";
        ScriptField scriptField = CheckScriptContents.getScriptFieldFromQuery(query);
        assertTrue(
                CheckScriptContents.scriptContainsString(
                        scriptField,
                        "Math.cosh(doc['age'].value)"));
    }

    @Test
    public void coshWithValueArgument() {
        String query = "SELECT cosh(0) " +
                       "FROM bank";
        ScriptField scriptField = CheckScriptContents.getScriptFieldFromQuery(query);
        assertTrue(
                CheckScriptContents.scriptContainsString(
                        scriptField,
                        "Math.cosh(0)"));
    }

    @Test
    public void powerWithPropertyArgument() {
        String query = "SELECT POWER(age, 2) FROM bank WHERE POWER(balance, 3) > 0";
        ScriptField scriptField = CheckScriptContents.getScriptFieldFromQuery(query);
        assertTrue(
                CheckScriptContents.scriptContainsString(
                        scriptField,
                        "Math.pow(doc['age'].value, 2)"));

        ScriptFilter scriptFilter = CheckScriptContents.getScriptFilterFromQuery(query, parser);
        assertTrue(
                CheckScriptContents.scriptContainsString(
                        scriptFilter,
                        "Math.pow(doc['balance'].value, 3)"));
    }

    @Test
    public void atan2WithPropertyArgument() {
        String query = "SELECT ATAN2(age, 2) FROM bank WHERE ATAN2(balance, 3) > 0";
        ScriptField scriptField = CheckScriptContents.getScriptFieldFromQuery(query);
        assertTrue(
                CheckScriptContents.scriptContainsString(
                        scriptField,
                        "Math.atan2(doc['age'].value, 2)"));

        ScriptFilter scriptFilter = CheckScriptContents.getScriptFilterFromQuery(query, parser);
        assertTrue(
                CheckScriptContents.scriptContainsString(
                        scriptFilter,
                        "Math.atan2(doc['balance'].value, 3)"));
    }

    @Test
    public void cotWithPropertyArgument() {
        String query = "SELECT COT(age) FROM bank WHERE COT(balance) > 0";
        ScriptField scriptField = CheckScriptContents.getScriptFieldFromQuery(query);
        assertTrue(
                CheckScriptContents.scriptContainsString(
                        scriptField,
                        "1 / Math.tan(doc['age'].value)"));

        ScriptFilter scriptFilter = CheckScriptContents.getScriptFilterFromQuery(query, parser);
        assertTrue(
                CheckScriptContents.scriptContainsString(
                        scriptFilter,
                        "1 / Math.tan(doc['balance'].value)"));
    }

    @Test
    public void signWithFunctionPropertyArgument() {
        String query = "SELECT SIGN(age) FROM bank WHERE SIGNUM(balance) = 1";
        ScriptField scriptField = CheckScriptContents.getScriptFieldFromQuery(query);
        assertTrue(CheckScriptContents.scriptContainsString(
                scriptField,
                "Math.signum(doc['age'].value)"));

        ScriptFilter scriptFilter = CheckScriptContents.getScriptFilterFromQuery(query, parser);
        assertTrue(
                CheckScriptContents.scriptContainsString(
                        scriptFilter,
                        "Math.signum(doc['balance'].value)"));
    }

    @Test
    public void logWithOneParam() {
        String query = "SELECT LOG(age) FROM bank WHERE LOG(age) = 5.0";
        ScriptField scriptField = CheckScriptContents.getScriptFieldFromQuery(query);
        assertTrue(
                CheckScriptContents.scriptContainsString(
                        scriptField,
                        "Math.log(doc['age'].value)"));

        ScriptFilter scriptFilter = CheckScriptContents.getScriptFilterFromQuery(query, parser);
        assertTrue(
                CheckScriptContents.scriptContainsString(
                        scriptFilter,
                        "Math.log(doc['age'].value)"));
    }

    @Test
    public void logWithTwoParams() {
        String query = "SELECT LOG(3, age) FROM bank WHERE LOG(3, age) = 5.0";
        ScriptField scriptField = CheckScriptContents.getScriptFieldFromQuery(query);
        assertTrue(
                CheckScriptContents.scriptContainsString(
                        scriptField,
                        "Math.log(doc['age'].value)/Math.log(3)"));

        ScriptFilter scriptFilter = CheckScriptContents.getScriptFilterFromQuery(query, parser);
        assertTrue(
                CheckScriptContents.scriptContainsString(
                        scriptFilter,
                        "Math.log(doc['age'].value)/Math.log(3)"));
    }

    @Test
    public void log10Test() {
        String query = "SELECT LOG10(age) FROM accounts";
        ScriptField scriptField = CheckScriptContents.getScriptFieldFromQuery(query);
        assertTrue(
                CheckScriptContents.scriptContainsString(
                        scriptField,
                        "Math.log10(doc['age'].value)"
                )
        );
    }

    @Test
    public void lnTest() {
        String query = "SELECT LN(age) FROM age WHERE LN(age) = 5.0";
        ScriptField scriptField = CheckScriptContents.getScriptFieldFromQuery(query);
        assertTrue(
                CheckScriptContents.scriptContainsString(
                        scriptField,
                        "Math.log(doc['age'].value)"));

        ScriptFilter scriptFilter = CheckScriptContents.getScriptFilterFromQuery(query, parser);
        assertTrue(
                CheckScriptContents.scriptContainsString(
                        scriptFilter,
                        "Math.log(doc['age'].value)"));
    }

    @Test
    public void randWithoutParamTest() {
        String query = "SELECT RAND() FROM bank";
        ScriptField scriptField = CheckScriptContents.getScriptFieldFromQuery(query);
        assertTrue(
                CheckScriptContents.scriptContainsString(
                        scriptField,
                        "new Random().nextDouble()"
                )
        );
    }

    @Test
    public void randWithOneParamTest() {
        String query = "SELECT RAND(age) FROM bank";
        ScriptField scriptField = CheckScriptContents.getScriptFieldFromQuery(query);
        assertTrue(
                CheckScriptContents.scriptContainsString(
                        scriptField,
                        "new Random(doc['age'].value).nextDouble()"
                )
        );
    }
}
