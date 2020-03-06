package com.amazon.opendistroforelasticsearch.ppl.planner;

import com.amazon.opendistroforelasticsearch.ppl.planner.dsl.QueryAction;
import com.amazon.opendistroforelasticsearch.ppl.planner.dsl.SearchRequestBuilder;
import com.amazon.opendistroforelasticsearch.ppl.planner.dsl.SourceFilter;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.AttributeReference;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.ToDSL;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.Filter;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.LogicalPlan;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.Project;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.Relation;
import com.amazon.opendistroforelasticsearch.ppl.plans.physical.PhysicalScroll;
import com.amazon.opendistroforelasticsearch.sql.expression.domain.BindingTuple;
import com.amazon.opendistroforelasticsearch.sql.query.planner.physical.PhysicalOperator;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.client.node.NodeClient;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class Planner {
    private final NodeClient client;

    public PhysicalOperator<BindingTuple> plan(LogicalPlan logicalPlan) {
        // select..from..where
        if ((logicalPlan instanceof Project) &&
            (logicalPlan.getInput() instanceof Filter) &&
            (logicalPlan.getInput().getInput() instanceof Relation)) {

            Project project = (Project) logicalPlan;
            Filter filter = (Filter) logicalPlan.getInput();
            Relation relation = (Relation) filter.getInput();

            if (filter.getCondition() instanceof ToDSL) {
                List<String> projectList = project.getProjectList().stream()
                        .filter(e -> e instanceof AttributeReference)
                        .map(v -> v.toString()).collect(
                                Collectors.toList());
                return new PhysicalScroll(
                        new QueryAction(
                                new SearchRequestBuilder(relation.getTableName(),
                                                         ((ToDSL) filter.getCondition()).build(),
                                                         new SourceFilter(projectList)).build(), client));
            }
        }

        throw new IllegalStateException("unsupported plan:" + logicalPlan);
    }
}
