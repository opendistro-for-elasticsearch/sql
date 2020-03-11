package com.amazon.opendistroforelasticsearch.ppl.plans.logical;

import com.amazon.opendistroforelasticsearch.ppl.plans.expression.And;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.Count;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.EqualTo;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.Literal;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.UnresolvedAttribute;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.AttributeList;

public interface ExpressionVisitor extends Visitor<Expression> {
    @Override
    default Expression visit(Expression node) {
        if (node instanceof Literal) {
            return visitLiteral((Literal) node);
        } else if (node instanceof UnresolvedAttribute) {
            return visitUnresolvedAttribute((UnresolvedAttribute) node);
        } else if (node instanceof EqualTo) {
            return visitEqualTo((EqualTo) node);
        } else if (node instanceof And) {
            return visitAnd((And) node);
        } else if (node instanceof Count) {
            return visitCount((Count) node);
        } else if (node instanceof AttributeList) {
            return visitUnresolvedAttributeList((AttributeList) node);
        } else {
            throw new IllegalArgumentException("unknown operator node: " + node);
        }
    }

    default Expression visitLiteral(Literal node) {
        return node;
    }

    default Expression visitUnresolvedAttribute(UnresolvedAttribute node) {
        return node;
    }

    default Expression visitEqualTo(EqualTo node) {
        return node;
    }

    default Expression visitAnd(And node) {
        return node;
    }

    default Expression visitCount(Count node) {
        return node;
    }

    default Expression visitUnresolvedAttributeList(AttributeList node) {
        return node;
    }
}
