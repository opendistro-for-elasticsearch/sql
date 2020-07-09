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

package com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.logical.rule;

import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Field;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.logical.LogicalPlanVisitor;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.logical.node.Group;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.logical.node.Join;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.logical.node.Project;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.logical.node.Join.JoinCondition;
import static java.util.stream.Collectors.toList;


/**
 * Projection push down optimization.
 */
public class ProjectionPushDown implements LogicalPlanVisitor {

    /**
     * Project used to collect column names in SELECT, ON, ORDER BY...
     */
    private final Project<?> project = new Project(null);

    @Override
    public boolean visit(Project project) {
        pushDown(project);
        return true;
    }

    @Override
    public boolean visit(Join join) {
        pushDown(join.conditions());
        return true;
    }

    @Override
    public boolean visit(Group group) {
        if (!project.isNoOp()) {
            group.pushDown(project);
        }
        return false; // avoid iterating operators in virtual Group
    }

    /**
     * Note that raw type Project cause generic type of forEach be erased at compile time
     */
    private void pushDown(Project<?> project) {
        project.forEach(this::project);
    }

    private void pushDown(JoinCondition orCond) {
        for (int i = 0; i < orCond.groupSize(); i++) {
            project(
                    orCond.leftTableAlias(),
                    columnNamesToFields(orCond.leftColumnNames(i))
            );
            project(
                    orCond.rightTableAlias(),
                    columnNamesToFields(orCond.rightColumnNames(i))
            );
        }
    }

    private void project(String tableAlias, Collection<Field> columns) {
        project.project(tableAlias, columns); // Bug: Field doesn't implement hashCode() which leads to duplicate
    }

    /**
     * Convert column name string to Field object with empty alias
     */
    private List<Field> columnNamesToFields(String[] colNames) {
        return Arrays.stream(colNames).
                map(name -> new Field(name, null)). // Alias is useless for pushed down project
                collect(toList());
    }

}
