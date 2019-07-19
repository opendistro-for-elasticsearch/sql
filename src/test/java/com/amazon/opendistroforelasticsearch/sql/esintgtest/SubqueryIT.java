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

import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Locale;

import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_ACCOUNT;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_DOGSUBQUERY;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.hitAll;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.kvInt;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.kvString;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.core.Is.is;

public class SubqueryIT extends SQLIntegTestCase {

    @Override
    protected void init() throws Exception {
        loadIndex(Index.ACCOUNT);
        loadIndex(Index.DOGSSUBQUERY);
    }

    @Test
    public void test() throws IOException {
        String query = String.format("SELECT * FROM %s/dog A JOIN %s/account B ON A.v = B.v",
                TEST_INDEX_DOGSUBQUERY, TEST_INDEX_ACCOUNT);
        System.out.println(explainQuery(query));
    }

    @Test
    public void testIN() throws IOException {
        String query = String.format(Locale.ROOT, "SELECT dog_name " +
                        "FROM %s/dog A " +
                        "WHERE holdersName IN (SELECT firstname FROM %s/account B) AND dog_name <> 'babala'",
                TEST_INDEX_DOGSUBQUERY, TEST_INDEX_ACCOUNT);
        System.out.println(explainQuery(query));

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
        String query = String.format(Locale.ROOT, "SELECT A.dog_name " +
                        "FROM %s/dog A " +
                        "WHERE A.holdersName IN (SELECT B.firstname FROM %s/account B) AND A.dog_name <> 'babala'",
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
        String query = String.format(Locale.ROOT, "SELECT * " +
                        "FROM %s/dog A " +
                        "WHERE holdersName IN (SELECT firstname FROM %s/account B) AND dog_name <> 'babala'",
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
        String query = String.format(Locale.ROOT, "SELECT dog_name " +
                        "FROM %s/dog A " +
                        "WHERE holdersName IN (SELECT firstname FROM %s/account B WHERE age <> 36) AND dog_name <> 'babala'",
                TEST_INDEX_DOGSUBQUERY, TEST_INDEX_ACCOUNT);

        JSONObject response = executeQuery(query);
        assertThat(
                response,
                hitAll(
                        kvString("/_source/A.dog_name", is("gogo"))
                )
        );
    }

    // todo Pending on DISTINCT support in JOIN
    @Ignore
    @Test
    public void testINWithDuplicate() throws IOException {
        String query = String.format(Locale.ROOT, "SELECT dog_name " +
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
}
