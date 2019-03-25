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

package com.amazon.opendistroforelasticsearch.sql.intgtest;

import com.amazon.opendistroforelasticsearch.sql.util.CheckPrettyFormatContents;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.amazon.opendistroforelasticsearch.sql.executor.format.DataRows;
import com.amazon.opendistroforelasticsearch.sql.executor.format.DataRows.Row;
import com.amazon.opendistroforelasticsearch.sql.executor.format.Protocol;
import com.amazon.opendistroforelasticsearch.sql.executor.format.Schema;
import org.hamcrest.MatcherAssert;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;

public class PrettyFormatResponseTest {

    private static Set<String> allAccountFields = Sets.newHashSet(
            "account_number", "balance", "firstname", "lastname", "age", "gender", "address", "employer",
            "email", "city", "state"
    );

    private static Set<String> regularFields = Sets.newHashSet("someField", "myNum");

    private static Set<String> messageFields = Sets.newHashSet(
            "message.dayOfWeek", "message.info", "message.author");

    private static List<String> nameFields = Arrays.asList("firstname", "lastname");

    /**
     * Tests for core classes (Protocol, Schema, etc.)
     *
     * Even though a format will be passed in to Protocol's constructor, the following tests are to see if the data
     * is being extracted correctly regardless of format
     */

    /** Test Schema */
    @Test
    public void noIndexType() {
        String query = String.format("SELECT * FROM %s", TestsConstants.TEST_INDEX_ACCOUNT);
        Protocol protocol = CheckPrettyFormatContents.execute(query, "jdbc");

        Schema schema = CheckPrettyFormatContents.getSchema(protocol);
        assertThat(schema.getIndexName(), equalTo(TestsConstants.TEST_INDEX_ACCOUNT));
        assertThat(schema.getTypeName(), equalTo(null));

        CheckPrettyFormatContents.containsColumnsInAnyOrder(schema, allAccountFields);
    }

    @Test
    public void withIndexType() {
        String query = String.format("SELECT * FROM %s/%s", TestsConstants.TEST_INDEX_ACCOUNT, "account");
        Protocol protocol = CheckPrettyFormatContents.execute(query, "jdbc");

        Schema schema = CheckPrettyFormatContents.getSchema(protocol);
        assertThat(schema.getIndexName(), equalTo(TestsConstants.TEST_INDEX_ACCOUNT));
        assertThat(schema.getTypeName(), equalTo("account"));
    }

