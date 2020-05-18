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

package com.amazon.opendistroforelasticsearch.sql.planner.physical;

import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.ReferenceExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.Aggregator;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Map;

/**
 * Physical Plan DSL.
 */
@UtilityClass
public class PhysicalPlanDSL {

    public static AggregationOperator agg(PhysicalPlan input, List<Aggregator> aggregators, List<Expression> groups) {
        return new AggregationOperator(input, aggregators, groups);
    }

    public static FilterOperator filter(PhysicalPlan input, Expression condition) {
        return new FilterOperator(input, condition);
    }

    public static RenameOperator rename(PhysicalPlan input, Map<ReferenceExpression, ReferenceExpression> renameMap) {
        return new RenameOperator(input, renameMap);
    }
}