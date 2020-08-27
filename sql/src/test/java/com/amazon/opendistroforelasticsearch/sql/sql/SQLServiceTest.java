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

package com.amazon.opendistroforelasticsearch.sql.sql;

import static com.amazon.opendistroforelasticsearch.sql.executor.ExecutionEngine.QueryResponse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import com.amazon.opendistroforelasticsearch.sql.common.response.ResponseListener;
import com.amazon.opendistroforelasticsearch.sql.executor.ExecutionEngine;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlan;
import com.amazon.opendistroforelasticsearch.sql.sql.config.SQLServiceConfig;
import com.amazon.opendistroforelasticsearch.sql.sql.domain.SQLQueryRequest;
import com.amazon.opendistroforelasticsearch.sql.storage.StorageEngine;
import java.util.Collections;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@ExtendWith(MockitoExtension.class)
class SQLServiceTest {

  private AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

  private SQLService sqlService;

  @Mock
  private StorageEngine storageEngine;

  @Mock
  private ExecutionEngine executionEngine;

  @Mock
  private ExecutionEngine.Schema schema;

  @BeforeEach
  public void setUp() {
    context.registerBean(StorageEngine.class, () -> storageEngine);
    context.registerBean(ExecutionEngine.class, () -> executionEngine);
    context.register(SQLServiceConfig.class);
    context.refresh();
    sqlService = context.getBean(SQLService.class);
  }

  @Test
  public void canExecuteSqlQuery() {
    doAnswer(invocation -> {
      ResponseListener<QueryResponse> listener = invocation.getArgument(1);
      listener.onResponse(new QueryResponse(schema, Collections.emptyList()));
      return null;
    }).when(executionEngine).execute(any(), any());

    sqlService.execute(
        new SQLQueryRequest(new JSONObject(), "SELECT 123", "_opendistro/_sql", "jdbc"),
        new ResponseListener<QueryResponse>() {
          @Override
          public void onResponse(QueryResponse response) {
            assertNotNull(response);
          }

          @Override
          public void onFailure(Exception e) {
            fail(e);
          }
        });
  }

  @Test
  public void canExecuteFromPhysicalPlan() {
    doAnswer(invocation -> {
      ResponseListener<QueryResponse> listener = invocation.getArgument(1);
      listener.onResponse(new QueryResponse(schema, Collections.emptyList()));
      return null;
    }).when(executionEngine).execute(any(), any());

    sqlService.execute(mock(PhysicalPlan.class),
        new ResponseListener<QueryResponse>() {
          @Override
          public void onResponse(QueryResponse response) {
            assertNotNull(response);
          }

          @Override
          public void onFailure(Exception e) {
            fail(e);
          }
        });
  }

  @Test
  public void canCaptureErrorDuringExecution() {
    sqlService.execute(
        new SQLQueryRequest(new JSONObject(), "SELECT", "_opendistro/_sql", ""),
        new ResponseListener<QueryResponse>() {
          @Override
          public void onResponse(QueryResponse response) {
            fail();
          }

          @Override
          public void onFailure(Exception e) {
            assertNotNull(e);
          }
        });
  }

  @Test
  public void canCaptureErrorDuringExecutionFromPhysicalPlan() {
    doThrow(new RuntimeException()).when(executionEngine).execute(any(), any());

    sqlService.execute(mock(PhysicalPlan.class),
        new ResponseListener<QueryResponse>() {
          @Override
          public void onResponse(QueryResponse response) {
            fail();
          }

          @Override
          public void onFailure(Exception e) {
            assertNotNull(e);
          }
        });
  }

}