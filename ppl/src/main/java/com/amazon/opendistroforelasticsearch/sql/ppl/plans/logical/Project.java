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

package com.amazon.opendistroforelasticsearch.sql.ppl.plans.logical;

import com.amazon.opendistroforelasticsearch.sql.ppl.node.AbstractNodeVisitor;
import com.amazon.opendistroforelasticsearch.sql.ppl.node.NodeVisitor;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.expression.Expression;
import com.google.common.collect.ImmutableList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Logical plan node of Project, the interface for building the list of searching fields
 */
@ToString
@EqualsAndHashCode
public class Project extends UnresolvedPlan {
    @Getter
    @Setter
    private List<Expression> projectList;
    @Getter
    private UnresolvedPlan input;

    public Project(List<Expression> projectList) {
        this.projectList = projectList;
    }

    @Override
    public Project withInput(UnresolvedPlan input) {
        this.input = input;
        return this;
    }

    @Override
    public List<UnresolvedPlan> getChild() {
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
