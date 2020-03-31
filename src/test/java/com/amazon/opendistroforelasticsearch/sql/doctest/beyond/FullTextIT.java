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

package com.amazon.opendistroforelasticsearch.sql.doctest.beyond;

import com.amazon.opendistroforelasticsearch.sql.doctest.core.DocTest;
import com.amazon.opendistroforelasticsearch.sql.doctest.core.annotation.DocTestConfig;
import com.amazon.opendistroforelasticsearch.sql.doctest.core.annotation.Section;

@DocTestConfig(template = "beyond/fulltext.rst", testData = {"accounts.json"})
public class FullTextIT extends DocTest {

    @Section(1)
    public void matchQuery() {
        section(
            title("Match Query"),
            description(
                "Match query is the standard query for full-text search in Elasticsearch. Both ``MATCHQUERY`` and",
                "``MATCH_QUERY`` are functions for performing match query."
            ),
            example(
                description("Both functions can accept field name as first argument and a text as second argument."),
                post(multiLine(
                    "SELECT account_number, address",
                    "FROM accounts",
                    "WHERE MATCH_QUERY(address, 'Holmes')"
                ))
            ),
            example(
                description("Both functions can also accept single argument and be used in the following manner."),
                post(multiLine(
                    "SELECT account_number, address",
                    "FROM accounts",
                    "WHERE address = MATCH_QUERY('Holmes')"
                ))
            )
        );
    }

    @Section(2)
    public void multiMatchQuery() {
        section(
            title("Multi-match Query"),
            description(
                "Besides match query against a single field, you can search for a text with multiple fields.",
                "Function ``MULTI_MATCH``, ``MULTIMATCH`` and ``MULTIMATCHQUERY`` are provided for this."
            ),
            example(
                description(
                    "Each preceding function accepts ``query`` for a text and ``fields`` for field names or pattern",
                    "that the text given is searched against. For example, the following query is searching for",
                    "documents in index accounts with 'Dale' as either firstname or lastname."
                ),
                post(multiLine(
                    "SELECT firstname, lastname",
                    "FROM accounts",
                    "WHERE MULTI_MATCH('query'='Dale', 'fields'='*name')"
                ))
            )
        );
    }

    @Section(3)
    public void queryStringQuery() {
        section(
            title("Query String Query"),
            description(
                "Query string query parses and splits a query string provided based on Lucene query string syntax.",
                "The mini language supports logical connectives, wildcard, regex and proximity search. Please refer",
                "to official documentation for more details. Note that an error is thrown in the case of any invalid",
                "syntax in query string."
            ),
            example(
                description(
                    "``QUERY`` function accepts query string and returns true or false respectively for document",
                    "that matches the query string or not."
                ),
                post(multiLine(
                    "SELECT account_number, address",
                    "FROM accounts",
                    "WHERE QUERY('address:Lane OR address:Street')"
                ))
            )
        );
    }

    @Section(4)
    public void matchPhraseQuery() {
        section(
            title("Match Phrase Query"),
            description(
                "Match phrase query is similar to match query but it is used for matching exact phrases.",
                "``MATCHPHRASE``, ``MATCH_PHRASE`` and ``MATCHPHRASEQUERY`` are provided for this purpose."
            ),
            example(
                description(),
                post(multiLine(
                    "SELECT account_number, address",
                    "FROM accounts",
                    "WHERE MATCH_PHRASE(address, '880 Holmes Lane')"
                ))
            )
        );
    }

    @Section(5)
    public void scoreQuery() {
        section(
            title("Score Query"),
            description(
                "Elasticsearch supports to wrap a filter query so as to return a relevance score along with",
                "every matching document. ``SCORE``, ``SCOREQUERY`` and ``SCORE_QUERY`` can be used for this."
            ),
            example(
                description(
                    "The first argument is a match query expression and the second argument is for an optional",
                    "floating point number to boost the score. The default value is 1.0. Apart from this, an",
                    "implicit variable ``_score`` is available so you can return score for each document or",
                    "use it for sorting."
                ),
                post(multiLine(
                    "SELECT account_number, address, _score",
                    "FROM accounts",
                    "WHERE SCORE(MATCH_QUERY(address, 'Lane'), 0.5) OR",
                    "  SCORE(MATCH_QUERY(address, 'Street'), 100)",
                    "ORDER BY _score"
                ))
            )
        );
    }

}
