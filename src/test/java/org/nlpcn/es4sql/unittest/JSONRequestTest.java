package org.nlpcn.es4sql.unittest;

import com.alibaba.druid.sql.parser.ParserException;
import com.google.common.io.Files;
import org.elasticsearch.client.Client;
import org.json.JSONObject;
import org.junit.Test;
import org.mockito.Mockito;
import org.nlpcn.es4sql.SqlRequest;
import org.nlpcn.es4sql.exception.SqlParseException;
import org.nlpcn.es4sql.query.ESActionFactory;
import org.nlpcn.es4sql.query.QueryAction;
import org.nlpcn.es4sql.query.SqlElasticRequestBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLFeatureNotSupportedException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.nlpcn.es4sql.intgtest.TestsConstants.*;

public class JSONRequestTest {

    @Test
    public void searchSanity() throws IOException {
        String result = explain(String.format("{\"query\":\"" +
                "SELECT * " +
                "FROM %s " +
                "WHERE firstname LIKE 'A%%' AND age > 20 " +
                "GROUP BY gender " +
                "ORDER BY _score\"}", TEST_INDEX_ACCOUNT));
        String expectedOutput = Files.toString(
                new File(getResourcePath() + "src/test/resources/expectedOutput/search_explain.json"), StandardCharsets.UTF_8)
                .replaceAll("\r", "");

        assertThat(removeSpaces(result), equalTo(removeSpaces(expectedOutput)));
    }

    @Test
    public void aggregationQuery() throws IOException {
        String result = explain(String.format("{\"query\":\"" +
                "SELECT a, CASE WHEN gender='0' THEN 'aaa' ELSE 'bbb' END AS a2345, count(c) " +
                "FROM %s " +
                "GROUP BY terms('field'='a','execution_hint'='global_ordinals'), a2345\"}", TEST_INDEX_ACCOUNT));
        String expectedOutput = Files.toString(
                new File(getResourcePath() + "src/test/resources/expectedOutput/aggregation_query_explain.json"), StandardCharsets.UTF_8)
                .replaceAll("\r", "");

        assertThat(removeSpaces(result), equalTo(removeSpaces(expectedOutput)));
    }

    @Test
    public void deleteSanity() throws IOException {
        String result = explain(String.format("{\"query\":\"" +
                "DELETE " +
                "FROM %s " +
                "WHERE firstname LIKE 'A%%' AND age > 20\"}", TEST_INDEX_ACCOUNT));
        String expectedOutput = Files.toString(
                new File(getResourcePath() + "src/test/resources/expectedOutput/delete_explain.json"), StandardCharsets.UTF_8)
                .replaceAll("\r", "");

        assertThat(removeSpaces(result), equalTo(removeSpaces(expectedOutput)));
    }

    @Test
    public void queryFilter() throws IOException {
        /*
         * Human readable format of the request defined below:
         * {
         *   "query": "SELECT * FROM accounts WHERE age > 25",
         *   "filter": {
         *     "range": {
         *       "balance": {
         *         "lte": 30000
         *       }
         *     }
         *   }
         * }
         */
        String result = explain(String.format("{\"query\":\"" +
                "SELECT * " +
                "FROM %s " +
                "WHERE age > 25\"," +
                "\"filter\":{\"range\":{\"balance\":{\"lte\":30000}}}}", TEST_INDEX_ACCOUNT));
        String expectedOutput = Files.toString(
                new File(getResourcePath() + "src/test/resources/expectedOutput/json_filter_explain.json"), StandardCharsets.UTF_8)
                .replaceAll("\r", "");

        assertThat(removeSpaces(result), equalTo(removeSpaces(expectedOutput)));
    }

    private String removeSpaces(String s) {
        return s.replaceAll("\\s+", "");
    }

    private String explain(String request) {
        try {
            JSONObject jsonRequest = new JSONObject(request);
            String sql = jsonRequest.getString("query");

            return translate(sql, jsonRequest);
        } catch (SqlParseException | SQLFeatureNotSupportedException e) {
            throw new ParserException("Illegal sql expr in request: " + request);
        }
    }

    private String translate(String sql, JSONObject jsonRequest) throws SQLFeatureNotSupportedException, SqlParseException {
        Client mockClient = Mockito.mock(Client.class);
        QueryAction queryAction = ESActionFactory.create(mockClient, sql);

        SqlRequest sqlRequest = new SqlRequest(sql, jsonRequest);
        queryAction.setSqlRequest(sqlRequest);

        SqlElasticRequestBuilder requestBuilder = queryAction.explain();
        return requestBuilder.explain();
    }

    private String getResourcePath() {
        String projectRoot = System.getProperty("project.root");
        if ( projectRoot!= null && projectRoot.trim().length() > 0) {
            return projectRoot.trim() + "/";
        } else {
            return "";
        }
    }
}
