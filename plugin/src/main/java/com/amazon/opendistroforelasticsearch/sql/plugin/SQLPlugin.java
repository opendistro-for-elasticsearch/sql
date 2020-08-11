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

package com.amazon.opendistroforelasticsearch.sql.plugin;

import com.amazon.opendistroforelasticsearch.sql.elasticsearch.setting.ElasticsearchSettings;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.script.ExpressionScriptEngine;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.serialization.DefaultExpressionSerializer;
import com.amazon.opendistroforelasticsearch.sql.legacy.esdomain.LocalClusterState;
import com.amazon.opendistroforelasticsearch.sql.legacy.executor.AsyncRestExecutor;
import com.amazon.opendistroforelasticsearch.sql.legacy.metrics.Metrics;
import com.amazon.opendistroforelasticsearch.sql.legacy.plugin.RestSqlAction;
import com.amazon.opendistroforelasticsearch.sql.legacy.plugin.RestSqlSettingsAction;
import com.amazon.opendistroforelasticsearch.sql.legacy.plugin.RestSqlStatsAction;
import com.amazon.opendistroforelasticsearch.sql.legacy.plugin.SqlSettings;
import com.amazon.opendistroforelasticsearch.sql.plugin.rest.RestPPLQueryAction;
import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.io.stream.NamedWriteableRegistry;
import org.elasticsearch.common.settings.ClusterSettings;
import org.elasticsearch.common.settings.IndexScopedSettings;
import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.SettingsFilter;
import org.elasticsearch.common.util.concurrent.EsExecutors;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.env.Environment;
import org.elasticsearch.env.NodeEnvironment;
import org.elasticsearch.plugins.ActionPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.plugins.ScriptPlugin;
import org.elasticsearch.repositories.RepositoriesService;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestHandler;
import org.elasticsearch.script.ScriptContext;
import org.elasticsearch.script.ScriptEngine;
import org.elasticsearch.script.ScriptService;
import org.elasticsearch.threadpool.ExecutorBuilder;
import org.elasticsearch.threadpool.FixedExecutorBuilder;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.watcher.ResourceWatcherService;

public class SQLPlugin extends Plugin implements ActionPlugin, ScriptPlugin {

  /**
   * Sql plugin specific settings in ES cluster settings.
   */
  private final SqlSettings sqlSettings = new SqlSettings();

  private ClusterService clusterService;

  /**
   * Settings should be inited when bootstrap the plugin.
   */
  private com.amazon.opendistroforelasticsearch.sql.common.setting.Settings pluginSettings;

  public String name() {
    return "sql";
  }

  public String description() {
    return "Use sql to query elasticsearch.";
  }

  @Override
  public List<RestHandler> getRestHandlers(Settings settings, RestController restController,
                                           ClusterSettings clusterSettings,
                                           IndexScopedSettings indexScopedSettings,
                                           SettingsFilter settingsFilter,
                                           IndexNameExpressionResolver indexNameExpressionResolver,
                                           Supplier<DiscoveryNodes> nodesInCluster) {
    Objects.requireNonNull(clusterService, "Cluster service is required");
    Objects.requireNonNull(pluginSettings, "Cluster settings is required");

    LocalClusterState.state().setResolver(indexNameExpressionResolver);
    Metrics.getInstance().registerDefaultMetrics();

    return Arrays.asList(
        new RestPPLQueryAction(restController, clusterService, pluginSettings),
        new RestSqlAction(settings, clusterService),
        new RestSqlStatsAction(settings, restController),
        new RestSqlSettingsAction(settings, restController)
    );
  }

  @Override
  public Collection<Object> createComponents(Client client, ClusterService clusterService,
                                             ThreadPool threadPool,
                                             ResourceWatcherService resourceWatcherService,
                                             ScriptService scriptService,
                                             NamedXContentRegistry contentRegistry,
                                             Environment environment,
                                             NodeEnvironment nodeEnvironment,
                                             NamedWriteableRegistry namedWriteableRegistry,
                                             IndexNameExpressionResolver indexNameResolver,
                                             Supplier<RepositoriesService>
                                                       repositoriesServiceSupplier) {
    this.clusterService = clusterService;
    this.pluginSettings = new ElasticsearchSettings(clusterService.getClusterSettings());

    LocalClusterState.state().setClusterService(clusterService);
    LocalClusterState.state().setSqlSettings(sqlSettings);

    return super
        .createComponents(client, clusterService, threadPool, resourceWatcherService, scriptService,
            contentRegistry, environment, nodeEnvironment, namedWriteableRegistry,
            indexNameResolver, repositoriesServiceSupplier);
  }

  @Override
  public List<ExecutorBuilder<?>> getExecutorBuilders(Settings settings) {
    return Collections.singletonList(
        new FixedExecutorBuilder(
            settings,
            AsyncRestExecutor.SQL_WORKER_THREAD_POOL_NAME,
            EsExecutors.allocatedProcessors(settings),
            1000,
            null
        )
    );
  }

  @Override
  public List<Setting<?>> getSettings() {
    ImmutableList<Setting<?>> settings =
        new ImmutableList.Builder<Setting<?>>().addAll(sqlSettings.getSettings())
            .addAll(ElasticsearchSettings.pluginSettings()).build();
    return settings;
  }

  @Override
  public ScriptEngine getScriptEngine(Settings settings, Collection<ScriptContext<?>> contexts) {
    return new ExpressionScriptEngine(new DefaultExpressionSerializer());
  }

}
