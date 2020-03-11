package com.amazon.opendistroforelasticsearch.ppl.plans.logical;

import com.amazon.opendistroforelasticsearch.ppl.plans.expression.Count;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.Literal;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class Top extends LogicalPlan {

    @Getter
    private LogicalPlan fieldList;
    @Getter
    private Literal count;
    @Getter
    private Expression topOptions;
    @Getter
    private Expression byClause;
    @Getter
    private LogicalPlan input;

    public Top(LogicalPlan fieldList) {
        this.fieldList = fieldList;
    }

    public Top count(Literal count) {
        this.count = count;
        return this;
    }

    public Top byClause(Expression byClause) {
        this.byClause = byClause;
        return this;
    }

    @Override
    public LogicalPlan withInput(LogicalPlan input) {
        this.input = input;
        return this;
    }
}
