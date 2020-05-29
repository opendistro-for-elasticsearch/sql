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

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.script;


import com.amazon.opendistroforelasticsearch.sql.data.model.ExprType;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.FunctionExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.ReferenceExpression;
import org.apache.lucene.index.LeafReaderContext;
import org.elasticsearch.SpecialPermission;
import org.elasticsearch.index.fielddata.ScriptDocValues;
import org.elasticsearch.script.FilterScript;
import org.elasticsearch.script.ScriptContext;
import org.elasticsearch.script.ScriptEngine;
import org.elasticsearch.search.lookup.SearchLookup;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Custom script language to support our expression execution inside Elasticsearch engine.
 */
public class ExpressionScriptEngine implements ScriptEngine {

    public static final String EXPRESSION_LANG_NAME = "opendistro_expression";

    @Override
    public String getType() {
        return EXPRESSION_LANG_NAME;
    }

    @Override
    public <FactoryType> FactoryType compile(String templateName,
                                             String templateSource,
                                             ScriptContext<FactoryType> context,
                                             Map<String, String> params) {

        Expression expression = compile(templateSource);
        ExpressionScriptFactory factory = new ExpressionScriptFactory(expression);
        return context.factoryClazz.cast(factory);
    }

    @Override
    public Set<ScriptContext<?>> getSupportedContexts() {
        return Collections.singleton(new ScriptContext<>("expression", ExpressionScriptFactory.class));
    }

    /**
     * In fact the expression source is already compiled in query engine.
     * The "source" is compiled expression tree in serialized format.
     * Therefore here compile is simply to deserialize and restore it to expression tree.
     */
    private Expression compile(String source) {
        try {
            ByteArrayInputStream input = new ByteArrayInputStream(Base64.getDecoder().decode(source));
            ObjectInputStream objectInput = new ObjectInputStream(input);
            return (Expression) objectInput.readObject();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize source: " + source, e);
        }
    }

    private static class ExpressionScriptFactory implements FilterScript.Factory {
        private final Expression expression;

        public ExpressionScriptFactory(Expression expression) {
            this.expression = expression;
        }

        @Override
        public FilterScript.LeafFactory newFactory(Map<String, Object> params, SearchLookup lookup) {
            return new ExpressionScriptLeafFactory(expression, params, lookup);
        }
    }

    private static class ExpressionScriptLeafFactory implements FilterScript.LeafFactory {
        private final Expression expression;
        private final Map<String, Object> params;
        private final SearchLookup lookup;

        public ExpressionScriptLeafFactory(Expression expression, Map<String, Object> params, SearchLookup lookup) {
            this.expression = expression;
            this.params = params;
            this.lookup = lookup;
        }

        @Override
        public FilterScript newInstance(LeafReaderContext ctx) {
            return new ExpressionScript(expression, lookup, ctx, params);
        }
    }

    private static class ExpressionScript extends FilterScript {
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

                // 1) getDoc() is not iterable; 2) Doc value is array; 3) Get text field ends up with exception
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

            if (result.type() != ExprType.BOOLEAN) {
                throw new IllegalStateException("Expression has wrong result type: " + result);
            }
            return result;
        }
    }

}
