/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Function;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.In;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Literal;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Map;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Not;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Or;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.UnresolvedAttribute;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Aggregation;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Filter;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.UnresolvedPlan;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Project;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Relation;
import java.util.Arrays;
import java.util.List;

/**
 * Class of static methods to create specific node instances
 */
public class DSL {

    public static UnresolvedPlan filter(UnresolvedPlan input, Expression expression) {
        return new Filter(expression).attach(input);
    }

    public static UnresolvedPlan relation(String tableName) {
        return new Relation(tableName);
    }

    public static UnresolvedPlan project(UnresolvedPlan input, Expression... projectList) {
        return new Project(Arrays.asList(projectList)).attach(input);
    }

    public static UnresolvedPlan projectWithArg(UnresolvedPlan input, List<Expression> argList, Expression... projectList) {
        return new Project(Arrays.asList(projectList), argList).attach(input);
    }

    public static UnresolvedPlan agg(UnresolvedPlan input, List<Expression> aggList, List<Expression> sortList,
                                     List<Expression> groupList, List<Expression> argList) {
        return new Aggregation(aggList, sortList, groupList, argList).attach(input);
    }

    public static Expression equalTo(Expression left, Expression right) {
        return new EqualTo(left, right);
    }

    public static Expression unresolvedAttr(String attr) {
        return new UnresolvedAttribute(attr);
    }

    private static Expression literal(Object value, DataType type) {
        return new Literal(value, type);
    }

    public static Expression intLiteral(Integer value) {
        return literal(value, DataType.INTEGER);
    }

    public static Expression doubleLiteral(Double value) {
        return literal(value, DataType.DOUBLE);
    }

    public static Expression stringLiteral(String value) {
        return literal(value, DataType.STRING);
    }

    public static Expression booleanLiteral(Boolean value) {
        return literal(value, DataType.BOOLEAN);
    }

    public static Expression map(String origin, String target) {
        return new Map(new UnresolvedAttribute(origin), new UnresolvedAttribute(target));
    }

    public static Expression map(Expression origin, Expression target) {
        return new Map(origin, target);
    }

    public static Expression aggregate(String func, Expression field) {
        return new AggregateFunction(func, field);
    }

    public static Expression aggregate(String func, Expression field, Expression... args) {
        return new AggregateFunction(func, field, Arrays.asList(args));
    }

    public static Expression function(String funcName, Expression... funcArgs) {
        return new Function(funcName, Arrays.asList(funcArgs));
    }

    public static Expression not(Expression expression) {
        return new Not(expression);
    }

    public static Expression or(Expression left, Expression right) {
        return new Or(left, right);
    }

    public static Expression and(Expression left, Expression right) {
        return new And(left, right);
    }

    public static Expression in(Expression field, Expression... valueList) {
        return new In(field, Arrays.asList(valueList));
    }

    public static Expression compare(String operator, Expression left, Expression right) {
        return new Compare(operator, left, right);
    }

    public static Expression argument(String argName, Expression argValue) {
        return new Argument(argName, argValue);
    }

    public static List<Expression> exprList(Expression... exprList) {
        return Arrays.asList(exprList);
    }

    public static List<Expression> defaultFieldsArgs() {
        return exprList(
                argument("exclude", booleanLiteral(false))
        );
    }

    public static List<Expression> defaultStatsArgs() {
        return exprList(
                argument("partitions", intLiteral(1)),
                argument("allnum", booleanLiteral(false)),
                argument("delim", stringLiteral(" ")),
                argument("dedupsplit", booleanLiteral(false))
        );
    }

    public static List<Expression> defaultDedupArgs() {
        return exprList(
                argument("number", intLiteral(1)),
                argument("keepevents", booleanLiteral(false)),
                argument("keepempty", booleanLiteral(false)),
                argument("consecutive", booleanLiteral(false))
        );
    }

    public static List<Expression> defaultSortArgs() {
        return exprList(
                argument("count", intLiteral(1000)),
                argument("desc", booleanLiteral(false))
        );
    }

}
