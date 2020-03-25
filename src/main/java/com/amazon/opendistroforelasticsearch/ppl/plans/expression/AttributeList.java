package com.amazon.opendistroforelasticsearch.ppl.plans.expression;

import com.amazon.opendistroforelasticsearch.ppl.node.AbstractNodeVisitor;
import com.amazon.opendistroforelasticsearch.ppl.node.NodeVisitor;
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
    public <T> T accept(NodeVisitor<T> visitor) {
        if (visitor instanceof AbstractNodeVisitor) {
            return ((AbstractNodeVisitor<T>) visitor).visitUnresolvedAttributeList(this);
        } else {
            return visitor.visitChildren(this);
        }
    }
}
