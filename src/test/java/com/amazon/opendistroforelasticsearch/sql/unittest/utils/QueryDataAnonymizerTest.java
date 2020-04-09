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

package com.amazon.opendistroforelasticsearch.sql.unittest.utils;

import com.amazon.opendistroforelasticsearch.sql.utils.QueryDataAnonymizer;
import org.junit.Assert;
import org.junit.Test;

public class QueryDataAnonymizerTest {

    @Test
    public void queriesShouldHaveAnonymousFieldAndIndex() {
        String query = "SELECT ABS(balance) FROM accounts WHERE age > 30 GROUP BY ABS(balance)";
        String expectedQuery = "(\n" +
                "\tSELECT ABS(identifier)\n" +
                "\tFROM table\n" +
                "\tWHERE identifier > number\n" +
                "\tGROUP BY ABS(identifier)\n" +
                "\t)";
        Assert.assertEquals(expectedQuery, QueryDataAnonymizer.anonymizeData(query));
    }

    @Test
    public void queriesShouldAnonymousNumbers() {
        String query = "SELECT ABS(20), LOG(20.20) FROM accounts";
        String expectedQuery = "(\n" +
                "\tSELECT ABS(number), LOG(number)\n" +
                "\tFROM table\n" +
                "\t)";
        Assert.assertEquals(expectedQuery, QueryDataAnonymizer.anonymizeData(query));
    }

    @Test
    public void queriesShouldHaveAnonymousBooleanLiterals() {
        String query = "SELECT TRUE FROM accounts";
        String expectedQuery = "(\n" +
                "\tSELECT boolean_literal\n" +
                "\tFROM table\n" +
                "\t)";
        Assert.assertEquals(expectedQuery, QueryDataAnonymizer.anonymizeData(query));
    }

    @Test
    public void queriesShouldHaveAnonymousInputStrings() {
        String query = "SELECT * FROM accounts WHERE name = 'Oliver'";
        String expectedQuery = "(\n" +
                "\tSELECT *\n" +
                "\tFROM table\n" +
                "\tWHERE identifier = 'string_literal'\n" +
                "\t)";
        Assert.assertEquals(expectedQuery, QueryDataAnonymizer.anonymizeData(query));
    }

    @Test
    public void queriesWithAliasesShouldAnonymizeSensitiveData() {
        String query = "SELECT balance AS b FROM accounts AS a";
        String expectedQuery = "(\n" +
                "\tSELECT identifier AS b\n" +
                "\tFROM table a\n" +
                "\t)";
        Assert.assertEquals(expectedQuery, QueryDataAnonymizer.anonymizeData(query));
    }

    @Test
    public void queriesWithFunctionsShouldAnonymizeSensitiveData() {
        String query = "SELECT LTRIM(firstname) FROM accounts";
        String expectedQuery = "(\n" +
                "\tSELECT LTRIM(identifier)\n" +
                "\tFROM table\n" +
                "\t)";
        Assert.assertEquals(expectedQuery, QueryDataAnonymizer.anonymizeData(query));
    }

    @Test
    public void queriesWithAggregatesShouldAnonymizeSensitiveData() {
        String query = "SELECT MAX(price) - MIN(price) from tickets";
        String expectedQuery = "(\n" +
                "\tSELECT MAX(identifier) - MIN(identifier)\n" +
                "\tFROM table\n" +
                "\t)";
        Assert.assertEquals(expectedQuery, QueryDataAnonymizer.anonymizeData(query));
    }

    @Test
    public void queriesWithSubqueriesShouldAnonymizeSensitiveData() {
        String query = "SELECT a.f, a.l, a.a FROM " +
                "(SELECT firstname AS f, lastname AS l, age AS a FROM accounts WHERE age > 30) " +
                "AS a";
        String expectedQuery = "(\n" +
                "\tSELECT identifier, identifier, identifier\n" +
                "\tFROM (SELECT identifier AS f, identifier AS l, identifier AS a\n" +
                "\t\tFROM table\n" +
                "\t\tWHERE identifier > number\n" +
                "\t\t) a\n" +
                "\t)";
        Assert.assertEquals(expectedQuery, QueryDataAnonymizer.anonymizeData(query));
    }

    @Test
    public void joinQueriesShouldAnonymizeSensitiveData() {
        String query = "SELECT a.account_number, a.firstname, a.lastname, e.id, e.name FROM accounts a JOIN employees e";
        String expectedQuery = "(\n" +
                "\tSELECT identifier, identifier, identifier, identifier, identifier\n" +
                "\tFROM table a\n" +
                "\t\tJOIN table e\n" +
                "\t)";
        Assert.assertEquals(expectedQuery, QueryDataAnonymizer.anonymizeData(query));
    }

    @Test
    public void unionQueriesShouldAnonymiazeSensitiveData() {
        String query = "SELECT name, age FROM accounts UNION SELECT name, age FROM employees";
        String expectedQuery = "(\n" +
                "\tSELECT identifier, identifier\n" +
                "\tFROM table\n" +
                "\tUNION\n" +
                "\tSELECT identifier, identifier\n" +
                "\tFROM table\n" +
                "\t)";
        Assert.assertEquals(expectedQuery, QueryDataAnonymizer.anonymizeData(query));
    }
}
