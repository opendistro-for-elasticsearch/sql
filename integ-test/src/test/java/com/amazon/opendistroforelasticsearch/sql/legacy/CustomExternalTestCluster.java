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

package com.amazon.opendistroforelasticsearch.sql.legacy;

import static org.elasticsearch.test.ESTestCase.getTestTransportType;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.cluster.node.info.NodeInfo;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.io.stream.NamedWriteableRegistry;
import org.elasticsearch.common.network.NetworkModule;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.env.Environment;
import org.elasticsearch.http.HttpInfo;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.test.InternalTestCluster;
import org.elasticsearch.test.TestCluster;
import org.elasticsearch.transport.MockTransportClient;
import org.elasticsearch.transport.nio.MockNioTransportPlugin;

import static org.elasticsearch.action.admin.cluster.node.info.NodesInfoRequest.Metric.HTTP;
import static org.elasticsearch.action.admin.cluster.node.info.NodesInfoRequest.Metric.SETTINGS;

public class CustomExternalTestCluster extends TestCluster {

  private static final Logger logger = LogManager.getLogger(CustomExternalTestCluster.class);

  private static final AtomicInteger counter = new AtomicInteger();
  public static final String EXTERNAL_CLUSTER_PREFIX = "external_";

  private final MockTransportClient client;

  private final InetSocketAddress[] httpAddresses;

  private final String clusterName;

  private final int numDataNodes;
  private final int numMasterAndDataNodes;

  public CustomExternalTestCluster(Path tempDir, Settings additionalSettings,
                                   Collection<Class<? extends Plugin>> pluginClasses,
                                   TransportAddress... transportAddresses) {
    super(0);
    Settings.Builder clientSettingsBuilder = Settings.builder()
        .put(additionalSettings)
        .put("node.name", InternalTestCluster.TRANSPORT_CLIENT_PREFIX + EXTERNAL_CLUSTER_PREFIX +
            counter.getAndIncrement())
        .put("client.transport.ignore_cluster_name", true)
        .put(Environment.PATH_HOME_SETTING.getKey(), tempDir);
    boolean addMockTcpTransport = additionalSettings.get(NetworkModule.TRANSPORT_TYPE_KEY) == null;

    if (addMockTcpTransport) {
      String transport = getTestTransportType();
      clientSettingsBuilder.put(NetworkModule.TRANSPORT_TYPE_KEY, transport);
      if (pluginClasses.contains(MockNioTransportPlugin.class) == false) {
        pluginClasses = new ArrayList<>(pluginClasses);
        if (transport.equals(MockNioTransportPlugin.MOCK_NIO_TRANSPORT_NAME)) {
          pluginClasses.add(MockNioTransportPlugin.class);
        }
      }
    }
    Settings clientSettings = clientSettingsBuilder.build();
    MockTransportClient client = new MockTransportClient(clientSettings, pluginClasses);
    try {
      client.addTransportAddresses(transportAddresses);
      NodesInfoResponse nodeInfos =
          client.admin().cluster().prepareNodesInfo().clear()
                  .addMetrics(SETTINGS.metricName(), HTTP.metricName())
                  .get();
      httpAddresses = new InetSocketAddress[nodeInfos.getNodes().size()];
      this.clusterName = nodeInfos.getClusterName().value();
      int dataNodes = 0;
      int masterAndDataNodes = 0;
      for (int i = 0; i < nodeInfos.getNodes().size(); i++) {
        NodeInfo nodeInfo = nodeInfos.getNodes().get(i);
        httpAddresses[i] = nodeInfo.getInfo(HttpInfo.class).address().publishAddress().address();
        if (DiscoveryNode.isDataNode(nodeInfo.getSettings())) {
          dataNodes++;
          masterAndDataNodes++;
        } else if (DiscoveryNode.isMasterNode(nodeInfo.getSettings())) {
          masterAndDataNodes++;
        }
      }
      this.numDataNodes = dataNodes;
      this.numMasterAndDataNodes = masterAndDataNodes;
      this.client = client;

      logger.info("Setup ExternalTestCluster [{}] made of [{}] nodes",
          nodeInfos.getClusterName().value(), size());
    } catch (Exception e) {
      client.close();
      throw e;
    }
  }

  @Override
  public void afterTest() {

  }

  @Override
  public Client client() {
    return client;
  }

  @Override
  public int size() {
    return httpAddresses.length;
  }

  @Override
  public int numDataNodes() {
    return numDataNodes;
  }

  @Override
  public int numDataAndMasterNodes() {
    return numMasterAndDataNodes;
  }

  @Override
  public InetSocketAddress[] httpAddresses() {
    return httpAddresses;
  }

  @Override
  public void close() throws IOException {
    client.close();
  }

  /**
   * This custom ExternalCluster class has ensureEstimatedStats() emptied out to prevent the transport client error
   * made from making a request using a closed client after the tests are complete.
   */
  @Override
  public void ensureEstimatedStats() {
  }

  @Override
  public Iterable<Client> getClients() {
    return Collections.singleton(client);
  }

  @Override
  public NamedWriteableRegistry getNamedWriteableRegistry() {
    return client.getNamedWriteableRegistry();
  }

  @Override
  public String getClusterName() {
    return clusterName;
  }
}
