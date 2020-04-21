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

package com.amazon.opendistroforelasticsearch.sql.expression.function;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprType;
import com.amazon.opendistroforelasticsearch.sql.exception.ExpressionEvaluationException;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@ExtendWith(MockitoExtension.class)
class FunctionResolverTest {
    @Mock
    private FunctionSignature exactlyMatchFS;
    @Mock
    private FunctionSignature bestMatchFS;
    @Mock
    private FunctionSignature leastMatchFS;
    @Mock
    private FunctionSignature notMatchFS;
    @Mock
    private FunctionSignature functionSignature;
    @Mock
    private FunctionExpressionBuilder exactlyMatchBuilder;
    @Mock
    private FunctionExpressionBuilder bestMatchBuilder;
    @Mock
    private FunctionExpressionBuilder leastMatchBuilder;
    @Mock
    private FunctionExpressionBuilder notMatchBuilder;

    private FunctionName functionName = FunctionName.of("add");

    @Test
    void resolve_function_signature_exactly_match() {
        when(functionSignature.match(exactlyMatchFS)).thenReturn(WideningTypeRule.TYPE_EQUAL);
        FunctionResolver resolver = new FunctionResolver(functionName,
                ImmutableMap.of(exactlyMatchFS, exactlyMatchBuilder));

        assertEquals(exactlyMatchBuilder, resolver.resolve(functionSignature));
    }

    @Test
    void resolve_function_signature_best_match() {
        when(functionSignature.match(bestMatchFS)).thenReturn(1);
        when(functionSignature.match(leastMatchFS)).thenReturn(2);
        FunctionResolver resolver = new FunctionResolver(functionName,
                ImmutableMap.of(bestMatchFS, bestMatchBuilder, leastMatchFS, leastMatchBuilder));

        assertEquals(bestMatchBuilder, resolver.resolve(functionSignature));
    }

    @Test
    void resolve_function_not_match() {
        List<ExprType> exprTypes = Arrays.asList(ExprType.INTEGER, ExprType.INTEGER);
        when(functionSignature.match(notMatchFS)).thenReturn(WideningTypeRule.IMPOSSIBLE_WIDENING);
        when(notMatchFS.getParamTypeList()).thenReturn(exprTypes);
        when(functionSignature.getParamTypeList()).thenReturn(Arrays.asList(ExprType.BOOLEAN, ExprType.BOOLEAN));
        FunctionResolver resolver = new FunctionResolver(functionName,
                ImmutableMap.of(notMatchFS, notMatchBuilder));

        ExpressionEvaluationException exception = assertThrows(ExpressionEvaluationException.class,
                () -> resolver.resolve(functionSignature));
        assertEquals("add function expected {[INTEGER,INTEGER]}, but get [BOOLEAN,BOOLEAN]", exception.getMessage());
    }
}