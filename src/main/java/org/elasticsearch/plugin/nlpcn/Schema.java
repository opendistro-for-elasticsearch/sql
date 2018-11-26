package org.elasticsearch.plugin.nlpcn;

import org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.Strings;
import org.nlpcn.es4sql.domain.Field;
import org.nlpcn.es4sql.domain.Query;
import org.nlpcn.es4sql.domain.Select;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsResponse.FieldMappingMetaData;

public class Schema implements Iterable<Schema.Column> {

    private Client client;
    private Query query;
    private boolean selectAll;

    private String indexName;
    private String typeName;
    private List<Column> columns;

    public Schema(Client client, Query query) {
        this.client = client;
        this.query = query;
        this.selectAll = false;

        loadFromEsState();
    }

    public String getIndexName() { return indexName; }

    public String getTypeName() { return typeName; }

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
        GetFieldMappingsResponse response = client.admin().indices().getFieldMappings(request).actionGet();
        Map<String, Map<String, FieldMappingMetaData>> indexMappings = response.mappings().get(indexName);

        // If the incorrect typeName was given, indexMappings will be null so Schema's columns will not be populated
        if (indexMappings == null) {
            this.indexName = indexName;
            this.columns = new ArrayList<>();
            return;
        }

        /*
         * There are three cases regarding type name to consider:
         * 1. If the incorrect type name was given then the response is null
         * 2. If a correct type name was given, its typeMapping is retrieved
         * 3. If no type name is given, the indexMapping is searched for a typeMapping
         */
        Map<String, FieldMappingMetaData> typeMappings = new HashMap<>();
        // TODO try making this if check "if typeName == null" if checking null key values is bad practice
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

    private String fetchIndexName() { return query.getFrom().get(0).getIndex(); }

    private String fetchTypeName() { return query.getFrom().get(0).getType(); }

    private List<Field> fetchFields() { return ((Select) query).getFields(); }

    private String[] fetchFieldsAsArray() {
        List<Field> fields = fetchFields();

        return fields.stream()
                .map(field -> field.getName())
                .toArray(String[]::new);
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

    private List<Column> populateColumns(String[] fieldNames, Map<String, FieldMappingMetaData> typeMappings) {
        List<String> fields;

        if (isSelectAll()) {
            fields = new ArrayList<>(typeMappings.keySet());
        } else {
            fields = Arrays.asList(fieldNames);
        }

        List<Column> columns = new ArrayList<>();
        for (String field : fields) {
            // Ignore unnecessary fields (ex. _index, _parent)
            // TODO map alias -> type if alias is given (Can be found in Field object)
            // TODO need to ignore things like .keyword when isSelectAll but grab when explicitly SELECT'd
            if (!field.startsWith("_") && !field.contains(".")) {
                FieldMappingMetaData metaData = typeMappings.get(field);
                String type = getTypeFromMetaData(field, metaData);

                columns.add(new Column(field, Type.valueOf(type.toUpperCase())));
            }
        }

        return columns;
    }

    private String getTypeFromMetaData(String fieldName, FieldMappingMetaData metaData) {
        Map<String, Object> source = metaData.sourceAsMap();
        Map<String, Object> fieldMapping = (Map<String, Object>) source.get(fieldName);

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
    private enum Type {
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
    public class Column {

        private String name;
        private Type type;

        public Column(String name, Type type) {
            this.name = name;
            this.type = type;
        }

        public String getName() { return name; }

        public String getType() { return type.nameLowerCase(); }
    }
}
