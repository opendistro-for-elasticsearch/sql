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

import static com.amazon.opendistroforelasticsearch.sql.utils.SystemIndexUtils.TABLE_INFO;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazon.opendistroforelasticsearch.sql.common.setting.Settings;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.client.ElasticsearchClient;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.system.ElasticsearchSystemIndex;
import com.amazon.opendistroforelasticsearch.sql.storage.Table;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ElasticsearchStorageEngineTest {

  @Mock private ElasticsearchClient client;

  @Mock private Settings settings;

  @Test
  public void getTable() {
    ElasticsearchStorageEngine engine = new ElasticsearchStorageEngine(client, settings);
    Table table = engine.getTable("test");
    assertNotNull(table);
  }

  @Test
  public void getSystemTable() {
    ElasticsearchStorageEngine engine = new ElasticsearchStorageEngine(client, settings);
    Table table = engine.getTable(TABLE_INFO);
    assertNotNull(table);
    assertTrue(table instanceof ElasticsearchSystemIndex);
  }
}
