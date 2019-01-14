package org.openes.sql.integtest;

import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentType;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

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

    public static void loadBulk(Client client, String jsonPath, String defaultIndex) throws Exception {
        System.out.println(String.format("Loading file %s into elasticsearch cluster", jsonPath));
        String absJsonPath = getResourceFilePath(jsonPath);

        BulkRequest bulkRequest = new BulkRequest();
        try (BufferedReader br = new BufferedReader(new FileReader(absJsonPath))) {
            while (true) {
                String actionLine = br.readLine();
                if (actionLine == null || actionLine.trim().length() == 0) {
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
            throw new Exception("Failed to load test data into index " + defaultIndex + ", " + bulkResponse.buildFailureMessage());
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
}
