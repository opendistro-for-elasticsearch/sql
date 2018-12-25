package org.elasticsearch.plugin.nlpcn;

import org.elasticsearch.client.Client;

public abstract class ResultSet {

    protected Schema schema;
    protected DataRows dataRows;

    protected Client client;
    protected String clusterName;

    public Schema getSchema() { return schema; }

    public DataRows getDataRows() { return dataRows; }

    protected String getClusterName() {
        return client.admin().cluster()
                .prepareHealth()
                .get()
                .getClusterName();
    }
}
