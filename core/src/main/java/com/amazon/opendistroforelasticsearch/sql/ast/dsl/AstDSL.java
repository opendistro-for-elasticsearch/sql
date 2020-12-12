/*
 *   Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.ast.dsl;

import com.amazon.opendistroforelasticsearch.sql.ast.expression.AggregateFunction;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Alias;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.AllFields;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.And;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Argument;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Case;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Compare;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.DataType;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.EqualTo;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Field;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Function;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.In;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Interval;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Let;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Literal;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Map;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Not;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Or;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.QualifiedName;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.UnresolvedArgument;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.UnresolvedAttribute;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.UnresolvedExpression;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.When;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.WindowFunction;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Xor;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Aggregation;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Dedupe;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Eval;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Filter;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Head;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Limit;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Project;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.RareTopN;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.RareTopN.CommandType;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Relation;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.RelationSubquery;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Rename;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.UnresolvedPlan;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Values;
import java.util.Arrays;
import java.util.List;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Class of static methods to create specific node instances.
 */
@UtilityClass
public class AstDSL {

  public static UnresolvedPlan filter(UnresolvedPlan input, UnresolvedExpression expression) {
    return new Filter(expression).attach(input);
  }

  public UnresolvedPlan relation(String tableName) {
    return new Relation(qualifiedName(tableName));
  }

  public UnresolvedPlan relation(String tableName, String alias) {
    return new Relation(qualifiedName(tableName), alias);
  }

  public static UnresolvedPlan project(UnresolvedPlan input, UnresolvedExpression... projectList) {
    return new Project(Arrays.asList(projectList)).attach(input);
  }

  public static Eval eval(UnresolvedPlan input, Let... projectList) {
    return new Eval(Arrays.asList(projectList)).attach(input);
  }

  public static UnresolvedPlan projectWithArg(
      UnresolvedPlan input, List<Argument> argList, UnresolvedExpression... projectList) {
    return new Project(Arrays.asList(projectList), argList).attach(input);
  }

  public static UnresolvedPlan agg(
      UnresolvedPlan input,
      List<UnresolvedExpression> aggList,
      List<UnresolvedExpression> sortList,
      List<UnresolvedExpression> groupList,
      List<Argument> argList) {
    return new Aggregation(aggList, sortList, groupList, argList).attach(input);
  }

  public static UnresolvedPlan rename(UnresolvedPlan input, Map... maps) {
    return new Rename(Arrays.asList(maps), input);
  }

  /**
   * Initialize Values node by rows of literals.
   * @param values  rows in which each row is a list of literal values
   * @return        Values node
   */
  @SafeVarargs
  public UnresolvedPlan values(List<Literal>... values) {
    return new Values(Arrays.asList(values));
  }

  public static UnresolvedExpression qualifiedName(String... parts) {
    return new QualifiedName(Arrays.asList(parts));
  }

  public static UnresolvedExpression equalTo(
      UnresolvedExpression left, UnresolvedExpression right) {
    return new EqualTo(left, right);
  }

  public static UnresolvedExpression unresolvedAttr(String attr) {
    return new UnresolvedAttribute(attr);
  }

  public static UnresolvedPlan relationSubquery(UnresolvedPlan subquery, String subqueryAlias) {
    return new RelationSubquery(subquery, subqueryAlias);
  }

  private static Literal literal(Object value, DataType type) {
    return new Literal(value, type);
  }

  public static Let let(Field var, UnresolvedExpression expression) {
    return new Let(var, expression);
  }

  public static Literal intLiteral(Integer value) {
    return literal(value, DataType.INTEGER);
  }

  public static Literal dateLiteral(String value) {
    return literal(value, DataType.DATE);
  }

  public static Literal timeLiteral(String value) {
    return literal(value, DataType.TIME);
  }

  public static Literal timestampLiteral(String value) {
    return literal(value, DataType.TIMESTAMP);
  }

  public static Literal doubleLiteral(Double value) {
    return literal(value, DataType.DOUBLE);
  }

  public static Literal stringLiteral(String value) {
    return literal(value, DataType.STRING);
  }

  public static Literal booleanLiteral(Boolean value) {
    return literal(value, DataType.BOOLEAN);
  }

  public static Interval intervalLiteral(Object value, DataType type, String unit) {
    return new Interval(literal(value, type), unit);
  }

  public static Literal nullLiteral() {
    return literal(null, DataType.NULL);
  }

  public static Map map(String origin, String target) {
    return new Map(new Field(origin), new Field(target));
  }

  public static Map map(UnresolvedExpression origin, UnresolvedExpression target) {
    return new Map(origin, target);
  }

  public static UnresolvedExpression aggregate(String func, UnresolvedExpression field) {
    return new AggregateFunction(func, field);
  }

  public static UnresolvedExpression aggregate(
      String func, UnresolvedExpression field, UnresolvedExpression... args) {
    return new AggregateFunction(func, field, Arrays.asList(args));
  }

  public static Function function(String funcName, UnresolvedExpression... funcArgs) {
    return new Function(funcName, Arrays.asList(funcArgs));
  }

  /**
   * CASE
   *     WHEN search_condition THEN result_expr
   *     [WHEN search_condition THEN result_expr] ...
   *     [ELSE result_expr]
   * END
   */
  public UnresolvedExpression caseWhen(UnresolvedExpression elseClause,
                                       When... whenClauses) {
    return caseWhen(null, elseClause, whenClauses);
  }

