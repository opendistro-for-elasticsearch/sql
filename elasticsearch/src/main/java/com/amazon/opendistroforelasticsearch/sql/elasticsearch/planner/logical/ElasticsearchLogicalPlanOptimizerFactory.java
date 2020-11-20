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

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.planner.logical;

import com.amazon.opendistroforelasticsearch.sql.elasticsearch.planner.logical.rule.MergeAggAndIndexScan;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.planner.logical.rule.MergeAggAndRelation;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.planner.logical.rule.MergeFilterAndRelation;
import com.amazon.opendistroforelasticsearch.sql.planner.optimizer.LogicalPlanOptimizer;
import java.util.Arrays;
import lombok.experimental.UtilityClass;

/**
 * Elasticsearch storage specified logical plan optimizer.
 */
@UtilityClass
public class ElasticsearchLogicalPlanOptimizerFactory {

  /**
   * Create Elasticsearch storage specified logical plan optimizer.
   */
  public static LogicalPlanOptimizer create() {
    return new LogicalPlanOptimizer(Arrays.asList(
        new MergeFilterAndRelation(),
        new MergeAggAndIndexScan(),
        new MergeAggAndRelation()));
  }
}
