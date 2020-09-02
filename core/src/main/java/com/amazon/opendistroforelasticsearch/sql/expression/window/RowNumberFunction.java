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

package com.amazon.opendistroforelasticsearch.sql.expression.window;

import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionName;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
public class RowNumberFunction extends RankingWindowFunction {

  private int rowNumber = 1;

  @Override
  public FunctionName getFunctionName() {
    return BuiltinFunctionName.ROW_NUMBER.getName();
  }

  @Override
  protected int rank(WindowFrame windowFrame) {
    if (windowFrame.isNewPartition()) {
      rowNumber = 1;
    }
    return rowNumber++;
  }

  @Override
  public String toString() {
    return getFunctionName().toString();
  }

}
