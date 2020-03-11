/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazon.opendistroforelasticsearch.ppl.plans.dsl;


import com.amazon.opendistroforelasticsearch.ppl.plans.expression.AggCount;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.And;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.AttributeReference;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.DataType;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.EqualTo;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.Literal;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.Or;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.UnresolvedAttribute;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.Aggregation;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.Expression;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.Filter;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.LogicalPlan;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.Project;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.Relation;

import java.util.Arrays;
import java.util.List;

public class DSL {
    public static LogicalPlan project(LogicalPlan input, Expression... projectList) {
        return new Project(Arrays.asList(projectList)).withInput(input);
    }

    public static LogicalPlan filter(LogicalPlan input, Expression expression) {
        return new Filter(expression).withInput(input);
    }

    public static LogicalPlan relation(String tableName) {
        return new Relation(tableName);
    }

    public static LogicalPlan agg(LogicalPlan input, List<Expression> groupList, List<Expression> aggList) {
        return new Aggregation(aggList, groupList).withInput(input);
    }

    public static Expression equalTo(Expression left, Expression right) {
        return new EqualTo(left, right);
    }

    public static Expression unresolvedAttr(String attr) {
        return new UnresolvedAttribute(attr);
    }

    public static Expression attr(String attr) {
        return new AttributeReference(attr);
    }

    public static Expression intLiteral(Integer literal) {
        return new Literal(literal, DataType.INTEGER);
    }

    public static Expression stringLiteral(String literal) {
        return new Literal(literal, DataType.STRING);
    }

    public static Expression and(Expression e1, Expression e2) {
        return new And(e1, e2);
    }

    public static Expression or(Expression e1, Expression e2) {
        return new Or(e1, e2);
    }

    public static Expression count(Expression e1) {
        return new AggCount(e1);
    }
}
