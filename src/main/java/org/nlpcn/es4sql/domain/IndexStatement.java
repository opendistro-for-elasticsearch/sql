package org.nlpcn.es4sql.domain;

/** Class used to differentiate SHOW and DESCRIBE statements */
public class IndexStatement implements QueryStatement {

    private StatementType statementType;
    private String query;
    private String indexName;
    private String typeName;

    public IndexStatement(StatementType statementType, String query) {
        this.statementType = statementType;
        this.query = query;

        parseIndexNameAndType();
    }

    private void parseIndexNameAndType() {
        String indexName = query.split(" ")[1];
        String type = null;

        // TODO Might remove this check for "<" and ">", see no cases of it in previous test but keeping for now
        if (indexName.startsWith("<")) {
            if (!indexName.endsWith(">")) {
                int index = indexName.lastIndexOf('/');
                if (index > -1) {
                    type = indexName.substring(index + 1);
                    indexName = indexName.substring(0, index);
                }
            }
        } else if (indexName.contains("/")) {
            String[] indexAndType = indexName.split("\\/");
            indexName = indexAndType[0];
            type = indexAndType[1];
        }

        this.indexName = indexName;
        this.typeName = type;
    }

    public StatementType getStatementType() { return statementType; }

    public String getQuery() { return query; }

    public String getIndexName() { return indexName; }

    public String getTypeName() { return typeName; }

    public enum StatementType {
        SHOW, DESCRIBE
    }
}
