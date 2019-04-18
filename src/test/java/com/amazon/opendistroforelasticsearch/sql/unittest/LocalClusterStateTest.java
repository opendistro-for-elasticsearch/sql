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

package com.amazon.opendistroforelasticsearch.sql.unittest;

import com.amazon.opendistroforelasticsearch.sql.esdomain.LocalClusterState;
import com.amazon.opendistroforelasticsearch.sql.esdomain.LocalClusterState.FieldMappings;
import com.amazon.opendistroforelasticsearch.sql.esdomain.LocalClusterState.IndexMappings;
import com.amazon.opendistroforelasticsearch.sql.esdomain.LocalClusterState.TypeMappings;
import com.amazon.opendistroforelasticsearch.sql.intgtest.TestsConstants;
import com.amazon.opendistroforelasticsearch.sql.plugin.SqlSettings;
import org.elasticsearch.cluster.ClusterChangedEvent;
import org.elasticsearch.cluster.ClusterStateListener;
import org.elasticsearch.cluster.service.ClusterService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.amazon.opendistroforelasticsearch.sql.plugin.SqlSettings.QUERY_SLOWLOG;
import static com.amazon.opendistroforelasticsearch.sql.util.CheckScriptContents.mockClusterService;
import static com.amazon.opendistroforelasticsearch.sql.util.CheckScriptContents.mockLocalClusterState;
import static java.util.Collections.emptyList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Local cluster state testing without covering ES logic, ex. resolve index pattern.
 */
public class LocalClusterStateTest {

    private static final String INDEX_NAME = TestsConstants.TEST_INDEX_BANK;
    private static final String TYPE_NAME = "account";

    private static final String MAPPING = "{\n" +
        "  \"elasticsearch-sql_test_index_bank\": {\n" +
        "    \"mappings\": {\n" +
        "      \"account\": {\n" +
        "        \"properties\": {\n" +
        "          \"address\": {\n" +
        "            \"type\": \"text\"\n" +
        "          },\n" +
        "          \"age\": {\n" +
        "            \"type\": \"integer\"\n" +
        "          },\n" +
        "          \"city\": {\n" +
        "            \"type\": \"keyword\"\n" +
        "          },\n" +
        "          \"employer\": {\n" +
        "            \"type\": \"text\",\n" +
        "            \"fields\": {\n" +
        "              \"keyword\": {\n" +
        "                \"type\": \"keyword\",\n" +
        "                \"ignore_above\": 256\n" +
        "              }\n" +
        "            }\n" +
        "          },\n" +
        "          \"state\": {\n" +
        "            \"type\": \"text\",\n" +
        "            \"fields\": {\n" +
        "              \"raw\": {\n" +
        "                \"type\": \"keyword\",\n" +
        "                \"ignore_above\": 256\n" +
        "              }\n" +
        "            }\n" +
        "          },\n" +
        "          \"manager\": {\n" +
        "            \"properties\": {\n" +
        "              \"name\": {\n" +
        "                \"type\": \"text\",\n" +
        "                \"fields\": {\n" +
        "                  \"keyword\": {\n" +
        "                    \"type\": \"keyword\",\n" +
        "                    \"ignore_above\": 256\n" +
        "                  }\n" +
        "                }\n" +
        "              },\n" +
        "              \"address\": {\n" +
        "                \"type\": \"keyword\"\n" +
        "              }\n" +
        "            }\n" +
        "          }\n" +
        "        }\n" +
        "      }\n" +
        "    },\n" +
        // ==== All required by IndexMetaData.fromXContent() ====
        "    \"settings\": {\n" +
        "      \"index\": {\n" +
        "        \"number_of_shards\": 5,\n" +
        "        \"number_of_replicas\": 0,\n" +
        "        \"version\": {\n" +
        "          \"created\": \"6050399\"\n" +
        "        }\n" +
        "      }\n" +
        "    },\n" +
        "    \"mapping_version\": \"1\",\n" +
        "    \"settings_version\": \"1\"\n" +
        //=======================================================
        "  }\n" +
        "}";

