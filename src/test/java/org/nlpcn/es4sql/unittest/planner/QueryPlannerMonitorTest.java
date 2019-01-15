package org.nlpcn.es4sql.unittest.planner;

import org.elasticsearch.search.SearchHits;
import org.junit.Test;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.nlpcn.es4sql.query.planner.core.QueryPlanner;
import org.nlpcn.es4sql.query.planner.resource.Stats;
import org.nlpcn.es4sql.query.planner.resource.Stats.MemStats;

import static org.mockito.Mockito.doAnswer;

/**
 * Circuit breaker component test
 */
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