    @Test
    public void wrongIndexType() {
        String type = "wrongType";
        try {
            String query = String.format("SELECT * FROM %s/%s", TestsConstants.TEST_INDEX_ACCOUNT, type);
            Protocol protocol = CheckPrettyFormatContents.execute(query, "jdbc");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is(String.format("Index type [%s/%s] does not exist", TestsConstants.TEST_INDEX_ACCOUNT, type)));
        }
    }

    @Test
    public void indexPattern() {
        String query = "SELECT * FROM " + TestsConstants.TEST_INDEX + "_loc*";
        Protocol protocol = CheckPrettyFormatContents.execute(query, "jdbc");
        assertThat(protocol.getResultSet().getDataRows().getSize(), equalTo(4L));
    }

    /** Test Protocol as a whole */
    @Test
    public void selectAll() {
        String query = String.format("SELECT * FROM %s/%s", TestsConstants.TEST_INDEX_ACCOUNT, "account");
        Protocol protocol = CheckPrettyFormatContents.execute(query, "jdbc");

        // This also tests that .keyword fields are ignored when SELECT * is called
        CheckPrettyFormatContents.containsColumnsInAnyOrder(CheckPrettyFormatContents.getSchema(protocol), allAccountFields);
        CheckPrettyFormatContents.containsData(CheckPrettyFormatContents.getDataRows(protocol), allAccountFields);
    }

    @Test
    public void selectNames() {
        String query = String.format("SELECT firstname, lastname FROM %s/%s", TestsConstants.TEST_INDEX_ACCOUNT, "account");
        Protocol protocol = CheckPrettyFormatContents.execute(query, "jdbc");

        CheckPrettyFormatContents.containsColumns(CheckPrettyFormatContents.getSchema(protocol), nameFields);
        CheckPrettyFormatContents.containsData(CheckPrettyFormatContents.getDataRows(protocol), nameFields);
    }

    @Test
    public void selectWrongField() {
        String query = String.format("SELECT wrongField FROM %s", TestsConstants.TEST_INDEX_ACCOUNT);
        Protocol protocol = CheckPrettyFormatContents.execute(query, "jdbc");

        assertThat(
                Iterables.size(CheckPrettyFormatContents.getSchema(protocol)),
                equalTo(0)
        );
        // DataRows still gets populated with SearchHits but wrongField is not available in the Map so nothing is
        // outputted in the final result
        assertThat(
                Iterables.size(CheckPrettyFormatContents.getDataRows(protocol)),
                equalTo(200)
        );
    }

    @Test
    public void selectKeyword() {
        String query = String.format("SELECT firstname.keyword FROM %s", TestsConstants.TEST_INDEX_ACCOUNT);
        Protocol protocol = CheckPrettyFormatContents.execute(query, "jdbc");

        List<String> fields = Arrays.asList("firstname.keyword");
        CheckPrettyFormatContents.containsColumns(CheckPrettyFormatContents.getSchema(protocol), fields);

        /*
         * firstname.keyword will appear in Schema but because there is no 'firstname.keyword' in SearchHits source
         * the DataRows will output null.
         *
         * Looks like x-pack adds this keyword field to "docvalue_fields", this might end up with it being in SearchHits
         */
        // containsData(getDataRows(protocol), fields);
    }

    @Test
    public void selectScore() {
        String query = String.format("SELECT _score FROM %s WHERE balance > 30000", TestsConstants.TEST_INDEX_ACCOUNT);
        Protocol protocol = CheckPrettyFormatContents.execute(query, "jdbc");

        List<String> fields = Arrays.asList("_score");
        CheckPrettyFormatContents.containsColumns(CheckPrettyFormatContents.getSchema(protocol), fields);
        CheckPrettyFormatContents.containsData(CheckPrettyFormatContents.getDataRows(protocol), fields);
    }

    @Test
    public void selectAllFromNestedWithoutFieldInFrom() {
        String query = String.format("SELECT * FROM %s", TestsConstants.TEST_INDEX_NESTED_TYPE);
        Protocol protocol = CheckPrettyFormatContents.execute(query, "jdbc");

        CheckPrettyFormatContents.containsColumnsInAnyOrder(CheckPrettyFormatContents.getSchema(protocol), regularFields);
        CheckPrettyFormatContents.containsData(CheckPrettyFormatContents.getDataRows(protocol), regularFields);
    }

    @Test
    public void selectAllFromNestedWithFieldInFrom() {
        String query = String.format("SELECT * FROM %s e, e.message m", TestsConstants.TEST_INDEX_NESTED_TYPE);
        Protocol protocol = CheckPrettyFormatContents.execute(query, "jdbc");

        CheckPrettyFormatContents.containsColumnsInAnyOrder(CheckPrettyFormatContents.getSchema(protocol), messageFields);
        CheckPrettyFormatContents.containsData(CheckPrettyFormatContents.getDataRows(protocol), messageFields);
    }

    @Test
    public void selectNestedFields() {
        String query = String.format("SELECT nested(message.info), someField FROM %s", TestsConstants.TEST_INDEX_NESTED_TYPE);
        Protocol protocol = CheckPrettyFormatContents.execute(query, "jdbc");

        List<String> fields = Arrays.asList("message.info", "someField");
        CheckPrettyFormatContents.containsColumns(CheckPrettyFormatContents.getSchema(protocol), fields);
        CheckPrettyFormatContents.containsData(CheckPrettyFormatContents.getDataRows(protocol), fields);

        // The nested test index being used contains 4 entries but one of them has an array of 2 message objects, so
        // we check to see if the amount of data rows is 5 since that is the result after flattening
        assertThat(
                Iterables.size(CheckPrettyFormatContents.getDataRows(protocol)),
                equalTo(5)
        );
    }

    @Test
    public void selectNestedFieldWithWildcard() {
        String query = String.format("SELECT nested(message.*) FROM %s", TestsConstants.TEST_INDEX_NESTED_TYPE);
        Protocol protocol = CheckPrettyFormatContents.execute(query, "jdbc");

        CheckPrettyFormatContents.containsColumnsInAnyOrder(CheckPrettyFormatContents.getSchema(protocol), messageFields);
        CheckPrettyFormatContents.containsData(CheckPrettyFormatContents.getDataRows(protocol), messageFields);
    }

    @Test
    public void selectWithWhere() {
        int balanceToCompare = 30000;
        String query = String.format("SELECT balance FROM %s/%s WHERE balance > %d",
                                     TestsConstants.TEST_INDEX_ACCOUNT, "account", balanceToCompare);
        Protocol protocol = CheckPrettyFormatContents.execute(query, "jdbc");

        DataRows dataRows = CheckPrettyFormatContents.getDataRows(protocol);
        for (Row row : dataRows) {
            int balance = (int) row.getData("balance");
            assertThat(balance, greaterThan(balanceToCompare));
        }
    }

    @Test
    public void groupBySingleField() {
        String query = String.format("SELECT * FROM %s GROUP BY age", TestsConstants.TEST_INDEX_ACCOUNT);
        Protocol protocol = CheckPrettyFormatContents.execute(query, "jdbc");

        List<String> fields = Arrays.asList("age");
        CheckPrettyFormatContents.containsColumns(CheckPrettyFormatContents.getSchema(protocol), fields);
        CheckPrettyFormatContents.containsData(CheckPrettyFormatContents.getDataRows(protocol), fields);
    }

    @Test
    public void groupByMultipleFields() {
        String query = String.format("SELECT age, balance FROM %s GROUP BY age, balance",
                TestsConstants.TEST_INDEX_ACCOUNT);
        Protocol protocol = CheckPrettyFormatContents.execute(query, "jdbc");

        List<String> fields = Arrays.asList("age", "balance");
        CheckPrettyFormatContents.containsColumns(CheckPrettyFormatContents.getSchema(protocol), fields);
        CheckPrettyFormatContents.containsData(CheckPrettyFormatContents.getDataRows(protocol), fields);
    }

    @Test
    public void testSizeAndTotal() {
        String query = String.format("SELECT * FROM %s WHERE balance > 30000 LIMIT 5", TestsConstants.TEST_INDEX_ACCOUNT);
        Protocol protocol = CheckPrettyFormatContents.execute(query, "jdbc");

        DataRows dataRows = CheckPrettyFormatContents.getDataRows(protocol);
        assertThat(dataRows.getSize(), equalTo((long) 5));
        // The value to compare here was obtained by running the query in the plugin and looking at the SearchHits
        assertThat(dataRows.getTotalHits(), equalTo((long) 402));
    }

    @Test
    public void testSizeWithGroupBy() {
        String query = String.format("SELECT * FROM %s GROUP BY age LIMIT 5", TestsConstants.TEST_INDEX_ACCOUNT);
        Protocol protocol = CheckPrettyFormatContents.execute(query, "jdbc");

        MatcherAssert.assertThat(
                CheckPrettyFormatContents.getDataRows(protocol).getSize(),
                equalTo((long) 5)
        );
    }

    @Test
    public void aggFunctionInSelect() {
        String query = String.format("SELECT COUNT(*) FROM %s GROUP BY age", TestsConstants.TEST_INDEX_ACCOUNT);
        Protocol protocol = CheckPrettyFormatContents.execute(query, "jdbc");

        String count = "COUNT(*)";
        List<String> fields = Arrays.asList("age", count);
        CheckPrettyFormatContents.containsColumns(CheckPrettyFormatContents.getSchema(protocol), fields);

        for(Row row : CheckPrettyFormatContents.getDataRows(protocol)) {
            assertThat(row.getContents(), hasKey(count));
            assertThat((double) row.getData(count), greaterThan((double) 0));
        }
    }

    @Test
    public void aggFunctionInSelectCaseCheck() {
        String query = String.format("SELECT count(*) FROM %s GROUP BY age", TestsConstants.TEST_INDEX_ACCOUNT);
    }

    @Test
    public void aggFunctionInSelectWithAlias() {
        String query = String.format("SELECT COUNT(*) AS total FROM %s GROUP BY age", TestsConstants.TEST_INDEX_ACCOUNT);
        Protocol protocol = CheckPrettyFormatContents.execute(query, "jdbc");

        String count = "total";
        List<String> fields = Arrays.asList("age", count);
        CheckPrettyFormatContents.containsColumns(CheckPrettyFormatContents.getSchema(protocol), fields);

        for (Row row : CheckPrettyFormatContents.getDataRows(protocol)) {
            assertThat(row.getContents(), hasKey(count));
            assertThat((double) row.getData(count), greaterThan((double) 0));
        }
    }

    @Test
    public void aggFunctionInSelectGroupByMultipleFields() {
        String query = String.format("SELECT SUM(age) FROM %s GROUP BY age, state.keyword", TestsConstants.TEST_INDEX_ACCOUNT);
        Protocol protocol = CheckPrettyFormatContents.execute(query, "jdbc");

        List<String> fields = Arrays.asList("age", "state.keyword", "SUM(age)");
        CheckPrettyFormatContents.containsColumns(CheckPrettyFormatContents.getSchema(protocol), fields);
        CheckPrettyFormatContents.containsData(CheckPrettyFormatContents.getDataRows(protocol), fields);
    }

    @Test
    public void aggFunctionInSelectNoGroupBy() {
        String query = String.format("SELECT SUM(age) FROM %s", TestsConstants.TEST_INDEX_ACCOUNT);
        Protocol protocol = CheckPrettyFormatContents.execute(query, "jdbc");

        String ageSum = "SUM(age)";
        CheckPrettyFormatContents.containsColumns(CheckPrettyFormatContents.getSchema(protocol), Arrays.asList(ageSum));

        for (Row row : CheckPrettyFormatContents.getDataRows(protocol)) {
            assertThat(row.getContents(), hasKey(ageSum));
            assertThat((double) row.getData(ageSum), greaterThan((double) 0));
        }
    }

    @Test
    public void multipleAggFunctionsInSelect() {
        String query = String.format("SELECT COUNT(*), AVG(age) FROM %s GROUP BY age", TestsConstants.TEST_INDEX_ACCOUNT);
        Protocol protocol = CheckPrettyFormatContents.execute(query, "jdbc");

        List<String> fields = Arrays.asList("age", "COUNT(*)", "AVG(age)");
        CheckPrettyFormatContents.containsColumns(CheckPrettyFormatContents.getSchema(protocol), fields);
        CheckPrettyFormatContents.containsData(CheckPrettyFormatContents.getDataRows(protocol), fields);
    }

    /**
     * This case doesn't seem to be support by NLP at the moment.
     * Looks like the painless script of the inner function is put inside the aggregation function but
     * this syntax may not be correct since it returns 0 which might be the default value (since 0 is returned in
     * cases like COUNT(wrongField) as well).
     */
