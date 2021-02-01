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

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.script.core;

import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.FLOAT;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static java.util.stream.Collectors.toMap;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprType;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.value.ElasticsearchExprValueFactory;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.script.ScriptUtils;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.ExpressionNodeVisitor;
import com.amazon.opendistroforelasticsearch.sql.expression.ReferenceExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.env.Environment;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.time.chrono.ChronoZonedDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import lombok.EqualsAndHashCode;
import org.elasticsearch.index.fielddata.ScriptDocValues;

/**
 * Expression script executor that executes the expression on each document
 * and determine if the document is supposed to be filtered out or not.
 */
@EqualsAndHashCode(callSuper = false)
public class ExpressionScript {

  /**
   * Expression to execute.
   */
  private final Expression expression;

  /**
   * ElasticsearchExprValueFactory.
   */
  @EqualsAndHashCode.Exclude
  private final ElasticsearchExprValueFactory valueFactory;

  /**
   * Reference Fields.
   */
  @EqualsAndHashCode.Exclude
  private final Set<ReferenceExpression> fields;

  /**
   * Expression constructor.
   */
  public ExpressionScript(Expression expression) {
    this.expression = expression;
    this.fields = AccessController.doPrivileged((PrivilegedAction<Set<ReferenceExpression>>) () ->
        extractFields(expression));
    this.valueFactory =
        AccessController.doPrivileged(
            (PrivilegedAction<ElasticsearchExprValueFactory>) () -> buildValueFactory(fields));
  }

  /**
   * Evaluate on the doc generate by the doc provider.
   * @param docProvider doc provider.
   * @param evaluator evaluator
   * @return
   */
  public ExprValue execute(Supplier<Map<String, ScriptDocValues<?>>> docProvider,
                         BiFunction<Expression,
                             Environment<Expression,
                                 ExprValue>, ExprValue> evaluator) {
    return AccessController.doPrivileged((PrivilegedAction<ExprValue>) () -> {
      Environment<Expression, ExprValue> valueEnv =
          buildValueEnv(fields, valueFactory, docProvider);
      ExprValue result = evaluator.apply(expression, valueEnv);
      return result;
    });
  }

  private Set<ReferenceExpression> extractFields(Expression expr) {
    Set<ReferenceExpression> fields = new HashSet<>();
    expr.accept(new ExpressionNodeVisitor<Object, Set<ReferenceExpression>>() {
      @Override
      public Object visitReference(ReferenceExpression node, Set<ReferenceExpression> context) {
        context.add(node);
        return null;
      }
    }, fields);
    return fields;
  }

  private ElasticsearchExprValueFactory buildValueFactory(Set<ReferenceExpression> fields) {
    Map<String, ExprType> typeEnv = fields.stream()
        .collect(toMap(
            ReferenceExpression::getAttr,
            ReferenceExpression::type));
    return new ElasticsearchExprValueFactory(typeEnv);
  }

  private Environment<Expression, ExprValue> buildValueEnv(
      Set<ReferenceExpression> fields, ElasticsearchExprValueFactory valueFactory,
      Supplier<Map<String, ScriptDocValues<?>>> docProvider) {

    Map<Expression, ExprValue> valueEnv = new HashMap<>();
    for (ReferenceExpression field : fields) {
      String fieldName = field.getAttr();
      ExprValue exprValue = valueFactory.construct(fieldName, getDocValue(field, docProvider));
      valueEnv.put(field, exprValue);
    }
    // Encapsulate map data structure into anonymous Environment class
    return valueEnv::get;
  }

  private Object getDocValue(ReferenceExpression field,
                             Supplier<Map<String, ScriptDocValues<?>>> docProvider) {
    String fieldName = getDocValueName(field);
    ScriptDocValues<?> docValue = docProvider.get().get(fieldName);
    if (docValue == null || docValue.isEmpty()) {
      return null; // No way to differentiate null and missing from doc value
    }

    Object value = docValue.get(0);
    if (value instanceof ChronoZonedDateTime) {
      return ((ChronoZonedDateTime<?>) value).toInstant();
    }
    return castNumberToFieldType(value, field.type());
  }

  /**
   * Text field doesn't have doc value (exception thrown even when you call "get")
   * Limitation: assume inner field name is always "keyword".
   */
  private String getDocValueName(ReferenceExpression field) {
    String fieldName = field.getAttr();
    return ScriptUtils.convertTextToKeyword(fieldName, field.type());
  }

  /**
   * DocValue only support long and double so cast to integer and float if needed.
   * The doc value must be Long and Double for expr type Long/Integer and Double/Float respectively.
   * Otherwise there must be bugs in our engine that causes the mismatch.
   */
  private Object castNumberToFieldType(Object value, ExprType type) {
    if (value == null) {
      return value;
    }

    if (type == INTEGER) {
      return ((Long) value).intValue();
    } else if (type == FLOAT) {
      return ((Double) value).floatValue();
    } else {
      return value;
    }
  }
}
