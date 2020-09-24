/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazon.opendistroforelasticsearch.sql.planner.physical;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType;
import com.amazon.opendistroforelasticsearch.sql.exception.ExpressionEvaluationException;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.ReferenceExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.config.ExpressionConfig;
import com.amazon.opendistroforelasticsearch.sql.expression.env.Environment;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Configuration
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ExpressionConfig.class})
public class PhysicalPlanTestBase {
  @Autowired
  protected DSL dsl;

  protected static final List<ExprValue> countTestInputs = new ImmutableList.Builder<ExprValue>()
          .add(ExprValueUtils.tupleValue(ImmutableMap
                  .of("id", 1, "testString", "asdf")))
          .add(ExprValueUtils.tupleValue(ImmutableMap
                  .of("id", 2, "testString", "asdf")))
          .add(ExprValueUtils.tupleValue(ImmutableMap
                  .of("id", 3, "testString", "asdf")))
          .add(ExprValueUtils.tupleValue(ImmutableMap
                  .of("id", 4, "testString", "asdf")))
          .add(ExprValueUtils.tupleValue(ImmutableMap
                  .of("id", 5, "testString", "asdf")))
          .add(ExprValueUtils.tupleValue(ImmutableMap
                  .of("id", 6, "testString", "asdf")))
          .add(ExprValueUtils.tupleValue(ImmutableMap
                  .of("id", 7, "testString", "asdf")))
          .add(ExprValueUtils.tupleValue(ImmutableMap
                  .of("id", 8, "testString", "asdf")))
          .add(ExprValueUtils.tupleValue(ImmutableMap
                  .of("id", 9, "testString", "asdf")))
          .add(ExprValueUtils.tupleValue(ImmutableMap
                  .of("id", 10, "testString", "asdf")))
          .add(ExprValueUtils.tupleValue(ImmutableMap
                  .of("id", 11, "testString", "asdf")))
          .build();

  protected static final List<ExprValue> inputs = new ImmutableList.Builder<ExprValue>()
      .add(ExprValueUtils.tupleValue(ImmutableMap
          .of("ip", "209.160.24.63", "action", "GET", "response", 200, "referer",
              "www.amazon.com")))
      .add(ExprValueUtils.tupleValue(ImmutableMap
          .of("ip", "209.160.24.63", "action", "GET", "response", 404, "referer",
              "www.amazon.com")))
      .add(ExprValueUtils.tupleValue(ImmutableMap
          .of("ip", "112.111.162.4", "action", "GET", "response", 200, "referer",
              "www.amazon.com")))
      .add(ExprValueUtils.tupleValue(ImmutableMap
          .of("ip", "74.125.19.106", "action", "POST", "response", 200, "referer",
              "www.google.com")))
      .add(ExprValueUtils
          .tupleValue(ImmutableMap.of("ip", "74.125.19.106", "action", "POST", "response", 500)))
      .build();

  private static Map<String, ExprCoreType> typeMapping =
      new ImmutableMap.Builder<String, ExprCoreType>()
          .put("ip", ExprCoreType.STRING)
          .put("action", ExprCoreType.STRING)
          .put("response", ExprCoreType.INTEGER)
          .put("referer", ExprCoreType.STRING)
          .build();

  @Bean
  protected Environment<Expression, ExprCoreType> typeEnv() {
    return var -> {
      if (var instanceof ReferenceExpression) {
        ReferenceExpression refExpr = (ReferenceExpression) var;
        if (typeMapping.containsKey(refExpr.getAttr())) {
          return typeMapping.get(refExpr.getAttr());
        }
      }
      throw new ExpressionEvaluationException("type resolved failed");
    };
  }

  protected List<ExprValue> execute(PhysicalPlan plan) {
    ImmutableList.Builder<ExprValue> builder = new ImmutableList.Builder<>();
    plan.open();
    while (plan.hasNext()) {
      builder.add(plan.next());
    }
    plan.close();
    return builder.build();
  }

  protected static class TestScan extends PhysicalPlan {
    private final Iterator<ExprValue> iterator;

    public TestScan() {
      iterator = inputs.iterator();
    }

    @Override
    public <R, C> R accept(PhysicalPlanNodeVisitor<R, C> visitor, C context) {
      return null;
    }

    @Override
    public List<PhysicalPlan> getChild() {
      return ImmutableList.of();
    }

    @Override
    public boolean hasNext() {
      return iterator.hasNext();
    }

    @Override
    public ExprValue next() {
      return iterator.next();
    }
  }

  protected static class CountTestScan extends PhysicalPlan {
    private final Iterator<ExprValue> iterator;

    public CountTestScan() {
      iterator = countTestInputs.iterator();
    }

    @Override
    public <R, C> R accept(PhysicalPlanNodeVisitor<R, C> visitor, C context) {
      return null;
    }

    @Override
    public List<PhysicalPlan> getChild() {
      return ImmutableList.of();
    }

    @Override
    public boolean hasNext() {
      return iterator.hasNext();
    }

    @Override
    public ExprValue next() {
      return iterator.next();
    }
  }
}
