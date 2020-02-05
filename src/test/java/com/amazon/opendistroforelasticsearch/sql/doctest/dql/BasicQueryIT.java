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

import static com.amazon.opendistroforelasticsearch.sql.doctest.core.request.SqlRequestFormat.IGNORE_REQUEST;
import static com.amazon.opendistroforelasticsearch.sql.doctest.core.request.SqlRequestFormat.KIBANA_REQUEST;
import static com.amazon.opendistroforelasticsearch.sql.doctest.core.response.SqlResponseFormat.IGNORE_RESPONSE;

/**
 * Doc test for basic SELECT query.
 */
@DocTestConfig(template = "dql/basics.rst", testData = {"accounts.json"})
public class BasicQueryIT extends DocTest {

    @Section(1)
    public void select() {
        section(
            title("SELECT"),
            description("``SELECT`` clause specifies which fields in Elasticsearch index should be retrieved."),
            example(
                title("Selecting All Fields"),
                description(
                    "You can use ``*`` to fetch all fields in the index which is very convenient when you",
                    "just want to have a quick look at your data."
                ),
                post("SELECT * FROM accounts")
            ),
            example(
                title("Selecting Specific Field(s)"),
                description(
                    "More often you would give specific field name(s) in ``SELECT`` clause to",
                    "avoid large and unnecessary data retrieved."
                ),
                post("SELECT firstname, lastname FROM accounts")
            ),
            example(
                title("Selecting Distinct Field(s)"),
                description(
                    "``DISTINCT`` is useful when you want to de-duplicate and get unique field value.",
                    "You can also provide one or more field names."
                ),
                post("SELECT DISTINCT age FROM accounts")
            )
        );
    }

    @Section(2)
    public void from() {
        section(
            title("FROM"),
            description(
                "``FROM`` clause specifies Elasticsearch index where the data should be retrieved from.",
                "You've seen how to specify a single index in FROM clause in last section. Here we",
                "list more use cases additionally.\n",
                "Subquery in ``FROM`` clause is also supported. Please check out our documentation for more details."
            ),
            kibanaExample(
                title("Selecting From Multiple Indices by Index Pattern"),
                description(
                    "Alternatively you can query from multiple indices of similar names by index pattern.",
                    "This is very convenient for indices created by Logstash index template with date as suffix."
                ),
                post("SELECT account_number FROM account*")
            ),
            kibanaExample(
                title("[Deprecating] Selecting From Specific Index Type"),
                description(
                    "You can also specify type name explicitly though this has been deprecated in",
                    "later Elasticsearch version."
                ),
                post("SELECT account_number FROM accounts/account")
            )
        );
    }

    @Section(3)
    public void where() {
        section(
            title("WHERE"),
            description(
                "`WHERE` clause specifies only Elasticsearch documents that meet the criteria should be affected.",
                "It consists of predicates that uses ``=``, ``<>``, ``>``, ``>=``, ``<``, ``<=``, ``IN``,",
                "``BETWEEN``, ``LIKE``, ``IS NULL`` or ``IS NOT NULL``.",
                "These predicates can be combined by logical operator ``NOT``, ``AND`` or ``OR``.\n",
                "For ``LIKE`` and other full text search topics, please refer to Full Text Search documentation.\n",
                "Besides SQL query, WHERE clause can also be used in SQL statement such as ``DELETE``. Please refer to",
                "Data Manipulation Language documentation for details."
            ),
            example(
                title("Comparison Operators"),
                description(
                    "Basic comparison operators, such as ``=``, ``<>``, ``>``, ``>=``, ``<``, ``<=``, can work for",
                    "number, string or date.",
                    "``IN`` and ``BETWEEN`` is convenient for comparison with multiple values or a range."
                ),
                post("SELECT account_number FROM accounts WHERE account_number = 1")
            ),
            example(
                title("Missing Fields"),
                description(
                    "As NoSQL database, Elasticsearch allows for flexible schema that documents in an index may have",
                    "different fields. In this case, you can use ``IS NULL`` or ``IS NOT NULL`` to retrieve missing",
                    "fields or existing fields only.\n",
                    "Note that for now we don't differentiate missing field and field set to ``NULL`` explicitly."
                ),
                post("SELECT account_number, employer FROM accounts WHERE employer IS NULL")
            )
        );
    }

