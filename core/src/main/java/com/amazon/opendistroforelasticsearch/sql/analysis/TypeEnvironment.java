/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.analysis;

import com.amazon.opendistroforelasticsearch.sql.analysis.symbol.Namespace;
import com.amazon.opendistroforelasticsearch.sql.analysis.symbol.Symbol;
import com.amazon.opendistroforelasticsearch.sql.analysis.symbol.SymbolTable;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprType;
import com.amazon.opendistroforelasticsearch.sql.exception.SemanticCheckException;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.ReferenceExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.env.Environment;
import lombok.Getter;

import java.util.Optional;

/**
 * The definition of Type Environment.
 */
public class TypeEnvironment implements Environment<Expression, ExprType> {
    @Getter
    private final TypeEnvironment parent;
    private final SymbolTable symbolTable;

    public TypeEnvironment(TypeEnvironment parent) {
        this.parent = parent;
        this.symbolTable = new SymbolTable();
    }

    public TypeEnvironment(TypeEnvironment parent, SymbolTable symbolTable) {
        this.parent = parent;
        this.symbolTable = symbolTable;
    }

    /**
     * Resolve the {@link Expression} from environment.
     *
     * @param var expression
     * @return resolved {@link ExprType}
     */
    @Override
    public ExprType resolve(Expression var) {
        if (var instanceof ReferenceExpression) {
            ReferenceExpression ref = (ReferenceExpression) var;
            for (TypeEnvironment cur = this; cur != null; cur = cur.parent) {
                Optional<ExprType> typeOptional = cur.symbolTable.lookup(new Symbol(Namespace.FIELD_NAME,
                        ref.getAttr()));
                if (typeOptional.isPresent()) {
                    return typeOptional.get();
                }
            }
        }
        throw new SemanticCheckException(String.format("can't resolve expression %s in type env", var));
    }

    /**
     * Define symbol with the type
     *
     * @param var  symbol to define
     * @param type type
     */
    public void define(Expression var, ExprType type) {
        if (var instanceof ReferenceExpression) {
            ReferenceExpression ref = (ReferenceExpression) var;
            symbolTable.store(new Symbol(Namespace.FIELD_NAME, ref.getAttr()), type);
        } else {
            throw new IllegalArgumentException(String.format("only support define reference, unexpected expression %s"
                    , var));
        }
    }

}
