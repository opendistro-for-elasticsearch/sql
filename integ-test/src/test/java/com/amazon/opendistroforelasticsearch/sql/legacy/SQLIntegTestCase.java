/*
 *    Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License").
 *    You may not use this file except in compliance with the License.
 *    A copy of the License is located at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    or in the "license" file accompanying this file. This file is distributed
 *    on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *    express or implied. See the License for the specific language governing
 *    permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.sql.legacy;

import static com.amazon.opendistroforelasticsearch.sql.legacy.TestUtils.createIndexByRestClient;
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestUtils.getAccountIndexMapping;
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestUtils.getBankIndexMapping;
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestUtils.getBankWithNullValuesIndexMapping;
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestUtils.getDateIndexMapping;
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestUtils.getDateTimeIndexMapping;
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestUtils.getDeepNestedIndexMapping;
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestUtils.getDogIndexMapping;
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestUtils.getDogs2IndexMapping;
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestUtils.getDogs3IndexMapping;
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestUtils.getEmployeeNestedTypeIndexMapping;
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestUtils.getGameOfThronesIndexMapping;
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestUtils.getJoinTypeIndexMapping;
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestUtils.getLocationIndexMapping;
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestUtils.getNestedSimpleIndexMapping;
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestUtils.getNestedTypeIndexMapping;
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestUtils.getOdbcIndexMapping;
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestUtils.getOrderIndexMapping;
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestUtils.getPeople2IndexMapping;
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestUtils.getPhraseIndexMapping;
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestUtils.getResponseBody;
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestUtils.getWeblogsIndexMapping;
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestUtils.isIndexExist;
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestUtils.loadDataByRestClient;
import static com.amazon.opendistroforelasticsearch.sql.legacy.plugin.RestSqlAction.CURSOR_CLOSE_ENDPOINT;
import static com.amazon.opendistroforelasticsearch.sql.legacy.plugin.RestSqlAction.EXPLAIN_API_ENDPOINT;
import static com.amazon.opendistroforelasticsearch.sql.legacy.plugin.RestSqlAction.QUERY_API_ENDPOINT;

import com.google.common.base.Strings;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;

/**
 * ES Rest integration test base for SQL testing
 */
public abstract class SQLIntegTestCase extends ODFERestTestCase {

  public static final String PERSISTENT = "persistent";
  public static final String TRANSIENT = "transient";

  @Before
  public void setUpIndices() throws Exception {
    if (client() == null) {
      initClient();
    }

    increaseScriptMaxCompilationsRate();
    enableNewQueryEngine();
    init();
  }

  @Override
  protected boolean preserveClusterUponCompletion() {
    return true; // Preserve test index, template and settings between test cases
  }

  /**
   * We need to be able to dump the jacoco coverage before cluster is shut down.
   * The new internal testing framework removed some of the gradle tasks we were listening to
   * to choose a good time to do it. This will dump the executionData to file after each test.
   * TODO: This is also currently just overwriting integTest.exec with the updated execData without
   * resetting after writing each time. This can be improved to either write an exec file per test
   * or by letting jacoco append to the file
   */
  public interface IProxy {
    byte[] getExecutionData(boolean reset);

    void dump(boolean reset);

    void reset();
  }

  @AfterClass
  public static void dumpCoverage() {
    // jacoco.dir is set in sqlplugin-coverage.gradle, if it doesn't exist we don't
    // want to collect coverage so we can return early
    String jacocoBuildPath = System.getProperty("jacoco.dir");
    if (Strings.isNullOrEmpty(jacocoBuildPath)) {
      return;
    }

    String serverUrl = "service:jmx:rmi:///jndi/rmi://127.0.0.1:7777/jmxrmi";
    try (JMXConnector connector = JMXConnectorFactory.connect(new JMXServiceURL(serverUrl))) {
      IProxy proxy = MBeanServerInvocationHandler.newProxyInstance(
          connector.getMBeanServerConnection(), new ObjectName("org.jacoco:type=Runtime"),
          IProxy.class,
          false);

      Path path = Paths.get(jacocoBuildPath + "/integTest.exec");
      Files.write(path, proxy.getExecutionData(false));
    } catch (Exception ex) {
      throw new RuntimeException("Failed to dump coverage", ex);
    }
  }

