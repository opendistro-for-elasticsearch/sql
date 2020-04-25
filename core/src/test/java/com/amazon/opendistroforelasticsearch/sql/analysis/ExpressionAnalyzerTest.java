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

package com.amazon.opendistroforelasticsearch.sql.analysis;

import com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.UnresolvedExpression;
import com.amazon.opendistroforelasticsearch.sql.exception.SemanticCheckException;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import org.junit.jupiter.api.Test;

import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.LITERAL_TRUE;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.integerValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExpressionAnalyzerTest extends AnalyzerTestBase {

    @Test
    public void equal() {
        assertAnalyzeEqual(
                dsl.equal(typeEnv(), DSL.ref("integer_value"), DSL.literal(integerValue(1))),
                AstDSL.equalTo(AstDSL.unresolvedAttr("integer_value"), AstDSL.intLiteral(1))
        );
    }

    @Test
    public void and() {
        assertAnalyzeEqual(
                dsl.and(typeEnv(), DSL.ref("boolean_value"), DSL.literal(LITERAL_TRUE)),
                AstDSL.and(AstDSL.unresolvedAttr("boolean_value"), AstDSL.booleanLiteral(true))
        );
    }

    @Test
    public void undefined_var_semantic_check_failed() {
        SemanticCheckException exception = assertThrows(SemanticCheckException.class,
                () -> analyze(AstDSL.and(AstDSL.unresolvedAttr("undefined_field"), AstDSL.booleanLiteral(true))));
        assertEquals("can't resolve expression undefined_field in type env", exception.getMessage());
    }

    protected Expression analyze(UnresolvedExpression unresolvedExpression) {
        return expressionAnalyzer.analyze(unresolvedExpression, analysisContext);
    }

    protected void assertAnalyzeEqual(Expression expected, UnresolvedExpression unresolvedExpression) {
        assertEquals(expected, analyze(unresolvedExpression));
    }
}