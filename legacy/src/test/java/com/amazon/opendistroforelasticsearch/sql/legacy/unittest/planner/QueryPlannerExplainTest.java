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

package com.amazon.opendistroforelasticsearch.sql.legacy.unittest.planner;

import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.core.QueryPlanner;
import org.junit.Test;

/**
 * Query planner explanation unit test
 */
public class QueryPlannerExplainTest extends QueryPlannerTest {

    @Test
    public void explainInJson() {
        QueryPlanner planner = plan(
            "SELECT d.name, e.lastname FROM employee e " +
            "  JOIN department d ON d.id = e.departmentId " +
            "    WHERE d.region = 'US' AND e.age > 30"
        );
        planner.explain();
    }

    @Test
    public void explainInJsonWithComplicatedOn() {
        QueryPlanner planner = plan(
            "SELECT d.name, e.lastname FROM employee e " +
            "  JOIN department d ON d.id = e.departmentId AND d.location = e.region " +
            "    WHERE d.region = 'US' AND e.age > 30"
        );
        planner.explain();
    }

    @Test
    public void explainInJsonWithDuplicateColumnsPushedDown() {
        QueryPlanner planner = plan(
            "SELECT d.id, e.departmentId FROM employee e " +
            "  JOIN department d ON d.id = e.departmentId AND d.location = e.region " +
            "    WHERE d.region = 'US' AND e.age > 30"
        );
        planner.explain();
    }

}