//    @Test
//    public void nestedAggFunctionInSelect() {
//        String query = String.format("SELECT SUM(SQRT(age)) FROM age GROUP BY age", TEST_INDEX_ACCOUNT);
//    }

    @Test
    public void fieldsWithAlias() {
        String query = String.format("SELECT firstname AS first, age AS a FROM %s", TestsConstants.TEST_INDEX_ACCOUNT);
        Protocol protocol = CheckPrettyFormatContents.execute(query, "jdbc");

        Map<String, String> aliases = new HashMap<>();
        aliases.put("firstname", "first");
        aliases.put("age", "a");

        CheckPrettyFormatContents.containsAliases(CheckPrettyFormatContents.getSchema(protocol), aliases);
    }

    /** Test JDBC format response */
    @Test
    public void indexWithMissingFields() {
        String query = String.format("SELECT phrase, insert_time2 FROM %s WHERE match_phrase(phrase, 'brown fox')", TestsConstants.TEST_INDEX_PHRASE);
        JSONObject jdbcResponse = CheckPrettyFormatContents.getJdbcResponse(query);

        JSONArray dataRowEntry = (JSONArray) CheckPrettyFormatContents.getJdbcDataRows(jdbcResponse).get(0);

        assertThat(dataRowEntry.length(), equalTo(2));
        assertThat(dataRowEntry.get(0), equalTo("brown fox"));
        assertThat(dataRowEntry.get(1), equalTo(JSONObject.NULL));
    }

    @Test
    public void joinQuery() {
        String query = String.format("SELECT b1.balance, b1.age, b2.firstname FROM %s b1 JOIN %s b2 ON b1.age = b2.age", TestsConstants.TEST_INDEX_ACCOUNT, TestsConstants.TEST_INDEX_ACCOUNT);
        Protocol protocol = CheckPrettyFormatContents.execute(query, "jdbc");

        List<String> fields = Arrays.asList("b1.balance", "b1.age", "b2.firstname");
        CheckPrettyFormatContents.containsColumns(CheckPrettyFormatContents.getSchema(protocol), fields);
        CheckPrettyFormatContents.containsData(CheckPrettyFormatContents.getDataRows(protocol), fields);
    }

    @Test
    public void joinQueryWithAlias() {
        String query = String.format("SELECT b1.balance AS bal, b1.age AS age, b2.firstname AS name FROM %s b1 JOIN %s b2 ON b1.age = b2.age", TestsConstants.TEST_INDEX_ACCOUNT, TestsConstants.TEST_INDEX_ACCOUNT);
        Protocol protocol = CheckPrettyFormatContents.execute(query, "jdbc");

        Map<String, String> aliases = new HashMap<>();
        aliases.put("b1.balance", "bal");
        aliases.put("b1.age", "age");
        aliases.put("b2.firstname", "name");
        CheckPrettyFormatContents.containsAliases(CheckPrettyFormatContents.getSchema(protocol), aliases);
    }

    @Test
    public void joinQueryWithObjectFieldInSelect() {
        String query = String.format("SELECT c.name.firstname, d.name.lastname FROM %s c JOIN %s d ON d.hname = c.house", TestsConstants.TEST_INDEX_GAME_OF_THRONES, TestsConstants.TEST_INDEX_GAME_OF_THRONES);
        Protocol protocol = CheckPrettyFormatContents.execute(query, "jdbc");

        List<String> fields = Arrays.asList("c.name.firstname", "d.name.lastname");
        CheckPrettyFormatContents.containsColumns(CheckPrettyFormatContents.getSchema(protocol), fields);
        CheckPrettyFormatContents.containsData(CheckPrettyFormatContents.getDataRows(protocol), fields);
    }
}
