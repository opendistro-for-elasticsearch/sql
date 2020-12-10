/*
 *   Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.analysis;

import static com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort.NullOrder.NULL_FIRST;
import static com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort.NullOrder.NULL_LAST;
import static com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort.SortOrder.ASC;
import static com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort.SortOrder.DESC;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRUCT;

import com.amazon.opendistroforelasticsearch.sql.analysis.symbol.Namespace;
import com.amazon.opendistroforelasticsearch.sql.analysis.symbol.Symbol;
import com.amazon.opendistroforelasticsearch.sql.ast.AbstractNodeVisitor;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Argument;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Field;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Let;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Literal;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Map;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.UnresolvedArgument;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.UnresolvedExpression;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Aggregation;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Dedupe;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Eval;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Filter;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Head;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Project;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.RareTopN;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Relation;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.RelationSubquery;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Rename;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort.SortOption;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.UnresolvedPlan;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Values;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprMissingValue;
import com.amazon.opendistroforelasticsearch.sql.exception.SemanticCheckException;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.LiteralExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.NamedExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.ReferenceExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.Aggregator;
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.NamedAggregator;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalAggregation;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalDedupe;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalEval;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalFilter;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalHead;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalProject;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalRareTopN;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalRelation;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalRemove;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalRename;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalSort;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalValues;
import com.amazon.opendistroforelasticsearch.sql.storage.StorageEngine;
import com.amazon.opendistroforelasticsearch.sql.storage.Table;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Analyze the {@link UnresolvedPlan} in the {@link AnalysisContext} to construct the {@link
 * LogicalPlan}.
 */
public class Analyzer extends AbstractNodeVisitor<LogicalPlan, AnalysisContext> {

  private final ExpressionAnalyzer expressionAnalyzer;

  private final SelectExpressionAnalyzer selectExpressionAnalyzer;

  private final NamedExpressionAnalyzer namedExpressionAnalyzer;

  private final StorageEngine storageEngine;

  /**
   * Constructor.
   */
  public Analyzer(
      ExpressionAnalyzer expressionAnalyzer,
      StorageEngine storageEngine) {
    this.expressionAnalyzer = expressionAnalyzer;
    this.storageEngine = storageEngine;
    this.selectExpressionAnalyzer = new SelectExpressionAnalyzer(expressionAnalyzer);
    this.namedExpressionAnalyzer = new NamedExpressionAnalyzer(expressionAnalyzer);
  }

  public LogicalPlan analyze(UnresolvedPlan unresolved, AnalysisContext context) {
    return unresolved.accept(this, context);
  }

  @Override
  public LogicalPlan visitRelation(Relation node, AnalysisContext context) {
    context.push();
    TypeEnvironment curEnv = context.peek();
    Table table = storageEngine.getTable(node.getTableName());
    table.getFieldTypes().forEach((k, v) -> curEnv.define(new Symbol(Namespace.FIELD_NAME, k), v));

    // Put index name or its alias in index namespace on type environment so qualifier
    // can be removed when analyzing qualified name. The value (expr type) here doesn't matter.
    curEnv.define(new Symbol(Namespace.INDEX_NAME, node.getTableNameOrAlias()), STRUCT);

    return new LogicalRelation(node.getTableName());
  }

  @Override
  public LogicalPlan visitRelationSubquery(RelationSubquery node, AnalysisContext context) {
    LogicalPlan subquery = analyze(node.getChild().get(0), context);
    // inherit the parent environment to keep the subquery fields in current environment
    TypeEnvironment curEnv = context.peek();

    // Put subquery alias in index namespace so the qualifier can be removed
    // when analyzing qualified name in the subquery layer
    curEnv.define(new Symbol(Namespace.INDEX_NAME, node.getAliasAsTableName()), STRUCT);
    return subquery;
  }

  @Override
  public LogicalPlan visitFilter(Filter node, AnalysisContext context) {
    LogicalPlan child = node.getChild().get(0).accept(this, context);
    Expression condition = expressionAnalyzer.analyze(node.getCondition(), context);

    ExpressionReferenceOptimizer optimizer =
        new ExpressionReferenceOptimizer(expressionAnalyzer.getRepository(), child);
    Expression optimized = optimizer.optimize(condition, context);
    return new LogicalFilter(child, optimized);
  }

  /**
   * Build {@link LogicalRename}.
   */
  @Override
  public LogicalPlan visitRename(Rename node, AnalysisContext context) {
    LogicalPlan child = node.getChild().get(0).accept(this, context);
    ImmutableMap.Builder<ReferenceExpression, ReferenceExpression> renameMapBuilder =
        new ImmutableMap.Builder<>();
    for (Map renameMap : node.getRenameList()) {
      Expression origin = expressionAnalyzer.analyze(renameMap.getOrigin(), context);
      // We should define the new target field in the context instead of analyze it.
      if (renameMap.getTarget() instanceof Field) {
        ReferenceExpression target =
            new ReferenceExpression(((Field) renameMap.getTarget()).getField().toString(),
                origin.type());
        ReferenceExpression originExpr = DSL.ref(origin.toString(), origin.type());
        TypeEnvironment curEnv = context.peek();
        curEnv.remove(originExpr);
        curEnv.define(target);
        renameMapBuilder.put(originExpr, target);
      } else {
        throw new SemanticCheckException(
            String.format("the target expected to be field, but is %s", renameMap.getTarget()));
      }
    }

    return new LogicalRename(child, renameMapBuilder.build());
  }

