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

package com.amazon.opendistroforelasticsearch.sql.antlr.visitor;

import java.util.Optional;

/**
 * Parse tree visitor
 */
public interface ParseTreeVisitor<T> {

    default void visitRoot() {}

    default void visitQuery() {}
    default T endVisitQuery() {
        return defaultValue();
    }

    default T visitSelectItem(T type, String alias) {
        return defaultValue();
    }

    default void visitFrom() {}
    default T endVisitFrom() {
        return defaultValue();
    }

    default void visitWhere() {}
    default T endVisitWhere() {
        return defaultValue();
    }

    default T visitIndexName(String indexName, String alias) {
        return defaultValue();
    }

    default T visitNestedIndexName(String indexName, String alias) {
        return defaultValue();
    }

    default T visitIndexPattern(String indexPattern, String alias) {
        return defaultValue();
    }

    default T visitJoin(String joinType) {
        return defaultValue();
    }

    default T visitFieldName(String fieldName) {
        return defaultValue();
    }

    default T visitFunctionName(String funcName) {
        return defaultValue();
    }

    default T visitString(String text) {
        return defaultValue();
    }

    default T visitInteger(String text) {
        return defaultValue();
    }

    default T visitFloat(String text) {
        return defaultValue();
    }

    default T visitBoolean(String text) {
        return defaultValue();
    }

    default T defaultValue() {
        return null;
    }
}
