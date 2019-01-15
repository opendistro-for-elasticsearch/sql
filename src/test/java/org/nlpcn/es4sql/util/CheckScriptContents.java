package org.nlpcn.es4sql.util;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.parser.ParserException;
import org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.DeprecationHandler;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.nlpcn.es4sql.domain.Condition;
import org.nlpcn.es4sql.domain.Select;
import org.nlpcn.es4sql.domain.Where;
import org.nlpcn.es4sql.exception.SqlParseException;
import org.nlpcn.es4sql.parse.ElasticSqlExprParser;
import org.nlpcn.es4sql.parse.ScriptFilter;
import org.nlpcn.es4sql.parse.SqlParser;
import org.nlpcn.es4sql.query.ESActionFactory;
import org.nlpcn.es4sql.query.QueryAction;
import org.nlpcn.es4sql.query.SqlElasticRequestBuilder;

import java.io.IOException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.elasticsearch.search.builder.SearchSourceBuilder.ScriptField;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class CheckScriptContents {

    private static SQLExpr queryToExpr(String query) {
        return new ElasticSqlExprParser(query).expr();
    }

    public static ScriptField getScriptFieldFromQuery(String query) {
        try {
            Client mockClient = mock(Client.class, RETURNS_DEEP_STUBS);
            stubMockClient(mockClient);
            QueryAction queryAction = ESActionFactory.create(mockClient, query);
            SqlElasticRequestBuilder requestBuilder = queryAction.explain();

            SearchRequestBuilder request = (SearchRequestBuilder) requestBuilder.getBuilder();
            List<ScriptField> scriptFields = request.request().source().scriptFields();

            assertTrue(scriptFields.size() == 1);

            return scriptFields.get(0);

        } catch (SQLFeatureNotSupportedException | SqlParseException e) {
            throw new ParserException("Unable to parse query: " + query);
        }
    }

    public static ScriptFilter getScriptFilterFromQuery(String query, SqlParser parser) {
        try {
            Select select = parser.parseSelect((SQLQueryExpr) queryToExpr(query));
            Where where = select.getWhere();

            assertTrue(where.getWheres().size() == 1);
            assertTrue(((Condition) (where.getWheres().get(0))).getValue() instanceof ScriptFilter);

            return (ScriptFilter) (((Condition) (where.getWheres().get(0))).getValue());

        } catch (SqlParseException e) {
            throw new ParserException("Unable to parse query: " + query);
        }
    }

    public static boolean scriptContainsString(ScriptField scriptField, String string) {
        return scriptField.script().getIdOrCode().contains(string);
    }

    public static boolean scriptContainsString(ScriptFilter scriptFilter, String string) {
        return scriptFilter.getScript().contains(string);
    }

    public static boolean scriptHasPattern(ScriptField scriptField, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(scriptField.script().getIdOrCode());
        return matcher.find();
    }

    public static boolean scriptHasPattern(ScriptFilter scriptFilter, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(scriptFilter.getScript());
        return matcher.find();
    }

    public static void stubMockClient(Client mockClient) {
        try {
            String mappings = "{\n" +
                "  \"elasticsearch-sql_test_index_bank\": {\n" +
                "    \"mappings\": {\n" +
                "      \"account\": {\n" +
                "        \"properties\": {\n" +
                "          \"account_number\": {\n" +
                "            \"type\": \"long\"\n" +
                "          },\n" +
                "          \"address\": {\n" +
                "            \"type\": \"text\"\n" +
                "          },\n" +
                "          \"age\": {\n" +
                "            \"type\": \"integer\"\n" +
                "          },\n" +
                "          \"balance\": {\n" +
                "            \"type\": \"long\"\n" +
                "          },\n" +
                "          \"birthdate\": {\n" +
                "            \"type\": \"date\"\n" +
                "          },\n" +
                "          \"city\": {\n" +
                "            \"type\": \"keyword\"\n" +
                "          },\n" +
                "          \"email\": {\n" +
                "            \"type\": \"text\"\n" +
                "          },\n" +
                "          \"employer\": {\n" +
                "            \"type\": \"text\",\n" +
                "            \"fields\": {\n" +
                "              \"keyword\": {\n" +
                "                \"type\": \"keyword\",\n" +
                "                \"ignore_above\": 256\n" +
                "              }\n" +
                "            }\n" +
                "          },\n" +
                "          \"firstname\": {\n" +
                "            \"type\": \"text\"\n" +
                "          },\n" +
                "          \"gender\": {\n" +
                "            \"type\": \"text\"\n" +
                "          },\n" +
                "          \"lastname\": {\n" +
                "            \"type\": \"keyword\"\n" +
                "          },\n" +
                "          \"male\": {\n" +
                "            \"type\": \"boolean\"\n" +
                "          },\n" +
                "          \"state\": {\n" +
                "            \"type\": \"text\",\n" +
                "            \"fields\": {\n" +
                "              \"raw\": {\n" +
                "                \"type\": \"keyword\",\n" +
                "                \"ignore_above\": 256\n" +
                "              }\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";

            XContentType xContentType = XContentType.JSON;
            XContentParser parser = xContentType.
                xContent()
                .createParser(NamedXContentRegistry.EMPTY, DeprecationHandler.THROW_UNSUPPORTED_OPERATION, mappings);

            when(mockClient.admin().indices().getFieldMappings(any(GetFieldMappingsRequest.class)).actionGet()).
                thenReturn(GetFieldMappingsResponse.fromXContent(parser));

        } catch (IOException e) {
            throw new ParserException(e.getMessage());
        }

    }
}
