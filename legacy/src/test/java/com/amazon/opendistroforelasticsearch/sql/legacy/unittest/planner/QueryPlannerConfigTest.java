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

import com.amazon.opendistroforelasticsearch.sql.legacy.domain.hints.Hint;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.hints.HintFactory;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.hints.HintType;
import com.amazon.opendistroforelasticsearch.sql.legacy.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.HashJoinQueryPlanRequestBuilder;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.core.Config;
import org.hamcrest.Matcher;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.is;
import static com.amazon.opendistroforelasticsearch.sql.legacy.util.MatcherUtils.featureValueOf;

/**
 * Hint & Configuring Ability Test Cases
 */
public class QueryPlannerConfigTest extends QueryPlannerTest {

    private static final Matcher<Integer[]> DEFAULT_TOTAL_AND_TABLE_LIMIT_MATCHER = totalAndTableLimit(200, 0, 0);

    @Test
    public void algorithmBlockSizeHint() {
        assertThat(
            parseHint("! JOIN_ALGORITHM_BLOCK_SIZE(100000)"),
            hint(
                hintType(HintType.JOIN_ALGORITHM_BLOCK_SIZE),
                hintValues(100000)
            )
        );
    }

    @Test
    public void algorithmUseLegacy() {
        assertThat(
            parseHint("! JOIN_ALGORITHM_USE_LEGACY"),
            hint(
                hintType(HintType.JOIN_ALGORITHM_USE_LEGACY),
                hintValues()
            )
        );
    }

    @Test
    public void algorithmBlockSizeHintWithSpaces() {
        assertThat(
            parseHint("! JOIN_ALGORITHM_BLOCK_SIZE ( 200000 ) "),
            hint(
                hintType(HintType.JOIN_ALGORITHM_BLOCK_SIZE),
                hintValues(200000)
            )
        );
    }

    @Test
    public void scrollPageSizeHint() {
        assertThat(
            parseHint("! JOIN_SCROLL_PAGE_SIZE(1000) "),
            hint(
                hintType(HintType.JOIN_SCROLL_PAGE_SIZE),
                hintValues(1000)
            )
        );
    }

    @Test
    public void scrollPageSizeHintWithTwoSizes() {
        assertThat(
            parseHint("! JOIN_SCROLL_PAGE_SIZE(1000, 2000) "),
            hint(
                hintType(HintType.JOIN_SCROLL_PAGE_SIZE),
                hintValues(1000, 2000)
            )
        );
    }

    @Test
    public void circuitBreakLimitHint() {
        assertThat(
            parseHint("! JOIN_CIRCUIT_BREAK_LIMIT(80)"),
            hint(
                hintType(HintType.JOIN_CIRCUIT_BREAK_LIMIT),
                hintValues(80)
            )
        );
    }

    @Test
    public void backOffRetryIntervalsHint() {
        assertThat(
            parseHint("! JOIN_BACK_OFF_RETRY_INTERVALS(1, 5)"),
            hint(
                hintType(HintType.JOIN_BACK_OFF_RETRY_INTERVALS),
                hintValues(1, 5)
            )
        );
    }

    @Test
    public void timeOutHint() {
        assertThat(
            parseHint("! JOIN_TIME_OUT(120)"),
            hint(
                hintType(HintType.JOIN_TIME_OUT),
                hintValues(120)
            )
        );
    }

    @Test
    public void blockSizeConfig() {
        assertThat(queryPlannerConfig(
            "SELECT /*! JOIN_ALGORITHM_BLOCK_SIZE(200000) */ " +
            "  d.name FROM employee e JOIN department d ON d.id = e.departmentId "),
            config(
                blockSize(200000),
                scrollPageSize(Config.DEFAULT_SCROLL_PAGE_SIZE, Config.DEFAULT_SCROLL_PAGE_SIZE),
                circuitBreakLimit(Config.DEFAULT_CIRCUIT_BREAK_LIMIT),
                backOffRetryIntervals(Config.DEFAULT_BACK_OFF_RETRY_INTERVALS),
                DEFAULT_TOTAL_AND_TABLE_LIMIT_MATCHER,
                timeOut(Config.DEFAULT_TIME_OUT)
            )
        );
    }

