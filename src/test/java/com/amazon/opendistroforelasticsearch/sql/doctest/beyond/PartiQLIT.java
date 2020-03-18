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

@DocTestConfig(template = "beyond/partiql.rst", testData = {"employees_nested.json"})
public class PartiQLIT extends DocTest {

    @Section(1)
    public void unnestingCollection() {
        section(
            title("Unnesting a Nested Collection"),
            description("..."),
            example(
                description(""),
                post(
                    "SELECT e.name AS employeeName, " +
                    "       p.name AS projectName " +
                    "FROM employees_nested AS e, " +
                    "     e.projects AS p " +
                    "WHERE p.name LIKE '%security%'"
                )
            )
        );
    }

    /*
    @Section(2)
    public void unnestingUsingJoin() {
        section(
            title("Unnesting Using JOIN"),
            description("PartiQL is ..."),
            example(
                description(""),
                post("SELECT e.id AS id, " +
                    "       e.name AS employeeName, " +
                    "       e.title AS title, " +
                    "       p.name AS projectName " +
                    "FROM employees_nested AS e LEFT JOIN e.projects AS p")
            )
        );
    }

    @Section(3)
    public void missing() {
        section(
            title("Missing"),
            description("PartiQL is ..."),
            example(
                description(""),
                post("")
            )
        );
    }
    */

}
