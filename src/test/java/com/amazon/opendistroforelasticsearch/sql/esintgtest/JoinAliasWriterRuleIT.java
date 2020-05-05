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

import org.elasticsearch.client.ResponseException;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;

/**
 * Test cases for writing missing join table aliases.
 */
public class JoinAliasWriterRuleIT extends SQLIntegTestCase {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    protected void init() throws Exception {
        loadIndex(Index.ORDER);     // elasticsearch-sql_test_index_order
        loadIndex(Index.BANK);      // elasticsearch-sql_test_index_bank
        loadIndex(Index.BANK_TWO);  // elasticsearch-sql_test_index_bank_two
    }

    @Test
    public void noTableAliasNoCommonColumns() throws IOException {
        sameExplain(
            query(
                "SELECT id, firstname" ,
                "FROM elasticsearch-sql_test_index_order",
                "INNER JOIN elasticsearch-sql_test_index_bank ",
                "ON name = firstname WHERE state = 'WA' OR id < 7"),
            query(
                "SELECT elasticsearch-sql_test_index_order_0.id, elasticsearch-sql_test_index_bank_1.firstname ",
                "FROM elasticsearch-sql_test_index_order elasticsearch-sql_test_index_order_0 ",
                "INNER JOIN elasticsearch-sql_test_index_bank elasticsearch-sql_test_index_bank_1 " ,
                "ON elasticsearch-sql_test_index_order_0.name = elasticsearch-sql_test_index_bank_1.firstname ",
                "WHERE elasticsearch-sql_test_index_bank_1.state = 'WA' OR elasticsearch-sql_test_index_order_0.id < 7")
        );
    }

    @Test
    public void oneTableAliasNoCommonColumns() throws IOException {
        sameExplain(
            query(
                "SELECT id, firstname ",
                "FROM elasticsearch-sql_test_index_order a ",
                "INNER JOIN elasticsearch-sql_test_index_bank ",
                "ON name = firstname WHERE state = 'WA' OR id < 7"),
            query(
                "SELECT a.id, elasticsearch-sql_test_index_bank_0.firstname ",
                "FROM elasticsearch-sql_test_index_order a ",
                "INNER JOIN elasticsearch-sql_test_index_bank elasticsearch-sql_test_index_bank_0 ",
                "ON a.name = elasticsearch-sql_test_index_bank_0.firstname ",
                "WHERE elasticsearch-sql_test_index_bank_0.state = 'WA' OR a.id < 7")
        );
    }

    @Test
    public void bothTableAliasNoCommonColumns() throws IOException {
        sameExplain(
            query(
                "SELECT id, firstname ",
                "FROM elasticsearch-sql_test_index_order a ",
                "INNER JOIN elasticsearch-sql_test_index_bank b ",
                "ON name = firstname WHERE state = 'WA' OR id < 7 "),
            query(
                "SELECT a.id, b.firstname ",
                "FROM elasticsearch-sql_test_index_order a ",
                "INNER JOIN elasticsearch-sql_test_index_bank b ",
                "ON a.name = b.firstname ",
                "WHERE b.state = 'WA' OR a.id < 7 ")
        );
    }

    @Test
    public void tableNamesWithTypeName() throws IOException {
        sameExplain(
            query(
                "SELECT id, firstname ",
                "FROM elasticsearch-sql_test_index_order/_doc ",
                "INNER JOIN elasticsearch-sql_test_index_bank/account ",
                "ON name = firstname WHERE state = 'WA' OR id < 7"),
            query(
                "SELECT elasticsearch-sql_test_index_order_0.id, elasticsearch-sql_test_index_bank_1.firstname ",
                "FROM elasticsearch-sql_test_index_order/_doc elasticsearch-sql_test_index_order_0 ",
                "INNER JOIN elasticsearch-sql_test_index_bank/_account elasticsearch-sql_test_index_bank_1 ",
                "ON elasticsearch-sql_test_index_order_0.name = elasticsearch-sql_test_index_bank_1.firstname ",
                "WHERE elasticsearch-sql_test_index_bank_1.state = 'WA' OR elasticsearch-sql_test_index_order_0.id < 7")
        );
    }

    @Ignore
    @Test
    public void tableNamesWithTypeNameExplicitTableAlias() throws IOException {
        sameExplain(
            query(
                "SELECT id, firstname " ,
                "FROM elasticsearch-sql_test_index_order/_doc a " ,
                "INNER JOIN elasticsearch-sql_test_index_bank/account b ",
                "ON name = firstname WHERE state = 'WA' OR id < 7"),
            query(
                "SELECT a.id, b.firstname ",
                "FROM elasticsearch-sql_test_index_order a ",
                "INNER JOIN elasticsearch-sql_test_index_bank b " ,
                "ON a.name = b.firstname ",
                "WHERE b.state = 'WA' OR a.id < 7")
        );
    }

    @Test
    public void actualTableNameAsAliasOnColumnFields() throws IOException {
        sameExplain(
            query(
                "SELECT elasticsearch-sql_test_index_order.id, b.firstname " ,
                "FROM elasticsearch-sql_test_index_order " ,
                "INNER JOIN elasticsearch-sql_test_index_bank b ",
                "ON elasticsearch-sql_test_index_order.name = firstname WHERE state = 'WA' OR id < 7"),
            query(
                "SELECT elasticsearch-sql_test_index_order_0.id, b.firstname ",
                "FROM elasticsearch-sql_test_index_order  elasticsearch-sql_test_index_order_0 ",
                "INNER JOIN elasticsearch-sql_test_index_bank b " ,
                "ON elasticsearch-sql_test_index_order_0.name = b.firstname ",
                "WHERE b.state = 'WA' OR elasticsearch-sql_test_index_order_0.id < 7")
        );
    }

