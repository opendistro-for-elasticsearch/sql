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

package com.amazon.opendistroforelasticsearch.sql.expression.window.ranking;

import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName;
import com.amazon.opendistroforelasticsearch.sql.expression.window.CumulativeWindowFrame;

/**
 * Rank window function that assigns a rank number to each row based on sort items
 * defined in window definition. Use same rank number if sort item values same on
 * previous and current row.
 */
public class RankFunction extends RankingWindowFunction {

  /**
   * Total number of rows have seen in current partition.
   */
  private int total;

  public RankFunction() {
    super(BuiltinFunctionName.RANK.getName());
  }

  @Override
  protected int rank(CumulativeWindowFrame frame) {
    if (frame.isNewPartition()) {
      total = 1;
      rank = 1;
    } else {
      total++;
      if (isSortFieldValueDifferent(frame)) {
        rank = total;
      }
    }
    return rank;
  }

}
