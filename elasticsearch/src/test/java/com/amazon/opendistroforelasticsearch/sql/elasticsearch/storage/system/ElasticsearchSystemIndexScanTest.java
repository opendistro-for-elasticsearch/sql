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

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.system;

import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.stringValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.amazon.opendistroforelasticsearch.sql.elasticsearch.request.system.ElasticsearchSystemRequest;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ElasticsearchSystemIndexScanTest {

  @Mock
  private ElasticsearchSystemRequest request;

  @Test
  public void queryData() {
    when(request.search()).thenReturn(Collections.singletonList(stringValue("text")));
    final ElasticsearchSystemIndexScan systemIndexScan = new ElasticsearchSystemIndexScan(request);

    systemIndexScan.open();
    assertTrue(systemIndexScan.hasNext());
    assertEquals(stringValue("text"), systemIndexScan.next());
  }

  @Test
  public void explain() {
    when(request.toString()).thenReturn("request");
    final ElasticsearchSystemIndexScan systemIndexScan = new ElasticsearchSystemIndexScan(request);

    assertEquals("request", systemIndexScan.explain());
  }
}