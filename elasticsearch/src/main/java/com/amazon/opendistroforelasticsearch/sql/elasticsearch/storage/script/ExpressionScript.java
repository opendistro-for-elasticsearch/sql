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

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.FunctionExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.ReferenceExpression;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
    SpecialPermission.check();

    return AccessController.doPrivileged((PrivilegedAction<Boolean>) () -> {

      // 1) getDoc() is not iterable;
      // 2) Doc value is array; 3) Get text field ends up with exception
      Set<String> fieldNames = extractInputFieldNames();
      Map<String, Object> values = extractFieldNameAndValues(fieldNames);
      ExprValue result = evaluateExpression(values);
      return (Boolean) result.value();
    });
  }

  private Set<String> extractInputFieldNames() {
    Set<String> fieldNames = new HashSet<>();
    doExtractInputFieldNames(expression, fieldNames);
    return fieldNames;
  }

  private void doExtractInputFieldNames(Expression expr, Set<String> fieldNames) {
    if (expr instanceof FunctionExpression) { // Assume only function input arguments is recursive
      FunctionExpression func = (FunctionExpression) expr;
      func.getArguments().forEach(argExpr -> doExtractInputFieldNames(argExpr, fieldNames));
    } else if (expr instanceof ReferenceExpression) {
      ReferenceExpression ref = (ReferenceExpression) expr;
      fieldNames.add(ref.getAttr());
    }
  }

  private Map<String, Object> extractFieldNameAndValues(Set<String> fieldNames) {
    Map<String, Object> values = new HashMap<>();
    for (String fieldName : fieldNames) {
      ScriptDocValues<?> value = extractFieldValue(fieldName);
      if (value != null && !value.isEmpty()) {
        values.put(fieldName, value.get(0));
      }
    }
    return values;
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

  private ExprValue evaluateExpression(Map<String, Object> values) {
    ExprValue tupleValue = ExprValueUtils.tupleValue(values);
    ExprValue result = expression.valueOf(tupleValue.bindingTuples());

    if (result.type() != ExprCoreType.BOOLEAN) {
      throw new IllegalStateException("Expression has wrong result type: " + result);
    }
    return result;
  }
}
