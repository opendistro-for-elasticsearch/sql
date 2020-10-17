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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.NamedExpression;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LogicalIndexScanTest {

  @Mock
  private Expression filter;

  @Mock
  private NamedExpression project;

  @Test
  public void has_filter() {
    LogicalIndexScan indexScan = new LogicalIndexScan("index", filter);
    assertTrue(indexScan.hasFilter());

    indexScan = new LogicalIndexScan("index", Collections.singletonList(project));
    assertFalse(indexScan.hasFilter());
  }

  @Test
  public void has_project() {
    LogicalIndexScan indexScan = new LogicalIndexScan("index", Collections.EMPTY_LIST);
    assertFalse(indexScan.hasProjects());

    indexScan = new LogicalIndexScan("index", filter);
    assertFalse(indexScan.hasProjects());

    indexScan = new LogicalIndexScan("index", filter, Collections.singletonList(project));
    assertTrue(indexScan.hasProjects());
  }

  @Test
  public void visitor_return_null() {
    LogicalPlan indexScan = new LogicalIndexScan("index", filter);
    assertNull(indexScan.accept(new LogicalPlanNodeVisitor<Integer, Object>() {
    }, null));
  }
}