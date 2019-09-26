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

    public void store(Symbol symbol, Type type) {
        tableByNamespace.computeIfAbsent(
            symbol.getNamespace(),
            ns -> new TreeMap<>()
        ).put(symbol.getName(), type);
    }

    public Optional<Type> lookup(Symbol symbol) {
        Map<String, Type> table = tableByNamespace.get(symbol.getNamespace());
        Type type = null;
        if (table != null) {
            type = table.get(symbol.getName());
        }
        return Optional.ofNullable(type);
    }

    public Map<String, Type> lookupByPrefix(Symbol prefix) {
        NavigableMap<String, Type> table = tableByNamespace.get(prefix.getNamespace());
        if (table != null) {
            return table.subMap(prefix.getName(), prefix.getName() + Character.MAX_VALUE);
        }
        return emptyMap();
    }

    public Map<String, Type> lookupAll(Namespace namespace) {
        return tableByNamespace.getOrDefault(namespace, emptyNavigableMap());
    }

    public boolean isEmpty(Namespace namespace) {
        return tableByNamespace.getOrDefault(namespace, emptyNavigableMap()).isEmpty();
    }
}
