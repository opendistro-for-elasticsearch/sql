package com.amazon.opendistroforelasticsearch.ppl.planner;

import com.amazon.opendistroforelasticsearch.ppl.plans.expression.AttributeReference;
import com.amazon.opendistroforelasticsearch.sql.expression.domain.BindingTuple;
import com.amazon.opendistroforelasticsearch.sql.query.planner.physical.PhysicalOperator;
import org.elasticsearch.client.node.NodeClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.amazon.opendistroforelasticsearch.ppl.plans.dsl.DSL.equalTo;
import static com.amazon.opendistroforelasticsearch.ppl.plans.dsl.DSL.filter;
import static com.amazon.opendistroforelasticsearch.ppl.plans.dsl.DSL.intLiteral;
import static com.amazon.opendistroforelasticsearch.ppl.plans.dsl.DSL.project;
import static com.amazon.opendistroforelasticsearch.ppl.plans.dsl.DSL.relation;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class PlannerTest {
    @Mock
    NodeClient client;

    @Test
    public void testSFW() {
        Planner planner = new Planner(client);
        PhysicalOperator<BindingTuple> plan = planner.plan(project(
                filter(
                        relation("t"),
                        equalTo(new AttributeReference("a"), intLiteral(1))
                ),
                new AttributeReference("a"), new AttributeReference("b")
        ));

        assertEquals("{\"from\":0,\"size\":1,\"timeout\":\"60s\",\"query\":{\"term\":{\"a\":{\"value\":1,\"boost\":1" +
                     ".0}}},\"_source\":{\"includes\":[\"a\",\"b\"],\"excludes\":[]}}", plan.toString());
    }
}