  /**
   * CASE case_value_expr
   *     WHEN compare_expr THEN result_expr
   *     [WHEN compare_expr THEN result_expr] ...
   *     [ELSE result_expr]
   * END
   */
  public UnresolvedExpression caseWhen(UnresolvedExpression caseValueExpr,
                                       UnresolvedExpression elseClause,
                                       When... whenClauses) {
    return new Case(caseValueExpr, Arrays.asList(whenClauses), elseClause);
  }

  public When when(UnresolvedExpression condition, UnresolvedExpression result) {
    return new When(condition, result);
  }

  public UnresolvedExpression window(Function function,
                                     List<UnresolvedExpression> partitionByList,
                                     List<Pair<String, UnresolvedExpression>> sortList) {
    return new WindowFunction(function, partitionByList, sortList);
  }

  public static UnresolvedExpression not(UnresolvedExpression expression) {
    return new Not(expression);
  }

  public static UnresolvedExpression or(UnresolvedExpression left, UnresolvedExpression right) {
    return new Or(left, right);
  }

  public static UnresolvedExpression and(UnresolvedExpression left, UnresolvedExpression right) {
    return new And(left, right);
  }

  public static UnresolvedExpression xor(UnresolvedExpression left, UnresolvedExpression right) {
    return new Xor(left, right);
  }

  public static UnresolvedExpression in(
      UnresolvedExpression field, UnresolvedExpression... valueList) {
    return new In(field, Arrays.asList(valueList));
  }

  public static UnresolvedExpression compare(
      String operator, UnresolvedExpression left, UnresolvedExpression right) {
    return new Compare(operator, left, right);
  }

  public static Argument argument(String argName, Literal argValue) {
    return new Argument(argName, argValue);
  }

  public static UnresolvedArgument unresolvedArg(String argName, UnresolvedExpression argValue) {
    return new UnresolvedArgument(argName, argValue);
  }

  public AllFields allFields() {
    return AllFields.of();
  }

  public Field field(UnresolvedExpression field) {
    return new Field((QualifiedName) field);
  }

  public Field field(String field) {
    return new Field(field);
  }

  public Field field(UnresolvedExpression field, Argument... fieldArgs) {
    return new Field(field, Arrays.asList(fieldArgs));
  }

  public Field field(String field, Argument... fieldArgs) {
    return new Field(field, Arrays.asList(fieldArgs));
  }

  public Field field(UnresolvedExpression field, List<Argument> fieldArgs) {
    return new Field(field, fieldArgs);
  }

  public Field field(String field, List<Argument> fieldArgs) {
    return new Field(field, fieldArgs);
  }

  public Alias alias(String name, UnresolvedExpression expr) {
    return new Alias(name, expr);
  }

  public Alias alias(String name, UnresolvedExpression expr, String alias) {
    return new Alias(name, expr, alias);
  }

  public static List<UnresolvedExpression> exprList(UnresolvedExpression... exprList) {
    return Arrays.asList(exprList);
  }

  public static List<Argument> exprList(Argument... exprList) {
    return Arrays.asList(exprList);
  }

  public static List<UnresolvedArgument> unresolvedArgList(UnresolvedArgument... exprList) {
    return Arrays.asList(exprList);
  }

  public static List<Argument> defaultFieldsArgs() {
    return exprList(argument("exclude", booleanLiteral(false)));
  }

  /**
   * Default Stats Command Args.
   */
  public static List<Argument> defaultStatsArgs() {
    return exprList(
        argument("partitions", intLiteral(1)),
        argument("allnum", booleanLiteral(false)),
        argument("delim", stringLiteral(" ")),
        argument("dedupsplit", booleanLiteral(false)));
  }

  /**
   * Default Dedup Command Args.
   */
  public static List<Argument> defaultDedupArgs() {
    return exprList(
        argument("number", intLiteral(1)),
        argument("keepempty", booleanLiteral(false)),
        argument("consecutive", booleanLiteral(false)));
  }

  public static List<Argument> sortOptions() {
    return exprList(argument("desc", booleanLiteral(false)));
  }

  public static List<Argument> defaultSortFieldArgs() {
    return exprList(argument("asc", booleanLiteral(true)), argument("type", nullLiteral()));
  }

  public static Sort sort(UnresolvedPlan input, Field... sorts) {
    return new Sort(input, Arrays.asList(sorts));
  }

  public static Dedupe dedupe(UnresolvedPlan input, List<Argument> options, Field... fields) {
    return new Dedupe(input, options, Arrays.asList(fields));
  }

  public static Head head(UnresolvedPlan input, List<UnresolvedArgument> options) {
    return new Head(input, options);
  }

  /**
   * Default Head Command Args.
   */
  public static List<UnresolvedArgument> defaultHeadArgs() {
    return unresolvedArgList(
            unresolvedArg("keeplast", booleanLiteral(true)),
            unresolvedArg("whileExpr", booleanLiteral(true)),
            unresolvedArg("number", intLiteral(10)));
  }

  public static List<Argument> defaultTopArgs() {
    return exprList(argument("noOfResults", intLiteral(10)));
  }

  public static RareTopN rareTopN(UnresolvedPlan input, CommandType commandType,
      List<Argument> noOfResults, List<UnresolvedExpression> groupList, Field... fields) {
    return new RareTopN(input, commandType, noOfResults, Arrays.asList(fields), groupList)
        .attach(input);
  }

  public static Limit limit(UnresolvedPlan input, Integer limit, Integer offset) {
    return new Limit(limit, offset).attach(input);
  }
}
