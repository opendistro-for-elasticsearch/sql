package com.amazon.opendistroforelasticsearch.sql.planner.physical;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.exception.ExpressionEvaluationException;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.ReferenceExpression;
import com.amazon.opendistroforelasticsearch.sql.storage.BindingTuple;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public class FilterOperator extends PhysicalPlan {
    private final PhysicalPlan input;
    private final Expression conditions;
    private BindingTuple next = null;

    @Override
    public <R, C> R accept(PhysicalPlanNodeVisitor<R, C> visitor, C context) {
        return visitor.visitFilter(this, context);
    }

    @Override
    public List<PhysicalPlan> getChild() {
        return Arrays.asList(input);
    }

    @Override
    public void close() throws Exception {

    }

    @Override
    public boolean hasNext() {
        while(input.hasNext()) {
            BindingTuple tuple = input.next();
            ExprValue exprValue = conditions.valueOf(expression -> {
                if (expression instanceof ReferenceExpression) {
                    return tuple.resolve(((ReferenceExpression) expression).getAttr());
                } else {
                    throw new ExpressionEvaluationException("con't resolve expression");
                }
            });
            if(ExprValueUtils.getBooleanValue(exprValue)) {
                next = tuple;
                return true;
            }
        }
        return false;
    }

    @Override
    public BindingTuple next() {
        return next;
    }
}
