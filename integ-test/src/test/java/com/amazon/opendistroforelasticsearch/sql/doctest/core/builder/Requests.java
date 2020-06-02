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

package com.amazon.opendistroforelasticsearch.sql.doctest.core.builder;

import com.amazon.opendistroforelasticsearch.sql.doctest.core.request.SqlRequest;
import com.amazon.opendistroforelasticsearch.sql.doctest.core.response.SqlResponse;
import org.elasticsearch.client.RestClient;

import java.util.Objects;

/**
 * Query and explain request tuple.
 */
public class Requests {

    private final RestClient client;
    private final SqlRequest query;
    private final SqlRequest explain;

    public Requests(RestClient client, SqlRequest query) {
        this(client, query, SqlRequest.NONE);
    }

    public Requests(RestClient client, SqlRequest query, SqlRequest explain) {
        this.client = client;
        this.query = query;
        this.explain = explain;
    }

    public SqlRequest query() {
        return query;
    }

    public SqlResponse queryResponse() {
        Objects.requireNonNull(query, "Query request is required");
        return query.send(client);
    }

    public SqlRequest explain() {
        return explain;
    }

    public SqlResponse explainResponse() {
        if (explain == SqlRequest.NONE) {
            return SqlResponse.NONE;
        }
        return explain.send(client);
    }
}
