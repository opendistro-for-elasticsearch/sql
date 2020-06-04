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
import org.elasticsearch.search.SearchHits;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.resource.Stats;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.resource.Stats.MemStats;

import static org.mockito.Mockito.doAnswer;

/**
 * Circuit breaker component test
 */
@Ignore
public class QueryPlannerMonitorTest extends QueryPlannerTest {

    /** Configure back off strategy 1s, 1s and 1s - retry 4 times at most */
    private static final String TEST_SQL1 =
        "SELECT /*! JOIN_BACK_OFF_RETRY_INTERVALS(1, 1, 1) */ " +
        "  /*! JOIN_CIRCUIT_BREAK_LIMIT(";

    private static final String TEST_SQL2 =
        ") */ d.name, e.lastname FROM employee e " +
        "  JOIN department d ON d.id = e.departmentId " +
        "    WHERE d.region = 'US' AND e.age > 30";

    private static final long[] PERCENT_USAGE_15 = freeAndTotalMem(85, 100);
    private static final long[] PERCENT_USAGE_24 = freeAndTotalMem(76, 100);
    private static final long[] PERCENT_USAGE_50 = freeAndTotalMem(50, 100);

    @Spy
    private Stats stats = new Stats(client);

    @Test
    public void reachedLimitAndRecoverAt1stAttempt() {
        mockMemUsage(PERCENT_USAGE_15, PERCENT_USAGE_50, PERCENT_USAGE_24);
        queryWithLimit(25); // TODO: assert if final result set is correct after recovery
    }

    @Test
    public void reachedLimitAndRecoverAt2ndAttempt() {
        mockMemUsage(PERCENT_USAGE_15, PERCENT_USAGE_50, PERCENT_USAGE_50, PERCENT_USAGE_15);
        queryWithLimit(25);
    }

    @Test
    public void reachedLimitAndRecoverAt3rdAttempt() {
        mockMemUsage(PERCENT_USAGE_15, PERCENT_USAGE_50, PERCENT_USAGE_50, PERCENT_USAGE_50, PERCENT_USAGE_15);
        queryWithLimit(25);
    }

    @Test(expected = IllegalStateException.class)
    public void reachedLimitAndFailFinally() {
        mockMemUsage(PERCENT_USAGE_15, PERCENT_USAGE_50);
        queryWithLimit(25);
    }

    @Test(expected = IllegalStateException.class)
    public void reachedLimitAndRejectNewRequest() {
        mockMemUsage(PERCENT_USAGE_50);
        queryWithLimit(25);
    }

    @Test(expected = IllegalStateException.class)
    public void timeOut() {
        query(
            "SELECT /*! JOIN_TIME_OUT(0) */ " +
            "  d.name FROM employee e JOIN department d ON d.id = e.departmentId",
            employees(
                employee(1, "Dell", "1")
            ),
            departments(
                department(1, "1", "Dell")
            )
        );
    }

    private void mockMemUsage(long[]... memUsages) {
        doAnswer(new Answer<MemStats>() {
            private int callCnt = -1;

            @Override
            public MemStats answer(InvocationOnMock invocation) {
                callCnt = Math.min(callCnt + 1, memUsages.length - 1);
                return new MemStats(
                    memUsages[callCnt][0], memUsages[callCnt][1]
                );
            }
        }).when(stats).collectMemStats();
    }

    private static long[] freeAndTotalMem(long free, long total) {
        return new long[]{ free, total };
    }

    private SearchHits queryWithLimit(int limit) {
        return query(
            TEST_SQL1 + limit + TEST_SQL2,
            employees(
                employee(1, "Dell", "1"),
                employee(2, "Hank", "1")
            ),
            departments(
                department(1, "1", "Dell")
            )
        );
    }

    @Override
    protected QueryPlanner plan(String sql) {
        QueryPlanner planner = super.plan(sql);
        planner.setStats(stats);
        return planner;
    }

}
