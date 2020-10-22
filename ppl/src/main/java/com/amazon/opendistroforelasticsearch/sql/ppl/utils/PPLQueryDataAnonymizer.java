/*
 *
 *    Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License").
 *    You may not use this file except in compliance with the License.
 *    A copy of the License is located at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    or in the "license" file accompanying this file. This file is distributed
 *    on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *    express or implied. See the License for the specific language governing
 *    permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.sql.ppl.utils;

import com.amazon.opendistroforelasticsearch.sql.ast.AbstractNodeVisitor;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.AggregateFunction;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Alias;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.And;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Argument;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Compare;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Field;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Function;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Interval;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Let;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Literal;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Map;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Not;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Or;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.UnresolvedArgument;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.UnresolvedExpression;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Xor;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Aggregation;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Dedupe;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Eval;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Filter;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Head;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Project;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.RareTopN;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Relation;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Rename;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.UnresolvedPlan;
import com.amazon.opendistroforelasticsearch.sql.common.utils.StringUtils;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalAggregation;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalDedupe;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalEval;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalHead;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalProject;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalRareTopN;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalRemove;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalRename;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalSort;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Utility class to mask sensitive information in incoming PPL queries.
 */
public class PPLQueryDataAnonymizer extends AbstractNodeVisitor<String, String> {

  private static final String MASK_LITERAL = "***";

  private final AnonymizerExpressionAnalyzer expressionAnalyzer;

  public PPLQueryDataAnonymizer() {
    this.expressionAnalyzer = new AnonymizerExpressionAnalyzer();
  }

  /**
   * This method is used to anonymize sensitive data in PPL query.
   * Sensitive data includes user data.,
   *
   * @return ppl query string with all user data replace with "***"
   */
  public String anonymizeData(UnresolvedPlan plan) {
    return plan.accept(this, null);
  }

  @Override
  public String visitRelation(Relation node, String context) {
    return StringUtils.format("source=%s", node.getTableName());
  }

  @Override
  public String visitFilter(Filter node, String context) {
    String child = node.getChild().get(0).accept(this, context);
    String condition = visitExpression(node.getCondition());
    return StringUtils.format("%s | where %s", child, condition);
  }

  /**
   * Build {@link LogicalRename}.
   */
  @Override
  public String visitRename(Rename node, String context) {
    String child = node.getChild().get(0).accept(this, context);
    ImmutableMap.Builder<String, String> renameMapBuilder = new ImmutableMap.Builder<>();
    for (Map renameMap : node.getRenameList()) {
      renameMapBuilder.put(visitExpression(renameMap.getOrigin()),
          ((Field) renameMap.getTarget()).getField().toString());
    }
    String renames =
        renameMapBuilder.build().entrySet().stream().map(entry -> StringUtils.format("%s as %s",
            entry.getKey(), entry.getValue())).collect(Collectors.joining(","));
    return StringUtils.format("%s | rename %s", child, renames);
  }

  /**
   * Build {@link LogicalAggregation}.
   */
  @Override
  public String visitAggregation(Aggregation node, String context) {
    String child = node.getChild().get(0).accept(this, context);
    final String group = visitExpressionList(node.getGroupExprList());
    return StringUtils.format("%s | stats %s", child,
        String.join(" ", visitExpressionList(node.getAggExprList()), groupBy(group)).trim());
  }

  /**
   * Build {@link LogicalRareTopN}.
   */
  @Override
  public String visitRareTopN(RareTopN node, String context) {
    final String child = node.getChild().get(0).accept(this, context);
    List<Argument> options = node.getNoOfResults();
    Integer noOfResults = (Integer) options.get(0).getValue().getValue();
    String fields = visitFieldList(node.getFields());
    String group = visitExpressionList(node.getGroupExprList());
    return StringUtils.format("%s | %s %d %s", child,
        node.getCommandType().name().toLowerCase(),
        noOfResults,
        String.join(" ", fields, groupBy(group)).trim()
    );
  }

  /**
   * Build {@link LogicalProject} or {@link LogicalRemove} from {@link Field}.
   */
  @Override
  public String visitProject(Project node, String context) {
    String child = node.getChild().get(0).accept(this, context);
    String arg = "+";
    String fields = visitExpressionList(node.getProjectList());

    if (node.hasArgument()) {
      Argument argument = node.getArgExprList().get(0);
      Boolean exclude = (Boolean) argument.getValue().getValue();
      if (exclude) {
        arg = "-";
      }
    }
    return StringUtils.format("%s | fields %s %s", child, arg, fields);
  }

  /**
   * Build {@link LogicalEval}.
   */
  @Override
  public String visitEval(Eval node, String context) {
    String child = node.getChild().get(0).accept(this, context);
    ImmutableList.Builder<Pair<String, String>> expressionsBuilder = new ImmutableList.Builder<>();
    for (Let let : node.getExpressionList()) {
      String expression = visitExpression(let.getExpression());
      String target = let.getVar().getField().toString();
      expressionsBuilder.add(ImmutablePair.of(target, expression));
    }
    String expressions = expressionsBuilder.build().stream().map(pair -> StringUtils.format("%s"
        + "=%s", pair.getLeft(), pair.getRight())).collect(Collectors.joining(" "));
    return StringUtils.format("%s | eval %s", child, expressions);
  }

