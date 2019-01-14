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
