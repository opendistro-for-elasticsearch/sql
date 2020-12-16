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

package com.amazon.opendistroforelasticsearch.sql.expression.window.frame;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.env.Environment;
import com.amazon.opendistroforelasticsearch.sql.expression.window.WindowDefinition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Window frame that only keep peers (tuples with same value of fields specified in sort list
 * in window definition).
 */
@RequiredArgsConstructor
public class PeerWindowFrame implements WindowFrame {

  private final WindowDefinition windowDefinition;

  private List<ExprValue> peers = new ArrayList<>();
  private ExprValue next;
  private int current;

  private boolean isNewPartition = true;

  @Override
  public boolean hasNext() {
    return current < peers.size();
  }

  @Override
  public void load(Iterator<ExprValue> it) {
    if (hasNext()) {
      return;
    }

    if (next != null) {
      isNewPartition = !isSamePartition(peers.get(peers.size() - 1), next);
      peers.clear();
      peers.add(next);
    }

    // load until next peer or partition
    ExprValue cur = null;
    while (it.hasNext() && isSamePartitionAndSortValues(cur = it.next())) {
      peers.add(cur);
    }

    current = 0;
    next = cur;
  }

  @Override
  public boolean isNewPartition() {
    return isNewPartition;
  }

  @Override
  public List<ExprValue> next() {
    isNewPartition = false;
    if (current++ == 0) {
      return peers;
    }
    return Collections.emptyList();
  }

  @Override
  public ExprValue current() {
    return peers.get(current);
  }

  private List<ExprValue> resolve(List<Expression> expressions, ExprValue row) {
    Environment<Expression, ExprValue> valueEnv = row.bindingTuples();
    return expressions.stream()
        .map(expr -> expr.valueOf(valueEnv))
        .collect(Collectors.toList());
  }

  private List<Expression> getSortFields() {
    return windowDefinition.getSortList()
        .stream()
        .map(Pair::getRight)
        .collect(Collectors.toList());
  }

  private boolean isSamePartitionAndSortValues(ExprValue cur) {
    if (peers.isEmpty()) {
      return true;
    }

    ExprValue prev = peers.get(peers.size() - 1);
    return isSamePartition(cur, prev)
        && resolve(getSortFields(), prev).equals(resolve(getSortFields(), cur));
  }

  private boolean isSamePartition(ExprValue cur, ExprValue prev) {
    return resolve(windowDefinition.getPartitionByList(), prev)
        .equals(resolve(windowDefinition.getPartitionByList(), cur));
  }

}
