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
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.logical.builder.UnresolvedPlanBuilder;
import com.google.common.collect.ImmutableList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Logical plan node of Project, the interface for building the list of searching fields
 */
@ToString
@Getter
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
public class Project extends UnresolvedPlan {
    @Setter
    private List<Expression> projectList;
    private UnresolvedPlan child;

    public Project(List<Expression> projectList) {
        this.projectList = projectList;
    }

    @Override
    public List<UnresolvedPlan> getChild() {
        return ImmutableList.of(this.child);
    }

    @Override
    public <T, C> T accept(AbstractNodeVisitor<T, C> nodeVisitor, C context) {
        this.child = (UnresolvedPlan) context;
        return nodeVisitor.visitProject(this, context);
    }

    private Project(Builder builder) {
        this.child = builder.child;
    }

    /**
     * Project plan builder
     */
    public static class Builder extends UnresolvedPlanBuilder<Project> {
        private UnresolvedPlan child;

        public Builder(Project plan) {
            super(plan);
        }

        @Override
        public UnresolvedPlanBuilder attachPlan(UnresolvedPlan attach) {
            this.child = attach;
            return this;
        }

        @Override
        public Project build() {
            return new Project(this);
        }
    }
}
