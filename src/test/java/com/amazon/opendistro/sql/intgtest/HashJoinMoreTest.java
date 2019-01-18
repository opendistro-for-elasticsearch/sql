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

package com.amazon.opendistro.sql.intgtest;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.elasticsearch.common.bytes.BytesReference;
import com.amazon.opendistro.sql.executor.join.ElasticUtils;
import com.amazon.opendistro.sql.executor.join.MetaSearchResult;
import org.elasticsearch.search.SearchHits;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

/**
 * More test cases for hash join that should not be parameterized in HashJoinTests.
 *
 * For example, cross join, object field...
 */
@RunWith(Parameterized.class)
public class HashJoinMoreTest extends HashJoinTest {

    private final static MetaSearchResult META_SEARCH_RESULT = new MetaSearchResult();

    /** Parameterized sql query */
    private final String hints;
    private final String selectCols;
    private final String joinType;
    private final String onConds;
    private final String whereConds;
    private final String orderBy;

    public HashJoinMoreTest(String hints,
                            String selectCols,
                            String joinType,
                            String onConds,
                            String whereConds,
                            String orderBy) {
        this.hints = hints;
        this.selectCols = selectCols;
        this.joinType = joinType;
        this.onConds = onConds;
        this.whereConds = whereConds;
        this.orderBy = orderBy;

        // Print on console to update progress
        System.out.println(String.format(
            "Hint=[%s], Select=[%s], Join=[%s], On=[%s], Where=[%s], OrderBy=[%s]",
            hints, selectCols, joinType, onConds, whereConds, orderBy));
    }

    @Parameterized.Parameters(
        name = "{index} hints={0}, SELECT={1}, JOIN={2}, ON={3}, WHERE={4}, ORDER_BY={5}"
    )
    public static Collection<Object[]> paramsForSqlQuery() {
        Set<Object> hints = ImmutableSet.of(
            "",                                   // Default all
            "/*! HASH_WITH_TERMS_FILTER*/",       // Enable term filter optimization
            "/*! JOIN_ALGORITHM_BLOCK_SIZE(5)*/", // Default page size > block size
            "/*! JOIN_ALGORITHM_BLOCK_SIZE(5)*/ /*! JOIN_SCROLL_PAGE_SIZE(2)*/" // Page size < block size
        );
        Set<Object> selectCols = ImmutableSet.of(
            "*",
            "c.*",
            "f.*",
            "c.*, f.*",
            "c.name.firstname, c.name.lastname, f.*",
            //"c.*, f.titles", // array field
            "c.name.firstname, c.name.lastname, f.hname, f.seat"
        );
        Set<Object> joinTypes = ImmutableSet.of(
            "INNER JOIN",
            "LEFT JOIN"
        );
        Set<Object> onConds = ImmutableSet.of(
            "",
            "f.name.firstname = c.parents.father",  // join on object field
            "f.gender = c.gender",
            "f.gender = c.gender AND f.house = c.house", // AND
            "f.gender = c.gender OR f.house = c.house"   // OR
        );
        Set<Object> whereConds = ImmutableSet.of(
            //"",                                     // No where
            "c.gender = 'M'"
        );
        Set<Object> orderBys = ImmutableSet.of(
            ""
            //"hname",
            //"f.parents.mother"
        );

        Set<List<Object>> params = Sets.cartesianProduct(
            hints, selectCols, joinTypes, onConds, whereConds, orderBys);
        return params.stream().map(List::toArray).collect(toList());
    }

    @BeforeClass
    public static void init() {
        META_SEARCH_RESULT.addTotalNumOfShards(10);
        META_SEARCH_RESULT.addSuccessfulShards(10);
        META_SEARCH_RESULT.setTookImMilli(123);
    }

    @Test
    public void join() throws IOException {
        String sql = joinSpace(
            selectCols,
            "FROM", TestsConstants.TEST_INDEX_GAME_OF_THRONES, "c",
            joinType, TestsConstants.TEST_INDEX_GAME_OF_THRONES, "f",
            useIfNotEmpty("ON ", onConds),
            useIfNotEmpty("WHERE", whereConds),
            "LIMIT 1000000" // Avoid partial result returned
        );

        String sqlForOldHashJoin = joinSpace("SELECT", USE_OLD_JOIN_ALGORITHM, sql);
        String sqlForNewHashJoin = joinSpace("SELECT", hints, BYPASS_CIRCUIT_BREAK, sql); // Avoid circuit break
        SearchHits oldHits = query(sqlForOldHashJoin);
        SearchHits newHits = query(sqlForNewHashJoin);
        assertEquals(oldHits, newHits);
        Assert.assertTrue(
            isTwoJsonSimilar(
                ElasticUtils.hitsAsStringResult(newHits, META_SEARCH_RESULT),
                BytesReference.bytes(ElasticUtils.hitsAsStringResultZeroCopy(Arrays.asList(newHits.getHits()), META_SEARCH_RESULT)).utf8ToString()
            )
        );
    }

    private String useIfNotEmpty(String keyword, String clause) {
        return clause.isEmpty() ? "" : keyword + " " + clause;
    }

    private boolean isTwoJsonSimilar(String json1, String json2) {
        return new JSONObject(json1).similar(new JSONObject(json2));
    }

}
