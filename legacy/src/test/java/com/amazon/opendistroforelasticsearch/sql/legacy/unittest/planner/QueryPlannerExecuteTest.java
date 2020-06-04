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

import com.amazon.opendistroforelasticsearch.sql.legacy.util.MatcherUtils;
import org.elasticsearch.search.SearchHit;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

import static com.amazon.opendistroforelasticsearch.sql.legacy.util.MatcherUtils.hit;
import static com.amazon.opendistroforelasticsearch.sql.legacy.util.MatcherUtils.hits;

/**
 * Query planner execution unit test
 */
public class QueryPlannerExecuteTest extends QueryPlannerTest {

    @Test
    public void simpleJoin() {
        MatcherAssert.assertThat(
            query(
                "SELECT d.name, e.lastname FROM employee e " +
                "  JOIN department d ON d.id = e.departmentId " +
                "    WHERE d.region = 'US' AND e.age > 30",
                employees(
                    employee(1, "Alice", "1"),
                    employee(2, "Hank", "1")
                ),
                departments(
                    department(1, "1", "AWS")
                )
            ),
            hits(
                hit(
                    MatcherUtils.kv("d.name", "AWS"),
                    MatcherUtils.kv("e.lastname", "Alice")
                ),
                hit(
                    MatcherUtils.kv("d.name", "AWS"),
                    MatcherUtils.kv("e.lastname", "Hank")
                )
            )
        );
    }

    @Test
    public void simpleJoinWithSelectAll() {
        MatcherAssert.assertThat(
            query(
                "SELECT * FROM employee e " +
                "  JOIN department d ON d.id = e.departmentId ",
                employees(
                    employee(1, "Alice", "1"),
                    employee(2, "Hank", "1")
                ),
                departments(
                    department(1, "1", "AWS")
                )
            ),
            hits(
                hit(
                    MatcherUtils.kv("d.name", "AWS"),
                    MatcherUtils.kv("d.id", "1"),
                    MatcherUtils.kv("e.lastname", "Alice"),
                    MatcherUtils.kv("e.departmentId", "1")
                ),
                hit(
                    MatcherUtils.kv("d.name", "AWS"),
                    MatcherUtils.kv("d.id", "1"),
                    MatcherUtils.kv("e.lastname", "Hank"),
                    MatcherUtils.kv("e.departmentId", "1")
                )
            )
        );
    }

    @Test
    public void simpleLeftJoinWithSelectAllFromOneTable() {
        MatcherAssert.assertThat(
            query(
                "SELECT e.lastname, d.* FROM employee e " +
                "  LEFT JOIN department d ON d.id = e.departmentId ",
                employees(
                    employee(1, "Alice", "1"),
                    employee(2, "Hank", "1"),
                    employee(3, "Allen", "3")
                ),
                departments(
                    department(1, "1", "AWS"),
                    department(2, "2", "Retail")
                )
            ),
            hits(
                hit(
                    MatcherUtils.kv("e.lastname", "Alice"),
                    MatcherUtils.kv("d.name", "AWS"),
                    MatcherUtils.kv("d.id", "1")
                ),
                hit(
                    MatcherUtils.kv("e.lastname", "Hank"),
                    MatcherUtils.kv("d.name", "AWS"),
                    MatcherUtils.kv("d.id", "1")
                ),
                hit(
                    MatcherUtils.kv("e.lastname", "Allen")
                    /*
                     * Not easy to figure out all column names for d.* without reading metadata
                     * or look into other rows from d. But in the extreme case, d could be empty table
                     * which requires metadata read anyway.
                     */
                    //kv("d.name", null),
                    //kv("d.id", null)
                )
            )
        );
    }

    @Test
    public void simpleJoinWithSelectAllFromBothTables() {
        MatcherAssert.assertThat(
            query(
                "SELECT e.*, d.* FROM employee e " +
                "  JOIN department d ON d.id = e.departmentId ",
                employees(
                    employee(1, "Alice", "1"),
                    employee(2, "Hank", "1")
                ),
                departments(
                    department(1, "1", "AWS")
                )
            ),
            hits(
                hit(
                    MatcherUtils.kv("d.name", "AWS"),
                    MatcherUtils.kv("d.id", "1"),
                    MatcherUtils.kv("e.lastname", "Alice"),
                    MatcherUtils.kv("e.departmentId", "1")
                ),
                hit(
                    MatcherUtils.kv("d.name", "AWS"),
                    MatcherUtils.kv("d.id", "1"),
                    MatcherUtils.kv("e.lastname", "Hank"),
                    MatcherUtils.kv("e.departmentId", "1")
                )
            )
        );
    }

