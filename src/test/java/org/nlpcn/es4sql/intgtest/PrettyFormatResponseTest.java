package org.nlpcn.es4sql.intgtest;

import com.alibaba.druid.sql.parser.ParserException;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.elasticsearch.client.Client;
import org.elasticsearch.plugin.nlpcn.DataRows;
import org.elasticsearch.plugin.nlpcn.DataRows.Row;
import org.elasticsearch.plugin.nlpcn.Protocol;
import org.elasticsearch.plugin.nlpcn.QueryActionElasticExecutor;
import org.elasticsearch.plugin.nlpcn.Schema;
import org.elasticsearch.plugin.nlpcn.Schema.Column;
import org.junit.Test;
import org.nlpcn.es4sql.MainTestSuite;
import org.nlpcn.es4sql.SearchDao;
import org.nlpcn.es4sql.domain.Query;
import org.nlpcn.es4sql.exception.SqlParseException;
import org.nlpcn.es4sql.query.QueryAction;

import java.io.IOException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;
import static org.nlpcn.es4sql.TestsConstants.TEST_INDEX_ACCOUNT;

public class PrettyFormatResponseTest {

    private static Set<String> allFields = Sets.newHashSet(
            "account_number", "balance", "firstname", "lastname", "age", "gender", "address", "employer",
            "email", "city", "state"
    );

    private static List<String> nameFields = Arrays.asList("firstname", "lastname");

    /**
     * Tests for core classes (Protocol, Schema, DataRows, etc.)
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

        containsColumnsInAnyOrder(protocol, allFields);
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
        String query = String.format("SELECT * FROM %s/%s", TEST_INDEX_ACCOUNT, "wrongType");
        Protocol protocol = execute(query, "jdbc");

        Schema schema = getSchema(protocol);
        assertThat(schema.getIndexName(), equalTo(TEST_INDEX_ACCOUNT));
        assertThat(Iterables.size(schema), equalTo(0));
    }

    /** Test Protocol as a whole */
    @Test
    public void selectAll() {
        String query = String.format("SELECT * FROM %s/%s", TEST_INDEX_ACCOUNT, "account");
        Protocol protocol = execute(query, "jdbc");

        containsColumnsInAnyOrder(protocol, allFields);
        containsData(protocol, allFields);
    }

    @Test
    public void selectNames() {
        String query = String.format("SELECT firstname, lastname FROM %s/%s", TEST_INDEX_ACCOUNT, "account");
        Protocol protocol = execute(query, "jdbc");

        containsColumns(protocol, nameFields);
        containsData(protocol, nameFields);
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

    private QueryAction getQueryAction(String sql) {
        try {
            SearchDao searchDao = MainTestSuite.getSearchDao();
            return searchDao.explain(sql);
        } catch (SqlParseException | SQLFeatureNotSupportedException e) {
            throw new ParserException("Illegal sql expr in request: " + sql);
        }
    }

    private Schema getSchema(Protocol protocol) {
        return protocol.getResultSet().getSchema();
    }

    private DataRows getDataRows(Protocol protocol) {
        return protocol.getResultSet().getDataRows();
    }

    private void containsColumnsInAnyOrder(Protocol protocol, Set<String> fields) {
        Schema schema = getSchema(protocol);
        Set<String> columnNames = new HashSet<>();
        for (Column column : schema) {
            columnNames.add(column.getName());
        }

        assertThat(columnNames, equalTo(fields));
    }

    private void containsColumns(Protocol protocol, List<String> fields) {
        Schema schema = getSchema(protocol);
        List<String> columnNames = new ArrayList<>();
        for (Column column : schema) {
            columnNames.add(column.getName());
        }

        assertThat(columnNames, equalTo(fields));
    }

    private void containsData(Protocol protocol, Collection<String> fields) {
        Row row = getDataRows(protocol).iterator().next();
        for (String field : fields) {
            assertTrue(row.hasField(field));
        }
    }

    private Protocol execute(String sql, String format) {
        try {
            QueryAction queryAction = getQueryAction(sql);
            Client client = queryAction.getClient();
            Query query = queryAction.getQuery();
            Object queryResult = QueryActionElasticExecutor.executeAnyAction(client, queryAction);

            return new Protocol(client, query, queryResult, format);
        }
        catch (IOException | SqlParseException e) {
            throw new ParserException("Unsupported query: " + sql);
        }
    }
}
