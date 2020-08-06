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

import static com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.type.ElasticsearchDataType.ES_TEXT_KEYWORD;
import static java.util.stream.Collectors.toMap;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprTupleValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprType;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.value.ElasticsearchExprValueFactory;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.ExpressionNodeVisitor;
import com.amazon.opendistroforelasticsearch.sql.expression.ReferenceExpression;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.time.chrono.ChronoZonedDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.EqualsAndHashCode;
import org.apache.lucene.index.LeafReaderContext;
import org.elasticsearch.SpecialPermission;
import org.elasticsearch.index.fielddata.ScriptDocValues;
import org.elasticsearch.script.FilterScript;
import org.elasticsearch.search.lookup.SearchLookup;

/**
 * Expression script executor that executes the expression on each document
 * and determine if the document is supposed to be filtered out or not.
 */
@EqualsAndHashCode(callSuper = false)
class ExpressionFilterScript extends FilterScript {

  /**
   * Expression to execute.
   */
  private final Expression expression;

  public ExpressionFilterScript(Expression expression,
                                SearchLookup lookup,
                                LeafReaderContext context,
                                Map<String, Object> params) {
    super(params, lookup, context);
    this.expression = expression;
  }

  @Override
  public boolean execute() {
    // Check we ourselves are not being called by unprivileged code.
    SpecialPermission.check();

    return AccessController.doPrivileged((PrivilegedAction<Boolean>) () -> {
      Set<ReferenceExpression> fields = extractFields(expression);
      ElasticsearchExprValueFactory valueFactory = buildValueFactory(fields);
      Map<String, ExprValue> valueEnv = buildValueEnv(fields, valueFactory);
      ExprValue result = evaluateExpression(valueEnv);
      return (Boolean) result.value();
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

  private Map<String, ExprValue> buildValueEnv(Set<ReferenceExpression> fields,
                                               ElasticsearchExprValueFactory valueFactory) {
    Map<String, ExprValue> valueEnv = new HashMap<>();
    for (ReferenceExpression field : fields) {
      String fieldName = field.getAttr();
      ExprValue exprValue = valueFactory.construct(fieldName, getDocValue(field));
      valueEnv.put(fieldName, exprValue);
    }
    return valueEnv;
  }

  private Object getDocValue(ReferenceExpression field) {
    String fieldName = field.getAttr();

    // Text field doesn't have doc value (exception thrown even when you call "get"
    // Limitation: assume inner field name is always "keyword"
    if (field.type() == ES_TEXT_KEYWORD) {
      fieldName += ".keyword";
    }

    ScriptDocValues<?> docValue = getDoc().get(fieldName);
    if (docValue == null) {
      throw new IllegalStateException("Doc value is not found or empty for field: " + fieldName);
    }

    Object value = docValue.get(0);
    if (value instanceof ChronoZonedDateTime) {
      return ((ChronoZonedDateTime<?>) value).toInstant();
    }
    return value;
  }

  private ExprValue evaluateExpression(Map<String, ExprValue> valueEnv) {
    ExprTupleValue tupleValue = ExprTupleValue.fromExprValueMap(valueEnv);
    ExprValue result = expression.valueOf(tupleValue.bindingTuples());

    if (result.type() != ExprCoreType.BOOLEAN) {
      throw new IllegalStateException(String.format(
          "Expression has wrong result type instead of boolean: "
              + "expression [%s], result [%s]", expression, result));
    }
    return result;
  }

}
