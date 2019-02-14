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

package com.amazon.opendistroforelasticsearch.sql.intgtest;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;


/**
 * Test new hash join algorithm by comparison with old implementation.
 */
@RunWith(Parameterized.class)
public class HashJoinBasicTest extends HashJoinTest {

    /** Parameterized sql query */
    private final String hints;
    private final String selectCols;
    private final String joinType;
    private final String onConds;
    private final String whereConds;

    public HashJoinBasicTest(String hints,
                             String selectCols,
                             String joinType,
                             String onConds,
                             String whereConds) {
        this.hints = hints;
        this.selectCols = selectCols;
        this.joinType = joinType;
        this.onConds = onConds;
        this.whereConds = whereConds;

        // Print on console to update progress
        System.out.println(String.format(
            "Hint=[%s], Select=[%s], Join=[%s], On=[%s], Where=[%s]",
            hints, selectCols, joinType, onConds, whereConds));
    }

    /** Parameterize test cases that really matter to avoid #cases explosion */
    @Parameters(
        name = "{index} hints={0}, SELECT={1}, JOIN={2}, ON={3}, WHERE={4}"
    )
    public static Collection<Object[]> paramsForSqlQuery() {
        Set<Object> hints = ImmutableSet.of(
            ""                                       // Default all
            //"/*! HASH_WITH_TERMS_FILTER*/",        // Enable term filter optimization
            //"/*! JOIN_ALGORITHM_BLOCK_SIZE(600)*/",// Default page size > block size
            //"/*! JOIN_ALGORITHM_BLOCK_SIZE(2000)*/ /*! JOIN_SCROLL_PAGE_SIZE(600)*/" // Page size < block size
        );
        Set<Object> selectCols = ImmutableSet.of(
            //"*",
            //"a.*",
            //"b.*",
            //"a.*, b.*",
            //"a.firstname, a.lastname, b.*",
            //"a.*, b.city",
            "a.firstname, a.lastname, b.city, b.state"
        );
        Set<Object> joinTypes = ImmutableSet.of(
            "INNER JOIN",
            "LEFT JOIN"
        );
        Set<Object> onConds = ImmutableSet.of(
            //"b.firstname = a.hello",                // join on null (null should be skipped)
            //"b.state = a.state",                    // text
            "b.age = a.age"                           // number
            //"b.state = a.state AND b.age = a.age"   // AND
            //"b.state = a.state OR b.age = a.age"    // OR
        );
        Set<Object> whereConds = ImmutableSet.of(
            //"",                                     // No where
            //"a.age > 10000",                        // No rows after filter
            "a.balance > 10000 AND b.age > 20"
        );

        Set<List<Object>> params = Sets.cartesianProduct(hints, selectCols, joinTypes, onConds, whereConds);
        return params.stream().map(List::toArray).collect(toList());
    }

    @Test
    public void join() {
        String sql = joinSpace(
            selectCols,
            "FROM", TestsConstants.TEST_INDEX_ACCOUNT, "a",
            joinType, TestsConstants.TEST_INDEX_ACCOUNT, "b",
            useIfNotEmpty("ON ", onConds),
            useIfNotEmpty("WHERE", whereConds),
            "LIMIT 1000000" // Avoid partial result returned
        );

        String sqlForOldHashJoin = joinSpace("SELECT", USE_OLD_JOIN_ALGORITHM, sql);
        String sqlForNewHashJoin = joinSpace("SELECT", hints, BYPASS_CIRCUIT_BREAK, sql); // Avoid circuit break
        assertEquals(
            query(sqlForOldHashJoin), query(sqlForNewHashJoin)
        );
    }

    private String useIfNotEmpty(String keyword, String clause) {
        return clause.isEmpty() ? "" : keyword + " " + clause;
    }

}
