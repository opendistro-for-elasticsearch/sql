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

package com.amazon.opendistroforelasticsearch.ppl.plans.logical;

import com.amazon.opendistroforelasticsearch.ppl.node.AbstractNodeVisitor;
import com.amazon.opendistroforelasticsearch.ppl.node.NodeVisitor;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.Expression;
import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@ToString
@EqualsAndHashCode
public class Project extends LogicalPlan {
    @Getter
    @Setter
    private List<Expression> projectList;
    @Getter
    private LogicalPlan input;

    public Project(List<Expression> projectList) {
        this.projectList = projectList;
    }

    @Override
    public Project withInput(LogicalPlan input) {
        this.input = input;
        return this;
    }

    @Override
    public List<LogicalPlan> getChild() {
        return ImmutableList.of(input);
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        if (visitor instanceof AbstractNodeVisitor) {
            return ((AbstractNodeVisitor<T>) visitor).visitProject(this);
        } else {
            return visitor.visitChildren(this);
        }
    }
}
