package com.amazon.opendistroforelasticsearch.ppl.plans.expression;

import com.amazon.opendistroforelasticsearch.ppl.plans.expression.visitor.AbstractExprVisitor;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.visitor.ExprVisitor;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.Expression;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.Visitor;
import com.google.common.collect.ImmutableList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class AttributeList extends Expression {
    @Getter
    private List<Expression> attrList;

    @Override
    public List<Expression> getChild() {
        return ImmutableList.of();
    }

    @Override
    public Expression bottomUp(Visitor<Expression> visitor) {
        return visitor.visit(this);
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        if (visitor instanceof AbstractExprVisitor) {
            return ((AbstractExprVisitor<T>) visitor).visitUnresolvedAttributeList(this);
        } else {
            return visitor.visitChildren(this);
        }
    }
}
