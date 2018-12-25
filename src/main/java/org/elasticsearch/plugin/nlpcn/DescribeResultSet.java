package org.elasticsearch.plugin.nlpcn;

import org.elasticsearch.action.admin.indices.get.GetIndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.plugin.nlpcn.DataRows.Row;
import org.elasticsearch.plugin.nlpcn.Schema.Column;
import org.elasticsearch.plugin.nlpcn.Schema.Type;
import org.nlpcn.es4sql.domain.IndexStatement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class DescribeResultSet extends ResultSet {

    private static final int DEFAULT_NUM_PREC_RADIX = 10;
    private static final String IS_AUTOINCREMENT = "NO";

    private Object queryResult;

    public DescribeResultSet(Client client, IndexStatement statement, Object queryResult) {
        this.client = client;
        this.clusterName = getClusterName();
        this.queryResult = queryResult;

        this.schema = new Schema(statement, loadColumns());
        this.dataRows = new DataRows(loadRows());
    }

    private List<Column> loadColumns() {
        List<Column> columns = new ArrayList<>();
        columns.add(new Column("TABLE_CAT", null, Type.KEYWORD));
        columns.add(new Column("TABLE_SCHEM", null, Type.KEYWORD));
        columns.add(new Column("TABLE_NAME", null, Type.KEYWORD));
        columns.add(new Column("COLUMN_NAME", null, Type.KEYWORD));
        columns.add(new Column("DATA_TYPE", null, Type.INTEGER));
        columns.add(new Column("TYPE_NAME", null, Type.KEYWORD));
        columns.add(new Column("COLUMN_SIZE", null, Type.INTEGER));
        columns.add(new Column("BUFFER_LENGTH", null, Type.INTEGER)); // Not used
        columns.add(new Column("DECIMAL_DIGITS", null, Type.INTEGER));
        columns.add(new Column("NUM_PREC_RADIX", null, Type.INTEGER));
        columns.add(new Column("NULLABLE", null, Type.INTEGER));
        columns.add(new Column("REMARKS", null, Type.KEYWORD));
        columns.add(new Column("COLUMN_DEF", null, Type.KEYWORD));
        columns.add(new Column("SQL_DATA_TYPE", null, Type.INTEGER)); // Not used
        columns.add(new Column("SQL_DATETIME_SUB", null, Type.INTEGER)); // Not used
        columns.add(new Column("CHAR_OCTET_LENGTH", null, Type.INTEGER));
        columns.add(new Column("ORDINAL_POSITION", null, Type.INTEGER));
        columns.add(new Column("IS_NULLABLE", null, Type.KEYWORD));
        columns.add(new Column("SCOPE_CATALOG", null, Type.KEYWORD)); // Not used
        columns.add(new Column("SCOPE_SCHEMA", null, Type.KEYWORD)); // Not used
        columns.add(new Column("SCOPE_TABLE", null, Type.KEYWORD)); // Not used
        columns.add(new Column("SOURCE_DATA_TYPE", null, Type.SHORT)); // Not used
        columns.add(new Column("IS_AUTOINCREMENT", null, Type.KEYWORD));
        columns.add(new Column("IS_GENERATEDCOLUMN", null, Type.KEYWORD));

        return columns;
    }

    private List<Row> loadRows() {
        List<Row> rows = new ArrayList<>();
        GetIndexResponse indexResponse = (GetIndexResponse) queryResult;
        ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> indexMappings = indexResponse.getMappings();

        // Iterate through indices in indexMappings
        for (Iterator<String> iter = indexMappings.keysIt(); iter.hasNext(); ) {
            String index = iter.next();
            ImmutableOpenMap<String, MappingMetaData> typeMapping = indexMappings.get(index);
            // Assuming ES 6.x, iterate through the only type of the index to get mapping data
            for (Iterator<String> typeIter = typeMapping.keysIt(); typeIter.hasNext(); ) {
                String type = typeIter.next();
                MappingMetaData mappingMetaData = typeMapping.get(type);
                // Load rows for each field in the mapping
                rows.addAll(loadIndexData(index, mappingMetaData.getSourceAsMap()));
            }
        }
        return rows;
    }

    @SuppressWarnings("unchecked")
    private List<Row> loadIndexData(String index, Map<String, Object> mappingMetaData) {
        List<Row> rows = new ArrayList<>();

        Map<String, String> flattenedMetaData = flattenMappingMetaData(mappingMetaData, "", new HashMap<>());
        int position = 1; // Used as an arbitrary ORDINAL_POSITION value for the time being
        for (Entry<String, String> entry : flattenedMetaData.entrySet()) {
            rows.add(
                    new Row(
                            loadRowData(index, entry.getKey(), entry.getValue(), position)
                    )
            );
            position++;
        }

        return rows;
    }

    private Map<String, Object> loadRowData(String index, String column, String type, int position) {
        Map<String, Object> data = new HashMap<>();
        data.put("TABLE_CAT", clusterName);
        data.put("TABLE_NAME", index);
        data.put("COLUMN_NAME", column);
        data.put("TYPE_NAME", type);
        data.put("NUM_PREC_RADIX", DEFAULT_NUM_PREC_RADIX);
        data.put("NULLABLE", 2); // TODO Defaulting to 2, need to find a way to check this
        data.put("ORDINAL_POSITION", position); // There is no deterministic position of column in table
        data.put("IS_NULLABLE", ""); // TODO Defaulting to unknown, need to check this
        data.put("IS_AUTOINCREMENT", IS_AUTOINCREMENT); // Defaulting to "NO"
        data.put("IS_GENERATEDCOLUMN", ""); // TODO Defaulting to unknown, need to check

        return data;
    }

    /**
     * To not disrupt old logic for the time being, ShowQueryAction and DescribeQueryAction are using the same
     * 'GetIndexRequestBuilder' that was used in the old ShowQueryAction. Since the format of the resulting meta data
     * is different, this method is being used to flatten and retrieve types.
     *
     * In the future, should look for a way to generalize this since Schema is currently using FieldMappingMetaData
     * whereas here we are using MappingMetaData.
     *
     * // TODO Try to merge mapping request and type retrieval logic when Schema and DataRows is refactored ^
     */
    @SuppressWarnings("unchecked")
    private Map<String, String> flattenMappingMetaData(Map<String, Object> mappingMetaData,
                                                       String currPath,
                                                       Map<String, String> flattenedMapping) {
        Map<String, Object> properties = (Map<String, Object>) mappingMetaData.get("properties");
        for (Entry<String, Object> entry : properties.entrySet()) {
            Map<String, Object> metaData = (Map<String, Object>) entry.getValue();

            String fullPath = addToPath(currPath, entry.getKey());
            flattenedMapping.put(fullPath, (String) metaData.get("type"));
            if (metaData.containsKey("properties")) {
                flattenedMapping = flattenMappingMetaData(metaData, fullPath, flattenedMapping);
            }
        }

        return flattenedMapping;
    }

    private String addToPath(String currPath, String field) {
        if (currPath.isEmpty())
            return field;

        return currPath + "." + field;
    }
}
