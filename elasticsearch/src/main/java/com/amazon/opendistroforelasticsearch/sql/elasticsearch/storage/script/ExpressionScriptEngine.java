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

import com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.serialization.ExpressionSerializer;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.script.ScriptContext;
import org.elasticsearch.script.ScriptEngine;

/**
 * Custom expression script engine that supports using our expression code in DSL
 * as a new script language just like built-in Painless language.
 */
@RequiredArgsConstructor
public class ExpressionScriptEngine implements ScriptEngine {

  /**
   * Expression script language name.
   */
  public static final String EXPRESSION_LANG_NAME = "opendistro_expression";

  /**
   * Expression serializer that (de-)serializes expression.
   */
  private final ExpressionSerializer serializer;

  @Override
  public String getType() {
    return EXPRESSION_LANG_NAME;
  }

  @Override
  public <T> T compile(String scriptName,
                       String scriptCode,
                       ScriptContext<T> context,
                       Map<String, String> params) {

    // For now the script itself "inline" all values without parameters.
    Expression expression = compile(scriptCode);
    ExpressionScriptFactory factory = new ExpressionScriptFactory(expression);
    return context.factoryClazz.cast(factory);
  }

  @Override
  public Set<ScriptContext<?>> getSupportedContexts() {
    return Collections.singleton(new ScriptContext<>("expression_filtering", ExpressionScriptFactory.class));
  }

  /**
   * Note that in fact the expression source is already compiled in query engine.
   * The "code" here is actually serialized expression tree by serializer.
   * Therefore compilation here is simply to deserialize to expression tree.
   */
  private Expression compile(String code) {
    return serializer.deserialize(code);
  }

}
