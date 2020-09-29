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
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestUtils.getDogIndexMapping;
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestUtils.getDogs2IndexMapping;
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestUtils.getDogs3IndexMapping;
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestUtils.getEmployeeNestedTypeIndexMapping;
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestUtils.getGameOfThronesIndexMapping;
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestUtils.getJoinTypeIndexMapping;
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestUtils.getLocationIndexMapping;
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestUtils.getNestedTypeIndexMapping;
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestUtils.getOdbcIndexMapping;
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestUtils.getOrderIndexMapping;
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestUtils.getPeople2IndexMapping;
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestUtils.getPhraseIndexMapping;
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestUtils.getWeblogsIndexMapping;
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestUtils.isIndexExist;
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestUtils.loadDataByRestClient;
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestsConstants.PERSISTENT;
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestsConstants.TRANSIENT;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.rest.RestStatus;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;

/**
 * SQL plugin integration test base class (migrated from SQLIntegTestCase)
 * <p>
 * The execution of order is as follows:
 * <p>
 * ESRestTestCase:   1) initClient()                                       N+1) closeClient()
 * \                                                     /
 * SQLIntegTestCase:     2) setUpIndices() -&gt; 4) setUpIndices() ... -&gt; N) cleanUpIndices()
 * \                      \
 * XXXTIT:                  3) init()             5) init()
 * <p>
 * TODO: this base class should extends ODFERestTestCase
 */
public abstract class RestIntegTestCase extends ODFERestTestCase {

  @Before
  public void setUpIndices() throws Exception {
    if (client() == null) {
      initClient();
    }

    init();
  }

  @Override
  protected boolean preserveClusterUponCompletion() {
    return true;
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

  /**
   * Provide for each test to load test index, data and other setup work
   */
  protected void init() throws Exception {
  }

  protected static void updateClusterSetting(String settingKey, Object value) throws IOException {
    updateClusterSetting(settingKey, value, true);
  }

  protected static void updateClusterSetting(String settingKey, Object value, boolean persistent)
      throws IOException {
    String property = persistent ? PERSISTENT : TRANSIENT;
    XContentBuilder builder = XContentFactory
        .jsonBuilder()
        .startObject()
        .startObject(property)
        .field(settingKey, value)
        .endObject()
        .endObject();
    Request request = new Request("PUT", "_cluster/settings");
    request.setJsonEntity(Strings.toString(builder));
    Response response = client().performRequest(request);
    Assert
        .assertEquals(RestStatus.OK, RestStatus.fromCode(response.getStatusLine().getStatusCode()));
  }

  protected static void wipeAllClusterSettings() throws IOException {
    updateClusterSetting("*", null, true);
    updateClusterSetting("*", null, false);
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
        "src/test/resources/dates.json");

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
