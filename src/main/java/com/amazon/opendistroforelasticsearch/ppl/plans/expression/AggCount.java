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

import com.amazon.opendistroforelasticsearch.ppl.node.AbstractNodeVisitor;
import com.amazon.opendistroforelasticsearch.ppl.node.NodeVisitor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;
import java.util.List;

@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class AggCount extends Expression {
    @Getter
    private final Expression field;

    @Override
    public List<Expression> getChild() {
        return Arrays.asList(field);
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        if (visitor instanceof AbstractNodeVisitor) {
            return ((AbstractNodeVisitor<T>) visitor).visitAggCount(this);
        } else {
            return visitor.visitChildren(this);
        }
    }
}
