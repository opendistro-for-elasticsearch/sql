package com.amazon.opendistroforelasticsearch.sql.planner.physical;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
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
    private final Integer number;
    @Getter
    private final Boolean keepLast;

    private static final Integer DEFAULT_LIMIT = 10;
    private static final Boolean IGNORE_LAST = false;

    @EqualsAndHashCode.Exclude
    private int recordCount = 0;
    @EqualsAndHashCode.Exclude
    private ExprValue next;

    @NonNull
    public HeadOperator(PhysicalPlan input) {
        this(input, DEFAULT_LIMIT, IGNORE_LAST);
    }

    @NonNull
    public HeadOperator(PhysicalPlan input, Integer number, Boolean keepLast) {
        this.input = input;
        this.number = number;
        this.keepLast = keepLast;
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
        while (input.hasNext()) {
            ExprValue next = input.next();
            if (recordCount < number) {
                this.next = next;
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
