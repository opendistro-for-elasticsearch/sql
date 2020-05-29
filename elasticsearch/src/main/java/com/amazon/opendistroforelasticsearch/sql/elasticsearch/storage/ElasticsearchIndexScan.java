/*
 *    Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License").
 *    You may not use this file except in compliance with the License.
 *    A copy of the License is located at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    or in the "license" file accompanying this file. This file is distributed
 *    on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *    express or implied. See the License for the specific language governing
 *    permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.client.ElasticsearchClient;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.request.ElasticsearchRequest;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.response.ElasticsearchResponse;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.script.ExpressionScriptEngine;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalFilter;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlan;
import com.amazon.opendistroforelasticsearch.sql.storage.TableScanOperator;
import com.google.common.collect.Iterables;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.SearchHit;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;

import static java.util.Collections.emptyMap;

/**
 * Elasticsearch index scan operator
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(onlyExplicitlyIncluded = true)
public class ElasticsearchIndexScan extends TableScanOperator {

    /**
     * Elasticsearch client.
     */
    private final ElasticsearchClient client;

    /**
     * Search request.
     */
    @EqualsAndHashCode.Include
    @ToString.Include
    private final ElasticsearchRequest request;

    /**
     * Search response for current batch.
     */
    private Iterator<SearchHit> hits;


    public ElasticsearchIndexScan(ElasticsearchClient client, String indexName) {
        this.client = client;
        this.request = new ElasticsearchRequest(indexName);
    }

    @Override
    public void open() {
        super.open();

        // For now pull all results immediately once open
        List<ElasticsearchResponse> responses = new ArrayList<>();
        ElasticsearchResponse response = client.search(request);
        while (!response.isEmpty()) {
            responses.add(response);
            response = client.search(request);
        }
        hits = Iterables.concat(responses.toArray(new ElasticsearchResponse[0])).iterator();
    }

    @Override
    public boolean hasNext() {
        return hits.hasNext();
    }

    @Override
    public ExprValue next() {
        return ExprValueUtils.fromObjectValue(hits.next().getSourceAsMap());
    }

    @Override
    public void close() {
        super.close();

        client.cleanup(request);
    }

    public PhysicalPlan pushDown(LogicalFilter filter) {
        request.getSourceBuilder().query(
            QueryBuilders.scriptQuery(
                new Script(
                    Script.DEFAULT_SCRIPT_TYPE,
                    ExpressionScriptEngine.EXPRESSION_LANG_NAME,
                    serialize(filter.getCondition()),
                    emptyMap() // TODO
                )
            )
        );
        return this; // Assume everything pushed down and nothing needed for physical filter operator
    }

    private String serialize(Expression expression) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ObjectOutputStream objectOutput = new ObjectOutputStream(output);
            objectOutput.writeObject(expression);
            objectOutput.flush();
            return Base64.getEncoder().encodeToString(output.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to serialize expression: " + expression, e);
        }
    }

}
