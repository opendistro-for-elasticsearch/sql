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
import com.google.common.collect.PeekingIterator;
import java.util.ArrayList;
import java.util.Collections;
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

  private final List<ExprValue> peers = new ArrayList<>();

  private int current;

  private boolean isNewPartition = true;

  @Override
  public boolean hasNext() {
    return current < peers.size();
  }

  @Override
  public void load(PeekingIterator<ExprValue> it) {
    if (hasNext()) {
      return;
    }

    // Reset state
    current = 0;
    isNewPartition = it.hasNext() && !isSamePartition(it.peek());
    peers.clear();

    // Load until:
    //  1) sort key different (different peer);
    //  2) or new partition
    //  3) or no more data
    while (it.hasNext()) {
      ExprValue next = it.peek();
      if (peers.isEmpty()) {
        peers.add(it.next());
      } else if (isSamePartition(next) && isPeer(next)) {
        peers.add(it.next());
      } else {
        break;
      }
    }
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

  private boolean isPeer(ExprValue next) {
    List<Expression> sortFields =
        windowDefinition.getSortList()
                        .stream()
                        .map(Pair::getRight)
                        .collect(Collectors.toList());

    ExprValue cur = peers.get(peers.size() - 1);
    return resolve(sortFields, cur).equals(resolve(sortFields, next));
  }

  private boolean isSamePartition(ExprValue next) {
    if (peers.isEmpty()) {
      return false;
    }

    List<Expression> partitionByList = windowDefinition.getPartitionByList();
    ExprValue cur = peers.get(peers.size() - 1);
    return resolve(partitionByList, cur).equals(resolve(partitionByList, next));
  }

  private List<ExprValue> resolve(List<Expression> expressions, ExprValue row) {
    Environment<Expression, ExprValue> valueEnv = row.bindingTuples();
    return expressions.stream()
                      .map(expr -> expr.valueOf(valueEnv))
                      .collect(Collectors.toList());
  }

}
