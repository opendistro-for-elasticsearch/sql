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

package com.amazon.opendistroforelasticsearch.sql.antlr.semantic.visitor;

import com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.Type;
import com.amazon.opendistroforelasticsearch.sql.antlr.visitor.GenericSqlParseTreeVisitor;
import com.amazon.opendistroforelasticsearch.sql.utils.BackticksUnquoter;

import java.util.List;

/**
 * Main visitor implementation to drive the entire semantic analysis.
 */
public class SemanticAnalyzer implements GenericSqlParseTreeVisitor<Type> {

    private final ESMappingLoader mappingLoader;

    private final TypeChecker typeChecker;

    public SemanticAnalyzer(ESMappingLoader mappingLoader, TypeChecker typeChecker) {
        this.mappingLoader = mappingLoader;
        this.typeChecker = typeChecker;
    }

    @Override
    public void visitRoot() {
        mappingLoader.visitRoot();
        typeChecker.visitRoot();
    }

    @Override
    public void visitQuery() {
        mappingLoader.visitQuery();
        typeChecker.visitQuery();
    }

    @Override
    public void endVisitQuery() {
        mappingLoader.endVisitQuery();
        typeChecker.endVisitQuery();
    }

    @Override
    public Type visitSelect(List<Type> itemTypes) {
        mappingLoader.visitSelect(itemTypes);
        return typeChecker.visitSelect(itemTypes);
    }

    @Override
    public void visitAs(String alias, Type type) {
        mappingLoader.visitAs(new BackticksUnquoter().unquoteSingleField(alias), type);
        typeChecker.visitAs(new BackticksUnquoter().unquoteSingleField(alias), type);
    }

    @Override
    public Type visitIndexName(String indexName) {
        mappingLoader.visitIndexName(new BackticksUnquoter().unquoteSingleField(indexName));
        return typeChecker.visitIndexName(new BackticksUnquoter().unquoteSingleField(indexName));
    }

    @Override
    public Type visitFieldName(String fieldName) {
        mappingLoader.visitFieldName(new BackticksUnquoter().unquoteFullColumn(fieldName));
        return typeChecker.visitFieldName(new BackticksUnquoter().unquoteFullColumn(fieldName));
    }

    @Override
    public Type visitFunctionName(String funcName) {
        mappingLoader.visitFunctionName(funcName);
        return typeChecker.visitFunctionName(funcName);
    }

    @Override
    public Type visitOperator(String opName) {
        mappingLoader.visitOperator(opName);
        return typeChecker.visitOperator(opName);
    }

    @Override
    public Type visitString(String text) {
        mappingLoader.visitString(text);
        return typeChecker.visitString(text);
    }

    @Override
    public Type visitInteger(String text) {
        mappingLoader.visitInteger(text);
        return typeChecker.visitInteger(text);
    }

    @Override
    public Type visitFloat(String text) {
        mappingLoader.visitFloat(text);
        return typeChecker.visitFloat(text);
    }

    @Override
    public Type visitBoolean(String text) {
        mappingLoader.visitBoolean(text);
        return typeChecker.visitBoolean(text);
    }

    @Override
    public Type visitDate(String text) {
        mappingLoader.visitDate(text);
        return typeChecker.visitDate(text);
    }

    @Override
    public Type defaultValue() {
        mappingLoader.defaultValue();
        return typeChecker.defaultValue();
    }
}
