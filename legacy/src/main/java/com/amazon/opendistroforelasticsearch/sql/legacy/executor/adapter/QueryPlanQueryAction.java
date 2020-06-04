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

package com.amazon.opendistroforelasticsearch.sql.legacy.executor.adapter;

import com.amazon.opendistroforelasticsearch.sql.legacy.query.QueryAction;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.SqlElasticRequestBuilder;
import com.google.common.base.Strings;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The definition of QueryPlan of QueryAction which works as the adapter to the current QueryAction framework.
 */
public class QueryPlanQueryAction extends QueryAction {
    private final QueryPlanRequestBuilder requestBuilder;

    public QueryPlanQueryAction(QueryPlanRequestBuilder requestBuilder) {
        super(null, null);
        this.requestBuilder = requestBuilder;
    }

    @Override
    public SqlElasticRequestBuilder explain() {
        return requestBuilder;
    }

    @Override
    public Optional<List<String>> getFieldNames() {
        List<String> fieldNames = ((QueryPlanRequestBuilder) requestBuilder).outputColumns()
                .stream()
                .map(node -> Strings.isNullOrEmpty(node.getAlias()) ? node.getName() : node.getAlias())
                .collect(Collectors.toList());
        return Optional.of(fieldNames);
    }
}
