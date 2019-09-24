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
 * Semantic analysis test for Elaticsearch special scalar functions
 */
public class SemanticAnalyzerESScalarFunctionTest extends SemanticAnalyzerTestBase {

    @Test
    public void dayOfFunctionCallWithDateInSelectClauseShouldPass() {
        validate("SELECT DAY_OF_MONTH(birthday) FROM semantics");
        validate("SELECT DAY_OF_WEEK(birthday) FROM semantics");
        validate("SELECT DAY_OF_YEAR(birthday) FROM semantics");
    }

    @Test
    public void dayOfFunctionCallWithDateInWhereClauseShouldPass() {
        validate("SELECT * FROM semantics WHERE DAY_OF_MONTH(birthday) = 1");
        validate("SELECT * FROM semantics WHERE DAY_OF_WEEK(birthday) = 1");
        validate("SELECT * FROM semantics WHERE DAY_OF_YEAR(birthday) = 1");
    }

}
