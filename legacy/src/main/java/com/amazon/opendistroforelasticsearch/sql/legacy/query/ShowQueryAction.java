/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.legacy.query;

import com.amazon.opendistroforelasticsearch.sql.legacy.domain.IndexStatement;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.QueryStatement;
import org.elasticsearch.action.admin.indices.get.GetIndexRequestBuilder;
import org.elasticsearch.client.Client;

import static com.amazon.opendistroforelasticsearch.sql.legacy.utils.Util.prepareIndexRequestBuilder;

public class ShowQueryAction extends QueryAction {

    private final IndexStatement statement;

    public ShowQueryAction(Client client, IndexStatement statement) {
        super(client, null);
        this.statement = statement;
    }

    @Override
    public QueryStatement getQueryStatement() {
        return statement;
    }

    @Override
    public SqlElasticSearchRequestBuilder explain() {
        final GetIndexRequestBuilder indexRequestBuilder = prepareIndexRequestBuilder(client, statement);

        return new SqlElasticSearchRequestBuilder(indexRequestBuilder);
    }
}