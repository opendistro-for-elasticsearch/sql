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

package com.amazon.opendistroforelasticsearch.sql.doctest.dql;

import com.amazon.opendistroforelasticsearch.sql.doctest.core.DocTest;
import com.amazon.opendistroforelasticsearch.sql.doctest.core.annotation.DocTestConfig;
import com.amazon.opendistroforelasticsearch.sql.doctest.core.annotation.Section;
import org.junit.Ignore;

@DocTestConfig(template = "dql/complex.rst", testData = {"accounts.json", "employees_nested.json"})
public class ComplexQueryIT extends DocTest {

    @Section(1)
    public void subqueries() {
        section(
            title("Subqueries"),
            description(""),
            example(
                title("IN/EXISTS"),
                description(""),
                post("SELECT * FROM accounts")
            )/*,
            example(
                title("Subqueries in FROM clause"),
                description(""),
                post("SELECT * FROM (SELECT * FROM accounts) AS a")
            )*/
        );
    }

    /*
    @Section(2)
    public void joins() {
        section(
            title("JOINs"),
            description(
                "A ``JOIN`` clause combines columns from one or more indices by using values common to each."
            ),
            example(
                title("Inner Join"),
                description(
                    "Inner join is very commonly used that creates a new result set by combining columns",
                    "of two indices based on the join predicates specified. It iterates both indices and",
                    "compare each row to find all that satisfy the join predicates. Keyword ``JOIN``",
                    "is used and preceded by ``INNER`` keyword optionally. The join predicates is specified",
                    "by ``ON`` clause."
                ),
                post(
                    "SELECT e.id FROM accounts a JOIN employees_nested e ON a.account_number = e.id"
                )
            ),
            example(
                title("Cross/Cartesian Join"),
                description(""),
                post("SELECT * FROM accounts a JOIN employees_nested e")
            ),
            example(
                title("Outer Join"),
                description(
                    "Outer join is used to retain rows from one or both indices although it does not satisfy",
                    "join predicate. For now, only ``LEFT OUTER JOIN`` is supported to retain rows from first index.",
                    "Keyword ``OUTER`` is optional."
                ),
                post("SELECT a.account_number FROM accounts a LEFT JOIN employees_nested e ON a.account_number = e.id")
            )
        );
    }
    */

    @Ignore("Multi-query doesn't work for default format: https://github.com/opendistro-for-elasticsearch/sql/issues/388")
    @Section(3)
    public void setOperations() {
        section(
            title("Set Operations"),
            description(
                "Set operations allow results of multiple queries to be combined into a single result set.",
                "The results to be combined are required to be of same type. In other word, they require to",
                "have same column. Otherwise, a semantic analysis exception is raised."
            ),
            example(
                title("UNION Operator"),
                description(
                    "A ``UNION`` clause combines the results of two queries into a single result set. Duplicate rows",
                    "are removed unless ``UNION ALL`` clause is being used. A common use case of ``UNION`` is to combine",
                    "result set from data partitioned in indices daily or monthly."
                ),
                post(
                    "SELECT balance, firstname, lastname FROM accounts WHERE balance < 10000 " +
                    "UNION " +
                    "SELECT balance, firstname, lastname FROM accounts WHERE balance > 30000 "
                )
            ),
            example(
                title("MINUS Operator"),
                description(
                    "A ``MINUS`` clause takes two queries too but returns resulting rows of first query that",
                    "do not appear in the other query. Duplicate rows are removed automatically as well."
                ),
                post(
                    "SELECT balance, age FROM accounts WHERE balance < 10000 " +
                    "MINUS " +
                    "SELECT balance, age FROM accounts WHERE age < 35 "
                )
            )
        );
    }

}
