package com.amazon.opendistroforelasticsearch.ppl.analysis;

import com.amazon.opendistroforelasticsearch.ppl.plans.expression.And;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.AttributeReference;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.EqualTo;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.Literal;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.UnresolvedAttribute;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.Expression;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.ExpressionVisitor;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.Filter;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.LogicalPlan;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.LogicalPlanVisitor;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.Project;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.Relation;
import com.amazon.opendistroforelasticsearch.ppl.spec.scope.Context;
import com.amazon.opendistroforelasticsearch.ppl.spec.scope.Environment;
import com.amazon.opendistroforelasticsearch.ppl.spec.scope.Namespace;
import com.amazon.opendistroforelasticsearch.ppl.spec.scope.Symbol;
import com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.Type;
import com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.base.ESDataType;
import com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.base.ESIndex;
import com.amazon.opendistroforelasticsearch.sql.antlr.visitor.EarlyExitAnalysisException;
import com.amazon.opendistroforelasticsearch.sql.esdomain.LocalClusterState;
import com.amazon.opendistroforelasticsearch.sql.esdomain.mapping.FieldMappings;
import com.amazon.opendistroforelasticsearch.sql.esdomain.mapping.IndexMappings;
import com.amazon.opendistroforelasticsearch.sql.utils.StringUtils;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.stream.Collectors;

import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.base.ESIndex.IndexType.INDEX;

@RequiredArgsConstructor
public class Analyzer implements LogicalPlanVisitor {
    private final Context<Type> context;
    private final LocalClusterState clusterState;
    private final int threshold;
    private final ExpressionVisitor expressionAnalyzer = new ExpressionVisitor() {
        @Override
        public Expression visitUnresolvedAttribute(UnresolvedAttribute node) {
            Optional<Type> resolve = environment().resolve(new Symbol(Namespace.FIELD_NAME, node.getAttr()));
            if (resolve.isPresent()) {
                return new AttributeReference(node.getAttr());
            } else {
                throw new IllegalArgumentException("can resolved node: " + node);
            }
        }
    };

    public LogicalPlan analyze(LogicalPlan unresolved) {
        unresolved.bottomUp(this);
        return unresolved;
    }

    @Override
    public LogicalPlan visitProject(Project node) {
        node.setProjectList(node.getProjectList()
                                    .stream()
                                    .map(expressionAnalyzer::visit)
                                    .collect(Collectors.toList()));
        return node;
    }

    @Override
    public LogicalPlan visitFilter(Filter node) {
        node.setCondition(node.getCondition().bottomUp(expressionAnalyzer));
        return node;
    }

    @Override
    public LogicalPlan visitRelation(Relation node) {
        defineIndex(node.getTableName());
        return node;
    }

    private void defineIndex(String indexName) {
        environment().define(new Symbol(Namespace.FIELD_NAME, indexName), new ESIndex(indexName, INDEX));
    }

    private void loadAllFieldsWith(String indexName) {
        FieldMappings mappings = getFieldMappings(indexName);
        mappings.flat(this::defineFieldName);
    }

    private Environment<Type> environment() {
        return context.peek();
    }

    private void defineFieldName(String fieldName, String type) {
        Symbol symbol = new Symbol(Namespace.FIELD_NAME, fieldName);
        if (!environment().resolve(symbol).isPresent()) {
            environment().define(symbol, ESDataType.typeOf(type));
        }
    }

    private FieldMappings getFieldMappings(String indexName) {
        IndexMappings indexMappings = clusterState.getFieldMappings(new String[]{indexName});
        FieldMappings fieldMappings = indexMappings.firstMapping().firstMapping();

        int size = fieldMappings.data().size();
        if (size > threshold) {
            throw new EarlyExitAnalysisException(StringUtils.format(
                    "Index [%s] has [%d] fields more than threshold [%d]", indexName, size, threshold));
        }
        return fieldMappings;
    }
}
