/*
 *    Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License").
 *    You may not use this file except in compliance with the License.
 *    A copy of the License is located at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    or in the "license" file accompanying this file. This file is distributed
 *    on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *    express or implied. See the License for the specific language governing
 *    permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.sql.sql.parser;

import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.aggregate;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.qualifiedName;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.IdentContext;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.KeywordsAsQualifiedNameContext;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazon.opendistroforelasticsearch.sql.ast.expression.UnresolvedExpression;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.IdentsAsQualifiedNameContext;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.KeywordsCanBeIdContext;
import com.amazon.opendistroforelasticsearch.sql.sql.parser.context.QuerySpecification;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@ExtendWith(MockitoExtension.class)
class AstHavingFilterBuilderTest {

  @Mock
  private QuerySpecification querySpec;

  private AstHavingFilterBuilder builder;

  @BeforeEach
  void setup() {
    builder = new AstHavingFilterBuilder(querySpec);
  }

  @Test
  void should_replace_alias_with_select_expression() {
    IdentsAsQualifiedNameContext qualifiedName = mock(IdentsAsQualifiedNameContext.class);
    IdentContext identifier = mock(IdentContext.class);
    UnresolvedExpression expression = aggregate("AVG", qualifiedName("age"));

    when(identifier.getText()).thenReturn("a");
    when(qualifiedName.ident()).thenReturn(ImmutableList.of(identifier));
    when(querySpec.isSelectAlias(any())).thenReturn(true);
    when(querySpec.getSelectItemByAlias(any())).thenReturn(expression);
    assertEquals(expression, builder.visitIdentsAsQualifiedName(qualifiedName));
  }

  @Test
  void should_replace_keyword_alias_with_select_expression() {
    KeywordsAsQualifiedNameContext qualifiedName = mock(KeywordsAsQualifiedNameContext.class);
    KeywordsCanBeIdContext keyword = mock(KeywordsCanBeIdContext.class);
    UnresolvedExpression expression = aggregate("AVG", qualifiedName("age"));

    when(keyword.getText()).thenReturn("a");
    when(qualifiedName.keywordsCanBeId()).thenReturn(keyword);
    when(querySpec.isSelectAlias(any())).thenReturn(true);
    when(querySpec.getSelectItemByAlias(any())).thenReturn(expression);
    assertEquals(expression, builder.visitKeywordsAsQualifiedName(qualifiedName));
  }

}