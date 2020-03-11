package com.amazon.opendistroforelasticsearch.ppl.plans.expression;

import com.amazon.opendistroforelasticsearch.ppl.plans.expression.visitor.AbstractExprVisitor;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.visitor.ExprVisitor;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.Expression;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.Visitor;
import com.google.common.collect.ImmutableList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class Count extends Expression {

    private Expression count;

    public int getCount() {
        return Integer.parseInt(count.toString());
    }

    @Override
    public List<Expression> getChild() {
        return ImmutableList.of();
    }

    @Override
    public Expression bottomUp(Visitor<Expression> visitor) {
        count = count.bottomUp(visitor);
        return visitor.visit(this);
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        if (visitor instanceof AbstractExprVisitor) {
            return ((AbstractExprVisitor<T>) visitor).visitCount(this);
        } else {
            return visitor.visitChildren(this);
        }
    }
}
