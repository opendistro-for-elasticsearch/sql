package org.nlpcn.es4sql.intgtest;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.elasticsearch.plugin.nlpcn.DataRows;
import org.elasticsearch.plugin.nlpcn.DataRows.Row;
import org.elasticsearch.plugin.nlpcn.Protocol;
import org.elasticsearch.plugin.nlpcn.Schema;
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
import static org.nlpcn.es4sql.TestsConstants.TEST_INDEX_ACCOUNT;
import static org.nlpcn.es4sql.TestsConstants.TEST_INDEX_NESTED_TYPE;
import static org.nlpcn.es4sql.TestsConstants.TEST_INDEX_PHRASE;
import static org.nlpcn.es4sql.util.CheckPrettyFormatContents.*;

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
        String query = String.format("SELECT * FROM %s", TEST_INDEX_ACCOUNT);
        Protocol protocol = execute(query, "jdbc");

        Schema schema = getSchema(protocol);
        assertThat(schema.getIndexName(), equalTo(TEST_INDEX_ACCOUNT));
        assertThat(schema.getTypeName(), equalTo(null));

        containsColumnsInAnyOrder(schema, allAccountFields);
    }

    @Test
    public void withIndexType() {
        String query = String.format("SELECT * FROM %s/%s", TEST_INDEX_ACCOUNT, "account");
        Protocol protocol = execute(query, "jdbc");

        Schema schema = getSchema(protocol);
        assertThat(schema.getIndexName(), equalTo(TEST_INDEX_ACCOUNT));
        assertThat(schema.getTypeName(), equalTo("account"));
    }

    @Test
    public void wrongIndexType() {
        String type = "wrongType";
        try {
            String query = String.format("SELECT * FROM %s/%s", TEST_INDEX_ACCOUNT, type);
            Protocol protocol = execute(query, "jdbc");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is(String.format("Index type %s does not exist", type)));
        }
    }

    /** Test Protocol as a whole */
    @Test
    public void selectAll() {
        String query = String.format("SELECT * FROM %s/%s", TEST_INDEX_ACCOUNT, "account");
        Protocol protocol = execute(query, "jdbc");

        // This also tests that .keyword fields are ignored when SELECT * is called
        containsColumnsInAnyOrder(getSchema(protocol), allAccountFields);
        containsData(getDataRows(protocol), allAccountFields);
    }

    @Test
    public void selectNames() {
        String query = String.format("SELECT firstname, lastname FROM %s/%s", TEST_INDEX_ACCOUNT, "account");
        Protocol protocol = execute(query, "jdbc");

        containsColumns(getSchema(protocol), nameFields);
        containsData(getDataRows(protocol), nameFields);
    }

    @Test
    public void selectWrongField() {
        String query = String.format("SELECT wrongField FROM %s", TEST_INDEX_ACCOUNT);
        Protocol protocol = execute(query, "jdbc");

        assertThat(
                Iterables.size(getSchema(protocol)),
                equalTo(0)
        );
        // DataRows still gets populated with SearchHits but wrongField is not available in the Map so nothing is
        // outputted in the final result
        assertThat(
                Iterables.size(getDataRows(protocol)),
                equalTo(200)
        );
    }

    @Test
    public void selectKeyword() {
        String query = String.format("SELECT firstname.keyword FROM %s", TEST_INDEX_ACCOUNT);
        Protocol protocol = execute(query, "jdbc");

        List<String> fields = Arrays.asList("firstname.keyword");
        containsColumns(getSchema(protocol), fields);

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
        String query = String.format("SELECT _score FROM %s WHERE balance > 30000", TEST_INDEX_ACCOUNT);
        Protocol protocol = execute(query, "jdbc");

        List<String> fields = Arrays.asList("_score");
        containsColumns(getSchema(protocol), fields);
        containsData(getDataRows(protocol), fields);
    }

    @Test
    public void selectAllFromNestedWithoutFieldInFrom() {
        String query = String.format("SELECT * FROM %s", TEST_INDEX_NESTED_TYPE);
        Protocol protocol = execute(query, "jdbc");

        containsColumnsInAnyOrder(getSchema(protocol), regularFields);
        containsData(getDataRows(protocol), regularFields);
    }

    @Test
    public void selectAllFromNestedWithFieldInFrom() {
        String query = String.format("SELECT * FROM %s e, e.message m", TEST_INDEX_NESTED_TYPE);
        Protocol protocol = execute(query, "jdbc");

        containsColumnsInAnyOrder(getSchema(protocol), messageFields);
        containsData(getDataRows(protocol), messageFields);
    }

    @Test
    public void selectNestedFields() {
        String query = String.format("SELECT nested(message.info), someField FROM %s", TEST_INDEX_NESTED_TYPE);
        Protocol protocol = execute(query, "jdbc");

        List<String> fields = Arrays.asList("message.info", "someField");
        containsColumns(getSchema(protocol), fields);
        containsData(getDataRows(protocol), fields);

        // The nested test index being used contains 4 entries but one of them has an array of 2 message objects, so
        // we check to see if the amount of data rows is 5 since that is the result after flattening
        assertThat(
                Iterables.size(getDataRows(protocol)),
                equalTo(5)
        );
    }

    @Test
    public void selectNestedFieldWithWildcard() {
        String query = String.format("SELECT nested(message.*) FROM %s", TEST_INDEX_NESTED_TYPE);
        Protocol protocol = execute(query, "jdbc");

        containsColumnsInAnyOrder(getSchema(protocol), messageFields);
        containsData(getDataRows(protocol), messageFields);
    }

    @Test
    public void selectWithWhere() {
        int balanceToCompare = 30000;
        String query = String.format("SELECT balance FROM %s/%s WHERE balance > %d",
                                     TEST_INDEX_ACCOUNT, "account", balanceToCompare);
        Protocol protocol = execute(query, "jdbc");

        DataRows dataRows = getDataRows(protocol);
        for (Row row : dataRows) {
            int balance = (int) row.getData("balance");
            assertThat(balance, greaterThan(balanceToCompare));
        }
    }

    @Test
    public void groupBySingleField() {
        String query = String.format("SELECT * FROM %s GROUP BY age", TEST_INDEX_ACCOUNT);
        Protocol protocol = execute(query, "jdbc");

        List<String> fields = Arrays.asList("age");
        containsColumns(getSchema(protocol), fields);
        containsData(getDataRows(protocol), fields);
    }

    @Test
    public void groupByMultipleFields() {
        String query = String.format("SELECT age, balance FROM %s GROUP BY age, balance",
                TEST_INDEX_ACCOUNT);
        Protocol protocol = execute(query, "jdbc");

        List<String> fields = Arrays.asList("age", "balance");
        containsColumns(getSchema(protocol), fields);
        containsData(getDataRows(protocol), fields);
    }

    @Test
    public void testSizeAndTotal() {
        String query = String.format("SELECT * FROM %s WHERE balance > 30000 LIMIT 5", TEST_INDEX_ACCOUNT);
        Protocol protocol = execute(query, "jdbc");

        DataRows dataRows = getDataRows(protocol);
        assertThat(dataRows.getSize(), equalTo((long) 5));
        // The value to compare here was obtained by running the query in the plugin and looking at the SearchHits
        assertThat(dataRows.getTotalHits(), equalTo((long) 402));
    }

    @Test
    public void testSizeWithGroupBy() {
        String query = String.format("SELECT * FROM %s GROUP BY age LIMIT 5", TEST_INDEX_ACCOUNT);
        Protocol protocol = execute(query, "jdbc");

        assertThat(
                getDataRows(protocol).getSize(),
                equalTo((long) 5)
        );
    }

    @Test
    public void aggFunctionInSelect() {
        String query = String.format("SELECT COUNT(*) FROM %s GROUP BY age", TEST_INDEX_ACCOUNT);
        Protocol protocol = execute(query, "jdbc");

        String count = "COUNT(*)";
        List<String> fields = Arrays.asList("age", count);
        containsColumns(getSchema(protocol), fields);

        for(Row row : getDataRows(protocol)) {
            assertThat(row.getContents(), hasKey(count));
            assertThat((double) row.getData(count), greaterThan((double) 0));
        }
    }

    @Test
    public void aggFunctionInSelectCaseCheck() {
        String query = String.format("SELECT count(*) FROM %s GROUP BY age", TEST_INDEX_ACCOUNT);
    }

    @Test
    public void aggFunctionInSelectWithAlias() {
        String query = String.format("SELECT COUNT(*) AS total FROM %s GROUP BY age", TEST_INDEX_ACCOUNT);
        Protocol protocol = execute(query, "jdbc");

        String count = "total";
        List<String> fields = Arrays.asList("age", count);
        containsColumns(getSchema(protocol), fields);

        for (Row row : getDataRows(protocol)) {
            assertThat(row.getContents(), hasKey(count));
            assertThat((double) row.getData(count), greaterThan((double) 0));
        }
    }

    @Test
    public void aggFunctionInSelectGroupByMultipleFields() {
        String query = String.format("SELECT SUM(age) FROM %s GROUP BY age, state.keyword", TEST_INDEX_ACCOUNT);
        Protocol protocol = execute(query, "jdbc");

        List<String> fields = Arrays.asList("age", "state.keyword", "SUM(age)");
        containsColumns(getSchema(protocol), fields);
        containsData(getDataRows(protocol), fields);
    }

    @Test
    public void aggFunctionInSelectNoGroupBy() {
        String query = String.format("SELECT SUM(age) FROM %s", TEST_INDEX_ACCOUNT);
        Protocol protocol = execute(query, "jdbc");

        String ageSum = "SUM(age)";
        containsColumns(getSchema(protocol), Arrays.asList(ageSum));

        for (Row row : getDataRows(protocol)) {
            assertThat(row.getContents(), hasKey(ageSum));
            assertThat((double) row.getData(ageSum), greaterThan((double) 0));
        }
    }

    @Test
    public void multipleAggFunctionsInSelect() {
        String query = String.format("SELECT COUNT(*), AVG(age) FROM %s GROUP BY age", TEST_INDEX_ACCOUNT);
        Protocol protocol = execute(query, "jdbc");

        List<String> fields = Arrays.asList("age", "COUNT(*)", "AVG(age)");
        containsColumns(getSchema(protocol), fields);
        containsData(getDataRows(protocol), fields);
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
        String query = String.format("SELECT firstname AS first, age AS a FROM %s", TEST_INDEX_ACCOUNT);
        Protocol protocol = execute(query, "jdbc");

        Map<String, String> aliases = new HashMap<>();
        aliases.put("firstname", "first");
        aliases.put("age", "a");

        containsAliases(getSchema(protocol), aliases);
    }

    /** Test JDBC format response */
    @Test
    public void indexWithMissingFields() {
        String query = String.format("SELECT phrase, insert_time2 FROM %s WHERE phrase = 'brown fox'", TEST_INDEX_PHRASE);
        JSONObject jdbcResponse = getJdbcResponse(query);

        JSONArray dataRowEntry = (JSONArray) getJdbcDataRows(jdbcResponse).get(0);

        assertThat(dataRowEntry.length(), equalTo(2));
        assertThat(dataRowEntry.get(0), equalTo("brown fox"));
        assertThat(dataRowEntry.get(1), equalTo(JSONObject.NULL));
    }
}