  /**
   * As JUnit JavaDoc says:
   * "The @AfterClass methods declared in superclasses will be run after those of the current class."
   * So this method is supposed to run before closeClients() in parent class.
   */
  @AfterClass
  public static void cleanUpIndices() throws IOException {
    wipeAllODFEIndices();
    wipeAllClusterSettings();
  }

  /**
   * Increase script.max_compilations_rate to large enough, which is only 75/5min by default.
   * This issue is due to our painless script not using params passed to compiled script.
   */
  private void increaseScriptMaxCompilationsRate() throws IOException {
    updateClusterSettings(
        new ClusterSetting("transient", "script.max_compilations_rate", "10000/1m"));
  }

  private void enableNewQueryEngine() throws IOException {
    boolean isEnabled = Boolean.parseBoolean(System.getProperty("enableNewEngine", "false"));
    if (isEnabled) {
      com.amazon.opendistroforelasticsearch.sql.util.TestUtils.enableNewQueryEngine(client());
    }
  }

  protected static void wipeAllClusterSettings() throws IOException {
    updateClusterSettings(new ClusterSetting("persistent", "*", null));
    updateClusterSettings(new ClusterSetting("transient", "*", null));
  }

  /**
   * Provide for each test to load test index, data and other setup work
   */
  protected void init() throws Exception {
  }

  /**
   * Make it thread-safe in case tests are running in parallel but does not guarantee
   * if test like DeleteIT that mutates cluster running in parallel.
   */
  protected synchronized void loadIndex(Index index) throws IOException {
    String indexName = index.getName();
    String mapping = index.getMapping();
    String dataSet = index.getDataSet();

    if (!isIndexExist(client(), indexName)) {
      createIndexByRestClient(client(), indexName, mapping);
      loadDataByRestClient(client(), indexName, dataSet);
    }
  }

  protected Request getSqlRequest(String request, boolean explain) {
    return getSqlRequest(request, explain, "json");
  }

  protected Request getSqlRequest(String request, boolean explain, String requestType) {
    String queryEndpoint = String.format("%s?format=%s", QUERY_API_ENDPOINT, requestType);
    Request sqlRequest = new Request("POST", explain ? EXPLAIN_API_ENDPOINT : queryEndpoint);
    sqlRequest.setJsonEntity(request);
    RequestOptions.Builder restOptionsBuilder = RequestOptions.DEFAULT.toBuilder();
    restOptionsBuilder.addHeader("Content-Type", "application/json");
    sqlRequest.setOptions(restOptionsBuilder);

    return sqlRequest;
  }

  protected Request getSqlCursorCloseRequest(String cursorRequest) {
    String queryEndpoint = String.format("%s?format=%s", CURSOR_CLOSE_ENDPOINT, "jdbc");
    Request sqlRequest = new Request("POST", queryEndpoint);
    sqlRequest.setJsonEntity(cursorRequest);
    RequestOptions.Builder restOptionsBuilder = RequestOptions.DEFAULT.toBuilder();
    restOptionsBuilder.addHeader("Content-Type", "application/json");
    sqlRequest.setOptions(restOptionsBuilder);

    return sqlRequest;
  }

