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

package org.openes.sql.integtest;

import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.test.ESIntegTestCase;
import org.junit.Test;

public class SampleIT extends ESIntegTestCase {

    @Test
    public void sample_test_case() {
        AdminClient adminClient = this.admin();

        ActionFuture<ClusterHealthResponse> healthFuture = adminClient.cluster().health(new ClusterHealthRequest());
        ClusterHealthResponse healthResponse = healthFuture.actionGet();

        System.out.println("Cluster name: " + healthResponse.getClusterName());
    }
}