    @Test
    public void scrollPageSizeConfig() {
        assertThat(queryPlannerConfig(
            "SELECT /*! JOIN_SCROLL_PAGE_SIZE(50, 20) */ " +
            "  d.name FROM employee e JOIN department d ON d.id = e.departmentId "),
            config(
                blockSize(Config.DEFAULT_BLOCK_SIZE),
                scrollPageSize(50, 20),
                circuitBreakLimit(Config.DEFAULT_CIRCUIT_BREAK_LIMIT),
                backOffRetryIntervals(Config.DEFAULT_BACK_OFF_RETRY_INTERVALS),
                DEFAULT_TOTAL_AND_TABLE_LIMIT_MATCHER,
                timeOut(Config.DEFAULT_TIME_OUT)
            )
        );
    }

    @Test
    public void circuitBreakLimitConfig() {
        assertThat(queryPlannerConfig(
            "SELECT /*! JOIN_CIRCUIT_BREAK_LIMIT(60) */ " +
            "  d.name FROM employee e JOIN department d ON d.id = e.departmentId "),
            config(
                blockSize(Config.DEFAULT_BLOCK_SIZE),
                scrollPageSize(Config.DEFAULT_SCROLL_PAGE_SIZE, Config.DEFAULT_SCROLL_PAGE_SIZE),
                circuitBreakLimit(60),
                backOffRetryIntervals(Config.DEFAULT_BACK_OFF_RETRY_INTERVALS),
                DEFAULT_TOTAL_AND_TABLE_LIMIT_MATCHER,
                timeOut(Config.DEFAULT_TIME_OUT)
            )
        );
    }

    @Test
    public void backOffRetryIntervalsConfig() {
        assertThat(queryPlannerConfig(
            "SELECT /*! JOIN_BACK_OFF_RETRY_INTERVALS(1, 3, 5, 10) */ " +
            "  d.name FROM employee e JOIN department d ON d.id = e.departmentId "),
            config(
                blockSize(Config.DEFAULT_BLOCK_SIZE),
                scrollPageSize(Config.DEFAULT_SCROLL_PAGE_SIZE, Config.DEFAULT_SCROLL_PAGE_SIZE),
                circuitBreakLimit(Config.DEFAULT_CIRCUIT_BREAK_LIMIT),
                backOffRetryIntervals(new double[]{1, 3, 5, 10}),
                DEFAULT_TOTAL_AND_TABLE_LIMIT_MATCHER,
                timeOut(Config.DEFAULT_TIME_OUT)
            )
        );
    }

    @Test
    public void totalAndTableLimitConfig() {
        assertThat(queryPlannerConfig(
            "SELECT /*! JOIN_TABLES_LIMIT(10, 20) */ " +
            "  d.name FROM employee e JOIN department d ON d.id = e.departmentId LIMIT 50"),
            config(
                blockSize(Config.DEFAULT_BLOCK_SIZE),
                scrollPageSize(Config.DEFAULT_SCROLL_PAGE_SIZE, Config.DEFAULT_SCROLL_PAGE_SIZE),
                circuitBreakLimit(Config.DEFAULT_CIRCUIT_BREAK_LIMIT),
                backOffRetryIntervals(Config.DEFAULT_BACK_OFF_RETRY_INTERVALS),
                totalAndTableLimit(50, 10, 20),
                timeOut(Config.DEFAULT_TIME_OUT)
            )
        );
    }

    @Test
    public void timeOutConfig() {
        assertThat(queryPlannerConfig(
            "SELECT /*! JOIN_TIME_OUT(120) */ " +
            "  d.name FROM employee e JOIN department d ON d.id = e.departmentId"),
            config(
                blockSize(Config.DEFAULT_BLOCK_SIZE),
                scrollPageSize(Config.DEFAULT_SCROLL_PAGE_SIZE, Config.DEFAULT_SCROLL_PAGE_SIZE),
                circuitBreakLimit(Config.DEFAULT_CIRCUIT_BREAK_LIMIT),
                backOffRetryIntervals(Config.DEFAULT_BACK_OFF_RETRY_INTERVALS),
                DEFAULT_TOTAL_AND_TABLE_LIMIT_MATCHER,
                timeOut(120)
            )
        );
    }

