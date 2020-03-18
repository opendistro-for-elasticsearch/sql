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

@DocTestConfig(template = "dql/complex.rst", testData = {"accounts.json"})
public class ComplexQueryIT extends DocTest {

    @Section(1)
    public void subqueries() {
        section(
            title("Subqueries"),
            description(""),
            example(
                title("IN/EXISTS"),
                description(""),
                post("")
            ),
            example(
                title("Subqueries in FROM clause"),
                description(""),
                post("")
            )
        );
    }

    @Section(2)
    public void joins() {
        section(
            title("JOINs"),
            description(""),
            example(
                title("INNER JOIN"),
                description(""),
                post("")
            ),
            example(
                title("Cartesian JOIN"),
                description(""),
                post("")
            ),
            example(
                title("LEFT OUTER JOIN"),
                description(""),
                post("")
            )
        );
    }

    @Section(3)
    public void setOperations() {
        section(
            title("Set Operations"),
            description(""),
            example(
                title("UNION"),
                description(
                    "A ``UNION`` clause combines the results of two queries into a single result. Any duplicate record",
                    "are removed unless ``UNION ALL`` is used. A common use case of ``UNION`` is to combine results",
                    "from an index rotated on a daily or monthly basis."
                ),
                post("")
            ),
            example(
                title("MINUS"),
                description(""),
                post("")
            )
        );
    }

}
