package com.amazon.opendistroforelasticsearch.sql.planner.logical;

import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Arrays;
import java.util.List;

@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
public class LogicalHead extends LogicalPlan {
    private final LogicalPlan child;
    private final Boolean keeplast;
    private final Expression whileExpr;
    private final Integer number;

    @Override
    public List<LogicalPlan> getChild() {
        return Arrays.asList(child);
    }

    @Override
    public <R, C> R accept(LogicalPlanNodeVisitor<R, C> visitor, C context) {
        return visitor.visitHead(this, context);
    }
}