  /**
   * Build {@link LogicalAggregation}.
   */
  @Override
  public LogicalPlan visitAggregation(Aggregation node, AnalysisContext context) {
    final LogicalPlan child = node.getChild().get(0).accept(this, context);
    ImmutableList.Builder<NamedAggregator> aggregatorBuilder = new ImmutableList.Builder<>();
    for (UnresolvedExpression expr : node.getAggExprList()) {
      NamedExpression aggExpr = namedExpressionAnalyzer.analyze(expr, context);
      aggregatorBuilder
          .add(new NamedAggregator(aggExpr.getName(), (Aggregator) aggExpr.getDelegated()));
    }
    ImmutableList<NamedAggregator> aggregators = aggregatorBuilder.build();

    ImmutableList.Builder<NamedExpression> groupbyBuilder = new ImmutableList.Builder<>();
    for (UnresolvedExpression expr : node.getGroupExprList()) {
      groupbyBuilder.add(namedExpressionAnalyzer.analyze(expr, context));
    }
    ImmutableList<NamedExpression> groupBys = groupbyBuilder.build();

    // new context
    context.push();
    TypeEnvironment newEnv = context.peek();
    aggregators.forEach(aggregator -> newEnv.define(new Symbol(Namespace.FIELD_NAME,
        aggregator.getName()), aggregator.type()));
    groupBys.forEach(group -> newEnv.define(new Symbol(Namespace.FIELD_NAME,
        group.getName()), group.type()));
    return new LogicalAggregation(child, aggregators, groupBys);
  }

  /**
   * Build {@link LogicalRareTopN}.
   */
  @Override
  public LogicalPlan visitRareTopN(RareTopN node, AnalysisContext context) {
    final LogicalPlan child = node.getChild().get(0).accept(this, context);

    ImmutableList.Builder<Expression> groupbyBuilder = new ImmutableList.Builder<>();
    for (UnresolvedExpression expr : node.getGroupExprList()) {
      groupbyBuilder.add(expressionAnalyzer.analyze(expr, context));
    }
    ImmutableList<Expression> groupBys = groupbyBuilder.build();

    ImmutableList.Builder<Expression> fieldsBuilder = new ImmutableList.Builder<>();
    for (Field f : node.getFields()) {
      fieldsBuilder.add(expressionAnalyzer.analyze(f, context));
    }
    ImmutableList<Expression> fields = fieldsBuilder.build();

    // new context
    context.push();
    TypeEnvironment newEnv = context.peek();
    groupBys.forEach(group -> newEnv.define(new Symbol(Namespace.FIELD_NAME,
        group.toString()), group.type()));
    fields.forEach(field -> newEnv.define(new Symbol(Namespace.FIELD_NAME,
        field.toString()), field.type()));

    List<Argument> options = node.getNoOfResults();
    Integer noOfResults = (Integer) options.get(0).getValue().getValue();

    return new LogicalRareTopN(child, node.getCommandType(), noOfResults, fields, groupBys);
  }

  /**
   * Build {@link LogicalProject} or {@link LogicalRemove} from {@link Field}.
   *
   * <p>Todo, the include/exclude fields should change the env definition. The cons of current
   * implementation is even the query contain the field reference which has been excluded from
   * fields command. There is no {@link SemanticCheckException} will be thrown. Instead, the during
   * runtime evaluation, the not exist field will be resolve to {@link ExprMissingValue} which will
   * not impact the correctness.
   *
   * <p>Postpone the implementation when finding more use case.
   */
  @Override
  public LogicalPlan visitProject(Project node, AnalysisContext context) {
    LogicalPlan child = node.getChild().get(0).accept(this, context);

    if (node.hasArgument()) {
      Argument argument = node.getArgExprList().get(0);
      Boolean exclude = (Boolean) argument.getValue().getValue();
      if (exclude) {
        TypeEnvironment curEnv = context.peek();
        List<ReferenceExpression> referenceExpressions =
            node.getProjectList().stream()
                .map(expr -> (ReferenceExpression) expressionAnalyzer.analyze(expr, context))
                .collect(Collectors.toList());
        referenceExpressions.forEach(ref -> curEnv.remove(ref));
        return new LogicalRemove(child, ImmutableSet.copyOf(referenceExpressions));
      }
    }

    // For each unresolved window function, analyze it by "insert" a window and sort operator
    // between project and its child.
    for (UnresolvedExpression expr : node.getProjectList()) {
      WindowExpressionAnalyzer windowAnalyzer =
          new WindowExpressionAnalyzer(expressionAnalyzer, child);
      child = windowAnalyzer.analyze(expr, context);
    }

    List<NamedExpression> namedExpressions =
        selectExpressionAnalyzer.analyze(node.getProjectList(), context,
            new ExpressionReferenceOptimizer(expressionAnalyzer.getRepository(), child));
    // new context
    context.push();
    TypeEnvironment newEnv = context.peek();
    namedExpressions.forEach(expr -> newEnv.define(new Symbol(Namespace.FIELD_NAME,
        expr.getName()), expr.type()));
    return new LogicalProject(child, namedExpressions);
  }

