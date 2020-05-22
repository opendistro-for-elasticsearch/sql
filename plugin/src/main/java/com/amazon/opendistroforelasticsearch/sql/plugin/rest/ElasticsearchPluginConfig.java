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

package com.amazon.opendistroforelasticsearch.sql.plugin.rest;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.client.ElasticsearchClient;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.client.ElasticsearchNodeClient;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.executor.ElasticsearchExecutionEngine;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.ElasticsearchStorageEngine;
import com.amazon.opendistroforelasticsearch.sql.executor.ExecutionEngine;
import com.amazon.opendistroforelasticsearch.sql.storage.StorageEngine;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.cluster.service.ClusterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ElasticsearchPluginConfig {

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private NodeClient nodeClient;

    @Bean
    public ElasticsearchClient client() {
        return new ElasticsearchNodeClient(clusterService, nodeClient);
    }

    @Bean
    public StorageEngine storageEngine() {
        return new ElasticsearchStorageEngine(client());
    }

    @Bean
    public ExecutionEngine<List<ExprValue>> executionEngine() {
        return new ElasticsearchExecutionEngine(client());
    }

}
