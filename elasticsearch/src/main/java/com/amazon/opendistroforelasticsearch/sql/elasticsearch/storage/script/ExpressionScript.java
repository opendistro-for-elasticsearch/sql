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

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprTupleValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprType;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.value.ElasticsearchExprValueFactory;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.FunctionExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.ReferenceExpression;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.lucene.index.LeafReaderContext;
import org.elasticsearch.SpecialPermission;
import org.elasticsearch.index.fielddata.ScriptDocValues;
import org.elasticsearch.script.FilterScript;
import org.elasticsearch.search.lookup.SearchLookup;

/**
 * Expression script executor that executes the expression on each document
 * and determine if the document is supposed to be filtered out or not.
 */
class ExpressionScript extends FilterScript {

  /**
   * Expression to execute.
   */
  private final Expression expression;

  public ExpressionScript(Expression expression,
                          SearchLookup lookup,
                          LeafReaderContext context,
                          Map<String, Object> params) {
    super(params, lookup, context);
    this.expression = expression;
  }

  @Override
  public boolean execute() {
    // Check we ourselves are not being called by unprivileged code.
    SecurityManager sm = System.getSecurityManager();
    if (sm != null) {
      SpecialPermission.check();
    }

    return AccessController.doPrivileged((PrivilegedAction<Boolean>) () -> {
      Set<ReferenceExpression> fields = extractInputFields();
      ElasticsearchExprValueFactory valueFactory = buildValueFactory(fields);
      Map<String, ExprValue> valueEnv = buildValueEnv(fields, valueFactory);
      ExprValue result = evaluateExpression(valueEnv);
      return (Boolean) result.value();
    });
  }

  private Set<ReferenceExpression> extractInputFields() {
    Set<ReferenceExpression> fields = new HashSet<>();
    doExtractInputFields(expression, fields);
    return fields;
  }

  private void doExtractInputFields(Expression expr, Set<ReferenceExpression> fields) {
    if (expr instanceof FunctionExpression) {
      FunctionExpression func = (FunctionExpression) expr;
      func.getArguments().forEach(argExpr -> doExtractInputFields(argExpr, fields));
    } else if (expr instanceof ReferenceExpression) {
      ReferenceExpression ref = (ReferenceExpression) expr;
      fields.add(ref);
    } // else ignore other expressions, ex. literal, aggregator etc.
  }

  private ElasticsearchExprValueFactory buildValueFactory(Set<ReferenceExpression> fields) {
    Map<String, ExprType> typeEnv = fields.stream()
                                          .collect(Collectors.toMap(
                                              ReferenceExpression::getAttr,
                                              ReferenceExpression::type));
    return new ElasticsearchExprValueFactory(typeEnv);
  }

  private Map<String, ExprValue> buildValueEnv(Set<ReferenceExpression> fields,
                                               ElasticsearchExprValueFactory valueFactory) {
    Map<String, ExprValue> valueEnv = new HashMap<>();
    for (ReferenceExpression field : fields) {
      String fieldName = field.getAttr();
      ScriptDocValues<?> value = extractFieldValue(fieldName);
      if (value != null && !value.isEmpty()) {
        valueEnv.put(fieldName, valueFactory.construct(fieldName, value.get(0)));
      }
    }
    return valueEnv;
  }

  private ScriptDocValues<?> extractFieldValue(String fieldName) {
    Map<String, ScriptDocValues<?>> doc = getDoc();
    String keyword = fieldName + ".keyword";

    ScriptDocValues<?> value = null;
    if (doc.containsKey(keyword)) {
      value = doc.get(keyword);
    } else if (doc.containsKey(fieldName)) {
      value = doc.get(fieldName);
    }
    return value;
  }

  private ExprValue evaluateExpression(Map<String, ExprValue> valueEnv) {
    ExprTupleValue tupleValue = ExprTupleValue.fromExprValueMap(valueEnv);
    ExprValue result = expression.valueOf(tupleValue.bindingTuples());

    if (result.type() != ExprCoreType.BOOLEAN) {
      throw new IllegalStateException("Expression has wrong result type: " + result);
    }
    return result;
  }

}
