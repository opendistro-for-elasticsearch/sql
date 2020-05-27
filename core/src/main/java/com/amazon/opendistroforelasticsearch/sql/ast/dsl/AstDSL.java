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
import com.amazon.opendistroforelasticsearch.sql.ast.expression.And;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Argument;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Compare;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.DataType;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.EqualTo;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Field;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Function;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.In;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Let;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Literal;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Map;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Not;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Or;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.QualifiedName;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.UnresolvedAttribute;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.UnresolvedExpression;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Aggregation;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Dedupe;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Eval;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Filter;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Project;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Relation;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Rename;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.UnresolvedPlan;
import java.util.Arrays;
import java.util.List;

/** Class of static methods to create specific node instances */
public class AstDSL {

  public static UnresolvedPlan filter(UnresolvedPlan input, UnresolvedExpression expression) {
    return new Filter(expression).attach(input);
  }

  public static UnresolvedPlan relation(String tableName) {
    return new Relation(qualifiedName(tableName));
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

  private static Literal literal(Object value, DataType type) {
    return new Literal(value, type);
  }

  public static Let let(Field var, UnresolvedExpression expression) {
    return new Let(var, expression);
  }

  public static Literal intLiteral(Integer value) {
    return literal(value, DataType.INTEGER);
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

  public static UnresolvedExpression function(String funcName, UnresolvedExpression... funcArgs) {
    return new Function(funcName, Arrays.asList(funcArgs));
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

  public static UnresolvedExpression field(UnresolvedExpression field) {
    return new Field((QualifiedName) field);
  }

  public static Field field(String field) {
    return new Field(field);
  }

  public static UnresolvedExpression field(UnresolvedExpression field, Argument... fieldArgs) {
    return new Field((QualifiedName) field, Arrays.asList(fieldArgs));
  }

  public static Field field(String field, Argument... fieldArgs) {
    return new Field(field, Arrays.asList(fieldArgs));
  }

  public static UnresolvedExpression field(UnresolvedExpression field, List<Argument> fieldArgs) {
    return new Field((QualifiedName) field, fieldArgs);
  }

  public static Field field(String field, List<Argument> fieldArgs) {
    return new Field(field, fieldArgs);
  }

  public static List<UnresolvedExpression> exprList(UnresolvedExpression... exprList) {
    return Arrays.asList(exprList);
  }

  public static List<Argument> exprList(Argument... exprList) {
    return Arrays.asList(exprList);
  }

  public static List<Argument> defaultFieldsArgs() {
    return exprList(argument("exclude", booleanLiteral(false)));
  }

  public static List<Argument> defaultStatsArgs() {
    return exprList(
        argument("partitions", intLiteral(1)),
        argument("allnum", booleanLiteral(false)),
        argument("delim", stringLiteral(" ")),
        argument("dedupsplit", booleanLiteral(false)));
  }

  public static List<Argument> defaultDedupArgs() {
    return exprList(
        argument("number", intLiteral(1)),
        argument("keepempty", booleanLiteral(false)),
        argument("consecutive", booleanLiteral(false)));
  }

  public static List<Argument> defaultSortOptions() {
    return exprList(argument("count", intLiteral(1000)), argument("desc", booleanLiteral(false)));
  }

  public static List<Argument> sortOptions(int count) {
    return exprList(argument("count", intLiteral(count)), argument("desc", booleanLiteral(false)));
  }

  public static List<Argument> defaultSortFieldArgs() {
    return exprList(argument("asc", booleanLiteral(true)), argument("type", nullLiteral()));
  }

  public static Sort sort(UnresolvedPlan input, List<Argument> options, Field... sorts) {
    return new Sort(input, options, Arrays.asList(sorts));
  }

  public static Dedupe dedupe(UnresolvedPlan input, List<Argument> options, Field... fields) {
    return new Dedupe(input, options, Arrays.asList(fields));
  }
}
