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

import java.util.EnumMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptyNavigableMap;

/**
 * Symbol table for symbol definition and resolution.
 */
public class SymbolTable {

    /** Two-dimension hash table to manage symbols with type in different namespace */
    private Map<Namespace, NavigableMap<String, Type>> tableByNamespace = new EnumMap<>(Namespace.class);

    /**
     * Store symbol with the type. Create new map for namespace for the first time.
     * @param symbol    symbol to define
     * @param type      symbol type
     */
    public void store(Symbol symbol, Type type) {
        tableByNamespace.computeIfAbsent(
            symbol.getNamespace(),
            ns -> new TreeMap<>()
        ).put(symbol.getName(), type);
    }

    /**
     * Look up symbol in the namespace map.
     * @param symbol    symbol to look up
     * @return          symbol type which is optional
     */
    public Optional<Type> lookup(Symbol symbol) {
        Map<String, Type> table = tableByNamespace.get(symbol.getNamespace());
        Type type = null;
        if (table != null) {
            type = table.get(symbol.getName());
        }
        return Optional.ofNullable(type);
    }

    /**
     * Look up symbols by a prefix.
     * @param prefix    a symbol prefix
     * @return          symbols starting with the prefix
     */
    public Map<String, Type> lookupByPrefix(Symbol prefix) {
        NavigableMap<String, Type> table = tableByNamespace.get(prefix.getNamespace());
        if (table != null) {
            return table.subMap(prefix.getName(), prefix.getName() + Character.MAX_VALUE);
        }
        return emptyMap();
    }

    /**
     * Look up all symbols in the namespace.
     * @param namespace     a namespace
     * @return              all symbols in the namespace map
     */
    public Map<String, Type> lookupAll(Namespace namespace) {
        return tableByNamespace.getOrDefault(namespace, emptyNavigableMap());
    }

    /**
     * Check if namespace map in empty (none definition)
     * @param namespace     a namespace
     * @return              true for empty
     */
    public boolean isEmpty(Namespace namespace) {
        return tableByNamespace.getOrDefault(namespace, emptyNavigableMap()).isEmpty();
    }
}
