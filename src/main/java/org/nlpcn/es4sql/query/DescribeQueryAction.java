package org.nlpcn.es4sql.query;

import org.elasticsearch.action.admin.indices.get.GetIndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.nlpcn.es4sql.domain.IndexStatement;
import org.nlpcn.es4sql.domain.QueryStatement;

import static org.nlpcn.es4sql.Util.prepareIndexRequestBuilder;

public class DescribeQueryAction extends QueryAction {

    private final IndexStatement statement;

    public DescribeQueryAction(Client client, IndexStatement statement) {
        super(client,null);
        this.statement = statement;
    }

    @Override
    public QueryStatement getQueryStatement() { return statement; }

    @Override
    public SqlElasticSearchRequestBuilder explain() {
        /*
         * For the time being the logic for parsing the SQL statement to get the indexRequestBuilder is the same for
         * both SHOW and DESCRIBE statements so the method to obtain it has been moved to Util to be shared.
         *
         * When enhancing the syntax and support for SHOW and DESCRIBE, if the statements need to be parsed differently,
         * then prepareIndexRequestBuilder() can be split or refactored.
         */
        final GetIndexRequestBuilder indexRequestBuilder = prepareIndexRequestBuilder(client, statement);

        return new SqlElasticSearchRequestBuilder(indexRequestBuilder);
    }
}
