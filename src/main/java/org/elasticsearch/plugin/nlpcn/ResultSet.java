package org.elasticsearch.plugin.nlpcn;

import org.elasticsearch.client.Client;
import org.nlpcn.es4sql.domain.Query;

public class ResultSet {

    private Client client;
    private Schema schema;
    private DataRows dataRows;

    public ResultSet(Client client, Query query, Object queryResult) {
        this.client = client;
        this.schema = new Schema(client, query);
        this.dataRows = new DataRows(queryResult);
    }

    public Schema getSchema() { return schema; }

    public DataRows getDataRows() { return dataRows; }
}
