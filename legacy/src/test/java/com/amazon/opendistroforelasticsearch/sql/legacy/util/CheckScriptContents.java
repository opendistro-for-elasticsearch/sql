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

package com.amazon.opendistroforelasticsearch.sql.legacy.util;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.parser.ParserException;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Condition;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Select;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Where;
import com.amazon.opendistroforelasticsearch.sql.legacy.esdomain.LocalClusterState;
import com.amazon.opendistroforelasticsearch.sql.legacy.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.legacy.parser.ElasticSqlExprParser;
import com.amazon.opendistroforelasticsearch.sql.legacy.parser.ScriptFilter;
import com.amazon.opendistroforelasticsearch.sql.legacy.parser.SqlParser;
import com.amazon.opendistroforelasticsearch.sql.legacy.plugin.SqlSettings;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.ESActionFactory;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.QueryAction;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.SqlElasticRequestBuilder;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.metadata.IndexMetadata;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.metadata.MappingMetadata;
import org.elasticsearch.cluster.metadata.Metadata;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.xcontent.DeprecationHandler;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Collections.emptyList;
import static org.elasticsearch.search.builder.SearchSourceBuilder.ScriptField;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class CheckScriptContents {

    private static SQLExpr queryToExpr(String query) {
        return new ElasticSqlExprParser(query).expr();
    }

    public static ScriptField getScriptFieldFromQuery(String query) {
        try {
            Client mockClient = mock(Client.class);
            stubMockClient(mockClient);
            QueryAction queryAction = ESActionFactory.create(mockClient, query);
            SqlElasticRequestBuilder requestBuilder = queryAction.explain();

            SearchRequestBuilder request = (SearchRequestBuilder) requestBuilder.getBuilder();
            List<ScriptField> scriptFields = request.request().source().scriptFields();

            assertTrue(scriptFields.size() == 1);

            return scriptFields.get(0);

        } catch (SQLFeatureNotSupportedException | SqlParseException e) {
            throw new ParserException("Unable to parse query: " + query, e);
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
                "    },\n" +
                // ==== All required by IndexMetaData.fromXContent() ====
                "    \"settings\": {\n" +
                "      \"index\": {\n" +
                "        \"number_of_shards\": 5,\n" +
                "        \"number_of_replicas\": 0,\n" +
                "        \"version\": {\n" +
                "          \"created\": \"6050399\"\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    \"mapping_version\": \"1\",\n" +
                "    \"settings_version\": \"1\"\n" +
                //=======================================================
                "  }\n" +
                "}";

            AdminClient mockAdminClient = mock(AdminClient.class);
            when(mockClient.admin()).thenReturn(mockAdminClient);

            IndicesAdminClient mockIndexClient = mock(IndicesAdminClient.class);
            when(mockAdminClient.indices()).thenReturn(mockIndexClient);

            ActionFuture<GetFieldMappingsResponse> mockActionResp = mock(ActionFuture.class);
            when(mockIndexClient.getFieldMappings(any(GetFieldMappingsRequest.class))).thenReturn(mockActionResp);

            when(mockActionResp.actionGet()).thenReturn(GetFieldMappingsResponse.fromXContent(createParser(mappings)));

            mockLocalClusterState(mappings);

        } catch (IOException e) {
            throw new ParserException(e.getMessage());
        }

    }

    public static XContentParser createParser(String mappings) throws IOException {
        return XContentType.JSON.xContent().createParser(
            NamedXContentRegistry.EMPTY,
            DeprecationHandler.THROW_UNSUPPORTED_OPERATION,
            mappings
        );
    }

    public static void mockLocalClusterState(String mappings) {
        LocalClusterState.state().setClusterService(mockClusterService(mappings));
        LocalClusterState.state().setResolver(mockIndexNameExpressionResolver());
        LocalClusterState.state().setSqlSettings(mockSqlSettings());
    }

    public static ClusterService mockClusterService(String mappings) {
        ClusterService mockService = mock(ClusterService.class);
        ClusterState mockState = mock(ClusterState.class);
        Metadata mockMetaData = mock(Metadata.class);

        when(mockService.state()).thenReturn(mockState);
        when(mockState.metadata()).thenReturn(mockMetaData);
        try {
            ImmutableOpenMap.Builder<String, ImmutableOpenMap<String, MappingMetadata>> builder = ImmutableOpenMap.builder();
            builder.put(TestsConstants.TEST_INDEX_BANK, IndexMetadata.fromXContent(createParser(mappings)).getMappings());
            when(mockMetaData.findMappings(any(), any(), any())).thenReturn(builder.build());
        }
        catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return mockService;
    }

    public static IndexNameExpressionResolver mockIndexNameExpressionResolver() {
        IndexNameExpressionResolver mockResolver = mock(IndexNameExpressionResolver.class);
        when(mockResolver.concreteIndexNames(any(), any(), anyString())).thenAnswer(
            (Answer<String[]>) invocation -> {
                // Return index expression directly without resolving
                Object indexExprs = invocation.getArguments()[2];
                if (indexExprs instanceof String) {
                    return new String[]{ (String) indexExprs };
                }
                return (String[]) indexExprs;
            }
        );
        return mockResolver;
    }

    public static SqlSettings mockSqlSettings() {
        SqlSettings settings = spy(new SqlSettings());

        // Force return empty list to avoid ClusterSettings be invoked which is a final class and hard to mock.
        // In this case, default value in Setting will be returned all the time.
        doReturn(emptyList()).when(settings).getSettings();
        return settings;
    }

}
