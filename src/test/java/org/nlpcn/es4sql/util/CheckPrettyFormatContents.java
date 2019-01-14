package org.nlpcn.es4sql.util;

import com.alibaba.druid.sql.parser.ParserException;
import org.elasticsearch.client.Client;
import org.elasticsearch.plugin.nlpcn.DataRows;
import org.elasticsearch.plugin.nlpcn.Protocol;
import org.elasticsearch.plugin.nlpcn.QueryActionElasticExecutor;
import org.elasticsearch.plugin.nlpcn.Schema;
import org.elasticsearch.plugin.nlpcn.Schema.Column;
import org.json.JSONArray;
import org.json.JSONObject;
import org.nlpcn.es4sql.intgtest.MainTestSuite;
import org.nlpcn.es4sql.SearchDao;
import org.nlpcn.es4sql.domain.QueryStatement;
import org.nlpcn.es4sql.exception.SqlParseException;
import org.nlpcn.es4sql.query.QueryAction;

import java.io.IOException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertTrue;

public class CheckPrettyFormatContents {

    public static JSONObject getJdbcResponse(String query) {
        Protocol protocol = execute(query, "jdbc");
        return new JSONObject(protocol.format());
    }

    public static JSONArray getJdbcDataRows(JSONObject jdbcResponse) {
        return jdbcResponse.getJSONArray("datarows");
    }

    public static Schema getSchema(Protocol protocol) {
        return protocol.getResultSet().getSchema();
    }

    public static DataRows getDataRows(Protocol protocol) {
        return protocol.getResultSet().getDataRows();
    }

    public static void containsColumnsInAnyOrder(Schema schema, Set<String> fields) {
        Set<String> columnNames = new HashSet<>();
        for (Column column : schema) {
            columnNames.add(column.getName());
        }

        assertThat(columnNames, equalTo(fields));
    }

    public static void containsColumns(Schema schema, List<String> fields) {
        List<String> columnNames = new ArrayList<>();
        for (Column column : schema) {
            columnNames.add(column.getName());
        }

        assertThat(columnNames, equalTo(fields));
    }

    public static void containsAliases(Schema schema, Map<String, String> aliases) {
        for (Column column : schema) {
            assertThat(
                    column.getAlias(),
                    equalTo(
                            aliases.get(
                                    column.getName()
                            )
                    )
            );
        }
    }

    public static void containsData(DataRows dataRows, Collection<String> fields) {
        DataRows.Row row = dataRows.iterator().next();
        for (String field : fields) {
            assertTrue(row.hasField(field));
        }
    }

    private static QueryAction getQueryAction(String sql) {
        try {
            SearchDao searchDao = MainTestSuite.getSearchDao();
            return searchDao.explain(sql);
        } catch (SqlParseException | SQLFeatureNotSupportedException e) {
            throw new ParserException("Illegal sql expr in request: " + sql);
        }
    }

    public static Protocol execute(String sql, String format) {
        try {
            QueryAction queryAction = getQueryAction(sql);
            Client client = queryAction.getClient();
            QueryStatement queryStatement = queryAction.getQueryStatement();
            Object queryResult = QueryActionElasticExecutor.executeAnyAction(client, queryAction);

            return new Protocol(client, queryStatement, queryResult, format);
        }
        catch (IOException | SqlParseException e) {
            throw new ParserException("Unsupported query: " + sql);
        }
    }
}
