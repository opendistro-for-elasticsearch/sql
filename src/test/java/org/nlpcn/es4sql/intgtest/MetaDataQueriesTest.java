package org.nlpcn.es4sql.intgtest;

import org.elasticsearch.plugin.nlpcn.DataRows.Row;
import org.elasticsearch.plugin.nlpcn.Protocol;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.nlpcn.es4sql.intgtest.TestsConstants.TEST_INDEX;
import static org.nlpcn.es4sql.intgtest.TestsConstants.TEST_INDEX_ACCOUNT;
import static org.nlpcn.es4sql.util.CheckPrettyFormatContents.*;

public class MetaDataQueriesTest {

    /** The following are tests for SHOW/DESCRIBE query support under Pretty Format Response protocol */

    @Test
    public void showSingleIndex() {
        String query = String.format("SHOW TABLES LIKE %s", TEST_INDEX_ACCOUNT);
        Protocol protocol = execute(query, "jdbc");

        List<String> fields = Arrays.asList("TABLE_CAT", "TABLE_NAME", "TABLE_TYPE");
        containsData(getDataRows(protocol), fields);
    }

    @Test
    public void showCaseSensitivityCheck() {
        String query = String.format("show tables like %s", TEST_INDEX_ACCOUNT);
        Protocol protocol = execute(query, "jdbc");

        List<String> fields = Arrays.asList("TABLE_CAT", "TABLE_NAME", "TABLE_TYPE");
        containsData(getDataRows(protocol), fields);
    }

    @Test
    public void showWildcardIndex() {
        String query = String.format("SHOW TABLES LIKE %s%%", TEST_INDEX);
        Protocol protocol = execute(query, "jdbc");

        String pattern = String.format("%s.*", TEST_INDEX);
        for (Row row : getDataRows(protocol)) {
            String tableName = (String) row.getData("TABLE_NAME");
            assertTrue(tableName.matches(pattern));
        }
    }

    @Test
    public void describeSingleIndex() {
        String query = String.format("DESCRIBE TABLES LIKE %s", TEST_INDEX_ACCOUNT);
        Protocol protocol = execute(query, "jdbc");

        // Schema for DESCRIBE is filled with a lot of fields that aren't used so only the important
        // ones are checked for here
        List<String> fields = Arrays.asList("TABLE_NAME", "COLUMN_NAME", "TYPE_NAME");

        // Since Schema is loaded statically for DESCRIBE, just the DataRows are checked in this test
        containsData(getDataRows(protocol), fields);
    }

    @Test
    public void describeCaseSensitivityCheck() {
        String query = String.format("describe tables like %s", TEST_INDEX_ACCOUNT);
        Protocol protocol = execute(query, "jdbc");

        List<String> fields = Arrays.asList("TABLE_NAME", "COLUMN_NAME", "TYPE_NAME");
        containsData(getDataRows(protocol), fields);
    }

    @Test
    public void describeWildcardIndex() {
        String query = String.format("DESCRIBE TABLES LIKE %s%%", TEST_INDEX);
        Protocol protocol = execute(query, "jdbc");

        String pattern = String.format("%s.*", TEST_INDEX);
        for (Row row : getDataRows(protocol)) {
            String tableName = (String) row.getData("TABLE_NAME");
            assertTrue(tableName.matches(pattern));
        }
    }

    @Test
    public void describeWildcardColumn() {
        String query = String.format("DESCRIBE TABLES LIKE %s COLUMNS LIKE %%name", TEST_INDEX_ACCOUNT);
        Protocol protocol = execute(query, "jdbc");

        String pattern = ".*name";
        for (Row row : getDataRows(protocol)) {
            String columnName = (String) row.getData("COLUMN_NAME");
            assertTrue(columnName.matches(pattern));
        }
    }

    @Test
    public void describeSingleCharacterWildcard() {
        String query = String.format("DESCRIBE TABLES LIKE %s COLUMNS LIKE %%na_e", TEST_INDEX_ACCOUNT);
        Protocol protocol = execute(query, "jdbc");

        String pattern = ".*na.e";
        for (Row row : getDataRows(protocol)) {
            String columnName = (String) row.getData("COLUMN_NAME");
            assertTrue(columnName.matches(pattern));
        }
    }
}
