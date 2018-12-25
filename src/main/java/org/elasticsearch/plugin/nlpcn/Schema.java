package org.elasticsearch.plugin.nlpcn;

import org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.Strings;
import org.elasticsearch.search.aggregations.Aggregations;
import org.nlpcn.es4sql.domain.Field;
import org.nlpcn.es4sql.domain.IndexStatement;
import org.nlpcn.es4sql.domain.MethodField;
import org.nlpcn.es4sql.domain.Query;
import org.nlpcn.es4sql.domain.Select;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsResponse.FieldMappingMetaData;

public class Schema implements Iterable<Schema.Column> {

    private Client client;
    private Query query;
    private Object queryResult;
    private boolean selectAll;

    private String indexName;
    private String typeName;
    private List<Column> columns;

    public Schema(Client client, Query query, Object queryResult) {
        this.client = client;
        this.query = query;
        this.queryResult = queryResult;
        this.selectAll = false;

        loadFromEsState();
    }

    public Schema(IndexStatement statement, List<Column> columns) {
        this.indexName = statement.getIndexName();
        this.typeName = statement.getTypeName();
        this.columns = columns;
    }

    public String getIndexName() { return indexName; }

    public String getTypeName() { return typeName; }

    public List<String> getHeaders() {
        return columns.stream()
                .map(column -> column.getName())
                .collect(Collectors.toList());
    }

    /**
     * Makes a request to local node to receive meta data information and maps each field specified in SELECT to its
     * type in the index mapping
     */
    private void loadFromEsState() {
        String indexName = fetchIndexName();
        String typeName = fetchTypeName();
        String[] fieldNames = fetchFieldsAsArray();

        if (fieldNames.length == 0)
            selectAll = true;

        GetFieldMappingsRequest request = new GetFieldMappingsRequest()
                .indices(indexName)
                .types(emptyArrayIfNull(typeName))
                .fields(selectAllFieldsIfEmpty(fieldNames))
                .local(true);
        GetFieldMappingsResponse response = client.admin().indices()
                .getFieldMappings(request)
                .actionGet();
        Map<String, Map<String, FieldMappingMetaData>> indexMappings = response.mappings().get(indexName);

        // If the incorrect typeName was given, indexMappings will be null so Schema's columns will not be populated
        if (indexMappings == null) {
            this.indexName = indexName;
            this.columns = new ArrayList<>();
            return;
        }

        /*
         * There are three cases regarding type name to consider:
         * 1. If the correct type name was given, its typeMapping is retrieved
         * 2. If the incorrect type name was given then the response is null
         * 3. If no type name is given, the indexMapping is searched for a typeMapping
         */
        Map<String, FieldMappingMetaData> typeMappings = new HashMap<>();
        if (indexMappings.containsKey(typeName)) {
            typeMappings = indexMappings.get(typeName);
        } else {
            // Assuming ES version 6.x, there can be only one type per index so this for loop should grab the only type
            for (String type : indexMappings.keySet()) {
                typeMappings = indexMappings.get(type);
            }
        }

        this.indexName = indexName;
        this.typeName = typeName;
        this.columns = populateColumns(fieldNames, typeMappings);
    }

    private boolean isSelectAll() { return selectAll; }

    private boolean containsWildcard() {
        for (Field field : fetchFields()) {
            if (!(field instanceof MethodField) && field.getName().contains("*"))
                return true;
        }

        return false;
    }

    private String fetchIndexName() { return query.getFrom().get(0).getIndex(); }

    private String fetchTypeName() { return query.getFrom().get(0).getType(); }

    /**
     * queryResult is checked to see if it's of type Aggregation in which case the aggregation fields in GROUP BY
     * are returned as well. This prevents returning a Schema of all fields when SELECT * is called with
     * GROUP BY (since all fields will be retrieved from the typeMappings request when no fields are returned from
     * fetchFields()).
     *
     * After getting all of the fields from GROUP BY, the fields from SELECT are iterated and only the fields of type
     * MethodField are added (to prevent duplicate field in Schema for queries like
     * "SELECT age, COUNT(*) FROM bank GROUP BY age" where 'age' is mentioned in both SELECT and GROUP BY).
     */
    private List<Field> fetchFields() {
        Select select = (Select) query;
        List<Field> fields;
        if (queryResult instanceof Aggregations) {
            fields = select.getGroupBys().isEmpty() ? new ArrayList<>() : select.getGroupBys().get(0);
            for (Field field : select.getFields()) {
                if (field instanceof MethodField) {
                    fields.add(field);
                }
            }
        } else {
            fields = select.getFields();
        }

        return fields;
    }

    private String[] fetchFieldsAsArray() {
        List<Field> fields = fetchFields();
        return fields.stream()
                .map(this::getFieldName)
                .toArray(String[]::new);
    }

    private String getFieldName(Field field) {
        if (field instanceof MethodField)
            return field.getAlias();

        return field.getName();
    }

    private Map<String, Field> fetchFieldMap() {
        Map<String, Field> fieldMap = new HashMap<>();

        for (Field field : fetchFields()) {
            fieldMap.put(getFieldName(field), field);
        }

        return fieldMap;
    }

