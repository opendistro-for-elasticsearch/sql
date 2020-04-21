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
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static com.amazon.opendistroforelasticsearch.sql.expression.function.WideningTypeRule.IMPOSSIBLE_WIDENING;
import static com.amazon.opendistroforelasticsearch.sql.expression.function.WideningTypeRule.TYPE_EQUAL;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@ExtendWith(MockitoExtension.class)
class FunctionSignatureTest {
    @Mock
    private FunctionSignature funcSignature;
    @Mock
    private List<ExprType> funcParamTypeList;


    private FunctionName unresolvedFuncName = FunctionName.of("add");
    private List<ExprType> unresolvedParamTypeList = Arrays.asList(ExprType.INTEGER, ExprType.FLOAT);

    @Test
    void signature_name_not_match() {
        when(funcSignature.getFunctionName()).thenReturn(FunctionName.of(("diff")));
        FunctionSignature unresolvedFunSig = new FunctionSignature(this.unresolvedFuncName, unresolvedParamTypeList);
        ExpressionEvaluationException exception = assertThrows(ExpressionEvaluationException.class,
                () -> unresolvedFunSig.match(funcSignature));
        assertEquals("expression name: add and diff doesn't match", exception.getMessage());
    }

    @Test
    void signature_arguments_size_not_match() {
        when(funcSignature.getFunctionName()).thenReturn(unresolvedFuncName);
        when(funcSignature.getParamTypeList()).thenReturn(funcParamTypeList);
        when(funcParamTypeList.size()).thenReturn(1);
        FunctionSignature unresolvedFunSig = new FunctionSignature(unresolvedFuncName, unresolvedParamTypeList);
        ExpressionEvaluationException exception = assertThrows(ExpressionEvaluationException.class,
                () -> unresolvedFunSig.match(funcSignature));
        assertEquals("add expression expected 2 argument, but actually are 1", exception.getMessage());
    }

    @Test
    void signature_exactly_match() {
        when(funcSignature.getFunctionName()).thenReturn(unresolvedFuncName);
        when(funcSignature.getParamTypeList()).thenReturn(unresolvedParamTypeList);
        FunctionSignature unresolvedFunSig = new FunctionSignature(unresolvedFuncName, unresolvedParamTypeList);

        assertEquals(TYPE_EQUAL, unresolvedFunSig.match(funcSignature));
    }

    @Test
    void signature_not_match() {
        when(funcSignature.getFunctionName()).thenReturn(unresolvedFuncName);
        when(funcSignature.getParamTypeList()).thenReturn(Arrays.asList(ExprType.STRING, ExprType.STRING));
        FunctionSignature unresolvedFunSig = new FunctionSignature(unresolvedFuncName, unresolvedParamTypeList);

        assertEquals(IMPOSSIBLE_WIDENING, unresolvedFunSig.match(funcSignature));
    }

    @Test
    void signature_widening_match() {
        when(funcSignature.getFunctionName()).thenReturn(unresolvedFuncName);
        when(funcSignature.getParamTypeList()).thenReturn(Arrays.asList(ExprType.FLOAT, ExprType.FLOAT));
        FunctionSignature unresolvedFunSig = new FunctionSignature(unresolvedFuncName, unresolvedParamTypeList);

        assertEquals(2, unresolvedFunSig.match(funcSignature));
    }
}