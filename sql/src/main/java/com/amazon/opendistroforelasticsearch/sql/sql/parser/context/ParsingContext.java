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

import java.util.Objects;

/**
 * SQL parsing context that maintains stack of query specifications for nested queries.
 */
public class ParsingContext {

  private QuerySpecification query = null;

  public void push() {
    query = new QuerySpecification();
  }

  public QuerySpecification peek() {
    Objects.requireNonNull(query, "Failed to pop context due to empty stack");
    return query;
  }

  /**
   * Pop up query context.
   * @return  query context after popup.
   */
  public QuerySpecification pop() {
    Objects.requireNonNull(query, "Failed to pop context due to empty stack");

    QuerySpecification current = query;
    query = query.getParent();
    return current;
  }

}
