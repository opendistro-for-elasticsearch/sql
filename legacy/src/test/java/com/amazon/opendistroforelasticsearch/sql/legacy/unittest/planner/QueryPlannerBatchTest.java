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

import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.runners.Parameterized.Parameters;
import static com.amazon.opendistroforelasticsearch.sql.legacy.util.MatcherUtils.hit;
import static com.amazon.opendistroforelasticsearch.sql.legacy.util.MatcherUtils.hits;
import static com.amazon.opendistroforelasticsearch.sql.legacy.util.MatcherUtils.kv;

/**
 * Batch prefetch testing. Test against different combination of algorithm block size and scroll page size.
 */
@SuppressWarnings("unchecked")
@RunWith(Parameterized.class)
public class QueryPlannerBatchTest extends QueryPlannerTest {

    private static final String TEST_SQL1 =
        "SELECT " +
        "  /*! JOIN_CIRCUIT_BREAK_LIMIT(100) */ " +
        "  /*! JOIN_ALGORITHM_BLOCK_SIZE(%d) */ " +
        "  /*! JOIN_SCROLL_PAGE_SIZE(%d) */ " +
        "  e.lastname AS name, d.id AS id, d.name AS dep ";

    private static final String TEST_SQL2_JOIN1 =
        "FROM department d " +
        "  %s employee e ";

    private static final String TEST_SQL2_JOIN2 =
        "FROM employee e " +
        "  %s department d ";

    private static final String TEST_SQL3 =
        "ON d.id = e.departmentId " +
        "  WHERE e.age <= 50";

    private SearchHit[] employees = {
        employee(1, "People 1", "A"),
        employee(2, "People 2", "A"),
        employee(3, "People 3", "A"),
        employee(4, "People 4", "B"),
        employee(5, "People 5", "B"),
        employee(6, "People 6", "C"),
        employee(7, "People 7", "D"),
        employee(8, "People 8", "D"),
        employee(9, "People 9", "E"),
        employee(10, "People 10", "F")
    };

    private SearchHit[] departments = {
        department(1, "A", "AWS"),
        department(2, "C", "Capital One"),
        department(3, "D", "Dell"),
        department(4, "F", "Facebook"),
        department(5, "G", "Google"),
        department(6, "M", "Microsoft"),
        department(7, "U", "Uber"),
    };

    private Matcher[] matched = {
        hit(
            kv("name", "People 1"),
            kv("id", "A"),
            kv("dep", "AWS")
        ),
        hit(
            kv("name", "People 2"),
            kv("id", "A"),
            kv("dep", "AWS")
        ),
        hit(
            kv("name", "People 3"),
            kv("id", "A"),
            kv("dep", "AWS")
        ),
        hit(
            kv("name", "People 6"),
            kv("id", "C"),
            kv("dep", "Capital One")
        ),
        hit(
            kv("name", "People 7"),
            kv("id", "D"),
            kv("dep", "Dell")
        ),
        hit(
            kv("name", "People 8"),
            kv("id", "D"),
            kv("dep", "Dell")
        ),
        hit(
            kv("name", "People 10"),
            kv("id", "F"),
            kv("dep", "Facebook")
        )
    };

    private Matcher[] mismatched1 = {
        hit(
            kv("name", null),
            kv("id", "G"),
            kv("dep", "Google")
        ),
        hit(
            kv("name", null),
            kv("id", "M"),
            kv("dep", "Microsoft")
        ),
        hit(
            kv("name", null),
            kv("id", "U"),
            kv("dep", "Uber")
        )
    };

    private Matcher[] mismatched2 = {
        hit(
            kv("name", "People 4"),
            kv("id", null),
            kv("dep", null)
        ),
        hit(
            kv("name", "People 5"),
            kv("id", null),
            kv("dep", null)
        ),
        hit(
            kv("name", "People 9"),
            kv("id", null),
            kv("dep", null)
        )
    };

    private Matcher<SearchHits> expectedInnerJoinResult = hits(matched);

    /** Department left join Employee */
    private Matcher<SearchHits> expectedLeftOuterJoinResult1 = hits(concat(matched, mismatched1));

    /** Employee left join Department */
    private Matcher<SearchHits> expectedLeftOuterJoinResult2 = hits(concat(matched, mismatched2));

    /** Parameterized test cases */
    private final int blockSize;
    private final int pageSize;

    public QueryPlannerBatchTest(int blockSize, int pageSize) {
        this.blockSize = blockSize;
        this.pageSize = pageSize;
    }

    @Parameters
    public static Collection<Object[]> data() {
        List<Object[]> params = new ArrayList<>();
        for (int blockSize = 1; blockSize <= 11; blockSize++) {
            for (int pageSize = 1; pageSize <= 11; pageSize++) {
                params.add(new Object[]{ blockSize, pageSize });
            }
        }
        return params;
    }

    @Test
    public void departmentInnerJoinEmployee() {
        assertThat(
            query(
                String.format(
                    TEST_SQL1 + TEST_SQL2_JOIN1 + TEST_SQL3,
                    blockSize, pageSize, "INNER JOIN"),
                departments(pageSize, departments),
                employees(pageSize, employees)
            ),
            expectedInnerJoinResult
        );
    }

    @Test
    public void employeeInnerJoinDepartment() {
        assertThat(
            query(
                String.format(
                    TEST_SQL1 + TEST_SQL2_JOIN2 + TEST_SQL3,
                    blockSize, pageSize, "INNER JOIN"),
                employees(pageSize, employees),
                departments(pageSize, departments)
            ),
            expectedInnerJoinResult
        );
    }

    @Test
    public void departmentLeftJoinEmployee() {
        assertThat(
            query(
                String.format(
                    TEST_SQL1 + TEST_SQL2_JOIN1 + TEST_SQL3,
                    blockSize, pageSize, "LEFT JOIN"),
                departments(pageSize, departments),
                employees(pageSize, employees)
            ),
            expectedLeftOuterJoinResult1
        );
    }

    @Test
    public void employeeLeftJoinDepartment() {
        assertThat(
            query(
                String.format(
                    TEST_SQL1 + TEST_SQL2_JOIN2 + TEST_SQL3,
                    blockSize, pageSize, "LEFT JOIN"),
                employees(pageSize, employees),
                departments(pageSize, departments)
            ),
            expectedLeftOuterJoinResult2
        );
    }

    private static Matcher[] concat(Matcher[] one, Matcher[] other) {
        return concat(one, other, Matcher.class);
    }

    /** Copy from ES ArrayUtils */
    private static <T> T[] concat(T[] one, T[] other, Class<T> clazz) {
        T[] target = (T[]) Array.newInstance(clazz, one.length + other.length);
        System.arraycopy(one, 0, target, 0, one.length);
        System.arraycopy(other, 0, target, one.length, other.length);
        return target;
    }
}
