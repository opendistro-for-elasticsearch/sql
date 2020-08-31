package com.amazon.opendistroforelasticsearch.sql.planner.physical;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class HeadOperatorTest extends PhysicalPlanTestBase {
    @Mock
    private PhysicalPlan inputPlan;

    private final int DEFAULT_RESULT_COUNT = 10;

    @Test
    public void headTest() {
//        FilterOperator plan = new FilterOperator(new TestScan(),
//                dsl.equal(DSL.ref("response", INTEGER), DSL.literal(404)));
//        List<ExprValue> result = execute(plan);
//        assertEquals(1, result.size());
//        assertThat(result, containsInAnyOrder(ExprValueUtils
//                .tupleValue(ImmutableMap
//                        .of("ip", "209.160.24.63", "action", "GET", "response", 404, "referer",
//                                "www.amazon.com"))));
        HeadOperator plan = new HeadOperator(new CountTestScan());
        List<ExprValue> result = execute(plan);
        assertEquals(DEFAULT_RESULT_COUNT, result.size());
    }

    @Test
    public void headTest_keepLastTrue() {
        HeadOperator plan = new HeadOperator(new CountTestScan(),
                true, dsl.less(DSL.ref("id", INTEGER), DSL.literal(5)), DEFAULT_RESULT_COUNT);
        List<ExprValue> result = execute(plan);
        assertEquals(5, result.size());
    }

    @Test
    public void headTest_keepLastTrue() {
        HeadOperator plan = new HeadOperator(new CountTestScan(),
                true, dsl.less(DSL.ref("id", INTEGER), DSL.literal(5)), DEFAULT_RESULT_COUNT);
        List<ExprValue> result = execute(plan);
        assertEquals(5, result.size());
    }
    }

    @Test
    public void headTest_keepLastFalse() {
        HeadOperator plan = new HeadOperator(new CountTestScan(),
                false, dsl.less(DSL.ref("id", INTEGER), DSL.literal(5)), DEFAULT_RESULT_COUNT);
        List<ExprValue> result = execute(plan);
        assertEquals(4, result.size());
    }
}