    private String[] selectAllFieldsIfEmpty(String[] fields) {
        if (isSelectAll())
            return new String[] {"*"};

        return fields;
    }

    private String[] emptyArrayIfNull(String typeName) {
        if (typeName != null)
            return new String[] {typeName};
        else
            return Strings.EMPTY_ARRAY;
    }

    private Type fetchMethodReturnType(Field field) {
        switch (field.getName().toLowerCase()) {
            case "count":
            case "sum":
            case "avg":
            case "min":
            case "max":
                return Type.DOUBLE;
            default:
                throw new UnsupportedOperationException(
                        String.format("The following method is not supported in Schema: %s", field.getName()));
        }
    }

    /**
     * Returns a list of Column objects which contain names identifying the field as well as its type.
     *
     * If all fields are being selected (SELECT *) then the order of fields returned will be random, otherwise
     * the output will be in the same order as how they were selected.
     *
     * If an alias was given for a field, that will be used to identify the field in Column, otherwise the field name
     * will be used.
     */
    private List<Column> populateColumns(String[] fieldNames, Map<String, FieldMappingMetaData> typeMappings) {
        List<String> fields;

        if (isSelectAll() || containsWildcard()) {
            fields = new ArrayList<>(typeMappings.keySet());
        } else {
            fields = Arrays.asList(fieldNames);
        }

        /*
         * The reason the 'fieldMap' mapping is needed on top of 'fields' is because the map would be empty in cases
         * like 'SELECT *' but List<String> fields will always be set in either case. That way, 'fields' is used to
         * access field names in order that they were selected, if given, and then 'fieldMap' is used to access the
         * respective Field object to check for aliases.
         */
        Map<String, Field> fieldMap = fetchFieldMap();
        List<Column> columns = new ArrayList<>();
        for (String field : fields) {
            // _score is a special case since it is not included in typeMappings, so it is checked for here
            if (field.equals("_score")) {
                columns.add(new Column(field, fetchAlias(field, fieldMap), Type.FLOAT));
            }
            /*
             * Methods are also a special case as their type cannot be determined from typeMappings, so it is checked
             * for here.
             *
             * Note: When adding the Column for Method, alias is used in place of getName() because the default name
             * is set as alias (ex. COUNT(*)) and overwritten if an alias is given. So alias is used as the
             * name instead.
             */
            if (fieldMap.get(field) instanceof MethodField) {
                Field methodField = fieldMap.get(field);
                columns.add(
                        new Column(
                                methodField.getAlias(),
                                null,
                                fetchMethodReturnType(methodField)
                        )
                );
            }

            /*
             * Unnecessary fields (ex. _index, _parent) are ignored.
             * Fields like field.keyword will be ignored when isSelectAll is true but will be returned if
             * explicitly selected.
             */
            if (typeMappings.containsKey(field) && !field.startsWith("_")) {
                if (!isSelectAll() || !field.endsWith(".keyword")) {
                    FieldMappingMetaData metaData = typeMappings.get(field);

                    // Ignore nested fields during SELECT *, expectation is that user will SELECT them specifically
                    // TODO isPropertyType() logic should be changed to check for nested more effectively
                    if (isSelectAll() && isPropertyType(field)) { continue; }

                    String type = getTypeFromMetaData(field, metaData);

                    columns.add(
                            new Column(
                                    field,
                                    fetchAlias(field, fieldMap),
                                    Type.valueOf(type.toUpperCase())
                            )
                    );
                }
            }
        }

        return columns;
    }

    /**
     * Since this helper method is called within a check to see if the field exists in type mapping, it's
     * already confirmed that the fieldName is valid. The check for fieldName in fieldMap has to be done in the case
     * that 'SELECT *' was called since the map will be empty.
     */
    private String fetchAlias(String fieldName, Map<String, Field> fieldMap) {
        if (fieldMap.containsKey(fieldName))
            return fieldMap.get(fieldName).getAlias();

        return null;
    }

    // TODO change this method, mostly for testing and not an effective way for checking if a field is nested
    // Unfortunately the typeMapping when returning all fields returns the full path so the outer field can't be
    // retrieved to check type for "nested"
    private boolean isPropertyType(String fieldName) {
        int lastDot = fieldName.lastIndexOf(".");

        return lastDot > -1 && !fieldName.substring(lastDot + 1).equals("keyword");
    }

    /** Used to retrieve the type of fields from metaData map structures for both regular and nested fields */
    @SuppressWarnings("unchecked")
    private String getTypeFromMetaData(String fieldName, FieldMappingMetaData metaData) {
        Map<String, Object> source = metaData.sourceAsMap();
        String[] fieldPath = fieldName.split("\\.");

        /*
         * When field is not nested the metaData source is fieldName -> type
         * When it is nested or contains "." in general (ex. fieldName.nestedName) the source is nestedName -> type
         */
        Map<String, Object> fieldMapping;
        if (fieldPath.length < 2) {
            fieldMapping = (Map<String, Object>) source.get(fieldName);
        } else {
            fieldMapping = (Map<String, Object>) source.get(fieldPath[1]);
        }

        for (int i = 2; i < fieldPath.length; i++) {
            fieldMapping = (Map<String, Object>) fieldMapping.get(fieldPath[i]);
        }

        return (String) fieldMapping.get("type");
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
