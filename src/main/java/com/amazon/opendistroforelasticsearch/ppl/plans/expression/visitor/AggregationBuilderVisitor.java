package com.amazon.opendistroforelasticsearch.ppl.plans.expression.visitor;

import com.amazon.opendistroforelasticsearch.ppl.node.AbstractNodeVisitor;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.Top;
import org.elasticsearch.search.aggregations.AggregationBuilder;

public class AggregationBuilderVisitor extends AbstractNodeVisitor<AggregationBuilder> {

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