  protected String executeQuery(String query, String requestType) {
    try {
      String endpoint = "/_opendistro/_sql?format=" + requestType;
      String requestBody = makeRequest(query);

      Request sqlRequest = new Request("POST", endpoint);
      sqlRequest.setJsonEntity(requestBody);

      Response response = client().performRequest(sqlRequest);
      Assert.assertEquals(200, response.getStatusLine().getStatusCode());
      String responseString = getResponseBody(response, true);

      return responseString;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected String executeFetchQuery(String query, int fetchSize, String requestType)
      throws IOException {
    String endpoint = "/_opendistro/_sql?format=" + requestType;
    String requestBody = makeRequest(query, fetchSize);

    Request sqlRequest = new Request("POST", endpoint);
    sqlRequest.setJsonEntity(requestBody);

    Response response = client().performRequest(sqlRequest);
    String responseString = getResponseBody(response, true);
    return responseString;
  }

  protected String executeFetchLessQuery(String query, String requestType) throws IOException {

    String endpoint = "/_opendistro/_sql?format=" + requestType;
    String requestBody = makeFetchLessRequest(query);

    Request sqlRequest = new Request("POST", endpoint);
    sqlRequest.setJsonEntity(requestBody);

    Response response = client().performRequest(sqlRequest);
    String responseString = getResponseBody(response, true);
    return responseString;
  }

  protected Request buildGetEndpointRequest(final String sqlQuery) {

    final String utf8CharsetName = StandardCharsets.UTF_8.name();
    String urlEncodedQuery = "";

    try {
      urlEncodedQuery = URLEncoder.encode(sqlQuery, utf8CharsetName);
    } catch (UnsupportedEncodingException e) {
      // Will never reach here since UTF-8 is always supported
      Assert.fail(utf8CharsetName + " not available");
    }

    final String requestUrl = String.format(Locale.ROOT, "%s?sql=%s&format=%s", QUERY_API_ENDPOINT,
        urlEncodedQuery, "json");
    return new Request("GET", requestUrl);
  }

  protected JSONObject executeQuery(final String sqlQuery) throws IOException {

    final String requestBody = makeRequest(sqlQuery);
    return executeRequest(requestBody);
  }

  protected String explainQuery(final String sqlQuery) throws IOException {

    final String requestBody = makeRequest(sqlQuery);
    return executeExplainRequest(requestBody);
  }

  protected String executeQueryWithStringOutput(final String sqlQuery) throws IOException {

    final String requestString = makeRequest(sqlQuery);
    return executeRequest(requestString, false);
  }

  protected JSONObject executeRequest(final String requestBody) throws IOException {

    return new JSONObject(executeRequest(requestBody, false));
  }

  protected String executeExplainRequest(final String requestBody) throws IOException {

    return executeRequest(requestBody, true);
  }

  private String executeRequest(final String requestBody, final boolean isExplainQuery)
      throws IOException {

    Request sqlRequest = getSqlRequest(requestBody, isExplainQuery);
    return executeRequest(sqlRequest);
  }

  protected static String executeRequest(final Request request) throws IOException {

    Response response = client().performRequest(request);
    Assert.assertEquals(200, response.getStatusLine().getStatusCode());
    return getResponseBody(response);
  }

  protected JSONObject executeQueryWithGetRequest(final String sqlQuery) throws IOException {

    final Request request = buildGetEndpointRequest(sqlQuery);
    final String result = executeRequest(request);
    return new JSONObject(result);
  }

  protected JSONObject executeCursorQuery(final String cursor) throws IOException {
    final String requestBody = makeCursorRequest(cursor);
    Request sqlRequest = getSqlRequest(requestBody, false, "jdbc");
    return new JSONObject(executeRequest(sqlRequest));
  }

  protected JSONObject executeCursorCloseQuery(final String cursor) throws IOException {
    final String requestBody = makeCursorRequest(cursor);
    Request sqlRequest = getSqlCursorCloseRequest(requestBody);
    return new JSONObject(executeRequest(sqlRequest));
  }

  protected static JSONObject updateClusterSettings(ClusterSetting setting) throws IOException {
    Request request = new Request("PUT", "/_cluster/settings");
    String persistentSetting = String.format(Locale.ROOT,
        "{\"%s\": {\"%s\": %s}}", setting.type, setting.name, setting.value);
    request.setJsonEntity(persistentSetting);
    RequestOptions.Builder restOptionsBuilder = RequestOptions.DEFAULT.toBuilder();
    restOptionsBuilder.addHeader("Content-Type", "application/json");
    request.setOptions(restOptionsBuilder);
    return new JSONObject(executeRequest(request));
  }

  protected static JSONObject getAllClusterSettings() throws IOException {
    Request request = new Request("GET", "/_cluster/settings?flat_settings&include_defaults");
    RequestOptions.Builder restOptionsBuilder = RequestOptions.DEFAULT.toBuilder();
    restOptionsBuilder.addHeader("Content-Type", "application/json");
    request.setOptions(restOptionsBuilder);
    return new JSONObject(executeRequest(request));
  }

  protected static class ClusterSetting {
    private final String type;
    private final String name;
    private final String value;

    public ClusterSetting(String type, String name, String value) {
      this.type = type;
      this.name = name;
      this.value = (value == null) ? "null" : ("\"" + value + "\"");
    }

    ClusterSetting nullify() {
      return new ClusterSetting(type, name, null);
    }

    @Override
    public String toString() {
      return "ClusterSetting{" +
          "type='" + type + '\'' +
          ", path='" + name + '\'' +
          ", value='" + value + '\'' +
          '}';
    }
  }

  protected String makeRequest(String query) {
    return makeRequest(query, 0);
  }

  protected String makeRequest(String query, int fetch_size) {
    return String.format("{\n" +
        "  \"fetch_size\": \"%s\",\n" +
        "  \"query\": \"%s\"\n" +
        "}", fetch_size, query);
  }

  protected String makeFetchLessRequest(String query) {
    return String.format("{\n" +
        "  \"query\": \"%s\"\n" +
        "}", query);
  }

  protected String makeCursorRequest(String cursor) {
    return String.format("{\"cursor\":\"%s\"}", cursor);
  }

  protected JSONArray getHits(JSONObject response) {
    Assert.assertTrue(response.getJSONObject("hits").has("hits"));

    return response.getJSONObject("hits").getJSONArray("hits");
  }

  protected int getTotalHits(JSONObject response) {
    Assert.assertTrue(response.getJSONObject("hits").has("total"));
    Assert.assertTrue(response.getJSONObject("hits").getJSONObject("total").has("value"));

    return response.getJSONObject("hits").getJSONObject("total").getInt("value");
  }

  protected JSONObject getSource(JSONObject hit) {
    return hit.getJSONObject("_source");
  }

  /**
   * Enum for associating test index with relevant mapping and data.
   */
  public enum Index {
    ONLINE(TestsConstants.TEST_INDEX_ONLINE,
        "online",
        null,
        "src/test/resources/online.json"),
    ACCOUNT(TestsConstants.TEST_INDEX_ACCOUNT,
        "account",
        getAccountIndexMapping(),
        "src/test/resources/accounts.json"),
    PHRASE(TestsConstants.TEST_INDEX_PHRASE,
        "phrase",
        getPhraseIndexMapping(),
        "src/test/resources/phrases.json"),
    DOG(TestsConstants.TEST_INDEX_DOG,
        "dog",
        getDogIndexMapping(),
        "src/test/resources/dogs.json"),
    DOGS2(TestsConstants.TEST_INDEX_DOG2,
        "dog",
        getDogs2IndexMapping(),
        "src/test/resources/dogs2.json"),
    DOGS3(TestsConstants.TEST_INDEX_DOG3,
        "dog",
        getDogs3IndexMapping(),
        "src/test/resources/dogs3.json"),
    DOGSSUBQUERY(TestsConstants.TEST_INDEX_DOGSUBQUERY,
        "dog",
        getDogIndexMapping(),
        "src/test/resources/dogsubquery.json"),
    PEOPLE(TestsConstants.TEST_INDEX_PEOPLE,
        "people",
        null,
        "src/test/resources/peoples.json"),
    PEOPLE2(TestsConstants.TEST_INDEX_PEOPLE2,
        "people",
        getPeople2IndexMapping(),
        "src/test/resources/people2.json"),
    GAME_OF_THRONES(TestsConstants.TEST_INDEX_GAME_OF_THRONES,
        "gotCharacters",
        getGameOfThronesIndexMapping(),
        "src/test/resources/game_of_thrones_complex.json"),
    SYSTEM(TestsConstants.TEST_INDEX_SYSTEM,
        "systems",
        null,
        "src/test/resources/systems.json"),
    ODBC(TestsConstants.TEST_INDEX_ODBC,
        "odbc",
        getOdbcIndexMapping(),
        "src/test/resources/odbc-date-formats.json"),
    LOCATION(TestsConstants.TEST_INDEX_LOCATION,
        "location",
        getLocationIndexMapping(),
        "src/test/resources/locations.json"),
    LOCATION_TWO(TestsConstants.TEST_INDEX_LOCATION2,
        "location2",
        getLocationIndexMapping(),
        "src/test/resources/locations2.json"),
    NESTED(TestsConstants.TEST_INDEX_NESTED_TYPE,
        "nestedType",
        getNestedTypeIndexMapping(),
        "src/test/resources/nested_objects.json"),
    NESTED_WITH_QUOTES(TestsConstants.TEST_INDEX_NESTED_WITH_QUOTES,
        "nestedType",
        getNestedTypeIndexMapping(),
        "src/test/resources/nested_objects_quotes_in_values.json"),
    EMPLOYEE_NESTED(TestsConstants.TEST_INDEX_EMPLOYEE_NESTED,
        "_doc",
        getEmployeeNestedTypeIndexMapping(),
        "src/test/resources/employee_nested.json"),
    JOIN(TestsConstants.TEST_INDEX_JOIN_TYPE,
        "joinType",
        getJoinTypeIndexMapping(),
        "src/test/resources/join_objects.json"),
    BANK(TestsConstants.TEST_INDEX_BANK,
        "account",
        getBankIndexMapping(),
        "src/test/resources/bank.json"),
    BANK_TWO(TestsConstants.TEST_INDEX_BANK_TWO,
        "account_two",
        getBankIndexMapping(),
        "src/test/resources/bank_two.json"),
    BANK_WITH_NULL_VALUES(TestsConstants.TEST_INDEX_BANK_WITH_NULL_VALUES,
        "account_null",
        getBankWithNullValuesIndexMapping(),
        "src/test/resources/bank_with_null_values.json"),
    ORDER(TestsConstants.TEST_INDEX_ORDER,
        "_doc",
        getOrderIndexMapping(),
        "src/test/resources/order.json"),
    WEBLOG(TestsConstants.TEST_INDEX_WEBLOG,
        "weblog",
        getWeblogsIndexMapping(),
        "src/test/resources/weblogs.json"),
    DATE(TestsConstants.TEST_INDEX_DATE,
        "dates",
        getDateIndexMapping(),
        "src/test/resources/dates.json"),
    DATETIME(TestsConstants.TEST_INDEX_DATE_TIME,
        "_doc",
        getDateTimeIndexMapping(),
        "src/test/resources/datetime.json"),
    NESTED_SIMPLE(TestsConstants.TEST_INDEX_NESTED_SIMPLE,
        "_doc",
        getNestedSimpleIndexMapping(),
        "src/test/resources/nested_simple.json"),
    DEEP_NESTED(TestsConstants.TEST_INDEX_DEEP_NESTED,
        "_doc",
        getDeepNestedIndexMapping(),
        "src/test/resources/deep_nested_index_data.json");

    private final String name;
    private final String type;
    private final String mapping;
    private final String dataSet;

    Index(String name, String type, String mapping, String dataSet) {
      this.name = name;
      this.type = type;
      this.mapping = mapping;
      this.dataSet = dataSet;
    }

    public String getName() {
      return this.name;
    }

    public String getType() {
      return this.type;
    }

    public String getMapping() {
      return this.mapping;
    }

    public String getDataSet() {
      return this.dataSet;
    }
  }
}
