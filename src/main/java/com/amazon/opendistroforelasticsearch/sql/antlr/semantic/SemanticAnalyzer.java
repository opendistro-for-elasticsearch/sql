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
import com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.ComparisonOperator;
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
        defineOperatorNames(ComparisonOperator.values());
    }

    @Override
    public void visitQuery() {
        context.push();
    }

    @Override
    public void endVisitQuery() {
        context.pop();
    }

    @Override
    public Type visitSelectItem(Type type, String alias) {
        if (!alias.isEmpty()) {
            defineFieldName(alias, type);
        }
        return type;
    }

    /**
     * Visit index name by initializing environment (scope).
     *
     * For example: "SELECT * FROM accounts [a]".
     * Suppose accounts includes 'name', 'age' and nested field 'projects'
     *  and projects includes 'name' and 'active'.
     *
     *  1. Define itself:
     *      ----- new definitions -----
     *      accounts or a -> INDEX
     *
     *  2. Define without alias no matter if alias given:
     *      'accounts' or 'a' -> INDEX
     *      ----- new definitions -----
     *      'name' -> TEXT
     *      'age' -> INTEGER
     *      'projects' -> NESTED
     *      'projects.name' -> KEYWORD
     *      'projects.active' -> BOOLEAN
     *
     *  3.1 Define with alias if given:
     *      'accounts' or 'a' -> INDEX
     *      'name' -> TEXT
     *      'age' -> INTEGER
     *      'projects' -> NESTED
     *      'projects.name' -> KEYWORD
     *      'projects.active' -> BOOLEAN
     *      ----- new definitions -----
     *      'a.name' -> TEXT
     *      'a.age' -> INTEGER
     *      'a.projects' -> NESTED
     *      'a.projects.name' -> KEYWORD
     *      'a.projects.active' -> BOOLEAN
     *
     *  3.2 Otherwise define by index full name:
     *      'accounts' or 'a' -> INDEX
     *      'name' -> TEXT
     *      'age' -> INTEGER
     *      'projects' -> NESTED
     *      'projects.name' -> KEYWORD
     *      'projects.active' -> BOOLEAN
     *      ----- new definitions -----
     *      'accounts.name' -> TEXT
     *      'accounts.age' -> INTEGER
     *      'accounts.projects' -> NESTED
     *      'accounts.projects.name' -> KEYWORD
     *      'accounts.projects.active' -> BOOLEAN
     */
    @Override
    public Type visitIndexName(String indexName, String alias) {
        FieldMappings mappings = context.getMapping(indexName);
        String aliasName = alias.isEmpty() ? indexName : alias;

        defineFieldName(aliasName, INDEX);
        mappings.flat(this::defineFieldName);
        mappings.flat((fieldName, type) -> defineFieldName(aliasName + "." + fieldName, type));
        return INDEX;
    }

    /**
     * Visit nested index (field) name by defining more by its alias.
     *
     * For example: "SELECT * FROM accounts a, a.projects p".
     * Suppose projects includes 'name' and 'active'
     *  and environment is like this after visiting 'accounts a':
     *      'accounts' or 'a' -> INDEX
     *      'name' -> TEXT
     *      'age' -> INTEGER
     *      'projects' -> NESTED
     *      'a.name' -> TEXT
     *      'a.age' -> INTEGER
     *      'a.projects' -> NESTED
     *      'a.projects.name' -> KEYWORD
     *      'a.projects.active' -> BOOLEAN
     *
     * Environment changed as below after visiting 'a.projects p':
     *      'accounts' or 'a' -> INDEX
     *      'name' -> TEXT
     *      'age' -> INTEGER
     *      'projects' -> NESTED
     *      'a.name' -> TEXT
     *      'a.age' -> INTEGER
     *      'a.projects' -> NESTED
     *      'a.projects.name' -> KEYWORD
     *      'a.projects.active' -> BOOLEAN
     *      ----- new definitions -----
     *      'p' -> NESTED
     *      'p.name' -> KEYWORD
     *      'p.active' -> BOOLEAN
     */
    @Override
    public Type visitNestedIndexName(String indexName, String alias) {
        Type type = resolve(new Symbol(Namespace.FIELD_NAME, indexName));
        if (type != BaseType.NESTED) {
            throw new SemanticAnalysisException(StringUtils.format(
                "Field [%s] is [%s] type but nested type is required.", indexName, type));
        }

        if (!alias.isEmpty()) {
            redefineFieldNameByPrefixingAlias(indexName, alias);
        }
        return NESTED_INDEX;
    }

    @Override
    public Type visitIndexPattern(String indexPattern, String alias) {
        throw new SemanticUnsupportedException("Index pattern is valid but current semantic analyzer cannot parse it");
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
        return resolve(new Symbol(Namespace.FUNCTION_NAME, StringUtils.toUpper(funcName)));
    }

    @Override
    public Type visitComparisonOperator(String opName) {
        return resolve(new Symbol(Namespace.OPERATOR_NAME, StringUtils.toUpper(opName)));
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

    /** Define by replace with alias */
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
            defineFunctionName(expr.getName(), expr);
        }
    }

    private void defineFunctionName(String funcName, Type type) {
        environment().define(new Symbol(Namespace.FUNCTION_NAME, funcName), type);
    }

    private void defineOperatorNames(TypeExpression[] expressions) {
        for (TypeExpression expr : expressions) {
            defineOperatorName(expr.getName(), expr);
        }
    }

    private void defineOperatorName(String opName, Type type) {
        environment().define(new Symbol(Namespace.OPERATOR_NAME, opName), type);
    }

    private Type resolve(Symbol symbol) {
        Optional<Type> type = environment().resolve(symbol);
        if (!type.isPresent()) {
            Set<String> allSymbolsInScope = environment().resolveAll(symbol.getNamespace()).keySet();
            List<String> suggestedWords = new StringSimilarity(allSymbolsInScope).similarTo(symbol.getName());

            // TODO: Give none or different suggestion if suggestion = original symbol
            throw new SemanticAnalysisException(
                StringUtils.format("%s cannot be found or used here. Did you mean [%s]?",
                    symbol, suggestedWords.get(0)));
        }
        return type.get();
    }

    private Environment environment() {
        return context.peek();
    }

}
