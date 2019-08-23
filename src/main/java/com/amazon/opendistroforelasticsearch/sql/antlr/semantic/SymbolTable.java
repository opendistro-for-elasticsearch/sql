package com.amazon.opendistroforelasticsearch.sql.antlr.semantic;

import java.util.HashMap;
import java.util.Map;

/**
 * Symbol table for CSA (context-sensitive analysis)
 * @param <Symbol>  type of symbol
 * @param <Value>   type of information associated with symbol
 */
public class SymbolTable<Symbol, Value> {

    //private final List<SymbolTable<Symbol, Value>> children = new ArrayList<>();

    private final Map<Symbol, Value> valuesBySymbol = new HashMap<>();


    public void define(Symbol symbol, Value value) {
        valuesBySymbol.put(symbol, value);
    }

    public Value resolve(Symbol symbol) {
        return valuesBySymbol.get(symbol); //Ignore multiple levels/scopes
    }
}