    @Test
    public void simpleJoinWithoutMatch() {
        MatcherAssert.assertThat(
            query(
                "SELECT d.name, e.lastname FROM employee e " +
                "  JOIN department d ON d.id = e.departmentId " +
                "    WHERE d.region = 'US' AND e.age > 30",
                employees(
                    employee(1, "Alice", "2"),
                    employee(2, "Hank", "3")
                ),
                departments(
                    department(1, "1", "AWS")
                )
            ),
            hits()
        );
    }

    @Test
    public void simpleJoinWithSomeMatches() {
        MatcherAssert.assertThat(
            query(
                "SELECT d.name, e.lastname FROM employee e " +
                "  JOIN department d ON d.id = e.departmentId " +
                "    WHERE d.region = 'US' AND e.age > 30",
                employees(
                    employee(1, "Alice", "2"),
                    employee(2, "Hank", "3")
                ),
                departments(
                    department(1, "1", "AWS"),
                    department(2, "2", "Retail")
                )
            ),
            hits(
                hit(
                    MatcherUtils.kv("d.name", "Retail"),
                    MatcherUtils.kv("e.lastname", "Alice")
                )
            )
        );
    }

    @Test
    public void simpleJoinWithAllMatches() {
        MatcherAssert.assertThat(
            query(
                "SELECT d.name, e.lastname FROM employee e " +
                "  JOIN department d ON d.id = e.departmentId " +
                "    WHERE d.region = 'US' AND e.age > 30",
                employees(
                    employee(1, "Alice", "1"),
                    employee(2, "Hank", "1"),
                    employee(3, "Mike", "2")
                ),
                departments(
                    department(1, "1", "AWS"),
                    department(2, "2", "Retail")
                )
            ),
            hits(
                hit(
                    MatcherUtils.kv("d.name", "AWS"),
                    MatcherUtils.kv("e.lastname", "Alice")
                ),
                hit(
                    MatcherUtils.kv("d.name", "AWS"),
                    MatcherUtils.kv("e.lastname", "Hank")
                ),
                hit(
                    MatcherUtils.kv("d.name", "Retail"),
                    MatcherUtils.kv("e.lastname", "Mike")
                )
            )
        );
    }

    @Test
    public void simpleJoinWithNull() {
        MatcherAssert.assertThat(
            query(
                "SELECT d.name, e.lastname FROM employee e " +
                "  JOIN department d ON d.id = e.departmentId " +
                "    WHERE d.region = 'US' AND e.age > 30",
                employees(
                    employee(1, "Alice", "1"),
                    employee(2, "Hank", null),
                    employee(3, "Mike", "2")
                ),
                departments(
                    department(1, "1", "AWS"),
                    department(2, null, "Retail")
                )
            ),
            hits(
                hit(
                    MatcherUtils.kv("d.name", "AWS"),
                    MatcherUtils.kv("e.lastname", "Alice")
                )
            )
        );
    }

    @Test
    public void simpleJoinWithColumnNameConflict() {
        // Add a same column 'name' as in department on purpose
        SearchHit alice = employee(1, "Alice", "1");
        alice.getSourceAsMap().put("name", "Alice Alice");
        SearchHit hank = employee(2, "Hank", "2");
        hank.getSourceAsMap().put("name", "Hank Hank");

        MatcherAssert.assertThat(
            query(
                "SELECT d.name, e.name FROM employee e " +
                "  JOIN department d ON d.id = e.departmentId " +
                "    WHERE d.region = 'US' AND e.age > 30",
                employees(
                    alice, hank
                ),
                departments(
                    department(1, "1", "AWS")
                )
            ),
            hits(
                hit(
                    MatcherUtils.kv("d.name", "AWS"),
                    MatcherUtils.kv("e.name", "Alice Alice")
                )
            )
        );
    }

    @Test
    public void simpleJoinWithAliasInSelect() {
        MatcherAssert.assertThat(
            query(
                "SELECT d.name AS dname, e.lastname AS ename FROM employee e " +
                "  JOIN department d ON d.id = e.departmentId " +
                "    WHERE d.region = 'US' AND e.age > 30",
                employees(
                    employee(1, "Alice", "2"),
                    employee(2, "Hank", "3")
                ),
                departments(
                    department(1, "1", "AWS"),
                    department(2, "2", "Retail")
                )
            ),
            hits(
                hit(
                    MatcherUtils.kv("dname", "Retail"),
                    MatcherUtils.kv("ename", "Alice")
                )
            )
        );
    }