  /**
   * Build {@link LogicalEval}.
   */
  @Override
  public LogicalPlan visitEval(Eval node, AnalysisContext context) {
    LogicalPlan child = node.getChild().get(0).accept(this, context);
    ImmutableList.Builder<Pair<ReferenceExpression, Expression>> expressionsBuilder =
        new Builder<>();
    for (Let let : node.getExpressionList()) {
      Expression expression = expressionAnalyzer.analyze(let.getExpression(), context);
      ReferenceExpression ref = DSL.ref(let.getVar().getField().toString(), expression.type());
      expressionsBuilder.add(ImmutablePair.of(ref, expression));
      TypeEnvironment typeEnvironment = context.peek();
      // define the new reference in type env.
      typeEnvironment.define(ref);
    }
    return new LogicalEval(child, expressionsBuilder.build());
  }

  /**
   * Build {@link LogicalSort}.
   */
  @Override
  public LogicalPlan visitSort(Sort node, AnalysisContext context) {
    LogicalPlan child = node.getChild().get(0).accept(this, context);
    ExpressionReferenceOptimizer optimizer =
        new ExpressionReferenceOptimizer(expressionAnalyzer.getRepository(), child);

    List<Pair<SortOption, Expression>> sortList =
        node.getSortList().stream()
            .map(
                sortField -> {
                  Expression expression = optimizer.optimize(
                      expressionAnalyzer.analyze(sortField.getField(), context), context);
                  return ImmutablePair.of(analyzeSortOption(sortField.getFieldArgs()), expression);
                })
            .collect(Collectors.toList());
    return new LogicalSort(child, sortList);
  }

  /**
   * Build {@link LogicalDedupe}.
   */
  @Override
  public LogicalPlan visitDedupe(Dedupe node, AnalysisContext context) {
    LogicalPlan child = node.getChild().get(0).accept(this, context);
    List<Argument> options = node.getOptions();
    // Todo, refactor the option.
    Integer allowedDuplication = (Integer) options.get(0).getValue().getValue();
    Boolean keepEmpty = (Boolean) options.get(1).getValue().getValue();
    Boolean consecutive = (Boolean) options.get(2).getValue().getValue();

    return new LogicalDedupe(
        child,
        node.getFields().stream()
            .map(f -> expressionAnalyzer.analyze(f, context))
            .collect(Collectors.toList()),
        allowedDuplication,
        keepEmpty,
        consecutive);
  }

  /**
   * Build {@link LogicalHead}.
   */
  public LogicalPlan visitHead(Head node, AnalysisContext context) {
    LogicalPlan child = node.getChild().get(0).accept(this, context);
    List<UnresolvedArgument> options = node.getOptions();
    Boolean keeplast = (Boolean) getOptionAsLiteral(options, 0).getValue();
    Expression whileExpr = expressionAnalyzer.analyze(options.get(1).getValue(), context);
    Integer number = (Integer) getOptionAsLiteral(options, 2).getValue();

    return new LogicalHead(child, keeplast, whileExpr, number);
  }

  private static Literal getOptionAsLiteral(List<UnresolvedArgument> options, int optionIdx) {
    return (Literal) options.get(optionIdx).getValue();
  }

  @Override
  public LogicalPlan visitValues(Values node, AnalysisContext context) {
    List<List<Literal>> values = node.getValues();
    List<List<LiteralExpression>> valueExprs = new ArrayList<>();
    for (List<Literal> value : values) {
      valueExprs.add(value.stream()
          .map(val -> (LiteralExpression) expressionAnalyzer.analyze(val, context))
          .collect(Collectors.toList()));
    }
    return new LogicalValues(valueExprs);
  }

  /**
   * The first argument is always "asc", others are optional.
   * Given nullFirst argument, use its value. Otherwise just use DEFAULT_ASC/DESC.
   */
  private SortOption analyzeSortOption(List<Argument> fieldArgs) {
    Boolean asc = (Boolean) fieldArgs.get(0).getValue().getValue();
    Optional<Argument> nullFirst = fieldArgs.stream()
        .filter(option -> "nullFirst".equals(option.getArgName())).findFirst();

    if (nullFirst.isPresent()) {
      Boolean isNullFirst = (Boolean) nullFirst.get().getValue().getValue();
      return new SortOption((asc ? ASC : DESC), (isNullFirst ? NULL_FIRST : NULL_LAST));
    }
    return asc ? SortOption.DEFAULT_ASC : SortOption.DEFAULT_DESC;
  }

}