    @Test
    public void actualTableNameAsAliasOnColumnFieldsTwo() throws IOException {
        sameExplain(
            query(
                "SELECT elasticsearch-sql_test_index_order.id, elasticsearch-sql_test_index_bank.firstname " ,
                "FROM elasticsearch-sql_test_index_order " ,
                "INNER JOIN elasticsearch-sql_test_index_bank ",
                "ON elasticsearch-sql_test_index_order.name = firstname ",
                "WHERE elasticsearch-sql_test_index_bank.state = 'WA' OR id < 7"),
            query(
                "SELECT elasticsearch-sql_test_index_order_0.id, elasticsearch-sql_test_index_bank_1.firstname ",
                "FROM elasticsearch-sql_test_index_order  elasticsearch-sql_test_index_order_0 ",
                "INNER JOIN elasticsearch-sql_test_index_bank elasticsearch-sql_test_index_bank_1" ,
                "ON elasticsearch-sql_test_index_order_0.name = elasticsearch-sql_test_index_bank_1.firstname ",
                "WHERE elasticsearch-sql_test_index_bank_1.state = 'WA' OR elasticsearch-sql_test_index_order_0.id < 7")
        );
    }

    @Test
    public void columnsWithTableAliasNotAffected() throws IOException {
        sameExplain(
            query(
                "SELECT a.id, firstname ",
                "FROM elasticsearch-sql_test_index_order a ",
                "INNER JOIN elasticsearch-sql_test_index_bank b ",
                "ON name = b.firstname WHERE state = 'WA' OR a.id < 7"),
            query(
                "SELECT a.id, b.firstname ",
                "FROM elasticsearch-sql_test_index_order a ",
                "INNER JOIN elasticsearch-sql_test_index_bank b ",
                "ON a.name = b.firstname ",
                "WHERE b.state = 'WA' OR a.id < 7")
        );
    }

    @Test
    public void commonColumnWithoutTableAliasDifferentTables() throws IOException {
        exception.expect(ResponseException.class);
        exception.expectMessage("Field name [firstname] is ambiguous");
        String explain = explainQuery(query(
            "SELECT firstname, lastname ",
            "FROM elasticsearch-sql_test_index_bank ",
            "LEFT JOIN elasticsearch-sql_test_index_bank_two ",
            "ON firstname = lastname WHERE state = 'VA' "
            ));
    }

    @Test
    public void sameTablesNoAliasAndNoAliasOnColumns() throws IOException {
        exception.expect(ResponseException.class);
        exception.expectMessage("Not unique table/alias: [elasticsearch-sql_test_index_bank]");
        String explain = explainQuery(query(
            "SELECT firstname, lastname ",
            "FROM elasticsearch-sql_test_index_bank ",
            "LEFT JOIN elasticsearch-sql_test_index_bank ",
            "ON firstname = lastname WHERE state = 'VA' "
        ));
    }

    @Test
    public void sameTablesNoAliasWithTableNameAsAliasOnColumns() throws IOException {
        exception.expect(ResponseException.class);
        exception.expectMessage("Not unique table/alias: [elasticsearch-sql_test_index_bank]");
        String explain = explainQuery(query(
            "SELECT elasticsearch-sql_test_index_bank.firstname",
            "FROM elasticsearch-sql_test_index_bank ",
            "JOIN elasticsearch-sql_test_index_bank ",
            "ON elasticsearch-sql_test_index_bank.firstname = elasticsearch-sql_test_index_bank.lastname"
        ));
    }

    @Test
    public void sameTablesWithExplicitAliasOnFirst() throws IOException {
        sameExplain(
            query(
                "SELECT elasticsearch-sql_test_index_bank.firstname, a.lastname ",
                "FROM elasticsearch-sql_test_index_bank a",
                "JOIN elasticsearch-sql_test_index_bank ",
                "ON elasticsearch-sql_test_index_bank.firstname = a.lastname "
            ),
            query(
                "SELECT elasticsearch-sql_test_index_bank_0.firstname, a.lastname ",
                "FROM elasticsearch-sql_test_index_bank a",
                "JOIN  elasticsearch-sql_test_index_bank elasticsearch-sql_test_index_bank_0",
                "ON elasticsearch-sql_test_index_bank_0.firstname = a.lastname "
            )

        );
    }

    @Test
    public void sameTablesWithExplicitAliasOnSecond() throws IOException {
        sameExplain(
            query(
                "SELECT elasticsearch-sql_test_index_bank.firstname, a.lastname ",
                "FROM elasticsearch-sql_test_index_bank ",
                "JOIN elasticsearch-sql_test_index_bank a",
                "ON elasticsearch-sql_test_index_bank.firstname = a.lastname "
            ),
            query(
                "SELECT elasticsearch-sql_test_index_bank_0.firstname, a.lastname ",
                "FROM elasticsearch-sql_test_index_bank elasticsearch-sql_test_index_bank_0",
                "JOIN  elasticsearch-sql_test_index_bank a",
                "ON elasticsearch-sql_test_index_bank_0.firstname = a.lastname "
            )

        );
    }

    private void sameExplain(String actualQuery, String expectedQuery) throws IOException {
        assertThat(explainQuery(actualQuery), equalTo(explainQuery(expectedQuery)));
    }

    private String query(String... statements) {
        return String.join(" ", statements);
    }
}
