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

import static com.amazon.opendistroforelasticsearch.sql.utils.SystemIndexUtils.isSystemIndex;

import com.amazon.opendistroforelasticsearch.sql.common.setting.Settings;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.client.ElasticsearchClient;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.system.ElasticsearchSystemIndex;
import com.amazon.opendistroforelasticsearch.sql.storage.StorageEngine;
import com.amazon.opendistroforelasticsearch.sql.storage.Table;
import lombok.RequiredArgsConstructor;

/** Elasticsearch storage engine implementation. */
@RequiredArgsConstructor
public class ElasticsearchStorageEngine implements StorageEngine {

  /** Elasticsearch client connection. */
  private final ElasticsearchClient client;

  private final Settings settings;

  @Override
  public Table getTable(String name) {
    if (isSystemIndex(name)) {
      return new ElasticsearchSystemIndex(client, name);
    } else {
      return new ElasticsearchIndex(client, settings, name);
    }
  }
}
