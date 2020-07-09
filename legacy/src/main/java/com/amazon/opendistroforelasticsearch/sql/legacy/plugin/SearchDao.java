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

package com.amazon.opendistroforelasticsearch.sql.legacy.plugin;

import com.amazon.opendistroforelasticsearch.sql.legacy.domain.QueryActionRequest;
import com.amazon.opendistroforelasticsearch.sql.legacy.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.ESActionFactory;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.QueryAction;
import org.elasticsearch.client.Client;

import java.sql.SQLFeatureNotSupportedException;
import java.util.HashSet;
import java.util.Set;


public class SearchDao {

    private static final Set<String> END_TABLE_MAP = new HashSet<>();

    static {
        END_TABLE_MAP.add("limit");
        END_TABLE_MAP.add("order");
        END_TABLE_MAP.add("where");
        END_TABLE_MAP.add("group");

    }

    private Client client = null;

    public SearchDao(Client client) {
        this.client = client;
    }

    public Client getClient() {
        return client;
    }

    /**
     * Prepare action And transform sql
     * into ES ActionRequest
     *
     * @param queryActionRequest SQL query action request to execute.
     * @return ES request
     * @throws SqlParseException
     */
    public QueryAction explain(QueryActionRequest queryActionRequest)
            throws SqlParseException, SQLFeatureNotSupportedException {
        return ESActionFactory.create(client, queryActionRequest);
    }
}
