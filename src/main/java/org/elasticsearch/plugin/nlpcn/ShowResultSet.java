package org.elasticsearch.plugin.nlpcn;

import org.elasticsearch.action.admin.indices.get.GetIndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.plugin.nlpcn.DataRows.Row;
import org.elasticsearch.plugin.nlpcn.Schema.Column;
import org.elasticsearch.plugin.nlpcn.Schema.Type;
import org.nlpcn.es4sql.domain.IndexStatement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShowResultSet extends ResultSet {

    private static final String TABLE_TYPE = "BASE TABLE";

    private Object queryResult;

    public ShowResultSet(Client client, IndexStatement statement, Object queryResult) {
        this.client = client;
        this.clusterName = getClusterName();
        this.queryResult = queryResult;

        this.schema = new Schema(statement, loadColumns());
        this.dataRows = new DataRows(loadRows());
    }

    private List<Column> loadColumns() {
        List<Column> columns = new ArrayList<>();
        columns.add(new Column("TABLE_CAT", null, Type.KEYWORD));
        columns.add(new Column("TABLE_NAME", null, Type.KEYWORD));
        columns.add(new Column("TABLE_TYPE", null, Type.KEYWORD));

        return columns;
    }

    private List<Row> loadRows() {
        List<Row> rows = new ArrayList<>();
        for (String index : extractIndices()) {
            rows.add(new Row(loadData(index)));
        }

        return rows;
    }

    /**
     * TODO Check if the following is true
     * If index type is not necessary for other format responses (since it isn't used for JDBC format), it might be
     * possible to remove Feature.MAPPINGS for the request used for SHOW since only index names are needed
     */
    private String[] extractIndices() {
        return ((GetIndexResponse) queryResult).getIndices();
    }

    private Map<String, Object> loadData(String tableName) {
        Map<String, Object> data = new HashMap<>();
        data.put("TABLE_CAT", clusterName);
        data.put("TABLE_NAME", tableName);
        data.put("TABLE_TYPE", TABLE_TYPE);

        return data;
    }
}