    @Section(4)
    public void alias() {
        section(
            title("Alias"),
            description(
                "Alias makes your query more readable by aliasing your index or field to clearer or shorter name."
            ),
            example(
                description("Here is an example of how to use table alias as well as field alias."),
                post("SELECT acc.account_number AS num FROM accounts acc WHERE acc.age > 30")
            )
        );
    }

    @Section(5)
    public void groupBy() {
        section(
            title("GROUP BY"),
            description("Limitation because ES ... NULL (missing value) won't be taken into account in aggregation."),
            example(
                title("Grouping By Fields"),
                description(),
                post("SELECT age FROM accounts GROUP BY age")
            ),
            example(
                title("Grouping By Alias"),
                description(""),
                post("SELECT age AS a FROM accounts GROUP BY a")
            ),
            example(
                title("Grouping By Field Ordinal in Select"),
                description(""),
                post("SELECT age FROM accounts GROUP BY 1")
            ),
            example(
                title("Grouping By Scalar Function"),
                description(""),
                post("SELECT age AS a FROM accounts GROUP BY 1")
            )
        );
    }

    @Section(6)
    public void having() {
        section(
            title("HAVING"),
            description(""),
            example(
                description(),
                post("SELECT age FROM accounts GROUP BY age")
            )/*,
            example(
                description(""),
                post("SELECT age AS a FROM accounts GROUP BY a")
            ),
            example(
                description(""),
                post("SELECT age FROM accounts GROUP BY 1")
            ),
            example(
                description(),
                post("SELECT FROM accounts GROUP BY SUBSTRING(firstname) ?")
            )*/
        );
    }

    @Section(7)
    public void orderBy() {
        section(
            title("ORDER BY"),
            description("``ORDER BY`` clause specifies which fields used to sort the result and in which direction."),
            example(
                description(
                    "Besides regular field names, ordinal, alias or scalar function can also be used similarly",
                    "as in ``GROUP BY``. ``ASC`` (by default) or ``DESC`` can be appended to indicate sorting in",
                    "ascending or descending order."
                ),
                post("SELECT account_number FROM accounts ORDER BY account_number DESC")
            ),
            example(
                description(
                    "Additionally you can specify if documents with missing field be put first or last.",
                    "The default behavior of Elasticsearch is to return nulls or missing last.",
                    "You can make them present before non-nulls by using ``IS NOT NULL``."
                ),
                post("SELECT employer FROM accounts ORDER BY employer IS NOT NULL")
            )
        );
    }

    @Section(8)
    public void limit() {
        section(
            title("LIMIT"),
            description(
                "Mostly specifying maximum number of documents returned is necessary to prevent fetching",
                "large amount of data into memory. `LIMIT` clause is helpful in this case."
            ),
            example(
                title("Limiting Result Size"),
                description(
                    "Given a positive number, ``LIMIT`` uses it as page size to fetch result of that size at most."
                ),
                post("SELECT account_number FROM accounts ORDER BY account_number LIMIT 1")
            ),
            example(
                title("Fetching at Offset"),
                description(
                    "Offset position can be given as first argument to indicate where to start fetching.",
                    "This can be used as simple pagination solution though it's inefficient on large index.",
                    "Generally ``ORDER BY`` is required in this case to ensure the same order between pages."
                ),
                post("SELECT account_number FROM accounts ORDER BY account_number LIMIT 1, 1")
            )
        );
    }

    /** Document only Kibana request for example and ignore response as well as explain */
    private Example kibanaExample(String title, String description, Requests requests) {
        return example(title, description, requests,
            queryFormat(KIBANA_REQUEST, IGNORE_RESPONSE),
            explainFormat(IGNORE_REQUEST, IGNORE_RESPONSE)
        );
    }

}
