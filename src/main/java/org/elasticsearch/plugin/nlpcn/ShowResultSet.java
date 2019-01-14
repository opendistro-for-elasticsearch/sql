package org.elasticsearch.plugin.nlpcn;

import org.elasticsearch.action.admin.indices.get.GetIndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.plugin.nlpcn.DataRows.Row;
import org.elasticsearch.plugin.nlpcn.Schema.Column;
import org.elasticsearch.plugin.nlpcn.Schema.Type;
import org.nlpcn.es4sql.domain.IndexStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ShowResultSet extends ResultSet {

    private static final String TABLE_TYPE = "BASE TABLE";

    private IndexStatement statement;
    private Object queryResult;

    public ShowResultSet(Client client, IndexStatement statement, Object queryResult) {
        this.client = client;
        this.clusterName = getClusterName();
        this.statement = statement;
        this.queryResult = queryResult;

        this.schema = new Schema(statement, loadColumns());
        this.dataRows = new DataRows(loadRows());
    }

    private List<Column> loadColumns() {
        List<Column> columns = new ArrayList<>();
        // Unused Columns are still included in Schema to match JDBC/ODBC standard
        columns.add(new Column("TABLE_CAT", null, Type.KEYWORD));
        columns.add(new Column("TABLE_SCHEM", null, Type.KEYWORD)); // Not used
        columns.add(new Column("TABLE_NAME", null, Type.KEYWORD));
        columns.add(new Column("TABLE_TYPE", null, Type.KEYWORD));
        columns.add(new Column("REMARKS", null, Type.KEYWORD)); // Not used
        columns.add(new Column("TYPE_CAT", null, Type.KEYWORD)); // Not used
        columns.add(new Column("TYPE_SCHEM", null, Type.KEYWORD)); // Not used
        columns.add(new Column("TYPE_NAME", null, Type.KEYWORD)); // Not used
        columns.add(new Column("SELF_REFERENCING_COL_NAME", null, Type.KEYWORD)); // Not used
        columns.add(new Column("REF_GENERATION", null, Type.KEYWORD)); // Not used

        return columns;
    }

    private List<Row> loadRows() {
        List<Row> rows = new ArrayList<>();
        for (String index : extractIndices()) {
            rows.add(new Row(loadData(index)));
        }

        return rows;
    }

    private List<String> extractIndices() {
        String indexPattern = statement.getIndexPattern();
        String[] indices = ((GetIndexResponse) queryResult).getIndices();

        return Arrays.stream(indices)
                .filter(index -> matchesPattern(index, indexPattern))
                .collect(Collectors.toList());
    }

    private Map<String, Object> loadData(String tableName) {
        Map<String, Object> data = new HashMap<>();
        data.put("TABLE_CAT", clusterName);
        data.put("TABLE_NAME", tableName);
        data.put("TABLE_TYPE", TABLE_TYPE);

        return data;
    }
}
