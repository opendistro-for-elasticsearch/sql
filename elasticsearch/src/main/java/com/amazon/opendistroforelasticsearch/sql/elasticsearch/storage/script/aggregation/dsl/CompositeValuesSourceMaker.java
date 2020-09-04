/*
 *
 *    Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License").
 *    You may not use this file except in compliance with the License.
 *    A copy of the License is located at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    or in the "license" file accompanying this file. This file is distributed
 *    on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *    express or implied. See the License for the specific language governing
 *    permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.script.aggregation.dsl;

import static com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.script.ExpressionScriptEngine.EXPRESSION_LANG_NAME;
import static java.util.Collections.emptyMap;
import static org.elasticsearch.script.Script.DEFAULT_SCRIPT_TYPE;

import com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.script.ScriptUtils;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.serialization.ExpressionSerializer;
import com.amazon.opendistroforelasticsearch.sql.expression.ExpressionNodeVisitor;
import com.amazon.opendistroforelasticsearch.sql.expression.NamedExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.ReferenceExpression;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.aggregations.bucket.composite.CompositeValuesSourceBuilder;

/**
 * Todo.
 */
@RequiredArgsConstructor
public class CompositeValuesSourceMaker extends ExpressionNodeVisitor<Object,
    CompositeValuesSourceBuilder<?>> {

  private final ExpressionSerializer serializer;

  public CompositeValuesSourceBuilder<?> build(CompositeValuesSourceBuilder<?> builder,
                                               NamedExpression expression) {
    expression.accept(this, builder);
    return builder;
  }

  @Override
  public Object visitNamed(NamedExpression node, CompositeValuesSourceBuilder<?> builder) {
    builder.script(new Script(
        DEFAULT_SCRIPT_TYPE, EXPRESSION_LANG_NAME, serializer.serialize(node.getDelegated()),
        emptyMap()));
    return null;
  }

  @Override
  public Object visitReference(ReferenceExpression node,
                               CompositeValuesSourceBuilder<?> builder) {

    builder.field(ScriptUtils.convertTextToKeyword(node.getAttr(), node.type()));
    return null;
  }
}
