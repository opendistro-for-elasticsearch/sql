/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.antlr.semantic;

import com.amazon.opendistroforelasticsearch.sql.antlr.StringSimilarity;
import com.amazon.opendistroforelasticsearch.sql.antlr.semantic.scope.Environment;
import com.amazon.opendistroforelasticsearch.sql.antlr.semantic.scope.Namespace;
import com.amazon.opendistroforelasticsearch.sql.antlr.semantic.scope.SemanticContext;
import com.amazon.opendistroforelasticsearch.sql.antlr.semantic.scope.Symbol;
import com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.AggregateFunction;
import com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.BaseType;
import com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.ESScalarFunction;
import com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.ScalarFunction;
import com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.Type;
import com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.TypeExpression;
import com.amazon.opendistroforelasticsearch.sql.antlr.visitor.GenericSqlParseTreeVisitor;
import com.amazon.opendistroforelasticsearch.sql.esdomain.mapping.FieldMappings;
import com.amazon.opendistroforelasticsearch.sql.utils.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.BaseType.UNKNOWN;
import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.Index.INDEX;
import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.Index.NESTED_INDEX;

/**
 * SQL semantic analyzer that determines if a syntactical correct query is meaningful.
 */
public class SemanticAnalyzer implements GenericSqlParseTreeVisitor<Type> {

    private static final Type DEFAULT = new Type() {
        @Override
        public boolean isCompatible(Type other) {
            throw new IllegalStateException("Compatibility check on NULL type");
        }

        @Override
        public Type construct(List<Type> others) {
            throw new IllegalStateException("Construct operation on NULL type");
        }

        @Override
        public String usage() {
            throw new IllegalStateException("Usage print operation on NULL type");
        }
    };

    /** Semantic context for symbol scope management */
    private final SemanticContext context;

    public SemanticAnalyzer(SemanticContext context) {
        this.context = context;
    }

    @Override
    public void visitRoot() {
        defineFunctionNames(ScalarFunction.values());
        defineFunctionNames(ESScalarFunction.values());
        defineFunctionNames(AggregateFunction.values());
    }

    @Override
    public void visitQuery() {
        context.push();
    }

    @Override
    public Type endVisitQuery() {
        context.pop();
        return null; // TODO: return production of type of select items
    }

    @Override
    public Type visitSelectItem(Type type, String alias) {
        if (!alias.isEmpty()) {
            defineFieldName(alias, type);
        }
        return type;
    }

    @Override
    public Type visitIndexName(String indexName, String alias) {
        FieldMappings mappings = context.getMapping(indexName);
        String aliasName = alias.isEmpty() ? indexName : alias;

        defineFieldName(aliasName, INDEX);
        mappings.flat(this::defineFieldName);
        mappings.flat((fieldName, type) -> defineFieldName(aliasName + "." + fieldName, type));
        return INDEX;
    }

    @Override
    public Type visitNestedIndexName(String indexName, String alias) {
        Type type = resolve(new Symbol(Namespace.FIELD_NAME, indexName));
        if (type != BaseType.NESTED) {
            throw new SemanticAnalysisException(StringUtils.format(
                "Field [%s] is [%s] type but nested type is required", indexName, type));
        }

        if (!alias.isEmpty()) {
            redefineFieldNameByPrefixingAlias(indexName, alias);
        }
        return NESTED_INDEX;
    }

    @Override
    public Type visitIndexPattern(String indexPattern, String alias) {
        throw new UnsupportedSemanticException("Index pattern is valid but current semantic analyzer cannot parse it");
    }

    @Override
    public Type visitFieldName(String fieldName) {
        // Bypass hidden fields which is not present in mapping, ex. _id, _type.
        if (fieldName.startsWith("_")) {
            return UNKNOWN;
        }
        return resolve(new Symbol(Namespace.FIELD_NAME, fieldName));
    }

    @Override
    public Type visitFunctionName(String funcName) {
        // Ignore case for function name
        return resolve(new Symbol(Namespace.FUNCTION_NAME, funcName.toUpperCase()));
    }

    @Override
    public Type visitString(String text) {
        return BaseType.STRING;
    }

    @Override
    public Type visitInteger(String text) {
        return BaseType.INTEGER;
    }

    @Override
    public Type visitFloat(String text) {
        return BaseType.FLOAT;
    }

    @Override
    public Type visitBoolean(String text) {
        return BaseType.BOOLEAN;
    }

    @Override
    public Type defaultValue() {
        return DEFAULT;
    }


    private void redefineFieldNameByPrefixingAlias(String indexName, String alias) {
        Map<String, Type> typeByFullName = environment().resolveByPrefix(new Symbol(Namespace.FIELD_NAME, indexName));
        typeByFullName.forEach(
            (fieldName, fieldType) -> defineFieldName(fieldName.replace(indexName, alias), fieldType)
        );
    }

    private void defineFieldName(String fieldName, String type) {
        defineFieldName(fieldName, BaseType.typeOf(type));
    }

    private void defineFieldName(String fieldName, Type type) {
        Symbol symbol = new Symbol(Namespace.FIELD_NAME, fieldName);
        if (!environment().resolve(symbol).isPresent()) {
            environment().define(symbol, type);
        }
    }

    private void defineFunctionNames(TypeExpression[] expressions) {
        for (TypeExpression expr : expressions) {
            defineFunctionName(expr.name(), expr);
        }
    }

    private void defineFunctionName(String funcName, Type type) {
        environment().define(new Symbol(Namespace.FUNCTION_NAME, funcName), type);
    }

    private Type resolve(Symbol symbol) {
        Optional<Type> type = environment().resolve(symbol);
        if (!type.isPresent()) {
            Set<String> allSymbolsInScope = environment().resolveAll(symbol.getNamespace()).keySet();
            List<String> suggestedWords = new StringSimilarity(allSymbolsInScope).similarTo(symbol.getName());
            throw new SemanticAnalysisException(
                StringUtils.format("%s cannot be found or used here. Did you mean [%s]?",
                    // TODO: Give none or different suggestion if suggestion = original symbol
                    symbol, suggestedWords.get(0)));
            //at(sql, ctx).
            //suggestion("Did you mean [%s]?", String.join(", ", suggestedWords)).build();
        }
        return type.get();
    }

    private Environment environment() {
        return context.peek();
    }

}
