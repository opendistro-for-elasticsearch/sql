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

package com.amazon.opendistroforelasticsearch.sql.planner.logical;

import com.amazon.opendistroforelasticsearch.sql.expression.ReferenceExpression;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Rename Operator.
 * renameList is list of mapping of source and target.
 */
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class LogicalRename extends LogicalPlan {
    private final LogicalPlan child;
    @Getter
    private final Map<ReferenceExpression, ReferenceExpression> renameMap;

    @Override
    public List<LogicalPlan> getChild() {
        return Collections.singletonList(child);
    }

    @Override
    public <R, C> R accept(LogicalPlanNodeVisitor<R, C> visitor, C context) {
        return visitor.visitRename(this, context);
    }
}
