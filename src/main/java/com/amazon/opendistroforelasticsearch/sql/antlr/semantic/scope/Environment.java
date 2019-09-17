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

package com.amazon.opendistroforelasticsearch.sql.antlr.semantic.scope;

import com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.Type;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Environment for symbol and its attribute (type) in the current scope
 */
public class Environment {

    private final Environment parent;

    private final SymbolTable symbolTable;

    public Environment(Environment parent) {
        this.parent = parent;
        this.symbolTable = new SymbolTable();
    }

    /**
     * Define symbol with the type
     * @param symbol    symbol to define
     * @param type      type
     */
    public void define(Symbol symbol, Type type) {
        symbolTable.store(symbol, type);
    }

    /**
     * Resolve symbol in the environment
     * @param symbol    symbol to look up
     * @return          type if exist
     */
    public Optional<Type> resolve(Symbol symbol) {
        Optional<Type> type = Optional.empty();
        for (Environment cur = this; cur != null; cur = cur.parent) {
            type = cur.symbolTable.lookup(symbol);
            if (type.isPresent()) {
                break;
            }
        }
        return type;
    }

    public Map<String, Type> resolveByPrefix(Symbol prefix) {
        return new HashMap<>(symbolTable.lookupByPrefix(prefix)); // TODO: look up in env chain or only current level?
    }

    public Map<String, Type> resolveAll(Namespace namespace) {
        return symbolTable.lookupAll(namespace);
    }

    public Environment getParent() {
        return parent;
    }

}
