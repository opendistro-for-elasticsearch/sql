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
 * Dense rank window function that assigns a rank number to each row similarly as
 * rank function. The difference is there is no gap between rank number assigned.
 */
public class DenseRankFunction extends RankingWindowFunction {

  public DenseRankFunction() {
    super(BuiltinFunctionName.DENSE_RANK.getName());
  }

  @Override
  protected int rank(CumulativeWindowFrame frame) {
    if (frame.isNewPartition()) {
      rank = 1;
    } else {
      if (isSortFieldValueDifferent(frame)) {
        rank++;
      }
    }
    return rank;
  }

}
