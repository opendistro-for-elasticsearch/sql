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

package com.amazon.opendistroforelasticsearch.sql.analysis.symbol;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptyNavigableMap;

import com.amazon.opendistroforelasticsearch.sql.data.type.ExprType;
import java.util.EnumMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Symbol table for symbol definition and resolution.
 */
public class SymbolTable {

  /**
   * Two-dimension hash table to manage symbols with type in different namespace.
   */
  private Map<Namespace, NavigableMap<String, ExprType>> tableByNamespace =
      new EnumMap<>(Namespace.class);

  /**
   * Store symbol with the type. Create new map for namespace for the first time.
   *
   * @param symbol symbol to define
   * @param type   symbol type
   */
  public void store(Symbol symbol, ExprType type) {
    tableByNamespace.computeIfAbsent(
        symbol.getNamespace(),
        ns -> new TreeMap<>()
    ).put(symbol.getName(), type);
  }

  /**
   * Remove a symbol from SymbolTable.
   */
  public void remove(Symbol symbol) {
    tableByNamespace.computeIfPresent(
        symbol.getNamespace(),
        (k, v) -> {
          v.remove(symbol.getName());
          return v;
        }
    );
  }

  /**
   * Look up symbol in the namespace map.
   *
   * @param symbol symbol to look up
   * @return symbol type which is optional
   */
  public Optional<ExprType> lookup(Symbol symbol) {
    Map<String, ExprType> table = tableByNamespace.get(symbol.getNamespace());
    ExprType type = null;
    if (table != null) {
      type = table.get(symbol.getName());
    }
    return Optional.ofNullable(type);
  }

  /**
   * Look up symbols by a prefix.
   *
   * @param prefix a symbol prefix
   * @return symbols starting with the prefix
   */
  public Map<String, ExprType> lookupByPrefix(Symbol prefix) {
    NavigableMap<String, ExprType> table = tableByNamespace.get(prefix.getNamespace());
    if (table != null) {
      return table.subMap(prefix.getName(), prefix.getName() + Character.MAX_VALUE);
    }
    return emptyMap();
  }

  /**
   * Look up all top level symbols in the namespace.
   * this function is mainly used by SELECT * use case to get the top level fields
   * Todo. currently, the top level fields is the field which doesn't include "." in the name or
   * the prefix doesn't exist in the symbol table.
   * e.g. The symbol table includes person, person.name, person/2.0.
   * person, is the top level field
   * person.name, isn't the top level field, because the prefix (person) in symbol table
   * person/2.0, is the top level field, because the prefix (person/2) isn't in symbol table
   *
   * @param namespace     a namespace
   * @return              all symbols in the namespace map
   */
  public Map<String, ExprType> lookupAllFields(Namespace namespace) {
    final Map<String, ExprType> allSymbols =
        tableByNamespace.getOrDefault(namespace, emptyNavigableMap());
    return allSymbols.entrySet().stream().filter(entry -> {
      String symbolName = entry.getKey();
      int lastDot = symbolName.lastIndexOf(".");
      return -1 == lastDot || !allSymbols.containsKey(symbolName.substring(0, lastDot));
    }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  /**
   * Check if namespace map in empty (none definition).
   *
   * @param namespace a namespace
   * @return true for empty
   */
  public boolean isEmpty(Namespace namespace) {
    return tableByNamespace.getOrDefault(namespace, emptyNavigableMap()).isEmpty();
  }
}
