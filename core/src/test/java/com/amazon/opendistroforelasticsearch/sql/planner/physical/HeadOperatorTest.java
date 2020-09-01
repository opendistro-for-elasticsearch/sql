package com.amazon.opendistroforelasticsearch.sql.planner.physical;

import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HeadOperatorTest extends PhysicalPlanTestBase {

  @Mock
  private PhysicalPlan inputPlan;

  private final int defaultResultCount = 10;

  @Test
  public void headTest() {
    HeadOperator plan = new HeadOperator(new CountTestScan());
    List<ExprValue> result = execute(plan);
    assertEquals(defaultResultCount, result.size());
  }

  @Test
  public void headTest_keepLastTrue() {
    HeadOperator plan = new HeadOperator(new CountTestScan(),
        true, dsl.less(DSL.ref("id", INTEGER), DSL.literal(5)), defaultResultCount);
    List<ExprValue> result = execute(plan);
    assertEquals(5, result.size());
  }

  @Test
  public void headTest_keepLastFalse() {
    HeadOperator plan = new HeadOperator(new CountTestScan(),
        false, dsl.less(DSL.ref("id", INTEGER), DSL.literal(5)), defaultResultCount);
    List<ExprValue> result = execute(plan);
    assertEquals(4, result.size());
  }
}