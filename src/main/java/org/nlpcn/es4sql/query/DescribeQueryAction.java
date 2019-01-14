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
        final GetIndexRequestBuilder indexRequestBuilder = prepareIndexRequestBuilder(client, statement);

        return new SqlElasticSearchRequestBuilder(indexRequestBuilder);
    }
}
