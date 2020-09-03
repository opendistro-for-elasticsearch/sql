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

package com.amazon.opendistroforelasticsearch.sql.sql.parser.context;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * SQL parsing context that maintains stack of query specifications for nested queries.
 * Currently this is just a thin wrapper by a stack.
 */
public class ParsingContext {

  /**
   * Use stack rather than linked query specification because there is no need
   * to look up through the stack.
   */
  private final Deque<QuerySpecification> contexts = new ArrayDeque<>();

  public void push() {
    contexts.push(new QuerySpecification());
  }

  public QuerySpecification peek() {
    return contexts.peek();
  }

  /**
   * Pop up query context.
   * @return  query context after popup.
   */
  public QuerySpecification pop() {
    return contexts.pop();
  }

}
