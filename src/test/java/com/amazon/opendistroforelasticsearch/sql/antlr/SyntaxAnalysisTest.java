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

package com.amazon.opendistroforelasticsearch.sql.antlr;

import com.amazon.opendistroforelasticsearch.sql.antlr.syntax.SyntaxAnalysisException;
import org.junit.Test;

public class SyntaxAnalysisTest {

    @Test(expected = SyntaxAnalysisException.class)
    public void unknownKeywordShouldThrowException() {
        analyze("INSERT INTO accounts VALUES ('a')");
    }

    //@Test(expected = SyntaxAnalysisException.class)
    public void unknownLiteralShouldThrowException() {
    }

    @Test(expected = SyntaxAnalysisException.class)
    public void unknownFunctionShouldThrowException() {
        analyze("SELECT CRC32() FROM accounts");
    }

    //@Test(expected = SyntaxAnalysisException.class)
    public void unknownOperatorShouldThrowException() {
        analyze("SELECT * FROM accounts WHERE age <=> 1");
    }

    @Test(expected = SyntaxAnalysisException.class)
    public void missingFromClauseShouldThrowException() {
        analyze("SELECT * FROMaccounts");
    }

    public void dateLiteralShouldSucceed() {
    }

    private void analyze(String sql) {
        new OpenDistroSqlAnalyzer().analyze(sql);
    }
}
