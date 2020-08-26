package com.amazon.opendistroforelasticsearch.sql.planner.logical;

import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Logical Top Plan.
 */
@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
public class LogicalTop extends LogicalPlan {

  private final LogicalPlan child;
  private final Integer noOfResults;
  private final List<Expression> fieldList;
  @Getter
  private final List<Expression> groupByList;

  @Override
  public List<LogicalPlan> getChild() {
    return Collections.singletonList(child);
  }

  @Override
  public <R, C> R accept(LogicalPlanNodeVisitor<R, C> visitor, C context) {
    return visitor.visitTop(this, context);
  }
}