    @Test
    public void multipleConfigCombined() {
        assertThat(queryPlannerConfig(
            "SELECT " +
            "  /*! JOIN_ALGORITHM_BLOCK_SIZE(100) */ " +
            "  /*! JOIN_SCROLL_PAGE_SIZE(50, 20) */ " +
            "  /*! JOIN_CIRCUIT_BREAK_LIMIT(10) */ " +
            "  d.name FROM employee e JOIN department d ON d.id = e.departmentId "),
            config(
                blockSize(100),
                scrollPageSize(50, 20),
                circuitBreakLimit(10),
                backOffRetryIntervals(Config.DEFAULT_BACK_OFF_RETRY_INTERVALS),
                DEFAULT_TOTAL_AND_TABLE_LIMIT_MATCHER,
                timeOut(Config.DEFAULT_TIME_OUT)
            )
        );
    }

    private Hint parseHint(String hintStr) {
        try {
            return HintFactory.getHintFromString(hintStr);
        }
        catch (SqlParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private Config queryPlannerConfig(String sql) {
        HashJoinQueryPlanRequestBuilder request = ((HashJoinQueryPlanRequestBuilder) createRequestBuilder(sql));
        request.plan();
        return request.getConfig();
    }

    private Matcher<Hint> hint(Matcher<HintType> typeMatcher, Matcher<Object[]> valuesMatcher) {
        return both(
            featureValueOf("HintType", typeMatcher, Hint::getType)
        ).and(
            featureValueOf("HintValue", valuesMatcher, Hint::getParams)
        );
    }

    private Matcher<HintType> hintType(HintType type) {
        return is(type);
    }

    private Matcher<Object[]> hintValues(Object... values) {
        if (values.length == 0) {
            return emptyArray();
        }
        return arrayContaining(values);
    }

    private Matcher<Config> config(Matcher<Integer> blockSizeMatcher,
                                   Matcher<Integer[]> scrollPageSizeMatcher,
                                   Matcher<Integer> circuitBreakLimitMatcher,
                                   Matcher<double[]> backOffRetryIntervalsMatcher,
                                   Matcher<Integer[]> totalAndTableLimitMatcher,
                                   Matcher<Integer> timeOutMatcher) {
        return allOf(
            featureValueOf("Block size", blockSizeMatcher, (cfg -> cfg.blockSize().size())),
            featureValueOf("Scroll page size", scrollPageSizeMatcher, Config::scrollPageSize),
            featureValueOf("Circuit break limit", circuitBreakLimitMatcher, Config::circuitBreakLimit),
            featureValueOf("Back off retry intervals", backOffRetryIntervalsMatcher, Config::backOffRetryIntervals),
            featureValueOf("Total and table limit", totalAndTableLimitMatcher,
                (cfg -> new Integer[]{cfg.totalLimit(), cfg.tableLimit1(), cfg.tableLimit2()})),
            featureValueOf("Time out", timeOutMatcher, Config::timeout)
        );
    }

    private Matcher<Integer> blockSize(int size) {
        return is(size);
    }

    @SuppressWarnings("unchecked")
    private Matcher<Integer[]> scrollPageSize(int size1, int size2) {
        return arrayContaining(is(size1), is(size2));
    }

    private Matcher<Integer> circuitBreakLimit(int limit) {
        return is(limit);
    }

    private Matcher<double[]> backOffRetryIntervals(double[] intervals) {
        return is(intervals);
    }

    @SuppressWarnings("unchecked")
    private static Matcher<Integer[]> totalAndTableLimit(int totalLimit, int tableLimit1, int tableLimit2) {
        return arrayContaining(is(totalLimit), is(tableLimit1), is(tableLimit2));
    }

    private static Matcher<Integer> timeOut(int timeout) {
        return is(timeout);
    }

}
