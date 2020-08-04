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

import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.DOUBLE;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;
import static com.amazon.opendistroforelasticsearch.sql.expression.DSL.literal;
import static com.amazon.opendistroforelasticsearch.sql.expression.DSL.ref;
import static java.util.Collections.emptyMap;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.config.ExpressionConfig;
import com.google.common.collect.ImmutableMap;
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

  @Mock
  private SearchLookup lookup;

  @Mock
  private LeafSearchLookup leafLookup;

  @Mock
  private LeafReaderContext context;

  private final DSL dsl = new ExpressionConfig().dsl(new ExpressionConfig().functionRepository());

  @Test
  void should_match_doc_if_true_literal() {
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
  void should_match_if_true_comparison_expression() {
    assertThat()
        .docValues("age", 30)
        .filterBy(
            dsl.greater(ref("age", INTEGER), literal(20)))
        .shouldMatch();
  }

  @Test
  void should_not_match_if_false_comparison_expression_with_function() {
    assertThat()
        .docValues("balance", -100.0)
        .filterBy(
            dsl.greater(
                dsl.abs(ref("balance", DOUBLE)), literal(101.0)))
        .shouldNotMatch();
  }

  @Test
  void can_execute_expression_script_with_multiple_fields_involved() {
    assertThat()
        .docValues(
            "age", 30,
            "name", "John")
        .filterBy(
            dsl.and(
                dsl.less(ref("age", INTEGER), literal(50)),
                dsl.equal(ref("name", STRING), literal("John"))))
        .shouldMatch();
  }

  private ExprScriptAssertion assertThat() {
    return new ExprScriptAssertion(lookup, leafLookup, context);
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
      LeafDocLookup leafDocLookup = mockLeafDocLookup(ImmutableMap.of(name, toDocValue(value)));
      when(lookup.getLeafSearchLookup(any())).thenReturn(leafLookup);
      when(leafLookup.doc()).thenReturn(leafDocLookup);
      return this;
    }

    ExprScriptAssertion docValues(String name1, Object value1,
                                  String name2, Object value2) {
      LeafDocLookup leafDocLookup = mockLeafDocLookup(
          ImmutableMap.of(
              name1, toDocValue(value1),
              name2, toDocValue(value2)));
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

    private static ScriptDocValues<?> toDocValue(Object object) {
      if (object instanceof Integer) {
        return new FakeScriptDocValues<>((Integer) object);
      } else if (object instanceof Long) {
        return new FakeScriptDocValues<>((Long) object);
      } else if (object instanceof Double) {
        return new FakeScriptDocValues<>((Double) object);
      } else if (object instanceof String) {
        return new FakeScriptDocValues<>((String) object);
      } else if (object instanceof Boolean) {
        return new FakeScriptDocValues<>((Boolean) object);
      } else {
        throw new IllegalStateException("Unsupported doc value type: " + object.getClass());
      }
    }

    private static LeafDocLookup mockLeafDocLookup(
        Map<String, ScriptDocValues<?>> docValueByNames) {
      LeafDocLookup leafDocLookup = mock(LeafDocLookup.class);
      when(leafDocLookup.containsKey(anyString()))
          .thenAnswer(invocation -> docValueByNames.containsKey(invocation.<String>getArgument(0)));
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