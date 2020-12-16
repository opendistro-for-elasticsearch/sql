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

import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.window.frame.WindowFrame;

/**
 * Window function abstraction.
 */
public interface WindowFunctionExpression extends Expression {

  /**
   * Create specific window frame based on window definition and what's current window function.
   * For now two types of cumulative window frame is returned:
   *  1. Ranking window functions: ignore frame definition and always operates on
   *      previous and current row.
   *  2. Aggregate window functions: frame partition into peers and sliding window is not supported.
   *
   * @param definition window definition
   * @return           window frame
   */
  WindowFrame createWindowFrame(WindowDefinition definition);

}
