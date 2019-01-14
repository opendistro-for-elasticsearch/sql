package org.elasticsearch.plugin.nlpcn;

import org.elasticsearch.client.Client;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    protected boolean matchesPattern(String string, String pattern) {
        Pattern p = Pattern.compile(pattern);
        Matcher matcher = p.matcher(string);
        return matcher.find();
    }
}
