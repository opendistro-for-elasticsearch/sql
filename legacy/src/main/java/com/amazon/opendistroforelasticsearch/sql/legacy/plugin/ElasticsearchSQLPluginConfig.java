/*
 *
 *    Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License").
 *    You may not use this file except in compliance with the License.
 *    A copy of the License is located at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    or in the "license" file accompanying this file. This file is distributed
 *    on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *    express or implied. See the License for the specific language governing
 *    permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.sql.legacy.plugin;

import com.amazon.opendistroforelasticsearch.sql.common.setting.Settings;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.client.ElasticsearchClient;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.client.ElasticsearchNodeClient;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.executor.ElasticsearchExecutionEngine;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.executor.protector.ElasticsearchExecutionProtector;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.executor.protector.ExecutionProtector;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.monitor.ElasticsearchMemoryHealthy;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.monitor.ElasticsearchResourceMonitor;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.ElasticsearchStorageEngine;
import com.amazon.opendistroforelasticsearch.sql.executor.ExecutionEngine;
import com.amazon.opendistroforelasticsearch.sql.monitor.ResourceMonitor;
import com.amazon.opendistroforelasticsearch.sql.storage.StorageEngine;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.cluster.service.ClusterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

/**
 * Elasticsearch Plugin Config for SQL.
 */
public class ElasticsearchSQLPluginConfig {
  @Autowired
  private ClusterService clusterService;

  @Autowired
  private NodeClient nodeClient;

  @Autowired
  private Settings settings;

  @Bean
  public ElasticsearchClient client() {
    return new ElasticsearchNodeClient(clusterService, nodeClient);
  }

  @Bean
  public StorageEngine storageEngine() {
    return new ElasticsearchStorageEngine(client(), settings);
  }

  @Bean
  public ExecutionEngine executionEngine() {
    return new ElasticsearchExecutionEngine(client(), protector());
  }

  @Bean
  public ResourceMonitor resourceMonitor() {
    return new ElasticsearchResourceMonitor(settings, new ElasticsearchMemoryHealthy());
  }

  @Bean
  public ExecutionProtector protector() {
    return new ElasticsearchExecutionProtector(resourceMonitor());
  }
}
