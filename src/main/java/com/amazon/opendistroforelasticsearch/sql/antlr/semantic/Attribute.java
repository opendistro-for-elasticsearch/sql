package com.amazon.opendistroforelasticsearch.sql.antlr.semantic;

import com.amazon.opendistroforelasticsearch.sql.antlr.OpenDistroSqlAnalyzer;

/**
 * Attribute on tree
 */
public class Attribute {
    public static final Attribute EMPTY = new Attribute("");

    private final String type;

    public Attribute(String type) {
        this.type = type;
    }

    // Should be method in ES type class in ES (mapping) domain layer
    public boolean isNumber() {
        return "long".equals(type) || "integer".equals(type);
    }

    public boolean isString() {
        return "text".equals(type) || "keyword".equals(type);
    }

    public boolean isCompatible(Attribute other) {
        return (isNumber() && other.isNumber()) || (isString() && other.isString());
    }

    @Override
    public String toString() {
        return "[" + type + ']';
    }
}
