package com.amazon.opendistroforelasticsearch.sql.ast.tree;

import com.amazon.opendistroforelasticsearch.sql.ast.AbstractNodeVisitor;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Field;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.UnresolvedExpression;
import com.google.common.collect.ImmutableList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * AST node represent Rare operation.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = false)
public class Rare extends UnresolvedPlan {
    private UnresolvedPlan child;
    private final List<Field> fields;
    private final List<UnresolvedExpression> groupExprList;

    /**
     * Rare Constructor.
     */
    public Rare(List<Field> fields,List<UnresolvedExpression> groupExprList) {
        this.fields = fields;
        this.groupExprList = groupExprList;
    }

    @Override
    public Rare attach(UnresolvedPlan child) {
        this.child = child;
        return this;
    }

    @Override
    public List<UnresolvedPlan> getChild() {
        return ImmutableList.of(this.child);
    }

    @Override
    public <T, C> T accept(AbstractNodeVisitor<T, C> nodeVisitor, C context) {
        return nodeVisitor.visitRare(this, context);
    }
}
