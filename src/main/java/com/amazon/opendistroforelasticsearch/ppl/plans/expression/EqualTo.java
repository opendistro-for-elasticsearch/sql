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

package com.amazon.opendistroforelasticsearch.ppl.plans.expression;

import com.amazon.opendistroforelasticsearch.ppl.plans.logical.Expression;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.Visitor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class EqualTo extends Expression implements ToDSL {
    private Expression left;
    private Expression right;

    @Override
    public Expression bottomUp(Visitor<Expression> visitor) {
        left = left.bottomUp(visitor);
        right = right.bottomUp(visitor);
        return visitor.visit(this);
    }

    @Override
    public QueryBuilder build() {
        if ((left instanceof AttributeReference) &&
            (right instanceof Literal)) {
            return QueryBuilders.termQuery(((AttributeReference) left).getAttr(),
                                           ((Literal) right).getValue());
        } else {
            throw new IllegalStateException("can translate to dsl");
        }
    }
}
