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

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.executor;

import static com.amazon.opendistroforelasticsearch.sql.common.setting.Settings.Key.QUERY_SIZE_LIMIT;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.tupleValue;
import static com.amazon.opendistroforelasticsearch.sql.executor.ExecutionEngine.QueryResponse;
import static com.google.common.collect.ImmutableMap.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazon.opendistroforelasticsearch.sql.common.response.ResponseListener;
import com.amazon.opendistroforelasticsearch.sql.common.setting.Settings;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.client.ElasticsearchClient;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.value.ElasticsearchExprValueFactory;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.executor.protector.ElasticsearchExecutionProtector;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.ElasticsearchIndexScan;
import com.amazon.opendistroforelasticsearch.sql.executor.ExecutionEngine;
import com.amazon.opendistroforelasticsearch.sql.executor.ExecutionEngine.ExplainResponse;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlan;
import com.amazon.opendistroforelasticsearch.sql.storage.TableScanOperator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ElasticsearchExecutionEngineTest {

  @Mock private ElasticsearchClient client;

  @Mock private ElasticsearchExecutionProtector protector;

  @Mock private static ExecutionEngine.Schema schema;

  @BeforeEach
  void setUp() {
    doAnswer(
        invocation -> {
          // Run task immediately
          Runnable task = invocation.getArgument(0);
          task.run();
          return null;
        })
        .when(client)
        .schedule(any());
  }

  @Test
  void executeSuccessfully() {
    List<ExprValue> expected =
        Arrays.asList(
            tupleValue(of("name", "John", "age", 20)), tupleValue(of("name", "Allen", "age", 30)));
    FakePhysicalPlan plan = new FakePhysicalPlan(expected.iterator());
    when(protector.protect(plan)).thenReturn(plan);

    ElasticsearchExecutionEngine executor = new ElasticsearchExecutionEngine(client, protector);
    List<ExprValue> actual = new ArrayList<>();
    executor.execute(
        plan,
        new ResponseListener<QueryResponse>() {
          @Override
          public void onResponse(QueryResponse response) {
            actual.addAll(response.getResults());
          }

          @Override
          public void onFailure(Exception e) {
            fail("Error occurred during execution", e);
          }
        });

    assertTrue(plan.hasOpen);
    assertEquals(expected, actual);
    assertTrue(plan.hasClosed);
  }

  @Test
  void executeWithFailure() {
    PhysicalPlan plan = mock(PhysicalPlan.class);
    RuntimeException expected = new RuntimeException("Execution error");
    when(plan.hasNext()).thenThrow(expected);
    when(protector.protect(plan)).thenReturn(plan);

    ElasticsearchExecutionEngine executor = new ElasticsearchExecutionEngine(client, protector);
    AtomicReference<Exception> actual = new AtomicReference<>();
    executor.execute(
        plan,
        new ResponseListener<QueryResponse>() {
          @Override
          public void onResponse(QueryResponse response) {
            fail("Expected error didn't happen");
          }

          @Override
          public void onFailure(Exception e) {
            actual.set(e);
          }
        });
    assertEquals(expected, actual.get());
    verify(plan).close();
  }

  @Test
  void explainSuccessfully() {
    ElasticsearchExecutionEngine executor = new ElasticsearchExecutionEngine(client, protector);
    Settings settings = mock(Settings.class);
    when(settings.getSettingValue(QUERY_SIZE_LIMIT)).thenReturn(100);
    PhysicalPlan plan = new ElasticsearchIndexScan(mock(ElasticsearchClient.class),
        settings, "test", mock(ElasticsearchExprValueFactory.class));

    AtomicReference<ExplainResponse> result = new AtomicReference<>();
    executor.explain(plan, new ResponseListener<ExplainResponse>() {
      @Override
      public void onResponse(ExplainResponse response) {
        result.set(response);
      }

      @Override
      public void onFailure(Exception e) {
        fail(e);
      }
    });

    assertNotNull(result.get());
  }

  @Test
  void explainWithFailure() {
    ElasticsearchExecutionEngine executor = new ElasticsearchExecutionEngine(client, protector);
    PhysicalPlan plan = mock(PhysicalPlan.class);
    when(plan.accept(any(), any())).thenThrow(IllegalStateException.class);

    AtomicReference<Exception> result = new AtomicReference<>();
    executor.explain(plan, new ResponseListener<ExplainResponse>() {
      @Override
      public void onResponse(ExplainResponse response) {
        fail("Should fail as expected");
      }

      @Override
      public void onFailure(Exception e) {
        result.set(e);
      }
    });

    assertNotNull(result.get());
  }

  @RequiredArgsConstructor
  private static class FakePhysicalPlan extends TableScanOperator {
    private final Iterator<ExprValue> it;
    private boolean hasOpen;
    private boolean hasClosed;

    @Override
    public void open() {
      super.open();
      hasOpen = true;
    }

    @Override
    public void close() {
      super.close();
      hasClosed = true;
    }

    @Override
    public boolean hasNext() {
      return it.hasNext();
    }

    @Override
    public ExprValue next() {
      return it.next();
    }

    @Override
    public ExecutionEngine.Schema schema() {
      return schema;
    }

    @Override
    public String explain() {
      return "explain";
    }
  }
}
