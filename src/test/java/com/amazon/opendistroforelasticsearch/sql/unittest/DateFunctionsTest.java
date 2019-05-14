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

package com.amazon.opendistroforelasticsearch.sql.unittest;

import com.amazon.opendistroforelasticsearch.sql.parser.ScriptFilter;
import com.amazon.opendistroforelasticsearch.sql.parser.SqlParser;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.elasticsearch.search.builder.SearchSourceBuilder.ScriptField;
import static org.junit.Assert.assertTrue;
import static com.amazon.opendistroforelasticsearch.sql.util.CheckScriptContents.getScriptFieldFromQuery;
import static com.amazon.opendistroforelasticsearch.sql.util.CheckScriptContents.getScriptFilterFromQuery;
import static com.amazon.opendistroforelasticsearch.sql.util.CheckScriptContents.scriptContainsString;
import static com.amazon.opendistroforelasticsearch.sql.util.CheckScriptContents.scriptHasPattern;

public class DateFunctionsTest {

    private static SqlParser parser;

    @BeforeClass
    public static void init() { parser = new SqlParser(); }

    /**
     * The following unit tests will only cover a subset of the available date functions as the painless script is
     * generated from the same template. More thorough testing will be done in integration tests since output will
     * differ for each function.
     */

    @Test
    public void yearInSelect() {
        String query = "SELECT YEAR(creationDate) " +
                       "FROM dates";
        ScriptField scriptField = getScriptFieldFromQuery(query);
        assertTrue(
                scriptContainsString(
                        scriptField,
                        "doc['creationDate'].value.year"));
    }

    @Test
    public void yearInWhere() {
        String query = "SELECT * " +
                       "FROM dates " +
                       "WHERE YEAR(creationDate) > 2012";
        ScriptFilter scriptFilter = getScriptFilterFromQuery(query, parser);
        assertTrue(
                scriptContainsString(
                        scriptFilter,
                        "doc['creationDate'].value.year"));
        assertTrue(
                scriptHasPattern(
                        scriptFilter,
                        "year_\\d+ > 2012"));
    }

    @Test
    public void weekOfYearInSelect() {
        String query = "SELECT WEEK_OF_YEAR(creationDate) " +
                       "FROM dates";
        ScriptField scriptField = getScriptFieldFromQuery(query);
        assertTrue(
                scriptContainsString(
                        scriptField,
                        "doc['creationDate'].value.weekOfWeekyear"));
    }

    @Test
    public void weekOfYearInWhere() {
        String query = "SELECT * " +
                       "FROM dates " +
                       "WHERE WEEK_OF_YEAR(creationDate) > 15";
        ScriptFilter scriptFilter = getScriptFilterFromQuery(query, parser);
        assertTrue(
                scriptContainsString(
                        scriptFilter,
                        "doc['creationDate'].value.weekOfWeekyear"));
        assertTrue(
                scriptHasPattern(
                        scriptFilter,
                        "weekOfWeekyear_\\d+ > 15"));
    }

    @Test
    public void dayOfMonth() {
        String query = "SELECT DAY_OF_MONTH(creationDate) " +
                       "FROM dates";
        ScriptField scriptField = getScriptFieldFromQuery(query);
        assertTrue(
                scriptContainsString(
                        scriptField,
                        "doc['creationDate'].value.dayOfMonth"));
    }

    @Test
    public void hourOfDay() {
        String query = "SELECT HOUR_OF_DAY(creationDate) " +
                       "FROM dates";
        ScriptField scriptField = getScriptFieldFromQuery(query);
        assertTrue(
                scriptContainsString(
                        scriptField,
                        "doc['creationDate'].value.hourOfDay"));
    }

    @Test
    public void secondOfMinute() {
        String query = "SELECT SECOND_OF_MINUTE(creationDate) " +
                       "FROM dates";
        ScriptField scriptField = getScriptFieldFromQuery(query);
        assertTrue(
                scriptContainsString(
                        scriptField,
                        "doc['creationDate'].value.secondOfMinute"));
    }

}
