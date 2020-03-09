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

package com.amazon.opendistroforelasticsearch.ppl.plans.expression.visitor;

import com.amazon.opendistroforelasticsearch.ppl.plans.expression.And;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.AttributeReference;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.EqualTo;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.Literal;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.Expression;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

public class QueryBuilderVisitor extends AbstractExprVisitor<QueryBuilder> {
    @Override
    public QueryBuilder visitEqualTo(EqualTo node) {
        Expression left = node.getLeft();
        Expression right = node.getRight();
        if ((left instanceof AttributeReference) &&
            (right instanceof Literal)) {
            return QueryBuilders.termQuery(((AttributeReference) left).getAttr(),
                                           ((Literal) right).getValue());
        } else {
            throw new IllegalStateException("Can't translate to dsl QueryBuilder: " + node);
        }
    }

    @Override
    public QueryBuilder visitAnd(And node) {
        QueryBuilder left = visit(node.getLeft());
        QueryBuilder right = visit(node.getRight());

        return QueryBuilders.boolQuery()
                .must(left)
                .must(right);
    }
}
