package org.nlpcn.es4sql.unittest;

import org.junit.BeforeClass;
import org.junit.Test;
import org.nlpcn.es4sql.parse.ScriptFilter;
import org.nlpcn.es4sql.parse.SqlParser;

import static org.elasticsearch.search.builder.SearchSourceBuilder.ScriptField;
import static org.junit.Assert.assertTrue;
import static org.nlpcn.es4sql.util.CheckScriptContents.getScriptFieldFromQuery;
import static org.nlpcn.es4sql.util.CheckScriptContents.getScriptFilterFromQuery;
import static org.nlpcn.es4sql.util.CheckScriptContents.scriptContainsString;
import static org.nlpcn.es4sql.util.CheckScriptContents.scriptHasPattern;

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
                        "doc['creationDate'].date.year"));
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
                        "doc['creationDate'].date.year"));
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
                        "doc['creationDate'].date.weekOfWeekyear"));
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
                        "doc['creationDate'].date.weekOfWeekyear"));
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
                        "doc['creationDate'].date.dayOfMonth"));
    }

    @Test
    public void hourOfDay() {
        String query = "SELECT HOUR_OF_DAY(creationDate) " +
                       "FROM dates";
        ScriptField scriptField = getScriptFieldFromQuery(query);
        assertTrue(
                scriptContainsString(
                        scriptField,
                        "doc['creationDate'].date.hourOfDay"));
    }

    @Test
    public void secondOfMinute() {
        String query = "SELECT SECOND_OF_MINUTE(creationDate) " +
                       "FROM dates";
        ScriptField scriptField = getScriptFieldFromQuery(query);
        assertTrue(
                scriptContainsString(
                        scriptField,
                        "doc['creationDate'].date.secondOfMinute"));
    }

}
