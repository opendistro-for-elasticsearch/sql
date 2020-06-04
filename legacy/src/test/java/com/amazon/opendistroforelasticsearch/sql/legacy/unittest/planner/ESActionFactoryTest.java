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

package com.amazon.opendistroforelasticsearch.sql.legacy.unittest.planner;

import com.amazon.opendistroforelasticsearch.sql.legacy.executor.Format;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.ESActionFactory;
import com.amazon.opendistroforelasticsearch.sql.legacy.util.SqlParserUtils;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ESActionFactoryTest {
    @Test
    public void josnOutputRequestShouldNotMigrateToQueryPlan() {
        String sql = "SELECT age, MAX(balance) " +
                     "FROM account " +
                     "GROUP BY age";

        assertFalse(ESActionFactory.shouldMigrateToQueryPlan(SqlParserUtils.parse(sql), Format.JSON));
    }

    @Test
    public void nestQueryShouldNotMigrateToQueryPlan() {
        String sql = "SELECT age, nested(balance) " +
                     "FROM account " +
                     "GROUP BY age";

        assertFalse(ESActionFactory.shouldMigrateToQueryPlan(SqlParserUtils.parse(sql), Format.JDBC));
    }

    @Test
    public void nonAggregationQueryShouldNotMigrateToQueryPlan() {
        String sql = "SELECT age " +
                     "FROM account ";

        assertFalse(ESActionFactory.shouldMigrateToQueryPlan(SqlParserUtils.parse(sql), Format.JDBC));
    }

    @Test
    public void aggregationQueryWithoutGroupByShouldMigrateToQueryPlan() {
        String sql = "SELECT age, COUNT(balance) " +
                     "FROM account ";

        assertTrue(ESActionFactory.shouldMigrateToQueryPlan(SqlParserUtils.parse(sql), Format.JDBC));
    }

    @Test
    public void aggregationQueryWithExpressionByShouldMigrateToQueryPlan() {
        String sql = "SELECT age, MAX(balance) - MIN(balance) " +
                     "FROM account ";

        assertTrue(ESActionFactory.shouldMigrateToQueryPlan(SqlParserUtils.parse(sql), Format.JDBC));
    }

    @Test
    public void queryOnlyHasGroupByShouldMigrateToQueryPlan() {
        String sql = "SELECT CAST(age AS DOUBLE) as alias " +
                     "FROM account " +
                     "GROUP BY alias";

        assertTrue(ESActionFactory.shouldMigrateToQueryPlan(SqlParserUtils.parse(sql), Format.JDBC));
    }
}
