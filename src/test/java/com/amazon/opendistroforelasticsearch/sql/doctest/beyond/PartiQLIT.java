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
import com.amazon.opendistroforelasticsearch.sql.doctest.core.builder.Example;
import com.amazon.opendistroforelasticsearch.sql.utils.JsonPrettyFormatter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.amazon.opendistroforelasticsearch.sql.doctest.core.TestData.TEST_DATA_FOLDER_ROOT;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestUtils.getResourceFilePath;

@DocTestConfig(template = "beyond/partiql.rst", testData = {"employees_nested.json"})
public class PartiQLIT extends DocTest {

    @Section(1)
    public void showTestData() {
        section(
            title("Test Data"),
            description(
                "The test index ``employees_nested`` used by all examples in this document is very similar to",
                "the one used in official PartiQL documentation."
            ),
            createDummyExampleForTestData("employees_nested.json")
        );
    }

    @Section(2)
    public void queryNestedCollection() {
        section(
            title("Querying Nested Collection"),
            description(
                "In SQL-92, a database table can only have tuples that consists of scalar values.",
                "PartiQL extends SQL-92 to allow you query and unnest nested collection conveniently.",
                "In Elasticsearch world, this is very useful for index with object or nested field."
            ),
            example(
                title("Unnesting a Nested Collection"),
                description(
                    "In the following example, it finds nested document (project) with field value (name)",
                    "that satisfies the predicate (contains 'security'). Note that because each parent document",
                    "can have more than one nested documents, the matched nested document is flattened. In other",
                    "word, the final result is the Cartesian Product between parent and nested documents."
                ),
                post(multiLine(
                    "SELECT e.name AS employeeName,",
                    "       p.name AS projectName",
                    "FROM employees_nested AS e,",
                    "     e.projects AS p",
                    "WHERE p.name LIKE '%security%'"
                ))
            ),
            /*
            Issue: https://github.com/opendistro-for-elasticsearch/sql/issues/397
            example(
                title("Preserving Parent Information with LEFT JOIN"),
                description(
                    "The query in the preceding example is very similar to traditional join queries, except ``ON`` clause missing.",
                    "This is because it is implicitly in the nesting of nested documents (projects) into parent (employee). Therefore,",
                    "you can use ``LEFT JOIN`` to preserve the information in parent document associated."
                ),
                post(
                    "SELECT e.id AS id, " +
                    "       e.name AS employeeName, " +
                    "       e.title AS title, " +
                    "       p.name AS projectName " +
                    "FROM employees_nested AS e " +
                    "LEFT JOIN e.projects AS p"
                )
            )*/
            example(
                title("Unnesting in Existential Subquery"),
                description(
                    "Alternatively, a nested collection can be unnested in subquery to check if it",
                    "satisfies a condition."
                ),
                post(multiLine(
                    "SELECT e.name AS employeeName",
                    "FROM employees_nested AS e",
                    "WHERE EXISTS (",
                    "  SELECT *",
                    "  FROM e.projects AS p",
                    "  WHERE p.name LIKE '%security%'",
                    ")"
                ))
            )/*,
            Issue: https://github.com/opendistro-for-elasticsearch/sql/issues/398
            example(
                title("Aggregating over a Nested Collection"),
                description(
                    "After unnested, a nested collection can be aggregated just like a regular field."
                ),
                post(multiLine(
                    "SELECT",
                    "  e.name AS employeeName,",
                    "  COUNT(p) AS cnt",
                    "FROM employees_nested AS e,",
                    "     e.projects AS p",
                    "WHERE p.name LIKE '%security%'",
                    "GROUP BY e.id, e.name",
                    "HAVING COUNT(p) >= 1"
                )
            ))
            */
        );
    }

    private Example createDummyExampleForTestData(String fileName) {
        Example example = new Example();
        example.setTitle("Employees");
        example.setDescription("");
        example.setResult(parseJsonFromTestData(fileName));
        return example;
    }

    /** Concat and pretty format document at odd number line in bulk request file */
    private String parseJsonFromTestData(String fileName) {
    Path path = Paths.get(getResourceFilePath(TEST_DATA_FOLDER_ROOT + fileName));
        try {
            List<String> lines = Files.readAllLines(path);
            String json = IntStream.range(0, lines.size()).
                                    filter(i -> i % 2 == 1).
                                    mapToObj(lines::get).
                                    collect(Collectors.joining(",","{\"employees\":[", "]}"));
            return JsonPrettyFormatter.format(json);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load test data: " + path, e);
        }
    }

}
