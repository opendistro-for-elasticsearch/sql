package org.elasticsearch.plugin.nlpcn;

import org.nlpcn.es4sql.domain.IndexStatement;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class Schema implements Iterable<Schema.Column> {

    private String indexName;
    private String typeName;
    private List<Column> columns;

    public Schema(String indexName, String typeName, List<Column> columns) {
        this.indexName = indexName;
        this.typeName = typeName;
        this.columns = columns;
    }

    public Schema(IndexStatement statement, List<Column> columns) {
        this.indexName = statement.getIndexPattern();
        this.columns = columns;
    }

    public String getIndexName() { return indexName; }

    public String getTypeName() { return typeName; }

    public List<String> getHeaders() {
        return columns.stream()
                .map(column -> column.getName())
                .collect(Collectors.toList());
    }

    // Iterator method for Schema
    @Override
    public Iterator<Column> iterator() {
        return new Iterator<Column>() {
            private final Iterator<Column> iter = columns.iterator();

            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }

            @Override
            public Column next() {
                return iter.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("No changes allowed to Schema columns");
            }
        };
    }

    // Only core ES datatypes currently supported
    public enum Type {
        TEXT, KEYWORD, // String types
        LONG, INTEGER, SHORT, BYTE, DOUBLE, FLOAT, HALF_FLOAT, SCALED_FLOAT, // Numeric types
        DATE, // Date types
        BOOLEAN, // Boolean types
        BINARY, // Binary types
        INTEGER_RANGE, FLOAT_RANGE, LONG_RANGE, DOUBLE_RANGE, DATE_RANGE; // Range types

        public String nameLowerCase() {
            return name().toLowerCase();
        }
    }

    // Inner class for Column object
    public static class Column {

        private final String name;
        private String alias;
        private final Type type;

        public Column(String name, String alias, Type type) {
            this.name = name;
            this.alias = alias;
            this.type = type;
        }

        public String getName() { return name; }

        public String getAlias() { return alias; }

        public String getType() { return type.nameLowerCase(); }
    }
}