  /**
   * Build {@link LogicalSort}.
   */
  @Override
  public String visitSort(Sort node, String context) {
    String child = node.getChild().get(0).accept(this, context);
    // the first options is {"count": "integer"}
    Integer count = (Integer) node.getOptions().get(0).getValue().getValue();
    String sortList = visitFieldList(node.getSortList());
    return StringUtils.format("%s | sort %d %s", child, count, sortList);
  }

  /**
   * Build {@link LogicalDedupe}.
   */
  @Override
  public String visitDedupe(Dedupe node, String context) {
    String child = node.getChild().get(0).accept(this, context);
    String fields = visitFieldList(node.getFields());
    List<Argument> options = node.getOptions();
    Integer allowedDuplication = (Integer) options.get(0).getValue().getValue();
    Boolean keepEmpty = (Boolean) options.get(1).getValue().getValue();
    Boolean consecutive = (Boolean) options.get(2).getValue().getValue();

    return StringUtils
        .format("%s | dedup %s %d keepempty=%b consecutive=%b", child, fields, allowedDuplication,
            keepEmpty,
            consecutive);
  }

  /**
   * Build {@link LogicalHead}.
   */
  @Override
  public String visitHead(Head node, String context) {
    String child = node.getChild().get(0).accept(this, context);
    List<UnresolvedArgument> options = node.getOptions();
    Boolean keeplast = (Boolean) ((Literal) options.get(0).getValue()).getValue();
    String whileExpr = visitExpression(options.get(1).getValue());
    Integer number = (Integer) ((Literal) options.get(2).getValue()).getValue();

    return StringUtils.format("%s | head keeplast=%b while(%s) %d", child, keeplast, whileExpr,
        number);
  }

  private String visitFieldList(List<Field> fieldList) {
    return fieldList.stream().map(this::visitExpression).collect(Collectors.joining(","));
  }

  private String visitExpressionList(List<UnresolvedExpression> expressionList) {
    return expressionList.isEmpty() ? "" :
        expressionList.stream().map(this::visitExpression).collect(Collectors.joining(","));
  }

  private String visitExpression(UnresolvedExpression expression) {
    return expressionAnalyzer.analyze(expression, null);
  }

  private String groupBy(String groupBy) {
    return Strings.isNullOrEmpty(groupBy) ? "" : StringUtils.format("by %s", groupBy);
  }

  /**
   * Expression Anonymizer.
   */
  private static class AnonymizerExpressionAnalyzer extends AbstractNodeVisitor<String,
      String> {

    public String analyze(UnresolvedExpression unresolved, String context) {
      return unresolved.accept(this, context);
    }

    @Override
    public String visitLiteral(Literal node, String context) {
      return MASK_LITERAL;
    }

    @Override
    public String visitInterval(Interval node, String context) {
      String value = node.getValue().accept(this, context);
      String unit = node.getUnit().name();
      return StringUtils.format("INTERVAL %s %s", value, unit);
    }

    @Override
    public String visitAnd(And node, String context) {
      String left = node.getLeft().accept(this, context);
      String right = node.getRight().accept(this, context);
      return StringUtils.format("%s and %s", left, right);
    }

    @Override
    public String visitOr(Or node, String context) {
      String left = node.getLeft().accept(this, context);
      String right = node.getRight().accept(this, context);
      return StringUtils.format("%s or %s", left, right);
    }

    @Override
    public String visitXor(Xor node, String context) {
      String left = node.getLeft().accept(this, context);
      String right = node.getRight().accept(this, context);
      return StringUtils.format("%s xor %s", left, right);
    }

    @Override
    public String visitNot(Not node, String context) {
      String expr = node.getExpression().accept(this, context);
      return StringUtils.format("not %s", expr);
    }

    @Override
    public String visitAggregateFunction(AggregateFunction node, String context) {
      String arg = node.getField().accept(this, context);
      return StringUtils.format("%s(%s)", node.getFuncName(), arg);
    }

    @Override
    public String visitFunction(Function node, String context) {
      String arguments =
          node.getFuncArgs().stream()
              .map(unresolvedExpression -> analyze(unresolvedExpression, context))
              .collect(Collectors.joining(","));
      return StringUtils.format("%s(%s)", node.getFuncName(), arguments);
    }

    @Override
    public String visitCompare(Compare node, String context) {
      String left = analyze(node.getLeft(), context);
      String right = analyze(node.getRight(), context);
      return StringUtils.format("%s %s %s", left, node.getOperator(), right);
    }

    @Override
    public String visitField(Field node, String context) {
      return node.getField().toString();
    }

    @Override
    public String visitAlias(Alias node, String context) {
      String expr = node.getDelegated().accept(this, context);
      return StringUtils.format("%s", expr);
    }
  }
}
