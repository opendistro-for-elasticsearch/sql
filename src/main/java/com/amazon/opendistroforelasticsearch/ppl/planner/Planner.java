package com.amazon.opendistroforelasticsearch.ppl.planner;

import com.amazon.opendistroforelasticsearch.ppl.planner.dsl.QueryAction;
import com.amazon.opendistroforelasticsearch.ppl.planner.dsl.SearchRequestBuilder;
import com.amazon.opendistroforelasticsearch.ppl.planner.dsl.SourceFilter;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.AttributeReference;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.visitor.AggregationBuilderVisitor;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.visitor.QueryBuilderVisitor;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.Aggregation;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.Filter;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.LogicalPlan;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.Project;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.Rare;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.Relation;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.Top;
import com.amazon.opendistroforelasticsearch.ppl.plans.physical.PhysicalScroll;
import com.amazon.opendistroforelasticsearch.sql.expression.domain.BindingTuple;
import com.amazon.opendistroforelasticsearch.sql.query.planner.physical.PhysicalOperator;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;

import java.util.List;
import java.util.stream.Collectors;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

@RequiredArgsConstructor
public class Planner {
    private final NodeClient client;

    public PhysicalOperator<BindingTuple> plan(LogicalPlan inputLogicalPlan) {
        // select..from..where..agg
        AggregationBuilder aggBuilder = null;
        LogicalPlan logicalPlan = inputLogicalPlan.getChild().get(0);
        if (inputLogicalPlan instanceof Aggregation) {
            Aggregation agg = (Aggregation) inputLogicalPlan;
            aggBuilder = agg.compile();
        } else if (inputLogicalPlan instanceof Top) {
            Top agg = (Top) inputLogicalPlan;
            aggBuilder = agg.compile();
        } else if (inputLogicalPlan instanceof Rare) {
            Rare agg = (Rare) inputLogicalPlan;
            aggBuilder = agg.compile();
        } else {
            logicalPlan = inputLogicalPlan;
        }

        if ((logicalPlan instanceof Filter) &&
            (logicalPlan.getChild().get(0) instanceof Relation)) {

            Filter filter = (Filter) logicalPlan;
            Relation relation = (Relation) filter.getInput();

            QueryBuilder queryBuilder = new QueryBuilderVisitor().visit(filter.getCondition());
            List<String> projectList = ImmutableList.of();
            return new PhysicalScroll(
                    new QueryAction(
                            new SearchRequestBuilder(relation.getTableName(),
                                                     queryBuilder,
                                                     aggBuilder,
                                                     new SourceFilter(projectList)).build(), client));
        }


        // select..from..where
        if ((logicalPlan instanceof Project) &&
            (logicalPlan.getChild().get(0) instanceof Filter) &&
            (logicalPlan.getChild().get(0).getChild().get(0) instanceof Relation)) {

            Project project = (Project) logicalPlan;
            Filter filter = (Filter) logicalPlan.getChild().get(0);
            Relation relation = (Relation) filter.getInput();

            QueryBuilder queryBuilder = new QueryBuilderVisitor().visit(filter.getCondition());
            List<String> projectList = project.getProjectList().stream()
                        .filter(e -> e instanceof AttributeReference)
                        .map(v -> v.toString()).collect(
                                Collectors.toList());
                return new PhysicalScroll(
                        new QueryAction(
                                new SearchRequestBuilder(relation.getTableName(),
                                                         queryBuilder,
                                                         aggBuilder,
                                                         new SourceFilter(projectList)).build(), client));
        }

        // select..from..
        if ((logicalPlan instanceof Project) && (logicalPlan.getChild().get(0) instanceof Relation)) {

            Project project = (Project) logicalPlan;
            Relation relation = (Relation) project.getInput();

            List<String> projectList = project.getProjectList().stream()
                    .filter(e -> e instanceof AttributeReference)
                    .map(Object::toString)
                    .collect(Collectors.toList());

            return new PhysicalScroll(
                    new QueryAction(
                            new SearchRequestBuilder(
                                    relation.getTableName(),
                                    new QueryBuilderVisitor().defaultResult(),
                                    aggBuilder,
                                    new SourceFilter(projectList)).build(),
                            client
                    ));
        }

        // select * from..
        if (logicalPlan instanceof Relation) {
            return new PhysicalScroll(
                    new QueryAction(
                            new SearchRequestBuilder(
                                    ((Relation) logicalPlan).getTableName(),
                                    new QueryBuilderVisitor().defaultResult(),
                                    aggBuilder,
                                    new SourceFilter(new ArrayList<>())).build(),
                            client
                    ));
        }

        if (logicalPlan instanceof Top) {
            AggregationBuilder aggregationBuilder = new AggregationBuilderVisitor()
                    .visitAggregationBuilder((Top) logicalPlan);
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                    .timeout(new TimeValue(60, TimeUnit.SECONDS))
                    .size(10)
                    .aggregation(aggregationBuilder);
            SearchRequest request = new SearchRequest().source(sourceBuilder);
            return new PhysicalScroll(new QueryAction(request, client));
        }
        throw new IllegalStateException("unsupported plan:" + logicalPlan);
    }
}
