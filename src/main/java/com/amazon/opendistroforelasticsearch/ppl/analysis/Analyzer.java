package com.amazon.opendistroforelasticsearch.ppl.analysis;

import com.amazon.opendistroforelasticsearch.ppl.node.AbstractNodeVisitor;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.AggCount;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.And;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.AttributeReference;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.EqualTo;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.Expression;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.Literal;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.Or;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.UnresolvedAttribute;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.Aggregation;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.Filter;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.LogicalPlan;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.Project;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.Rare;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.Relation;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.Top;
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
public class Analyzer extends AbstractNodeVisitor<LogicalPlan> {
    private final Context<Type> context;
    private final LocalClusterState clusterState;
    private final int threshold;
    private final AbstractNodeVisitor<Expression> expressionAnalyzer = new AbstractNodeVisitor<Expression>() {
        @Override
        public Expression visitUnresolvedAttribute(UnresolvedAttribute node) {
            Optional<Type> resolve = environment().resolve(new Symbol(Namespace.FIELD_NAME, node.getAttr()));
            if (resolve.isPresent()) {
                return new AttributeReference(node.getAttr());
            } else {
                throw new IllegalArgumentException("can't resolved node: " + node);
            }
        }

        @Override
        public Expression visitLiteral(Literal node) {
            return node;
        }

        @Override
        public Expression visitEqualTo(EqualTo node) {
            return new EqualTo(this.visit(node.getLeft()), this.visit(node.getRight()));
        }

        @Override
        public Expression visitAnd(And node) {
            return new And(this.visit(node.getLeft()), this.visit(node.getRight()));
        }

        @Override
        public Expression visitOr(Or node) {
            return new Or(this.visit(node.getLeft()), this.visit(node.getRight()));
        }

        @Override
        public Expression visitAggCount(AggCount node) {
            return new AggCount(visit(node.getField()));
        }
    };

    public LogicalPlan analyze(LogicalPlan unresolved) {
        unresolved.accept(this);
        return unresolved;
    }

    @Override
    public LogicalPlan visitProject(Project node) {
        node.getChild().forEach(n -> n.accept(this));
        node.setProjectList(node.getProjectList()
                                    .stream()
                                    .map(expressionAnalyzer::visit)
                                    .collect(Collectors.toList()));
        return node;
    }

    @Override
    public LogicalPlan visitFilter(Filter node) {
        node.getChild().forEach(n -> n.accept(this));
        node.setCondition(node.getCondition().accept(expressionAnalyzer));
        return node;
    }

    @Override
    public LogicalPlan visitAggregation(Aggregation node) {
        node.getChild().forEach(n -> n.accept(this));
        node.setAggExprs(node.getAggExprs().stream().map(expressionAnalyzer::visit).collect(Collectors.toList()));
        node.setGroupExprs(node.getGroupExprs().stream().map(expressionAnalyzer::visit).collect(Collectors.toList()));
        return node;
    }

    @Override
    public LogicalPlan visitTop(Top node) {
        node.getChild().forEach(n -> n.accept(this));
        node.setFieldExprs(node.getFieldExprs().stream().map(expressionAnalyzer::visit).collect(Collectors.toList()));
        node.setGroupExprs(node.getGroupExprs().stream().map(expressionAnalyzer::visit).collect(Collectors.toList()));
        return node;
    }

    @Override
    public LogicalPlan visitRare(Rare node) {
        node.getChild().forEach(n -> n.accept(this));
        node.setFieldExprs(node.getFieldExprs().stream().map(expressionAnalyzer::visit).collect(Collectors.toList()));
        node.setGroupExprs(node.getGroupExprs().stream().map(expressionAnalyzer::visit).collect(Collectors.toList()));
        return node;
    }

    @Override
    public LogicalPlan visitRelation(Relation node) {
        node.getChild().forEach(n -> n.accept(this));
        defineIndex(node.getTableName());
        loadAllFieldsWith(node.getTableName());
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
