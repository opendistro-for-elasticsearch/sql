/*
 *   Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.ast.tree;

import static com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort.NullOrder.NULL_FIRST;
import static com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort.NullOrder.NULL_LAST;
import static com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort.SortOrder.ASC;
import static com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort.SortOrder.DESC;

import com.amazon.opendistroforelasticsearch.sql.ast.AbstractNodeVisitor;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Argument;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Field;
import com.google.common.collect.ImmutableList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * AST node for Sort {@link Sort#sortList} represent a list of sort expression and sort options.
 */
@ToString
@EqualsAndHashCode(callSuper = false)
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class Sort extends UnresolvedPlan {
  private UnresolvedPlan child;
  private final List<Argument> options;
  private final List<Field> sortList;

  @Override
  public Sort attach(UnresolvedPlan child) {
    this.child = child;
    return this;
  }

  @Override
  public List<UnresolvedPlan> getChild() {
    return ImmutableList.of(child);
  }

  @Override
  public <T, C> T accept(AbstractNodeVisitor<T, C> nodeVisitor, C context) {
    return nodeVisitor.visitSort(this, context);
  }

  /**
   * Sort Options.
   */
  @Data
  public static class SortOption {

    /**
     * Default ascending sort option, null first.
     */
    public static SortOption DEFAULT_ASC = new SortOption(ASC, NULL_FIRST);
    /**
     * Default descending sort option, null last.
     */
    public static SortOption DEFAULT_DESC = new SortOption(DESC, NULL_LAST);

    private final SortOrder sortOrder;
    private final NullOrder nullOrder;
  }

  public enum SortOrder {
    ASC,
    DESC
  }

  public enum NullOrder {
    NULL_FIRST,
    NULL_LAST
  }
}