    @Test
    public void simpleLeftJoinWithoutMatchInLeft() {
        MatcherAssert.assertThat(
            query(
                "SELECT d.name, e.lastname FROM employee e " +
                "  LEFT JOIN department d ON d.id = e.departmentId " +
                "    WHERE d.region = 'US' AND e.age > 30",
                employees(
                    employee(1, "Alice", "2"),
                    employee(2, "Hank", "3")
                ),
                departments(
                    department(1, "1", "AWS")
                )
            ),
            hits(
                hit(
                    MatcherUtils.kv("d.name", null),
                    MatcherUtils.kv("e.lastname", "Alice")
                ),
                hit(
                    MatcherUtils.kv("d.name", null),
                    MatcherUtils.kv("e.lastname", "Hank")
                )
            )
        );
    }

    @Test
    public void simpleLeftJoinWithSomeMismatchesInLeft() {
        MatcherAssert.assertThat(
            query(
                "SELECT d.name, e.lastname FROM employee e " +
                "  LEFT JOIN department d ON d.id = e.departmentId " +
                "    WHERE d.region = 'US' AND e.age > 30",
                employees(
                    employee(1, "Alice", "1"),
                    employee(2, "Hank", "2")
                ),
                departments(
                    department(1, "1", "AWS")
                )
            ),
            hits(
                hit(
                    MatcherUtils.kv("d.name", "AWS"),
                    MatcherUtils.kv("e.lastname", "Alice")
                ),
                hit(
                    MatcherUtils.kv("d.name", null),
                    MatcherUtils.kv("e.lastname", "Hank")
                )
            )
        );
    }

    @Test
    public void simpleLeftJoinWithSomeMismatchesInRight() {
        MatcherAssert.assertThat(
            query(
                "SELECT d.name, e.lastname FROM employee e " +
                "  LEFT JOIN department d ON d.id = e.departmentId " +
                "    WHERE d.region = 'US' AND e.age > 30",
                employees(
                    employee(1, "Alice", "1"),
                    employee(2, "Hank", "1")
                ),
                departments(
                    department(1, "1", "AWS"),
                    department(2, "2", "Retail")
                )
            ),
            hits(
                hit(
                    MatcherUtils.kv("d.name", "AWS"),
                    MatcherUtils.kv("e.lastname", "Alice")
                ),
                hit(
                    MatcherUtils.kv("d.name", "AWS"),
                    MatcherUtils.kv("e.lastname", "Hank")
                )
            )
        );
    }

    @Test
    public void simpleQueryWithTotalLimit() {
        MatcherAssert.assertThat(
            query(
                "SELECT d.name, e.lastname FROM employee e JOIN department d ON d.id = e.departmentId LIMIT 1",
                employees(
                    employee(1, "Alice", "1"),
                    employee(2, "Hank", "2")
                ),
                departments(
                    department(1, "1", "AWS"),
                    department(1, "2", "Retail")
                )
            ),
            hits(
                hit(
                    MatcherUtils.kv("d.name", "AWS"),
                    MatcherUtils.kv("e.lastname", "Alice")
                )
            )
        );
    }

    @Test
    public void simpleQueryWithTableLimit() {
        MatcherAssert.assertThat(
            query(
                "SELECT /*! JOIN_TABLES_LIMIT(1, 5) */ d.name, e.lastname FROM employee e JOIN department d ON d.id = e.departmentId",
                employees(
                    employee(1, "Alice", "1"),
                    employee(2, "Hank", "1")
                ),
                departments(
                    department(1, "1", "AWS"),
                    department(1, "2", "Retail")
                )
            ),
            hits(
                hit(
                    MatcherUtils.kv("d.name", "AWS"),
                    MatcherUtils.kv("e.lastname", "Alice")
                )
            )
        );
    }

    @Test
    public void simpleQueryWithOrderBy() {
        MatcherAssert.assertThat(
            query(
                "SELECT d.name, e.lastname FROM employee e JOIN department d ON d.id = e.departmentId ORDER BY e.lastname",
                employees(
                    employee(1, "Hank", "1"),
                    employee(2, "Alice", "2"),
                    employee(3, "Allen", "1"),
                    employee(4, "Ellis", "2"),
                    employee(5, "Frank", "2")
                ),
                departments(
                    department(1, "1", "AWS"),
                    department(2, "2", "Retail")
                )
            ),
            MatcherUtils.hitsInOrder(
                hit(
                    MatcherUtils.kv("d.name", "Retail"),
                    MatcherUtils.kv("e.lastname", "Alice")
                ),
                hit(
                    MatcherUtils.kv("d.name", "AWS"),
                    MatcherUtils.kv("e.lastname", "Allen")
                ),
                hit(
                    MatcherUtils.kv("d.name", "Retail"),
                    MatcherUtils.kv("e.lastname", "Ellis")
                ),
                hit(
                    MatcherUtils.kv("d.name", "Retail"),
                    MatcherUtils.kv("e.lastname", "Frank")
                ),
                hit(
                    MatcherUtils.kv("d.name", "AWS"),
                    MatcherUtils.kv("e.lastname", "Hank")
                )
            )
        );
    }

