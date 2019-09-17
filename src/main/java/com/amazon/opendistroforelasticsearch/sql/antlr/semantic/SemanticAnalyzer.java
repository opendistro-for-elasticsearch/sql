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

import java.util.Map;
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
        if (isDotId(indexName)) {
            Type type = resolve(new Symbol(Namespace.FIELD_NAME, indexName));
            if (type != BaseType.NESTED) {
                throw new SemanticAnalysisException(StringUtils.format(
                    "Field [%s] is [%s] type but nested type is required", indexName, type));
            }

            if (alias.isPresent()) {
                redefineByAlias(indexName, alias);
            }
        }
        else {
            IndexMappings indexMappings = clusterState.getFieldMappings(new String[]{indexName});
            FieldMappings mappings = indexMappings.firstMapping().firstMapping();
            flatMappings(mappings.data(), alias.orElse(""));
        }
        return null;
    }

    private void redefineByAlias(String indexName, Optional<String> alias) {
        Map<String, Type> map = environment.resolveByPrefix(new Symbol(Namespace.FIELD_NAME, indexName));
        map.forEach(
            (fieldName, fieldType) ->
                environment.define(
                    new Symbol(Namespace.FIELD_NAME, fieldName.replace(indexName, alias.get())),
                    fieldType
                )
        );
    }

    private boolean isDotId(String indexName) {
        return indexName.indexOf('.', 1) != -1; // taking care of .kibana
    }

    @SuppressWarnings("unchecked")
    private void flatMappings(Map<String, Map<String, Object>> mappings, String path) {
        mappings.forEach(
            (fieldName, mapping) -> {
                String fullFieldName = path + "." + fieldName;
                String type = (String) mapping.getOrDefault("type", "object");

                if (isFieldAlreadyDefined(fullFieldName)) {
                    throw new SemanticAnalysisException(StringUtils.format(
                        "Field [%s] is conflicting with field of same name defined by other index", fullFieldName));
                }
                defineFieldName(fullFieldName, type);

                if (mapping.containsKey("properties")) {
                    flatMappings(
                        (Map<String, Map<String, Object>>) mapping.get("properties"),
                        fullFieldName
                    );
                }
            }
        );
    }

    private boolean isFieldAlreadyDefined(String fieldName) {
        return environment.resolve(new Symbol(Namespace.FIELD_NAME, fieldName)).isPresent();
    }

    private void defineFieldName(String fieldName, String type) {
        environment.define(new Symbol(Namespace.FIELD_NAME, fieldName), BaseType.valueOf(StringUtils.toUpper(type)));
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
