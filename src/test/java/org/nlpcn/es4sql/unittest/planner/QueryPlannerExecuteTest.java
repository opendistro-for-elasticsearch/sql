package org.nlpcn.es4sql.unittest.planner;

import org.elasticsearch.search.SearchHit;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.nlpcn.es4sql.util.MatcherUtils.hit;
import static org.nlpcn.es4sql.util.MatcherUtils.hits;
import static org.nlpcn.es4sql.util.MatcherUtils.hitsInOrder;
import static org.nlpcn.es4sql.util.MatcherUtils.kv;

/**
 * Query planner execution unit test
 */
public class QueryPlannerExecuteTest extends QueryPlannerTest {

    @Test
    public void simpleJoin() {
        assertThat(
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
                    kv("d.name", "AWS"),
                    kv("e.lastname", "Alice")
                ),
                hit(
                    kv("d.name", "AWS"),
                    kv("e.lastname", "Hank")
                )
            )
        );
    }

    @Test
    public void simpleJoinWithSelectAll() {
        assertThat(
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
                    kv("d.name", "AWS"),
                    kv("d.id", "1"),
                    kv("e.lastname", "Alice"),
                    kv("e.departmentId", "1")
                ),
                hit(
                    kv("d.name", "AWS"),
                    kv("d.id", "1"),
                    kv("e.lastname", "Hank"),
                    kv("e.departmentId", "1")
                )
            )
        );
    }

    @Test
    public void simpleLeftJoinWithSelectAllFromOneTable() {
        assertThat(
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
                    kv("e.lastname", "Alice"),
                    kv("d.name", "AWS"),
                    kv("d.id", "1")
                ),
                hit(
                    kv("e.lastname", "Hank"),
                    kv("d.name", "AWS"),
                    kv("d.id", "1")
                ),
                hit(
                    kv("e.lastname", "Allen")
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
        assertThat(
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
                    kv("d.name", "AWS"),
                    kv("d.id", "1"),
                    kv("e.lastname", "Alice"),
                    kv("e.departmentId", "1")
                ),
                hit(
                    kv("d.name", "AWS"),
                    kv("d.id", "1"),
                    kv("e.lastname", "Hank"),
                    kv("e.departmentId", "1")
                )
            )
        );
    }

    @Test
    public void simpleJoinWithoutMatch() {
        assertThat(
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
        assertThat(
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
                    kv("d.name", "Retail"),
                    kv("e.lastname", "Alice")
                )
            )
        );
    }

    @Test
    public void simpleJoinWithAllMatches() {
        assertThat(
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
                    kv("d.name", "AWS"),
                    kv("e.lastname", "Alice")
                ),
                hit(
                    kv("d.name", "AWS"),
                    kv("e.lastname", "Hank")
                ),
                hit(
                    kv("d.name", "Retail"),
                    kv("e.lastname", "Mike")
                )
            )
        );
    }

    @Test
    public void simpleJoinWithNull() {
        assertThat(
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
                    kv("d.name", "AWS"),
                    kv("e.lastname", "Alice")
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

        assertThat(
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
                    kv("d.name", "AWS"),
                    kv("e.name", "Alice Alice")
                )
            )
        );
    }

    @Test
    public void simpleJoinWithAliasInSelect() {
        assertThat(
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
                    kv("dname", "Retail"),
                    kv("ename", "Alice")
                )
            )
        );
    }

    @Test
    public void simpleLeftJoinWithoutMatchInLeft() {
        assertThat(
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
                    kv("d.name", null),
                    kv("e.lastname", "Alice")
                ),
                hit(
                    kv("d.name", null),
                    kv("e.lastname", "Hank")
                )
            )
        );
    }

    @Test
    public void simpleLeftJoinWithSomeMismatchesInLeft() {
        assertThat(
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
                    kv("d.name", "AWS"),
                    kv("e.lastname", "Alice")
                ),
                hit(
                    kv("d.name", null),
                    kv("e.lastname", "Hank")
                )
            )
        );
    }

    @Test
    public void simpleLeftJoinWithSomeMismatchesInRight() {
        assertThat(
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
                    kv("d.name", "AWS"),
                    kv("e.lastname", "Alice")
                ),
                hit(
                    kv("d.name", "AWS"),
                    kv("e.lastname", "Hank")
                )
            )
        );
    }

    @Test
    public void simpleQueryWithTotalLimit() {
        assertThat(
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
                    kv("d.name", "AWS"),
                    kv("e.lastname", "Alice")
                )
            )
        );
    }

    @Test
    public void simpleQueryWithTableLimit() {
        assertThat(
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
                    kv("d.name", "AWS"),
                    kv("e.lastname", "Alice")
                )
            )
        );
    }

    @Test
    public void simpleQueryWithOrderBy() {
        assertThat(
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
            hitsInOrder(
                hit(
                    kv("d.name", "Retail"),
                    kv("e.lastname", "Alice")
                ),
                hit(
                    kv("d.name", "AWS"),
                    kv("e.lastname", "Allen")
                ),
                hit(
                    kv("d.name", "Retail"),
                    kv("e.lastname", "Ellis")
                ),
                hit(
                    kv("d.name", "Retail"),
                    kv("e.lastname", "Frank")
                ),
                hit(
                    kv("d.name", "AWS"),
                    kv("e.lastname", "Hank")
                )
            )
        );
    }

    /** Doesn't support muliple columns from both tables (order is missing) */
    @Test
    public void simpleQueryWithLeftJoinAndOrderByMultipleColumnsFromOneTableInDesc() {
        assertThat(
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
            hitsInOrder(
                hit(
                    kv("id", null),
                    kv("lastname", "Gary")
                ),
                hit(
                    kv("id", null),
                    kv("lastname", "Frank")
                ),
                hit(
                    kv("id", "2"),
                    kv("lastname", "Ellis")
                ),
                hit(
                    kv("id", "2"),
                    kv("lastname", "Alice")
                ),
                hit(
                    kv("id", "1"),
                    kv("lastname", "Hank")
                ),
                hit(
                    kv("id", "1"),
                    kv("lastname", "Allen")
                )
            )
        );
    }

    @Test
    public void simpleCrossJoin() {
        assertThat(
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
                    kv("dname", "AWS"),
                    kv("ename", "Alice")
                ),
                hit(
                    kv("dname", "AWS"),
                    kv("ename", "Hank")
                ),
                hit(
                    kv("dname", "Retail"),
                    kv("ename", "Alice")
                ),
                hit(
                    kv("dname", "Retail"),
                    kv("ename", "Hank")
                )
            )
        );
    }

    @Test
    public void simpleQueryWithTermsFilterOptimization() {
        assertThat(
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
                    kv("e.lastname", "Johnson"),
                    kv("d.id", "1")
                ),
                hit(
                    kv("e.lastname", "Dell"),
                    kv("d.id", "4")
                )
            )
        );
    }

    @Test
    public void complexJoinWithMultipleConditions() {
        assertThat(
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
                    kv("d.name", "Dell"),
                    kv("e.lastname", "Dell"),
                    kv("d.id", "1")
                )
            )
        );
    }

    @Test
    public void complexJoinWithOrConditions() {
        assertThat(
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
                    kv("d.name", "Dell"),
                    kv("e.lastname", "Alice")
                ),
                hit(
                    kv("d.name", "Dell"),
                    kv("e.lastname", "Dell")
                )
            )
        );
    }

    @Test
    public void complexJoinWithOrConditionsDuplicate() {
        assertThat(
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
                    kv("d.name", "Dell"),
                    kv("e.departmentId", "1")
                )
            )
        );
    }

    @Test
    public void complexJoinWithOrConditionsAndTermsFilterOptimization() {
        assertThat(
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
                    kv("d.name", "Dell"),
                    kv("e.lastname", "Alice")
                ),
                hit(
                    kv("d.name", "Dell"),
                    kv("e.lastname", "Dell")
                )
            )
        );
    }

    @Test
    public void complexLeftJoinWithOrConditions() {
        assertThat(
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
                    kv("d.name", "Dell"),
                    kv("e.lastname", "Alice")
                ),
                hit(
                    kv("d.name", "Dell"),
                    kv("e.lastname", "Dell")
                ),
                hit(
                    kv("d.name", null),
                    kv("e.lastname", "Hank")
                )
            )
        );
    }

    @Test
    public void complexJoinWithTableLimitHint() {
        assertThat(
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
                    kv("d.name", "Dell"),
                    kv("e.lastname", "Alice")
                )
            )
        );
    }

}
