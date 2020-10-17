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

package com.amazon.opendistroforelasticsearch.sql.planner.logical;

import static org.junit.jupiter.api.Assertions.assertNull;

import com.amazon.opendistroforelasticsearch.sql.expression.NamedExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.NamedAggregator;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LogicalIndexScanAggregationTest {

  @Mock
  private NamedAggregator aggregator;

  @Mock
  private NamedExpression groupBy;

  @Test
  public void visitor_return_null() {
    LogicalPlan indexScan = new LogicalIndexScanAggregation("index", Arrays.asList(aggregator),
        Arrays.asList(groupBy));
    assertNull(indexScan.accept(new LogicalPlanNodeVisitor<Integer, Object>() {
    }, null));
  }
}