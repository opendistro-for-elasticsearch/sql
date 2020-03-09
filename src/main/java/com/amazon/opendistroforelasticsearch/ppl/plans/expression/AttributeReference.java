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

import com.amazon.opendistroforelasticsearch.ppl.plans.expression.visitor.ExprVisitor;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.Expression;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.Visitor;
import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class AttributeReference extends Expression {
    @Getter
    private final String attr;

    @Override
    public Expression bottomUp(Visitor<Expression> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return attr;
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitChildren(this);
    }

    @Override
    public List<Expression> getChild() {
        return ImmutableList.of();
    }
}
