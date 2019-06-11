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

package com.amazon.opendistroforelasticsearch.sql.esintgtest;

import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Response;
import org.elasticsearch.common.xcontent.XContentType;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class TestUtils {

    public static void createTestIndex(AdminClient admin, String index, String type, String mapping) {
        System.out.println("Creating index " + index);

        CreateIndexRequest createIndexRequest = new CreateIndexRequest(index);
        if(mapping != null) {
            createIndexRequest.mapping(type, mapping, XContentType.JSON);
        }
        ActionFuture<CreateIndexResponse> responseFuture = admin.indices().create(createIndexRequest);
        CreateIndexResponse response = responseFuture.actionGet();
        if (response.isAcknowledged()) {
            System.out.println("Index " + index + " created");
        } else {
            throw new IllegalStateException("Failed to create index " + index);
        }

        admin.indices().prepareRefresh(index).execute().actionGet();
    }

    public static String getAccountIndexMapping() {
        return "{  \"account\": {" +
                " \"properties\": {\n" +
                "          \"gender\": {\n" +
                "            \"type\": \"text\",\n" +
                "            \"fielddata\": true\n" +
                "          }," +
                "          \"address\": {\n" +
                "            \"type\": \"text\",\n" +
                "            \"fielddata\": true\n" +
                "          }," +
                "          \"state\": {\n" +
                "            \"type\": \"text\",\n" +
                "            \"fielddata\": true,\n" +
                "            \"fields\": {\n" +
                "              \"keyword\": {\n" +
                "                \"type\": \"keyword\",\n" +
                "                \"ignore_above\": 256\n" +
                "              }" +
                "            }" +
                "          }" +
                "       }"+
                "   }" +
                "}";
    }

    public static String getPhraseIndexMapping() {
        return "{  \"phrase\": {" +
                " \"properties\": {\n" +
                "          \"phrase\": {\n" +
                "            \"type\": \"text\",\n" +
                "            \"store\": true\n" +
                "          }" +
                "       }"+
                "   }" +
                "}";
    }

    public static String getDogIndexMapping() {
        return "{  \"dog\": {" +
                " \"properties\": {\n" +
                "          \"dog_name\": {\n" +
                "            \"type\": \"text\",\n" +
                "            \"fielddata\": true\n" +
                "          }"+
                "       }"+
                "   }" +
                "}";
    }

    public static String getDogs2IndexMapping() {
        return "{  \"dog\": {" +
                " \"properties\": {\n" +
                "          \"holdersName\": {\n" +
                "            \"type\": \"keyword\"\n" +
                "          }"+
                "       }"+
                "   }" +
                "}";
    }

    public static String getPeople2IndexMapping() {
        return "{  \"people\": {" +
                " \"properties\": {\n" +
                "          \"firstname\": {\n" +
                "            \"type\": \"keyword\"\n" +
                "          }"+
                "       }"+
                "   }" +
                "}";
    }

    public static String getGameOfThronesIndexMapping() {
        return "{  \"gotCharacters\": { " +
                "    \"properties\": {\n" +
                "      \"nickname\": {\n" +
                "        \"type\":\"text\", "+
                "        \"fielddata\":true"+
                "      },\n"+
                "      \"name\": {\n" +
                "        \"properties\": {\n" +
                "          \"firstname\": {\n" +
                "            \"type\": \"text\",\n" +
                "            \"fielddata\": true\n" +
                "          },\n" +
                "          \"lastname\": {\n" +
                "            \"type\": \"text\",\n" +
                "            \"fielddata\": true\n" +
                "          },\n" +
                "          \"ofHerName\": {\n" +
                "            \"type\": \"integer\"\n" +
                "          },\n" +
                "          \"ofHisName\": {\n" +
                "            \"type\": \"integer\"\n" +
                "          }\n" +
                "        }\n" +
                "      },\n" +
                "      \"house\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"fields\": {\n" +
                "          \"keyword\": {\n" +
                "            \"type\": \"keyword\"\n" +
                "          }\n" +
                "        }\n" +
                "      },\n" +
                "      \"gender\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"fields\": {\n" +
                "          \"keyword\": {\n" +
                "            \"type\": \"keyword\"\n" +
                "          }\n" +
                "        }\n" +
                "      }" +
                "} } }";
    }

    // System

    public static String getOdbcIndexMapping() {
        return "{\n" +
                "\t\"odbc\" :{\n" +
                "\t\t\"properties\":{\n" +
                "\t\t\t\"odbc_time\":{\n" +
                "\t\t\t\t\"type\":\"date\",\n" +
                "\t\t\t\t\"format\": \"'{ts' ''yyyy-MM-dd HH:mm:ss.SSS'''}'\"\n" +
                "\t\t\t},\n" +
                "\t\t\t\"docCount\":{\n" +
                "\t\t\t\t\"type\":\"text\"\n" +
                "\t\t\t}\n" +
                "\t\t}\n" +
                "\t}\n" +
                "}";
    }

    public static String getLocationIndexMapping(String type) {
        return "{\n" +
                "\t\"" + type + "\" :{\n" +
                "\t\t\"properties\":{\n" +
                "\t\t\t\"place\":{\n" +
                "\t\t\t\t\"type\":\"geo_shape\",\n" +
                "\t\t\t\t\"tree\": \"quadtree\",\n" +
                "\t\t\t\t\"precision\": \"10km\"\n" +
                "\t\t\t},\n" +
                "\t\t\t\"center\":{\n" +
                "\t\t\t\t\"type\":\"geo_point\"\n" +
                "\t\t\t},\n" +
                "\t\t\t\"description\":{\n" +
                "\t\t\t\t\"type\":\"text\"\n" +
                "\t\t\t}\n" +
                "\t\t}\n" +
                "\t}\n" +
                "}";
    }

    public static String getNestedTypeIndexMapping() {
        return "{ \"nestedType\": {\n" +
                "        \"properties\": {\n" +
                "          \"message\": {\n" +
                "            \"type\": \"nested\",\n" +
                "            \"properties\": {\n" +
                "              \"info\": {\n" +
                "                \"type\": \"keyword\",\n" +
                "                \"index\": \"true\"\n" +
                "              },\n" +
                "              \"author\": {\n" +
                "                \"type\": \"keyword\",\n" +
                "                \"index\": \"true\"\n" +
                "              },\n" +
                "              \"dayOfWeek\": {\n" +
                "                \"type\": \"long\"\n" +
                "              }\n" +
                "            }\n" +
                "          },\n" +
                "          \"comment\": {\n" +
                "            \"type\": \"nested\",\n" +
                "            \"properties\": {\n" +
                "              \"data\": {\n" +
                "                \"type\": \"keyword\",\n" +
                "                \"index\": \"true\"\n" +
                "              },\n" +
                "              \"likes\": {\n" +
                "                \"type\": \"long\"\n" +
                "              }\n" +
                "            }\n" +
                "          },\n" +
                "          \"myNum\": {\n" +
                "            \"type\": \"long\"\n" +
                "          },\n" +
                "          \"someField\": {\n" +
                "                \"type\": \"keyword\",\n" +
                "                \"index\": \"true\"\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }}";
    }

    public static String getJoinTypeIndexMapping() {
        return "{\n" +
                "  \"joinType\": {\n" +
                "    \"properties\": {\n" +
                "      \"join_field\": {\n" +
                "        \"type\": \"join\",\n" +
                "        \"relations\": {\n" +
                "          \"parentType\": \"childrenType\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"parentTile\": {\n" +
                "        \"index\": \"true\",\n" +
                "        \"type\": \"keyword\"\n" +
                "      },\n" +
                "      \"dayOfWeek\": {\n" +
                "        \"type\": \"long\"\n" +
                "      },\n" +
                "      \"author\": {\n" +
                "        \"index\": \"true\",\n" +
                "        \"type\": \"keyword\"\n" +
                "      },\n" +
                "      \"info\": {\n" +
                "        \"index\": \"true\",\n" +
                "        \"type\": \"keyword\"\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }

    public static String getBankIndexMapping(String type) {
        return "{\n" +
                "  \"" + type +"\": {\n" +
                "    \"properties\": {\n" +
                "      \"account_number\": {\n" +
                "        \"type\": \"long\"\n" +
                "      },\n" +
                "      \"address\": {\n" +
                "        \"type\": \"text\"\n" +
                "      },\n" +
                "      \"age\": {\n" +
                "        \"type\": \"integer\"\n" +
                "      },\n" +
                "      \"balance\": {\n" +
                "        \"type\": \"long\"\n" +
                "      },\n" +
                "      \"birthdate\": {\n" +
                "        \"type\": \"date\"\n" +
                "      },\n" +
                "      \"city\": {\n" +
                "        \"type\": \"keyword\"\n" +
                "      },\n" +
                "      \"email\": {\n" +
                "        \"type\": \"text\"\n" +
                "      },\n" +
                "      \"employer\": {\n" +
                "        \"type\": \"text\"\n" +
                "      },\n" +
                "      \"firstname\": {\n" +
                "        \"type\": \"text\"\n" +
                "      },\n" +
                "      \"gender\": {\n" +
                "        \"type\": \"text\"\n" +
                "      },\n" +
                "      \"lastname\": {\n" +
                "        \"type\": \"keyword\"\n" +
                "      },\n" +
                "      \"male\": {\n" +
                "        \"type\": \"boolean\"\n" +
                "      },\n" +
                "      \"state\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"fields\": {\n" +
                "          \"keyword\": {\n" +
                "            \"type\": \"keyword\",\n" +
                "            \"ignore_above\": 256\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }

    public static void loadBulk(Client client, String jsonPath, String defaultIndex) throws Exception {
        System.out.println(String.format("Loading file %s into elasticsearch cluster", jsonPath));
        String absJsonPath = getResourceFilePath(jsonPath);

        BulkRequest bulkRequest = new BulkRequest();
        try (final InputStream stream =  new FileInputStream(absJsonPath);
             final Reader streamReader = new InputStreamReader(stream, StandardCharsets.UTF_8);
             final BufferedReader br = new BufferedReader(streamReader)) {

            while (true) {

                String actionLine = br.readLine();
                if (actionLine == null || actionLine.trim().isEmpty()) {
                    break;
                }
                String sourceLine = br.readLine();
                JSONObject actionJson = new JSONObject(actionLine);

                IndexRequest indexRequest = new IndexRequest();
                indexRequest.index(defaultIndex);
                String docType = actionJson.getJSONObject("index").getString("_type");
                indexRequest.type(docType);
                if (actionJson.getJSONObject("index").has("_id")) {
                    String docId = actionJson.getJSONObject("index").getString("_id");
                    indexRequest.id(docId);
                }
                if (actionJson.getJSONObject("index").has("_routing")) {
                    String routing = actionJson.getJSONObject("index").getString("_routing");
                    indexRequest.routing(routing);
                }
                indexRequest.source(sourceLine, XContentType.JSON);
                bulkRequest.add(indexRequest);
            }
        }

        BulkResponse bulkResponse = client.bulk(bulkRequest).actionGet();

        if (bulkResponse.hasFailures()) {
            throw new Exception("Failed to load test data into index " + defaultIndex + ", " +
                    bulkResponse.buildFailureMessage());
        }
        System.out.println(bulkResponse.getItems().length + " documents loaded.");
        // ensure the documents are searchable
        client.admin().indices().prepareRefresh(defaultIndex).execute().actionGet();
    }

    public static String getResourceFilePath(String relPath) {
        String projectRoot = System.getProperty("project.root", null);
        if (projectRoot == null) {
            return new File(relPath).getAbsolutePath();
        } else {
            return new File(projectRoot + "/" + relPath).getAbsolutePath();
        }
    }

    public static String getResponseBody(Response response) throws IOException {
        final StringBuilder sb = new StringBuilder();

        try (final InputStream is = response.getEntity().getContent();
             final BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    public static String fileToString(final String filePathFromProjectRoot, final boolean removeNewLines)
            throws IOException {

        final String absolutePath = getResourceFilePath(filePathFromProjectRoot);

        try (final InputStream stream = new FileInputStream(absolutePath);
             final Reader streamReader = new InputStreamReader(stream, StandardCharsets.UTF_8);
             final BufferedReader br = new BufferedReader(streamReader)) {

            final StringBuilder stringBuilder = new StringBuilder();
            String line = br.readLine();

            while (line != null) {

                stringBuilder.append(line);
                if (!removeNewLines) {
                    stringBuilder.append(String.format(Locale.ROOT, "%n"));
                }
                line = br.readLine();
            }

            return stringBuilder.toString();
        }
    }
}