    /** Doesn't support muliple columns from both tables (order is missing) */
    @Test
    public void simpleQueryWithLeftJoinAndOrderByMultipleColumnsFromOneTableInDesc() {
        MatcherAssert.assertThat(
            query(
                "SELECT d.id AS id, e.lastname AS lastname FROM employee e " +
                "  LEFT JOIN department d ON d.id = e.departmentId " +
                "    ORDER BY e.departmentId, e.lastname DESC",
                employees(
                    employee(1, "Hank", "1"),
                    employee(2, "Alice", "2"),
                    employee(3, "Allen", "1"),
                    employee(4, "Ellis", "2"),
                    employee(5, "Gary", "3"),
                    employee(5, "Frank", "3")
                ),
                departments(
                    department(1, "1", "AWS"),
                    department(2, "2", "Retail")
                )
            ),
            MatcherUtils.hitsInOrder(
                hit(
                    MatcherUtils.kv("id", null),
                    MatcherUtils.kv("lastname", "Gary")
                ),
                hit(
                    MatcherUtils.kv("id", null),
                    MatcherUtils.kv("lastname", "Frank")
                ),
                hit(
                    MatcherUtils.kv("id", "2"),
                    MatcherUtils.kv("lastname", "Ellis")
                ),
                hit(
                    MatcherUtils.kv("id", "2"),
                    MatcherUtils.kv("lastname", "Alice")
                ),
                hit(
                    MatcherUtils.kv("id", "1"),
                    MatcherUtils.kv("lastname", "Hank")
                ),
                hit(
                    MatcherUtils.kv("id", "1"),
                    MatcherUtils.kv("lastname", "Allen")
                )
            )
        );
    }

    @Test
    public void simpleCrossJoin() {
        MatcherAssert.assertThat(
            query(
                "SELECT d.name AS dname, e.lastname AS ename FROM employee e JOIN department d",
                employees(
                    employee(1, "Alice", "2"),
                    employee(2, "Hank", "3")
                ),
                departments(
                    department(1, "1", "AWS"),
                    department(2, "2", "Retail")
                )
            ),
            hits(
                hit(
                    MatcherUtils.kv("dname", "AWS"),
                    MatcherUtils.kv("ename", "Alice")
                ),
                hit(
                    MatcherUtils.kv("dname", "AWS"),
                    MatcherUtils.kv("ename", "Hank")
                ),
                hit(
                    MatcherUtils.kv("dname", "Retail"),
                    MatcherUtils.kv("ename", "Alice")
                ),
                hit(
                    MatcherUtils.kv("dname", "Retail"),
                    MatcherUtils.kv("ename", "Hank")
                )
            )
        );
    }

    @Test
    public void simpleQueryWithTermsFilterOptimization() {
        MatcherAssert.assertThat(
            query(
                "SELECT /*! HASH_WITH_TERMS_FILTER*/ " + // Be careful that no space between ...FILTER and */
                "  e.lastname, d.id FROM employee e " +
                "    JOIN department d ON d.id = e.departmentId AND d.name = e.lastname",
                employees(
                    employee(1, "Johnson", "1"),
                    employee(2, "Allen", "4"),
                    employee(3, "Ellis", "2"),
                    employee(4, "Dell", "1"),
                    employee(5, "Dell", "4")
                ),
                departments(
                    department(1, "1", "Johnson"),
                    department(1, "4", "Dell")
                )
            ),
            hits(
                hit(
                    MatcherUtils.kv("e.lastname", "Johnson"),
                    MatcherUtils.kv("d.id", "1")
                ),
                hit(
                    MatcherUtils.kv("e.lastname", "Dell"),
                    MatcherUtils.kv("d.id", "4")
                )
            )
        );
    }

    @Test
    public void complexJoinWithMultipleConditions() {
        MatcherAssert.assertThat(
            query(
                "SELECT d.name, e.lastname, d.id " +
                "  FROM employee e " +
                "    JOIN department d " +
                "      ON d.id = e.departmentId AND d.name = e.lastname" +
                "        WHERE d.region = 'US' AND e.age > 30",
                employees(
                    employee(1, "Dell", "1"),
                    employee(2, "Hank", "1")
                ),
                departments(
                    department(1, "1", "Dell")
                )
            ),
            hits(
                hit(
                    MatcherUtils.kv("d.name", "Dell"),
                    MatcherUtils.kv("e.lastname", "Dell"),
                    MatcherUtils.kv("d.id", "1")
                )
            )
        );
    }

