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

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.planner.logical.rule;

import com.amazon.opendistroforelasticsearch.sql.expression.ReferenceExpression;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalSort;
import lombok.experimental.UtilityClass;

@UtilityClass
public class OptimizationRuleUtils {

  /**
   * Does the sort list only contain {@link ReferenceExpression}.
   *
   * @param logicalSort LogicalSort.
   * @return true only contain ReferenceExpression, otherwise false.
   */
  public static boolean sortByFieldsOnly(LogicalSort logicalSort) {
    return logicalSort.getSortList().stream()
        .map(sort -> sort.getRight() instanceof ReferenceExpression)
        .reduce(true, Boolean::logicalAnd);
  }
}
