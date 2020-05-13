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

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprType;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.client.ElasticsearchClient;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.mapping.IndexMapping;
import com.amazon.opendistroforelasticsearch.sql.storage.Table;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Elasticsearch table (index) implementation.
 */
@RequiredArgsConstructor
public class ElasticsearchIndex implements Table {

    private final ElasticsearchClient client;

    private final String indexName;

    /*
     * TODO: Assume indexName doesn't have wildcard.
     *  Need to either handle field name conflicts
     *   or lazy evaluate when query engine pulls field type.
     */
    @Override
    public Map<String, ExprType> getFieldTypes() {
        Map<String, ExprType> fieldTypes = new HashMap<>();
        Map<String, IndexMapping> indexMappings = client.getIndexMappings(indexName);
        for (IndexMapping indexMapping : indexMappings.values()) {
            fieldTypes.putAll(indexMapping.getAllFieldTypes(this::transform));
        }
        return fieldTypes;
    }

    private ExprType transform(Object fieldMapping) {
        return ExprType.STRING;
    }
}
