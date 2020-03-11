package com.amazon.opendistroforelasticsearch.ppl.plans.logical;

public interface LogicalPlanVisitor extends Visitor<LogicalPlan> {

    @Override
    default LogicalPlan visit(LogicalPlan plan) {
        if (plan instanceof Project) {
            return visitProject((Project) plan);
        } else if (plan instanceof Filter) {
            return visitFilter((Filter) plan);
        } else if (plan instanceof Relation) {
            return visitRelation((Relation) plan);
        } else if (plan instanceof Top) {
            return visitTop((Top) plan);
        } else {
            throw new IllegalArgumentException("unknown operator plan: " + plan);
        }
    }

    default LogicalPlan visitProject(Project node) {
        return node;
    }

    default LogicalPlan visitFilter(Filter node) {
        return node;
    }

    default LogicalPlan visitRelation(Relation node) {
        return node;
    }

    default LogicalPlan visitTop(Top node) {
        return node;
    }
}
