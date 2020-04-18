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
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.expression.Array;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.expression.AttributeList;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.expression.EqualTo;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.expression.Function;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.expression.In;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.expression.Literal;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.expression.Map;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.expression.Nest;
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
public class AbstractNodeVisitor<T> implements NodeVisitor<T> {

    public T visitRelation(Relation node) {
        return visitChildren(node);
    }

    public T visitFilter(Filter node) {
        return visitChildren(node);
    }

    public T visitProject(Project node) {
        return visitChildren(node);
    }

    public T visitAggregation(Aggregation node) {
        return visitChildren(node);
    }

    public T visitEqualTo(EqualTo node) {
        return visitChildren(node);
    }

    public T visitLiteral(Literal node) {
        return visitChildren(node);
    }

    public T visitUnresolvedAttribute(UnresolvedAttribute node) {
        return visitChildren(node);
    }

    public T visitUnresolvedAttributeList(AttributeList node) {
        return visitChildren(node);
    }

    public T visitMap(Map node) {
        return visitChildren(node);
    }

    public T visitNot(Not node) {
        return visitChildren(node);
    }

    public T visitOr(Or node) {
        return visitChildren(node);
    }

    public T visitAnd(And node) {
        return visitChildren(node);
    }

    public T visitAggregateFunction(AggregateFunction node) {
        return visitChildren(node);
    }

    public T visitFunction(Function node) {
        return visitChildren(node);
    }

    public T visitIn(In node) {
        return visitChildren(node);
    }

    public T visitNest(Nest node) {
        return visitChildren(node);
    }

    public T visitArray(Array node) {
        return visitChildren(node);
    }

}

