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

/**
 * Doc test for basic SELECT query.
 */
@DocTestConfig(template = "dql/basics.rst", testData = {"accounts.json"})
public class BasicQueryIT extends DocTest {

    @Section(1)
    public void select() {
        section(
            title("SELECT"),
            description("SELECT clause specifies data of which fields should be retrieved."),
            example(
                description(
                    "You can use '*' to fetch all fields in the index which is useful to",
                    "have a quick look at your data."
                ),
                post("SELECT * FROM accounts")
            ),
            example(
                description(
                    "More often you would give specific field names in SELECT clause to",
                    "avoid large and unnecessary data."
                ),
                post("SELECT firstname, lastname FROM accounts")
            ),
            example(
                description(
                    "DISTINCT is useful when you want to de-duplicate and get unique field value.",
                    "You can provide one or more field name in SELECT."
                ),
                post("SELECT DISTINCT age FROM accounts")
            )
        );
    }

    @Section(2)
    public void from() {
        section(
            title("FROM"),
            description("FROM clause specifies Elasticsearch index where the data should be retrieved from."),
            example(
                description("Typically a single Elasticsearch index name is expected to present in FROM clause."),
                post("SELECT account_number FROM accounts")
            ),
            example(
                description("It is convenient to query from multiple indices by index pattern using wildcard."),
                post("SELECT account_number FROM account*")
            ),
            example(
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
            description(""),
            example(
                description(),
                post("SELECT account_number FROM accounts")
            ),
            example(
                description("logical operator to combine expression"),
                post("SELECT account_number FROM accounts")
            ),
            example(
                description("You can ... but this would be deprecated..."),
                post("SELECT account_number FROM accounts")
            )
        );
    }

    @Section(4)
    public void alias() {
        section(
            title("Alias"),
            description("Alias makes your query more readable by renaming your index or field to clear and short alias."),
            example(
                description(),
                post("SELECT acc.account_number AS num FROM accounts acc WHERE acc.age > 30")
            )
        );
    }

    @Section(5)
    public void groupBy() {
        section(
            title("FROM"),
            description(""),
            example(
                description(),
                post("SELECT age FROM accounts GROUP BY age")
            ),
            example(
                description(""),
                post("SELECT age AS a FROM accounts GROUP BY a")
            ),
            example(
                description(""),
                post("SELECT age FROM accounts GROUP BY 1")
            )/*,
            example(
                description(),
                post("SELECT FROM accounts GROUP BY SUBSTRING(firstname) ?")
            )*/
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
            ),
            example(
                description(""),
                post("SELECT age AS a FROM accounts GROUP BY a")
            ),
            example(
                description(""),
                post("SELECT age FROM accounts GROUP BY 1")
            )/*,
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
            description(""),
            example(
                description(),
                post("SELECT age FROM accounts ORDER BY age")
            ),
            example(
                description(""),
                post("SELECT age AS a FROM accounts ORDER BY a") //?
            ),
            example(
                description(""),
                post("SELECT age FROM accounts ORDER BY 1")
            )/*,
            example(
                description(),
                post("SELECT FROM accounts ORDER BY SUBSTRING(firstname) ?")
            )*/
        );
    }

    @Section(8)
    public void limit() {
        section(
            title("LIMIT"),
            description(""),
            example(
                description(),
                post("SELECT age FROM accounts LIMIT 1")
            )/*,
            example(
                description(),
                post("SELECT FROM accounts LIMIT 1, 2") // OFFSET
            )*/
        );
    }

}
