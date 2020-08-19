package com.amazon.opendistroforelasticsearch.sql.ast.tree;


import com.amazon.opendistroforelasticsearch.sql.ast.AbstractNodeVisitor;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Argument;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Field;
import com.google.common.collect.ImmutableList;
import lombok.*;

import java.util.List;

/**
 * AST node represent Dedupe operation.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
@AllArgsConstructor
public class Head extends UnresolvedPlan {
    private UnresolvedPlan child;
    private final List<Argument> options;

    @Override
    public Head attach(UnresolvedPlan child) {
        this.child = child;
        return this;
    }

    @Override
    public List<UnresolvedPlan> getChild() {
        return ImmutableList.of(this.child);
    }

    @Override
    public <T, C> T accept(AbstractNodeVisitor<T, C> nodeVisitor, C context) {
        return nodeVisitor.visitHead(this, context);
    }
}