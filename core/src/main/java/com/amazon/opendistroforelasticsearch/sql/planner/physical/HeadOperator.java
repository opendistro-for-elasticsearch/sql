package com.amazon.opendistroforelasticsearch.sql.planner.physical;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprBooleanValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.LiteralExpression;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import java.util.Collections;
import java.util.List;

@Getter
@EqualsAndHashCode
public class HeadOperator extends PhysicalPlan {
    @Getter
    private final PhysicalPlan input;
    @Getter
    private final Boolean keepLast;
    @Getter
    private final Expression whileExpr;
    @Getter
    private final Integer number;

    private static final Integer DEFAULT_LIMIT = 10;
    private static final Boolean IGNORE_LAST = false;

    @EqualsAndHashCode.Exclude
    private int recordCount = 0;
    @EqualsAndHashCode.Exclude
    private boolean foundFirstFalse = false;
    @EqualsAndHashCode.Exclude
    private ExprValue next;

    @NonNull
    public HeadOperator(PhysicalPlan input) {
        this(input, IGNORE_LAST, new LiteralExpression(ExprBooleanValue.of(true)), DEFAULT_LIMIT);
    }

    @NonNull
    public HeadOperator(PhysicalPlan input, Boolean keepLast, Expression whileExpr, Integer number) {
        this.input = input;
        this.keepLast = keepLast;
        this.whileExpr = whileExpr;
        this.number = number;
    }

    @Override
    public <R, C> R accept(PhysicalPlanNodeVisitor<R, C> visitor, C context) {
        return visitor.visitHead(this, context);
    }

    @Override
    public List<PhysicalPlan> getChild() {
        return Collections.singletonList(input);
    }

    @Override
    public boolean hasNext() {
        while (input.hasNext() && !foundFirstFalse) {
            ExprValue inputVal = input.next();
            ExprValue exprValue = whileExpr.valueOf(inputVal.bindingTuples());
//            if (!(exprValue.isNull() || exprValue.isMissing()) && (exprValue.booleanValue())) {
//                next = inputValue;
//                return true;
//            }
            boolean exprResolution = (!(exprValue.isNull() || exprValue.isMissing()) && (exprValue.booleanValue()));
            boolean underRecordLimit = recordCount < number;

            if (underRecordLimit) {
                if (!exprResolution) {
                    // First false is when we decide whether to keep the last value
                    foundFirstFalse = true;
                    if (!keepLast) {
                        return false;
                    }
                }
                this.next = inputVal;
                recordCount++;
                return true;
            }
        }
        return false;
    }

    @Override
    public ExprValue next() {
        return this.next;
    }
}
