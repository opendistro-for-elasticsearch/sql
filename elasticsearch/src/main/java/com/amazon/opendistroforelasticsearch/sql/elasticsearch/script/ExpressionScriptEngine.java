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


import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import org.apache.lucene.index.LeafReaderContext;
import org.elasticsearch.SpecialPermission;
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
import java.util.Map;
import java.util.Set;

/**
 * Custom script language to support our expression execution inside Elasticsearch engine.
 */
public class ExpressionScriptEngine implements ScriptEngine {

    @Override
    public String getType() {
        return "expression";
    }

    @Override
    public <FactoryType> FactoryType compile(String templateName,
                                             String templateSource,
                                             ScriptContext<FactoryType> context,
                                             Map<String, String> params) {

        Expression template = compile(templateSource);
        ExpressionScriptFactory factory = new ExpressionScriptFactory(template);
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
        private final Expression template;

        public ExpressionScriptFactory(Expression template) {
            this.template = template;
        }

        @Override
        public FilterScript.LeafFactory newFactory(Map<String, Object> params, SearchLookup lookup) {
            return new ExpressionScriptLeafFactory(template, params, lookup);
        }
    }

    private static class ExpressionScriptLeafFactory implements FilterScript.LeafFactory {
        private final Expression template;
        private final Map<String, Object> params;
        private final SearchLookup lookup;

        public ExpressionScriptLeafFactory(Expression template, Map<String, Object> params, SearchLookup lookup) {
            this.template = template;
            this.params = params;
            this.lookup = lookup;
        }

        @Override
        public FilterScript newInstance(LeafReaderContext ctx) {
            return new ExpressionScript(template, lookup, ctx, params);
        }
    }

    private static class ExpressionScript extends FilterScript {
        private final Expression template;

        public ExpressionScript(Expression template,
                                SearchLookup lookup,
                                LeafReaderContext context,
                                Map<String, Object> params) {
            super(params, lookup, context);

            this.template = template;
        }

        @Override
        public boolean execute() {
            // Check we ourselves are not being called by unprivileged code.
            SpecialPermission.check();

            return AccessController.doPrivileged((PrivilegedAction<Boolean>) () -> {
                return true;
            });
        }
    }

}
