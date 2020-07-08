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

import static com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionSignature.EXACTLY_MATCH;
import static com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionSignature.NOT_MATCH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprType;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@ExtendWith(MockitoExtension.class)
class FunctionSignatureTest {
  @Mock
  private FunctionSignature funcSignature;
  @Mock
  private List<ExprType> funcParamTypeList;

  private FunctionName unresolvedFuncName = FunctionName.of("add");
  private List<ExprType> unresolvedParamTypeList =
      Arrays.asList(ExprCoreType.INTEGER, ExprCoreType.FLOAT);

  @Test
  void signature_name_not_match() {
    when(funcSignature.getFunctionName()).thenReturn(FunctionName.of(("diff")));
    FunctionSignature unresolvedFunSig =
        new FunctionSignature(this.unresolvedFuncName, unresolvedParamTypeList);

    assertEquals(NOT_MATCH, unresolvedFunSig.match(funcSignature));
  }

  @Test
  void signature_arguments_size_not_match() {
    when(funcSignature.getFunctionName()).thenReturn(unresolvedFuncName);
    when(funcSignature.getParamTypeList()).thenReturn(funcParamTypeList);
    when(funcParamTypeList.size()).thenReturn(1);
    FunctionSignature unresolvedFunSig =
        new FunctionSignature(unresolvedFuncName, unresolvedParamTypeList);

    assertEquals(NOT_MATCH, unresolvedFunSig.match(funcSignature));
  }

  @Test
  void signature_exactly_match() {
    when(funcSignature.getFunctionName()).thenReturn(unresolvedFuncName);
    when(funcSignature.getParamTypeList()).thenReturn(unresolvedParamTypeList);
    FunctionSignature unresolvedFunSig =
        new FunctionSignature(unresolvedFuncName, unresolvedParamTypeList);

    assertEquals(EXACTLY_MATCH, unresolvedFunSig.match(funcSignature));
  }

  @Test
  void signature_not_match() {
    when(funcSignature.getFunctionName()).thenReturn(unresolvedFuncName);
    when(funcSignature.getParamTypeList())
        .thenReturn(Arrays.asList(ExprCoreType.STRING, ExprCoreType.STRING));
    FunctionSignature unresolvedFunSig =
        new FunctionSignature(unresolvedFuncName, unresolvedParamTypeList);

    assertEquals(NOT_MATCH, unresolvedFunSig.match(funcSignature));
  }

  @Test
  void signature_widening_match() {
    when(funcSignature.getFunctionName()).thenReturn(unresolvedFuncName);
    when(funcSignature.getParamTypeList())
        .thenReturn(Arrays.asList(ExprCoreType.FLOAT, ExprCoreType.FLOAT));
    FunctionSignature unresolvedFunSig =
        new FunctionSignature(unresolvedFuncName, unresolvedParamTypeList);

    assertEquals(2, unresolvedFunSig.match(funcSignature));
  }

  @Test
  void format_types() {
    FunctionSignature unresolvedFunSig =
        new FunctionSignature(unresolvedFuncName, unresolvedParamTypeList);

    assertEquals("[INTEGER,FLOAT]", unresolvedFunSig.formatTypes());
  }
}