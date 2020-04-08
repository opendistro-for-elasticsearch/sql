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
import com.amazon.opendistroforelasticsearch.sql.doctest.core.builder.Example;
import com.amazon.opendistroforelasticsearch.sql.doctest.core.builder.Requests;
import org.junit.Ignore;

import static com.amazon.opendistroforelasticsearch.sql.doctest.core.request.SqlRequestFormat.IGNORE_REQUEST;
import static com.amazon.opendistroforelasticsearch.sql.doctest.core.request.SqlRequestFormat.KIBANA_REQUEST;
import static com.amazon.opendistroforelasticsearch.sql.doctest.core.response.SqlResponseFormat.IGNORE_RESPONSE;
import static com.amazon.opendistroforelasticsearch.sql.doctest.core.response.SqlResponseFormat.TABLE_RESPONSE;

@DocTestConfig(template = "dql/complex.rst", testData = {"accounts.json", "employees_nested.json"})
public class ComplexQueryIT extends DocTest {

    @Section(1)
    public void subquery() {
        section(
            title("Subquery"),
            description(
                "A subquery is a complete ``SELECT`` statement which is used within another statement",
                "and enclosed in parenthesis. From the explain output, you can notice that some subquery",
                "are actually transformed to an equivalent join query to execute."
            ),
            /*
            example(
                title("Scalar Value Subquery"),
                description(
                    ""
                ),
                post(
                    "SELECT firstname, lastname, balance " +
                    "FROM accounts " +
                    "WHERE balance >= ( " +
                    " SELECT AVG(balance) FROM accounts " +
                    ") "
                )
            ),*/
            example(
                title("Table Subquery"),
                description(""),
                post(multiLine(
                    "SELECT a1.firstname, a1.lastname, a1.balance",
                    "FROM accounts a1",
                    "WHERE a1.account_number IN (",
                    "  SELECT a2.account_number",
                    "  FROM accounts a2",
                    "  WHERE a2.balance > 10000",
                    ")"
                ))
            ),
            example(
                title("Subquery in FROM Clause"),
                description(""),
                post(multiLine(
                    "SELECT a.f, a.l, a.a",
                    "FROM (",
                    "  SELECT firstname AS f, lastname AS l, age AS a",
                    "  FROM accounts",
                    "  WHERE age > 30",
                    ") AS a"
                ))
            )
        );
    }

    @Section(2)
    public void joins() {
        section(
            title("JOINs"),
            description(
                "A ``JOIN`` clause combines columns from one or more indices by using values common to each."
            ),
            images("rdd/tableSource.png", "rdd/joinPart.png"),
            example(
                title("Inner Join"),
                description(
                    "Inner join is very commonly used that creates a new result set by combining columns",
                    "of two indices based on the join predicates specified. It iterates both indices and",
                    "compare each document to find all that satisfy the join predicates. Keyword ``JOIN``",
                    "is used and preceded by ``INNER`` keyword optionally. The join predicate(s) is specified",
                    "by ``ON`` clause.\n\n",
                    "Remark that the explain API output for join queries looks complicated. This is because",
                    "a join query is associated with two Elasticsearch DSL queries underlying and execute in",
                    "the separate query planner framework. You can interpret it by looking into the logical",
                    "plan and physical plan."
                ),
                post(multiLine(
                    "SELECT",
                    "  a.account_number, a.firstname, a.lastname,",
                    "  e.id, e.name",
                    "FROM accounts a",
                    "JOIN employees_nested e",
                    " ON a.account_number = e.id"
                ))
            ),
            joinExampleWithoutExplain(
                title("Cross Join"),
                description(
                    "Cross join or Cartesian join combines each document from the first index with each from",
                    "the second. The result set is the Cartesian Product of documents from both indices.",
                    "It appears to be similar to inner join without ``ON`` clause to specify join condition.\n\n",
                    "Caveat: It is risky to do cross join even on two indices of medium size. This may trigger",
                    "our circuit breaker to terminate the query to avoid out of memory issue."
                ),
                post(multiLine(
                    "SELECT",
                    "  a.account_number, a.firstname, a.lastname,",
                    "  e.id, e.name",
                    "FROM accounts a",
                    "JOIN employees_nested e"
                ))
            ),
            joinExampleWithoutExplain(
                title("Outer Join"),
                description(
                    "Outer join is used to retain documents from one or both indices although it does not satisfy",
                    "join predicate. For now, only ``LEFT OUTER JOIN`` is supported to retain rows from first index.",
                    "Note that keyword ``OUTER`` is optional."
                ),
                post(multiLine(
                    "SELECT",
                    "  a.account_number, a.firstname, a.lastname,",
                    "  e.id, e.name",
                    "FROM accounts a",
                    "LEFT JOIN employees_nested e",
                    " ON a.account_number = e.id"
                ))
            )
        );
    }

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
                post(multiLine(
                    "SELECT balance, firstname, lastname",
                    "FROM accounts WHERE balance < 10000",
                    "UNION",
                    "SELECT balance, firstname, lastname",
                    "FROM accounts WHERE balance > 30000"
                ))
            ),
            example(
                title("MINUS Operator"),
                description(
                    "A ``MINUS`` clause takes two queries too but returns resulting rows of first query that",
                    "do not appear in the other query. Duplicate rows are removed automatically as well."
                ),
                post(multiLine(
                    "SELECT balance, age",
                    "FROM accounts",
                    "WHERE balance < 10000",
                    "MINUS",
                    "SELECT balance, age",
                    "FROM accounts",
                    "WHERE age < 35"
                ))
            )
        );
    }

    private Example joinExampleWithoutExplain(String title, String description, Requests requests) {
        return example(title, description, requests,
            queryFormat(KIBANA_REQUEST, TABLE_RESPONSE),
            explainFormat(IGNORE_REQUEST, IGNORE_RESPONSE)
        );
    }

}
