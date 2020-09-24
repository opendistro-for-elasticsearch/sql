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

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.script;

import static java.util.Collections.emptyMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.script.filter.ExpressionFilterScriptFactory;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.serialization.ExpressionSerializer;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import org.elasticsearch.script.AggregationScript;
import org.elasticsearch.script.FilterScript;
import org.elasticsearch.script.ScriptContext;
import org.elasticsearch.script.ScriptEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@ExtendWith(MockitoExtension.class)
class ExpressionScriptEngineTest {

  @Mock
  private ExpressionSerializer serializer;

  private ScriptEngine scriptEngine;

  private final Expression expression = DSL.literal(true);

  @BeforeEach
  void set_up() {
    scriptEngine = new ExpressionScriptEngine(serializer);
  }

  @Test
  void should_return_custom_script_language_name() {
    assertEquals(ExpressionScriptEngine.EXPRESSION_LANG_NAME, scriptEngine.getType());
  }

  @Test
  void can_initialize_filter_script_factory_by_compiled_script() {
    when(serializer.deserialize("test code")).thenReturn(expression);

    assertThat(scriptEngine.getSupportedContexts(),
        contains(FilterScript.CONTEXT, AggregationScript.CONTEXT));

    Object actualFactory = scriptEngine.compile(
        "test", "test code", FilterScript.CONTEXT, emptyMap());
    assertEquals(new ExpressionFilterScriptFactory(expression), actualFactory);
  }

  @Test
  void should_throw_exception_for_unsupported_script_context() {
    ScriptContext<?> unknownCtx = mock(ScriptContext.class);
    assertThrows(IllegalStateException.class, () ->
        scriptEngine.compile("test", "test code", unknownCtx, emptyMap()));
  }

}