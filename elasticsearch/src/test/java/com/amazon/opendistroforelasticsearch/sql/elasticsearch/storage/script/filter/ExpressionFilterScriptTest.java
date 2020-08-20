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

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.script.filter;

import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.FLOAT;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.TIMESTAMP;
import static com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.type.ElasticsearchDataType.ES_TEXT_KEYWORD;
import static com.amazon.opendistroforelasticsearch.sql.expression.DSL.literal;
import static com.amazon.opendistroforelasticsearch.sql.expression.DSL.ref;
import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprTimestampValue;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.LiteralExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.config.ExpressionConfig;
import com.google.common.collect.ImmutableMap;
import java.time.ZonedDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.lucene.index.LeafReaderContext;
import org.elasticsearch.index.fielddata.ScriptDocValues;
import org.elasticsearch.search.lookup.LeafDocLookup;
import org.elasticsearch.search.lookup.LeafSearchLookup;
import org.elasticsearch.search.lookup.SearchLookup;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@ExtendWith(MockitoExtension.class)
class ExpressionFilterScriptTest {

  private final DSL dsl = new ExpressionConfig().dsl(new ExpressionConfig().functionRepository());

  @Mock
  private SearchLookup lookup;

  @Mock
  private LeafSearchLookup leafLookup;

  @Mock
  private LeafReaderContext context;

  @Test
  void should_match_if_true_literal() {
    assertThat()
        .docValues()
        .filterBy(literal(true))
        .shouldMatch();
  }

  @Test
  void should_not_match_if_false_literal() {
    assertThat()
        .docValues()
        .filterBy(literal(false))
        .shouldNotMatch();
  }

  @Test
  void can_execute_expression_with_integer_field() {
    assertThat()
        .docValues("age", 30L) // DocValue only supports long
        .filterBy(
            dsl.greater(ref("age", INTEGER), literal(20)))
        .shouldMatch();
  }

  @Test
  void can_execute_expression_with_text_keyword_field() {
    assertThat()
        .docValues("name.keyword", "John")
        .filterBy(
            dsl.equal(ref("name", ES_TEXT_KEYWORD), literal("John")))
        .shouldMatch();
  }

  @Test
  void can_execute_expression_with_float_field() {
    assertThat()
        .docValues(
            "balance", 100.0, // DocValue only supports double
            "name", "John")
        .filterBy(
            dsl.and(
                dsl.less(ref("balance", FLOAT), literal(150.0F)),
                dsl.equal(ref("name", STRING), literal("John"))))
        .shouldMatch();
  }

  @Test
  void can_execute_expression_with_date_field() {
    ExprTimestampValue ts = new ExprTimestampValue("2020-08-04 10:00:00");
    assertThat()
        .docValues("birthday", ZonedDateTime.parse("2020-08-04T10:00:00Z"))
        .filterBy(dsl.equal(ref("birthday", TIMESTAMP), new LiteralExpression(ts)))
        .shouldMatch();
  }

  @Test
  void can_execute_expression_with_missing_field() {
    assertThat()
        .docValues("age", 30)
        .filterBy(ref("name", STRING))
        .shouldNotMatch();
  }

  @Test
  void cannot_execute_non_predicate_expression() {
    assertThrow(IllegalStateException.class,
                "Expression has wrong result type instead of boolean: expression [10], result [10]")
        .docValues()
        .filterBy(literal(10));
  }

  private ExprScriptAssertion assertThat() {
    return new ExprScriptAssertion(lookup, leafLookup, context);
  }

  private <T extends Throwable> ExprScriptAssertion assertThrow(Class<T> clazz,
                                                                String message) {
    return new ExprScriptAssertion(lookup, leafLookup, context) {
      @Override
      ExprScriptAssertion filterBy(Expression expr) {
        Throwable t = assertThrows(clazz, () -> super.filterBy(expr));
        assertEquals(message, t.getMessage());
        return null;
      }
    };
  }

  @RequiredArgsConstructor
  private static class ExprScriptAssertion {
    private final SearchLookup lookup;
    private final LeafSearchLookup leafLookup;
    private final LeafReaderContext context;
    private boolean isMatched;

    ExprScriptAssertion docValues() {
      return this;
    }

    ExprScriptAssertion docValues(String name, Object value) {
      LeafDocLookup leafDocLookup = mockLeafDocLookup(
          ImmutableMap.of(name, new FakeScriptDocValues<>(value)));

      when(lookup.getLeafSearchLookup(any())).thenReturn(leafLookup);
      when(leafLookup.doc()).thenReturn(leafDocLookup);
      return this;
    }

    ExprScriptAssertion docValues(String name1, Object value1,
                                  String name2, Object value2) {
      LeafDocLookup leafDocLookup = mockLeafDocLookup(
          ImmutableMap.of(
              name1, new FakeScriptDocValues<>(value1),
              name2, new FakeScriptDocValues<>(value2)));

      when(lookup.getLeafSearchLookup(any())).thenReturn(leafLookup);
      when(leafLookup.doc()).thenReturn(leafDocLookup);
      return this;
    }

    ExprScriptAssertion filterBy(Expression expr) {
      ExpressionFilterScript script = new ExpressionFilterScript(expr, lookup, context, emptyMap());
      isMatched = script.execute();
      return this;
    }

    void shouldMatch() {
      Assertions.assertTrue(isMatched);
    }

    void shouldNotMatch() {
      Assertions.assertFalse(isMatched);
    }

    private LeafDocLookup mockLeafDocLookup(Map<String, ScriptDocValues<?>> docValueByNames) {
      LeafDocLookup leafDocLookup = mock(LeafDocLookup.class);
      when(leafDocLookup.get(anyString()))
          .thenAnswer(invocation -> docValueByNames.get(invocation.<String>getArgument(0)));
      return leafDocLookup;
    }
  }

  @RequiredArgsConstructor
  private static class FakeScriptDocValues<T> extends ScriptDocValues<T> {
    private final T value;

    @Override
    public void setNextDocId(int docId) {
      throw new UnsupportedOperationException("Fake script doc values doesn't implement this yet");
    }

    @Override
    public T get(int index) {
      return value;
    }

    @Override
    public int size() {
      return 1;
    }
  }

}