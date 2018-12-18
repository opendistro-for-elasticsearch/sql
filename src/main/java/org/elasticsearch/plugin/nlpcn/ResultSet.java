package org.elasticsearch.plugin.nlpcn;

import org.elasticsearch.client.Client;
import org.nlpcn.es4sql.domain.Query;

public class ResultSet {

    private Schema schema;
    private DataRows dataRows;

    public ResultSet(Client client, Query query, Object queryResult) {
        this.schema = new Schema(client, query, queryResult);
        this.dataRows = new DataRows(queryResult, schema.getHeaders());
    }

    public Schema getSchema() { return schema; }

    public DataRows getDataRows() { return dataRows; }
}
