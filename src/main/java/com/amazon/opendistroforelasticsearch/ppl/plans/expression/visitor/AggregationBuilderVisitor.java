package com.amazon.opendistroforelasticsearch.ppl.plans.expression.visitor;

import com.amazon.opendistroforelasticsearch.ppl.plans.expression.UnresolvedAttribute;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.AttributeList;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.Expression;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.Project;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.Top;
import java.util.List;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.TopHitsAggregationBuilder;

public class AggregationBuilderVisitor extends AbstractExprVisitor<AggregationBuilder> {

    public AggregationBuilder visitAggregationBuilder(Top node) {
//        String[] byClauses = ((AttributeList) node.getByClause()).getAttrList().stream()
//                .map(s -> ((UnresolvedAttribute) s).getAttr()).toArray(String[]::new);
//
//
//        return AggregationBuilders.terms("top_sites")
//                .field(String.join(",", byClauses))
//                .size((Integer) node.getCount().getValue())
//                .subAggregation(visitTopHitsAggregationBuilder(node));
        return null;
    }

    public AggregationBuilder visitTopHitsAggregationBuilder(Top node) {
//        TopHitsAggregationBuilder topBuilder = AggregationBuilders.topHits("top_tags");
//        List<Expression> projectList = ((Project) node.getFieldList()).getProjectList();
//        String[] includes = projectList.stream().map(s -> ((UnresolvedAttribute) s).getAttr()).toArray(String[]::new);
//        topBuilder.fetchSource(includes, null);
//        return topBuilder;
        return null;
    }
}