    @Test
    public void complexJoinWithOrConditions() {
        MatcherAssert.assertThat(
            query(
                "SELECT d.name, e.lastname " +
                "  FROM employee e " +
                "    JOIN department d " +
                "      ON d.id = e.departmentId OR d.name = e.lastname",
                employees(
                    employee(1, "Alice", "1"),
                    employee(2, "Dell", "2"),
                    employee(3, "Hank", "3")
                ),
                departments(
                    department(1, "1", "Dell"),
                    department(2, "4", "AWS")
                )
            ),
            hits(
                hit(
                    MatcherUtils.kv("d.name", "Dell"),
                    MatcherUtils.kv("e.lastname", "Alice")
                ),
                hit(
                    MatcherUtils.kv("d.name", "Dell"),
                    MatcherUtils.kv("e.lastname", "Dell")
                )
            )
        );
    }

    @Test
    public void complexJoinWithOrConditionsDuplicate() {
        MatcherAssert.assertThat(
            query(
                "SELECT d.name, e.departmentId " +
                "  FROM employee e " +
                "    JOIN department d " +
                "      ON d.id = e.departmentId OR d.name = e.lastname",
                employees(
                    employee(1, "Dell", "1") // Match both condition but should only show once in result
                ),
                departments(
                    department(1, "1", "Dell"),
                    department(2, "4", "AWS")
                )
            ),
            hits(
                hit(
                    MatcherUtils.kv("d.name", "Dell"),
                    MatcherUtils.kv("e.departmentId", "1")
                )
            )
        );
    }

    @Test
    public void complexJoinWithOrConditionsAndTermsFilterOptimization() {
        MatcherAssert.assertThat(
            query(
                "SELECT /*! HASH_WITH_TERMS_FILTER*/ " +
                " d.name, e.lastname " +
                "  FROM employee e " +
                "    JOIN department d " +
                "      ON d.id = e.departmentId OR d.name = e.lastname",
                employees(
                    employee(1, "Alice", "1"),
                    employee(2, "Dell", "2"),
                    employee(3, "Hank", "3")
                ),
                departments(
                    department(1, "1", "Dell"),
                    department(2, "4", "AWS")
                )
            ),
            hits(
                hit(
                    MatcherUtils.kv("d.name", "Dell"),
                    MatcherUtils.kv("e.lastname", "Alice")
                ),
                hit(
                    MatcherUtils.kv("d.name", "Dell"),
                    MatcherUtils.kv("e.lastname", "Dell")
                )
            )
        );
    }

    @Test
    public void complexLeftJoinWithOrConditions() {
        MatcherAssert.assertThat(
            query(
                "SELECT d.name, e.lastname " +
                "  FROM employee e " +
                "    LEFT JOIN department d " +
                "      ON d.id = e.departmentId OR d.name = e.lastname",
                employees(
                    employee(1, "Alice", "1"),
                    employee(2, "Dell", "2"),
                    employee(3, "Hank", "3")
                ),
                departments(
                    department(1, "1", "Dell"),
                    department(2, "4", "AWS")
                )
            ),
            hits(
                hit(
                    MatcherUtils.kv("d.name", "Dell"),
                    MatcherUtils.kv("e.lastname", "Alice")
                ),
                hit(
                    MatcherUtils.kv("d.name", "Dell"),
                    MatcherUtils.kv("e.lastname", "Dell")
                ),
                hit(
                    MatcherUtils.kv("d.name", null),
                    MatcherUtils.kv("e.lastname", "Hank")
                )
            )
        );
    }

    @Test
    public void complexJoinWithTableLimitHint() {
        MatcherAssert.assertThat(
            query(
                "SELECT " +
                " /*! JOIN_TABLES_LIMIT(2, 1)*/" +
                " d.name, e.lastname " +
                "  FROM employee e " +
                "    JOIN department d " +
                "      ON d.id = e.departmentId",
                employees(
                    employee(1, "Alice", "1"),  // Only this and the second row will be pulled out
                    employee(2, "Dell", "4"),
                    employee(3, "Hank", "1")
                ),
                departments(
                    department(1, "1", "Dell"), // Only this row will be pulled out
                    department(2, "4", "AWS")
                )
            ),
            hits(
                hit(
                    MatcherUtils.kv("d.name", "Dell"),
                    MatcherUtils.kv("e.lastname", "Alice")
                )
            )
        );
    }

}
