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

package com.amazon.opendistroforelasticsearch.sql.executor;

import com.amazon.opendistroforelasticsearch.sql.common.response.ResponseListener;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprType;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlan;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Execution engine that encapsulates execution details.
 */
public interface ExecutionEngine {

  /**
   * Execute physical plan and call back response listener.
   *
   * @param plan     executable physical plan
   * @param listener response listener
   */
  void execute(PhysicalPlan plan, ResponseListener<QueryResponse> listener);

  /**
   * Explain physical plan and call back response listener. The reason why this has to
   * be part of execution engine interface is that the physical plan probably needs to
   * be executed to get more info for profiling, such as actual execution time, rows fetched etc.
   *
   * @param plan     physical plan to explain
   * @param listener response listener
   */
  void explain(PhysicalPlan plan, ResponseListener<ExplainResponse> listener);

  /**
   * Data class that encapsulates ExprValue.
   */
  @Data
  class QueryResponse {
    private final Schema schema;
    private final List<ExprValue> results;
  }

  @Data
  class Schema {
    private final List<Column> columns;

    @Data
    public static class Column {
      private final String name;
      private final String alias;
      private final ExprType exprType;
    }
  }

  /**
   * Data class that encapsulates explain result. This can help decouple core engine
   * from concrete explain response format.
   */
  @Data
  class ExplainResponse {
    private final ExplainResponseNode root;
  }

  @AllArgsConstructor
  @Data
  @RequiredArgsConstructor
  class ExplainResponseNode {
    private final String name;
    private Map<String, Object> description;
    private List<ExplainResponseNode> children;
  }

}
