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

package com.amazon.opendistroforelasticsearch.sql.doctest.dml;

import com.amazon.opendistroforelasticsearch.sql.doctest.core.DocTest;
import com.amazon.opendistroforelasticsearch.sql.doctest.core.annotation.DocTestConfig;
import com.amazon.opendistroforelasticsearch.sql.doctest.core.annotation.Section;

import static com.amazon.opendistroforelasticsearch.sql.doctest.core.request.SqlRequestFormat.IGNORE_REQUEST;
import static com.amazon.opendistroforelasticsearch.sql.doctest.core.request.SqlRequestFormat.KIBANA_REQUEST;
import static com.amazon.opendistroforelasticsearch.sql.doctest.core.response.SqlResponseFormat.PRETTY_JSON_RESPONSE;

@DocTestConfig(template = "dml/delete.rst", testData = {"accounts.json"})
public class DeleteIT extends DocTest {

    @Section(1)
    public void delete() {
        section(
            title("DELETE"),
            description(
                "``DELETE`` statement deletes documents that satisfy the predicates in ``WHERE`` clause.",
                "Note that all documents are deleted in the case of ``WHERE`` clause absent."
            ),
            images("rdd/singleDeleteStatement.png"),
            example(
                description(
                    "The ``datarows`` field in this case shows rows impacted, in other words how many",
                    "documents were just deleted."
                ),
                post(multiLine(
                    "DELETE FROM accounts",
                    "WHERE age > 30"
                )),
                queryFormat(KIBANA_REQUEST, PRETTY_JSON_RESPONSE),
                explainFormat(IGNORE_REQUEST, PRETTY_JSON_RESPONSE)
            )
        );
    }

}
