package com.amazon.opendistroforelasticsearch.ppl.planner;

import com.amazon.opendistroforelasticsearch.ppl.plans.expression.AttributeReference;
import com.amazon.opendistroforelasticsearch.sql.expression.domain.BindingTuple;
import com.amazon.opendistroforelasticsearch.sql.query.planner.physical.PhysicalOperator;
import org.elasticsearch.client.node.NodeClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static com.amazon.opendistroforelasticsearch.ppl.plans.dsl.DSL.agg;
import static com.amazon.opendistroforelasticsearch.ppl.plans.dsl.DSL.and;
import static com.amazon.opendistroforelasticsearch.ppl.plans.dsl.DSL.attr;
import static com.amazon.opendistroforelasticsearch.ppl.plans.dsl.DSL.count;
import static com.amazon.opendistroforelasticsearch.ppl.plans.dsl.DSL.equalTo;
import static com.amazon.opendistroforelasticsearch.ppl.plans.dsl.DSL.filter;
import static com.amazon.opendistroforelasticsearch.ppl.plans.dsl.DSL.intLiteral;
import static com.amazon.opendistroforelasticsearch.ppl.plans.dsl.DSL.project;
import static com.amazon.opendistroforelasticsearch.ppl.plans.dsl.DSL.relation;
import static com.amazon.opendistroforelasticsearch.ppl.plans.dsl.DSL.stringLiteral;
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

        assertEquals("{\"from\":0,\"size\":10,\"timeout\":\"30s\",\"query\":{\"term\":{\"a\":{\"value\":1,\"boost\":1" +
                     ".0}}},\"_source\":{\"includes\":[\"a\",\"b\"],\"excludes\":[]}}", plan.toString());
    }

    @Test
    public void testSearchAndCondition() {
        Planner planner = new Planner(client);
        PhysicalOperator<BindingTuple> plan = planner.plan(project(
                filter(
                        relation("t"),
                        and(equalTo(attr("a"), intLiteral(1)), equalTo(attr("b"), stringLiteral("value")))
                ),
                new AttributeReference("a"), new AttributeReference("b")
        ));

        assertEquals("{\"from\":0,\"size\":10,\"timeout\":\"30s\",\"query\":{\"term\":{\"a\":{\"value\":1,\"boost\":1" +
                     ".0}}},\"_source\":{\"includes\":[\"a\",\"b\"],\"excludes\":[]}}", plan.toString());
    }

    @Test
    public void testSearchAggregation() {
        Planner planner = new Planner(client);
        PhysicalOperator<BindingTuple> plan = planner.plan(agg(
                filter(
                        relation("t"),
                        and(equalTo(attr("a"), intLiteral(1)), equalTo(attr("b"), stringLiteral("value")))
                ),
                Arrays.asList(new AttributeReference("a")), Arrays.asList(count(new AttributeReference("b")))
        ));

        assertEquals("{\"from\":0,\"size\":10,\"timeout\":\"30s\",\"query\":{\"term\":{\"a\":{\"value\":1,\"boost\":1" +
                     ".0}}},\"_source\":{\"includes\":[\"a\",\"b\"],\"excludes\":[]}}", plan.toString());
    }
}
