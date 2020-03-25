package com.amazon.opendistroforelasticsearch.ppl.plans.expression;

import com.amazon.opendistroforelasticsearch.ppl.node.AbstractNodeVisitor;
import com.amazon.opendistroforelasticsearch.ppl.node.NodeVisitor;
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
    public <T> T accept(NodeVisitor<T> visitor) {
        if (visitor instanceof AbstractNodeVisitor) {
            return ((AbstractNodeVisitor<T>) visitor).visitCount(this);
        } else {
            return visitor.visitChildren(this);
        }
    }
}
