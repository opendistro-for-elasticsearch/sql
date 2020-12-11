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
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprType;
import com.amazon.opendistroforelasticsearch.sql.exception.SemanticCheckException;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.ReferenceExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.env.Environment;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;

/**
 * The definition of Type Environment.
 */
public class TypeEnvironment implements Environment<Symbol, ExprType> {
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
   * @param symbol Symbol
   * @return resolved {@link ExprType}
   */
  @Override
  public ExprType resolve(Symbol symbol) {
    for (TypeEnvironment cur = this; cur != null; cur = cur.parent) {
      Optional<ExprType> typeOptional = cur.symbolTable.lookup(symbol);
      if (typeOptional.isPresent()) {
        return typeOptional.get();
      }
    }
    throw new SemanticCheckException(
        String.format("can't resolve %s in type env", symbol));
  }

  /**
   * Resolve all fields in the current environment.
   * @param namespace     a namespace
   * @return              all symbols in the namespace
   */
  public Map<String, ExprType> lookupAllFields(Namespace namespace) {
    Map<String, ExprType> result = new LinkedHashMap<>();
    symbolTable.lookupAllFields(namespace).forEach(result::putIfAbsent);
    return result;
  }

  /**
   * Define symbol with the type.
   *
   * @param symbol symbol to define
   * @param type   type
   */
  public void define(Symbol symbol, ExprType type) {
    symbolTable.store(symbol, type);
  }

  /**
   * Define expression with the type.
   *
   * @param ref {@link ReferenceExpression}
   */
  public void define(ReferenceExpression ref) {
    define(new Symbol(Namespace.FIELD_NAME, ref.getAttr()), ref.type());
  }

  public void remove(Symbol symbol) {
    symbolTable.remove(symbol);
  }

  /**
   * Remove ref.
   */
  public void remove(ReferenceExpression ref) {
    remove(new Symbol(Namespace.FIELD_NAME, ref.getAttr()));
  }
}