    @Before
    public void init() {
        mockLocalClusterState(MAPPING);
    }

    @After
    public void cleanUp() {
        LocalClusterState.state(null);
    }

    @Test
    public void getMappingForExistingField() {
        IndexMappings indexMappings = LocalClusterState.state().getFieldMappings(new String[]{INDEX_NAME});
        Assert.assertNotNull(indexMappings);

        TypeMappings typeMappings = indexMappings.mapping(INDEX_NAME);
        Assert.assertNotNull(typeMappings);

        FieldMappings fieldMappings = typeMappings.mapping(TYPE_NAME);
        Assert.assertNotNull(fieldMappings);

        Assert.assertEquals("text", fieldMappings.mapping("address").get("type"));
        Assert.assertEquals("integer", fieldMappings.mapping("age").get("type"));
        Assert.assertEquals("keyword", fieldMappings.mapping("city").get("type"));
        Assert.assertEquals("text", fieldMappings.mapping("employer").get("type"));

        Assert.assertEquals("text", fieldMappings.mapping("manager.name").get("type"));
        Assert.assertEquals("keyword", fieldMappings.mapping("manager.address").get("type"));
    }

    @Test
    public void getMappingForInvalidField() {
        IndexMappings indexMappings = LocalClusterState.state().getFieldMappings(new String[]{INDEX_NAME});
        TypeMappings typeMappings = indexMappings.mapping(INDEX_NAME);
        FieldMappings fieldMappings = typeMappings.mapping(TYPE_NAME);

        Assert.assertNull(fieldMappings.mapping("work-email"));
        Assert.assertNull(fieldMappings.mapping("manager.home-address"));
        Assert.assertNull(fieldMappings.mapping("manager.name.first"));
        Assert.assertNull(fieldMappings.mapping("manager.name.first.uppercase"));
    }

    @Test
    public void getMappingFromCache() throws IOException {
        // Mock here again for verification below and mock addListener()
        ClusterService mockService = mockClusterService(MAPPING);
        ClusterStateListener[] listener = new ClusterStateListener[1]; // Trick to access inside lambda
        doAnswer(invocation -> {
            listener[0] = (ClusterStateListener) invocation.getArguments()[0];
            return null;
        }).when(mockService).addListener(any());
        LocalClusterState.state().setClusterService(mockService);

        // 1.Actual findMappings be invoked only once
        for (int i = 0; i < 10; i++) {
            LocalClusterState.state().getFieldMappings(new String[]{INDEX_NAME});
        }
        verify(mockService.state().metaData(), times(1)).findMappings(eq(new String[]{INDEX_NAME}), any(), any());

        // 2.Fire cluster state change event
        Assert.assertNotNull(listener[0]);
        ClusterChangedEvent mockEvent = mock(ClusterChangedEvent.class);
        when(mockEvent.metaDataChanged()).thenReturn(true);
        listener[0].clusterChanged(mockEvent);

        // 3.Cache should be invalidated and call findMapping another time only
        for (int i = 0; i < 5; i++) {
            LocalClusterState.state().getFieldMappings(new String[]{INDEX_NAME});
        }
        verify(mockService.state().metaData(), times(2)).findMappings(eq(new String[]{INDEX_NAME}), any(), any());
    }

    @Test
    public void getDefaultValueForQuerySlowLog() {
        // Force return empty list to avoid ClusterSettings be invoked which is a final class and hard to mock.
        SqlSettings settings = spy(new SqlSettings());
        doReturn(emptyList()).when(settings).getSettings();
        LocalClusterState.state().setSqlSettings(settings);

        Assert.assertEquals(2, (int) LocalClusterState.state().getSettingValue(QUERY_SLOWLOG));
    }

}
