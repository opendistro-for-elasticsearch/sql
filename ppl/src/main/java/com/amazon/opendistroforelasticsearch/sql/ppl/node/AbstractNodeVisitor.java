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

package com.amazon.opendistroforelasticsearch.sql.ppl.node;

import com.amazon.opendistroforelasticsearch.sql.ppl.plans.expression.AggregateFunction;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.expression.And;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.expression.Argument;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.expression.AttributeList;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.expression.Compare;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.expression.EqualTo;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.expression.Function;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.expression.In;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.expression.Literal;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.expression.Map;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.expression.Not;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.expression.Or;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.expression.UnresolvedAttribute;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.logical.Aggregation;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.logical.Filter;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.logical.Project;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.logical.Relation;

/**
 * AST nodes visitor
 * Defines the traverse path
 */
public abstract class AbstractNodeVisitor<T, C> {

    public T visit(Node node, C context) {
        return null;
    }

    public T visitChildren(Node node, C context) {
        T result = defaultResult();

        for (Node child : node.getChild()) {
            T childResult = child.accept(this, context);
            result = aggregateResult(result, childResult);
        }
        return result;
    }

    private T defaultResult() {
        return null;
    }

    private T aggregateResult(T aggregate, T nextResult) {
        return nextResult;
    }

    public T visitRelation(Relation node, C context) {
        return visitChildren(node, context);
    }

    public T visitFilter(Filter node, C context) {
        return visitChildren(node, context);
    }

    public T visitProject(Project node, C context) {
        return visitChildren(node, context);
    }

    public T visitAggregation(Aggregation node, C context) {
        return visitChildren(node, context);
    }

    public T visitEqualTo(EqualTo node, C context) {
        return visitChildren(node, context);
    }

    public T visitLiteral(Literal node, C context) {
        return visitChildren(node, context);
    }

    public T visitUnresolvedAttribute(UnresolvedAttribute node, C context) {
        return visitChildren(node, context);
    }

    public T visitAttributeList(AttributeList node, C context) {
        return visitChildren(node, context);
    }

    public T visitMap(Map node, C context) {
        return visitChildren(node, context);
    }

    public T visitNot(Not node, C context) {
        return visitChildren(node, context);
    }

    public T visitOr(Or node, C context) {
        return visitChildren(node, context);
    }

    public T visitAnd(And node, C context) {
        return visitChildren(node, context);
    }

    public T visitAggregateFunction(AggregateFunction node, C context) {
        return visitChildren(node, context);
    }

    public T visitFunction(Function node, C context) {
        return visitChildren(node, context);
    }

    public T visitIn(In node, C context) {
        return visitChildren(node, context);
    }

    public T visitArgument(Argument node, C context) {
        return visitChildren(node, context);
    }

    public T visitCompare(Compare node, C context) {
        return visitChildren(node, context);
    }

}

