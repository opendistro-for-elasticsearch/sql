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
import com.amazon.opendistroforelasticsearch.sql.antlr.semantic.scope.Symbol;
import com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.BaseType;
import com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.ScalarFunction;
import com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.Type;
import com.amazon.opendistroforelasticsearch.sql.antlr.visitor.ParseTreeVisitor;
import com.amazon.opendistroforelasticsearch.sql.esdomain.LocalClusterState;
import com.amazon.opendistroforelasticsearch.sql.esdomain.mapping.FieldMappings;
import com.amazon.opendistroforelasticsearch.sql.esdomain.mapping.IndexMappings;
import com.amazon.opendistroforelasticsearch.sql.utils.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.BaseType.UNKNOWN;

/**
 * SQL semantic analyzer that determines if a syntactical correct query is meaningful.
 */
public class SemanticAnalyzer implements ParseTreeVisitor<Type> {

    /** Environment stack for symbol scope management */
    private Environment environment = new Environment(null);

    /** Local cluster state for mapping query */
    private final LocalClusterState clusterState;

    public SemanticAnalyzer(LocalClusterState clusterState) {
        this.clusterState = clusterState;
    }

    @Override
    public void visitRoot() {
        for (ScalarFunction func : ScalarFunction.values()) {
            defineFunctionName(func.name(), func);
        }
    }

    @Override
    public void visitQuery() {
        environment = new Environment(environment);
    }

    @Override
    public Type endVisitQuery() {
        environment = environment.getParent();
        return null;
    }

    /**
     *
     * Example:
     *
     * visit(
     *
     * visit(
     *
     * @param indexName     index name in the FROM clause
     * @param alias         optional alias
     * @return              type
     */
    @Override
    public Type visitIndexName(String indexName, Optional<String> alias) {
        if (isPath(indexName)) {
            Type type = resolve(new Symbol(Namespace.FIELD_NAME, indexName));
            if (type != BaseType.NESTED) {
                throw new SemanticAnalysisException(StringUtils.format(
                    "Field [%s] is [%s] type but nested type is required", indexName, type));
            }

            if (alias.isPresent()) {
                redefineFieldNameByAlias(indexName, alias);
            }
        }
        else {
            IndexMappings indexMappings = clusterState.getFieldMappings(new String[]{indexName});
            FieldMappings mappings = indexMappings.firstMapping().firstMapping();

            if (alias.isPresent()) {
                defineFieldName(alias.get(), UNKNOWN); //TODO: need a Index type here
                mappings.flat((fieldName, type) -> defineFieldName(alias.get() + "." + fieldName, type));
            } else {
                mappings.flat(this::defineFieldName);
            }
        }
        return null;
    }

    private void redefineFieldNameByAlias(String indexName, Optional<String> alias) {
        Map<String, Type> typeByFullName = environment.resolveByPrefix(new Symbol(Namespace.FIELD_NAME, indexName));
        typeByFullName.forEach(
            (fieldName, fieldType) -> defineFieldName(fieldName.replace(indexName, alias.get()), fieldType)
        );
    }

    private boolean isPath(String indexName) {
        return indexName.indexOf('.', 1) != -1; // taking care of .kibana
    }

    private void defineFieldName(String fieldName, String type) {
        defineFieldName(fieldName, BaseType.valueOf(StringUtils.toUpper(type)));
    }

    private void defineFieldName(String fieldName, Type type) {
        Symbol symbol = new Symbol(Namespace.FIELD_NAME, fieldName);
        if (environment.resolve(symbol).isPresent()) {
            throw new SemanticAnalysisException(StringUtils.format(
                "%s is conflicting with field of same name defined by other index", symbol));
        }
        environment.define(symbol, type);
    }

    private void defineFunctionName(String funcName, Type type) {
        environment.define(new Symbol(Namespace.FUNCTION_NAME, funcName), type);
    }

    public Type visitWhere(Runnable visitDeep) {
        environment = new Environment(environment);

        /*
        for (ScalarFunctionTypeExpression type : ScalarFunctionTypeExpression.values()) {
            environment.define(new Symbol(Namespace.FUNCTION_NAME, type.getName()), type);
        }
        */

        visitDeep.run();

        //environment = environment.getParent();
        return null;
    }

    public Type visitSelectClause(Runnable visitDeep) {
        environment = new Environment(environment);

        /*
        for (AggregateFunctionType type : AggregateFunctionType.values()) {
            environment.define(new Symbol(Namespace.FUNCTION_NAME, type.getName()), type);
        }
        */

        visitDeep.run();

        environment = environment.getParent();
        return null;
    }

    public Type visitFunctionCall(Type constructorType, Type... actualArgTypes) {
        //return constructorType.apply(actualArgTypes);
        return null;
    }

    @Override
    public Type visitFieldName(String fieldName) {
        return resolve(new Symbol(Namespace.FIELD_NAME, fieldName));
    }

    @Override
    public Type visitFunctionName(String funcName) {
        return resolve(new Symbol(Namespace.FUNCTION_NAME, funcName));
    }

    public Type visitOperatorName(String opName) {
        //return new OperatorType(opName);
        return null;
    }

    public Type resolve(Symbol symbol) {
        Optional<Type> type = environment.resolve(symbol);
        if (!type.isPresent()) {
            Set<String> allSymbolsInScope = environment.resolveAll(symbol.getNamespace()).keySet();
            List<String> suggestedWords = new StringSimilarity(allSymbolsInScope).similarTo(symbol.getName());
            throw new SemanticAnalysisException(
                StringUtils.format("%s cannot be found or used here. Did you mean [%s]?", // TODO: Give none or different suggestion if suggestion = original symbol
                    symbol, suggestedWords.get(0)));
                //at(sql, ctx).
                //suggestion("Did you mean [%s]?", String.join(", ", suggestedWords)).build();
        }
        return type.get();
    }

    public Type visitString(String text) {
        return BaseType.STRING;
    }

    public Type visitNumber(String text) { //TODO: float or integer?
        return BaseType.NUMBER;
    }

    public Type visitBoolean(String text) {
        return BaseType.BOOLEAN;
    }

}
