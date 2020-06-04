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

package com.amazon.opendistroforelasticsearch.sql.legacy.antlr.visitor;

import java.util.List;

/**
 * Generic parse tree visitor without dependency on concrete parse tree class.
 */
public interface GenericSqlParseTreeVisitor<T> {

    default void visitRoot() {}

    default void visitQuery() {}

    default void endVisitQuery() {}

    default T visitSelect(List<T> items) {
        return defaultValue();
    }

    default T visitSelectAllColumn() {
        return defaultValue();
    }

    default void visitAs(String alias, T type) {}

    default T visitIndexName(String indexName) {
        return defaultValue();
    }

    default T visitFieldName(String fieldName) {
        return defaultValue();
    }

    default T visitFunctionName(String funcName) {
        return defaultValue();
    }

    default T visitOperator(String opName) {
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

    default T visitDate(String text) {
        return defaultValue();
    }

    default T visitNull() {
        return defaultValue();
    }

    default T visitConvertedType(String text) {
        return defaultValue();
    }

    default T defaultValue() {
        return null;
    }

}
