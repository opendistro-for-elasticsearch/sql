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

import static com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort.SortOption;
import static com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort.SortOption.DEFAULT_ASC;

import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Window definition that consists of partition and sort by information for a window.
 */
@Data
public class WindowDefinition {

  private final List<Expression> partitionByList;
  private final List<Pair<SortOption, Expression>> sortList;

  /**
   * Return all items in partition by and sort list.
   * @return  all sort items
   */
  public List<Pair<SortOption, Expression>> getAllSortItems() {
    List<Pair<SortOption, Expression>> allSorts = new ArrayList<>();
    partitionByList.forEach(expr -> allSorts.add(ImmutablePair.of(DEFAULT_ASC, expr)));
    allSorts.addAll(sortList);
    return allSorts;
  }

}
