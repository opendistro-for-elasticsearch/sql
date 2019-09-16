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

import com.amazon.opendistroforelasticsearch.sql.antlr.semantic.scope.Environment;
import com.amazon.opendistroforelasticsearch.sql.antlr.semantic.scope.Namespace;
import com.amazon.opendistroforelasticsearch.sql.antlr.semantic.scope.Symbol;
import com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.BaseType;
import com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.Type;
import com.amazon.opendistroforelasticsearch.sql.antlr.visitor.ParseTreeVisitor;
import com.amazon.opendistroforelasticsearch.sql.esdomain.LocalClusterState;
import com.amazon.opendistroforelasticsearch.sql.esdomain.LocalClusterState.IndexMappings;
import com.amazon.opendistroforelasticsearch.sql.utils.StringUtils;

import java.util.Optional;

import static com.amazon.opendistroforelasticsearch.sql.esdomain.LocalClusterState.FieldMappings;

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
    public Type visitIndexName(String indexName, Optional<String> alias) {
        IndexMappings indexMappings = clusterState.getFieldMappings(new String[]{ indexName });
        FieldMappings mappings = indexMappings.firstMapping().firstMapping();
        mappings.data().forEach(
            (fieldName, mapping) -> environment.define(
                new Symbol(Namespace.FIELD_NAME, alias.map(s -> s + "." + fieldName).orElse(fieldName)),
                BaseType.typeIn(mapping)
            )
        );
        return null;
    }

    public void visitQuery() {
        //environment = new Environment(environment);

        //visitDeep.run();

        //environment = environment.getParent();
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
            //List<String> suggestedWords = new StringSimilarity(
            //    environment.allSymbolsIn(symbol.getNamespace())).similarTo(symbol.getName());
            throw new SemanticAnalysisException(StringUtils.format("%s cannot be found or used here.", symbol));
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
