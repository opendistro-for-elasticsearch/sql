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
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlan;
import com.amazon.opendistroforelasticsearch.sql.storage.Table;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanDSL.relation;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ElasticsearchIndexTest {

    @Mock
    private ElasticsearchClient client;

    @Test
    public void getFieldTypes() {
        when(client.getIndexMappings("test")).thenReturn(
            ImmutableMap.of("test", new IndexMapping(
                    ImmutableMap.<String, String>builder().
                        put("name", "keyword").
                        put("address", "text").
                        put("age", "integer").
                        put("account_number", "long").
                        put("balance1", "float").
                        put("balance2", "double").
                        put("gender", "boolean").
                        put("family", "nested").
                        put("employer", "object").
                        put("birthday", "date").
                        build()
                )
            )
        );

        Table index = new ElasticsearchIndex(client, "test");
        Map<String, ExprType> fieldTypes = index.getFieldTypes();
        assertThat(
            fieldTypes,
            allOf(
                aMapWithSize(10),
                hasEntry("name", ExprType.STRING),
                hasEntry("address", ExprType.STRING),
                hasEntry("age", ExprType.INTEGER),
                hasEntry("account_number", ExprType.LONG),
                hasEntry("balance1", ExprType.FLOAT),
                hasEntry("balance2", ExprType.DOUBLE),
                hasEntry("gender", ExprType.BOOLEAN),
                hasEntry("family", ExprType.ARRAY),
                hasEntry("employer", ExprType.STRUCT),
                hasEntry("birthday", ExprType.UNKNOWN)
            )
        );
    }

    @Test
    public void implement() {
        String indexName = "test";
        Table index = new ElasticsearchIndex(client, indexName);
        LogicalPlan plan = relation(indexName);

        PhysicalPlan actual = index.implement(plan);
        assertTrue(actual instanceof ElasticsearchIndexScan);
    }

}