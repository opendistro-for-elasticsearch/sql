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

package com.amazon.opendistroforelasticsearch.sql.esintgtest;

import com.amazon.opendistroforelasticsearch.sql.exception.SqlParseException;
import org.elasticsearch.client.ResponseException;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.util.Locale;

import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_ACCOUNT;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_DOGSUBQUERY;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_EMPLOYEE_NESTED;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.hitAll;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.kvInt;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.kvString;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.core.Is.is;

public class SubqueryIT extends SQLIntegTestCase {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();


    @Override
    protected void init() throws Exception {
        loadIndex(Index.ACCOUNT);
        loadIndex(Index.DOGSSUBQUERY);
        loadIndex(Index.EMPLOYEE_NESTED);
    }

    @Test
    public void testIN() throws IOException {
        String query = String.format(Locale.ROOT,
                "SELECT dog_name " +
                            "FROM %s/dog A " +
                            "WHERE holdersName IN (SELECT firstname FROM %s/account B) " +
                            "AND dog_name <> 'babala'",
                TEST_INDEX_DOGSUBQUERY, TEST_INDEX_ACCOUNT);

        JSONObject response = executeQuery(query);
        assertThat(
                response,
                hitAll(
                        kvString("/_source/A.dog_name", is("snoopy")),
                        kvString("/_source/A.dog_name", is("gogo"))
                )
        );
    }

    @Test
    public void testINWithAlias() throws IOException {
        String query = String.format(Locale.ROOT,
                "SELECT A.dog_name " +
                            "FROM %s/dog A " +
                            "WHERE A.holdersName IN (SELECT B.firstname FROM %s/account B) " +
                            "AND A.dog_name <> 'babala'",
                TEST_INDEX_DOGSUBQUERY, TEST_INDEX_ACCOUNT);

        JSONObject response = executeQuery(query);
        assertThat(
                response,
                hitAll(
                        kvString("/_source/A.dog_name", is("snoopy")),
                        kvString("/_source/A.dog_name", is("gogo"))
                )
        );
    }

    @Test
    public void testINSelectAll() throws IOException {
        String query = String.format(Locale.ROOT,
                "SELECT * " +
                            "FROM %s/dog A " +
                            "WHERE holdersName IN (SELECT firstname FROM %s/account B) " +
                            "AND dog_name <> 'babala'",
                TEST_INDEX_DOGSUBQUERY, TEST_INDEX_ACCOUNT);

        JSONObject response = executeQuery(query);
        assertThat(
                response,
                hitAll(
                        both(kvString("/_source/A.dog_name", is("snoopy")))
                                .and(kvString("/_source/A.holdersName", is("Hattie")))
                                .and(kvInt("/_source/A.age", is(4))),
                        both(kvString("/_source/A.dog_name", is("gogo")))
                                .and(kvString("/_source/A.holdersName", is("Gabrielle")))
                                .and(kvInt("/_source/A.age", is(6)))
                )
        );
    }

    @Test
    public void testINWithInnerWhere() throws IOException {
        String query = String.format(Locale.ROOT,
                "SELECT dog_name " +
                            "FROM %s/dog A " +
                            "WHERE holdersName IN (SELECT firstname FROM %s/account B WHERE age <> 36) " +
                            "AND dog_name <> 'babala'",
                TEST_INDEX_DOGSUBQUERY, TEST_INDEX_ACCOUNT);

        JSONObject response = executeQuery(query);
        assertThat(
                response,
                hitAll(
                        kvString("/_source/A.dog_name", is("gogo"))
                )
        );
    }

    @Test
    public void testNotSupportedQuery() throws IOException {
        exceptionRule.expect(ResponseException.class);
        exceptionRule.expectMessage("Unsupported subquery");
        String query = String.format(Locale.ROOT,
                "SELECT dog_name " +
                        "FROM %s/dog A " +
                        "WHERE holdersName NOT IN (SELECT firstname FROM %s/account B WHERE age <> 36) " +
                        "AND dog_name <> 'babala'",
                TEST_INDEX_DOGSUBQUERY, TEST_INDEX_ACCOUNT);
        executeQuery(query);
    }

    // todo Pending on DISTINCT support in JOIN
    @Ignore
    @Test
    public void testINWithDuplicate() throws IOException {
        String query = String.format(Locale.ROOT,
                "SELECT dog_name " +
                            "FROM %s/dog A " +
                            "WHERE holdersName IN (SELECT firstname FROM %s/account B)",
                TEST_INDEX_DOGSUBQUERY, TEST_INDEX_ACCOUNT);

        JSONObject response = executeQuery(query);
        assertThat(
                response,
                hitAll(
                        kvString("/_source/A.dog_name", is("snoopy")),
                        kvString("/_source/A.dog_name", is("babala"))
                )
        );
    }

