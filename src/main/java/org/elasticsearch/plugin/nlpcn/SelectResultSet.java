package org.elasticsearch.plugin.nlpcn;

import org.elasticsearch.client.Client;
import org.nlpcn.es4sql.domain.Query;

public class SelectResultSet extends ResultSet {

    public SelectResultSet(Client client, Query query, Object queryResult) {
        this.schema = new Schema(client, query, queryResult);
        this.dataRows = new DataRows(queryResult, schema.getHeaders());
    }
}
