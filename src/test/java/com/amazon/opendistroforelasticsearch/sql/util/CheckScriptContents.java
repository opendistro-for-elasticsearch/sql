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

package com.amazon.opendistroforelasticsearch.sql.util;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.parser.ParserException;
import com.amazon.opendistroforelasticsearch.sql.domain.Condition;
import com.amazon.opendistroforelasticsearch.sql.domain.Select;
import com.amazon.opendistroforelasticsearch.sql.domain.Where;
import com.amazon.opendistroforelasticsearch.sql.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.intgtest.TestsConstants;
import com.amazon.opendistroforelasticsearch.sql.parser.ElasticSqlExprParser;
import com.amazon.opendistroforelasticsearch.sql.parser.ScriptFilter;
import com.amazon.opendistroforelasticsearch.sql.parser.SqlParser;
import com.amazon.opendistroforelasticsearch.sql.plugin.SqlSettings;
import com.amazon.opendistroforelasticsearch.sql.query.ESActionFactory;
import com.amazon.opendistroforelasticsearch.sql.query.QueryAction;
import com.amazon.opendistroforelasticsearch.sql.query.SqlElasticRequestBuilder;
import com.amazon.opendistroforelasticsearch.sql.esdomain.LocalClusterState;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
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

import static org.elasticsearch.search.builder.SearchSourceBuilder.ScriptField;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
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

    private static XContentParser createParser(String mappings) throws IOException {
        return XContentType.JSON.xContent().createParser(
            NamedXContentRegistry.EMPTY,
            DeprecationHandler.THROW_UNSUPPORTED_OPERATION,
            mappings
        );
    }

    public static void mockLocalClusterState(String mappings) {
        LocalClusterState.state().setClusterService(mockClusterService(mappings));
        LocalClusterState.state().setResolver(mockIndexNameExpressionResolver());
    }

    public static ClusterService mockClusterService(String mappings) {
        ClusterService mockService = mock(ClusterService.class);
        ClusterState mockState = mock(ClusterState.class);
        MetaData mockMetaData = mock(MetaData.class);

        when(mockService.state()).thenReturn(mockState);
        when(mockState.metaData()).thenReturn(mockMetaData);
        try {
            ImmutableOpenMap.Builder<String, ImmutableOpenMap<String, MappingMetaData>> builder = ImmutableOpenMap.builder();
            builder.put(TestsConstants.TEST_INDEX_BANK, IndexMetaData.fromXContent(createParser(mappings)).getMappings());
            when(mockMetaData.findMappings(any(), any(), any())).thenReturn(builder.build());
        }
        catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return mockService;
    }

    private static IndexNameExpressionResolver mockIndexNameExpressionResolver() {
        IndexNameExpressionResolver mockResolver = mock(IndexNameExpressionResolver.class);
        when(mockResolver.concreteIndexNames(any(), any(), any())).thenAnswer(
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

}