    @Test
    public void nonCorrelatedExists() throws IOException {
        String query = String.format(Locale.ROOT,
                                     "SELECT e.name " +
                                     "FROM %s as e " +
                                     "WHERE EXISTS (SELECT * FROM e.projects as p)",
                                     TEST_INDEX_EMPLOYEE_NESTED);

        JSONObject response = executeQuery(query);
        assertThat(
                response,
                hitAll(
                        kvString("/_source/name", is("Bob Smith")),
                        kvString("/_source/name", is("Jane Smith"))
                )
        );
    }

    @Test
    public void nonCorrelatedExistsWhere() throws IOException {
        String query = String.format(Locale.ROOT,
                                     "SELECT e.name " +
                                     "FROM %s as e " +
                                     "WHERE EXISTS (SELECT * FROM e.projects as p WHERE p.name LIKE 'aurora')",
                                     TEST_INDEX_EMPLOYEE_NESTED);

        JSONObject response = executeQuery(query);
        assertThat(
                response,
                hitAll(
                        kvString("/_source/name", is("Bob Smith"))
                )
        );
    }

    @Test
    public void nonCorrelatedExistsParentWhere() throws IOException {
        String query = String.format(Locale.ROOT,
                                     "SELECT e.name " +
                                     "FROM %s as e " +
                                     "WHERE EXISTS (SELECT * FROM e.projects as p WHERE p.name LIKE 'security') " +
                                     "AND e.name LIKE 'jane'",
                                     TEST_INDEX_EMPLOYEE_NESTED);

        JSONObject response = executeQuery(query);
        assertThat(
                response,
                hitAll(
                        kvString("/_source/name", is("Jane Smith"))
                )
        );
    }

    @Test
    public void nonCorrelatedNotExists() throws IOException {
        String query = String.format(Locale.ROOT,
                                     "SELECT e.name " +
                                     "FROM %s as e " +
                                     "WHERE NOT EXISTS (SELECT * FROM e.projects as p)",
                                     TEST_INDEX_EMPLOYEE_NESTED);

        JSONObject response = executeQuery(query);
        System.out.println(response);
        assertThat(
                response,
                hitAll(
                        kvString("/_source/name", is("Susan Smith")),
                        kvString("/_source/name", is("John Doe"))
                )
        );
    }

    @Test
    public void nonCorrelatedNotExistsWhere() throws IOException {
        String query = String.format(Locale.ROOT,
                                     "SELECT e.name " +
                                     "FROM %s as e " +
                                     "WHERE NOT EXISTS (SELECT * FROM e.projects as p WHERE p.name LIKE 'aurora')",
                                     TEST_INDEX_EMPLOYEE_NESTED);

        JSONObject response = executeQuery(query);
        System.out.println(response);
        assertThat(
                response,
                hitAll(
                        kvString("/_source/name", is("Susan Smith")),
                        kvString("/_source/name", is("Jane Smith")),
                        kvString("/_source/name", is("John Doe"))
                )
        );
    }

    @Test
    public void nonCorrelatedNotExistsParentWhere() throws IOException {
        String query = String.format(Locale.ROOT,
                                     "SELECT e.name " +
                                     "FROM %s as e " +
                                     "WHERE NOT EXISTS (SELECT * FROM e.projects as p WHERE p.name LIKE 'security') " +
                                     "AND e.name LIKE 'smith'",
                                     TEST_INDEX_EMPLOYEE_NESTED);

        JSONObject response = executeQuery(query);
        System.out.println(response);
        assertThat(
                response,
                hitAll(
                        kvString("/_source/name", is("Susan Smith"))
                )
        );
    }

//    @Test
//    public void nonCorrelatedNotExistsUnsupported() throws IOException {
//        exceptionRule.expect(ResponseException.class);
//        exceptionRule.expectMessage("Unsupported subquery");
//        String query = String.format(Locale.ROOT,
//                                     "SELECT e.name " +
//                                     "FROM %s as e " +
//                                     "WHERE NOT EXISTS (SELECT * FROM e.projects as p)",
//                                     TEST_INDEX_EMPLOYEE_NESTED);
//        executeQuery(query);
//    }
}
