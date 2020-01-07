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

package com.amazon.opendistroforelasticsearch.sql.correctness;

import com.amazon.opendistroforelasticsearch.sql.correctness.report.TestReport;
import com.amazon.opendistroforelasticsearch.sql.correctness.runner.ComparisonTest;
import com.amazon.opendistroforelasticsearch.sql.correctness.runner.connection.DBConnection;
import com.amazon.opendistroforelasticsearch.sql.correctness.runner.connection.ESConnection;
import com.amazon.opendistroforelasticsearch.sql.correctness.runner.connection.JDBCConnection;
import com.amazon.opendistroforelasticsearch.sql.correctness.testset.TestDataSet;
import com.amazon.opendistroforelasticsearch.sql.esintgtest.SQLIntegTestCase;
import com.amazon.opendistroforelasticsearch.sql.esintgtest.TestUtils;
import com.google.common.collect.Maps;
import org.apache.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.RestClient;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestUtils.getResourceFilePath;

/**
 * Correctness integration test by performing comparison test with other databases.
 */
public class CorrectnessIT extends SQLIntegTestCase {

    private static final Logger LOG = LogManager.getLogger();

    @Test
    public void performComparisonTest() {
        TestConfig config = new TestConfig(getCmdLineArgs());
        LOG.info("Starting comparison test {}", config);

        try (ComparisonTest test = new ComparisonTest(getThisDBConnection(config),
                                                      getOtherDBConnections(config))) {
            LOG.info("Loading test data set...");
            test.connect();
            for (TestDataSet dataSet : config.getTestDataSets()) {
                test.loadData(dataSet);
            }

            LOG.info("Verifying test queries...");
            TestReport report = test.verify(config.getTestQuerySet());

            LOG.info("Saving test report to disk...");
            store(report);

            LOG.info("Cleaning up test data...");
            for (TestDataSet dataSet : config.getTestDataSets()) {
                test.cleanUp(dataSet);
            }
        }
        LOG.info("Completed comparison test.");
    }

    private Map<String, String> getCmdLineArgs() {
        return Maps.fromProperties(System.getProperties());
    }

    private DBConnection getThisDBConnection(TestConfig config) {
        String dbUrl = config.getDbConnectionUrl();
        if (dbUrl.isEmpty()) {
            return getESConnection(config);
        }
        return new JDBCConnection("DB Tested", dbUrl);
    }

    /** Use Elasticsearch cluster given on CLI arg or internal embedded in SQLIntegTestCase */
    private DBConnection getESConnection(TestConfig config) {
        RestClient client;
        String esHost = config.getESHostUrl();
        if (esHost.isEmpty()) {
            client = getRestClient();
            esHost = client.getNodes().get(0).getHost().toString();
        } else {
            client = RestClient.builder(HttpHost.create(esHost)).build();
        }
        return new ESConnection("jdbc:elasticsearch://" + esHost, client);
    }

    /** Create database connection with database name and connect URL */
    private DBConnection[] getOtherDBConnections(TestConfig config) {
        return config.getOtherDbConnectionNameAndUrls().
                      entrySet().stream().
                      map(e -> new JDBCConnection(e.getKey(), e.getValue())).
                      toArray(DBConnection[]::new);
    }

    private void store(TestReport report) {
        try {
            // Create reports folder if not exists
            String folderPath = "reports/";
            Path path = Paths.get(getResourceFilePath(folderPath));
            if (Files.notExists(path)) {
                Files.createDirectory(path);
            }

            // Write to report file
            String relFilePath = folderPath + reportFileName();
            String absFilePath = getResourceFilePath(relFilePath);
            byte[] content = report.report().getBytes();

            LOG.info("Report file location is {}", absFilePath);
            Files.write(Paths.get(absFilePath), content);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to store report file", e);
        }
    }

    private String reportFileName() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        String dateTime = df.format(new Date());
        return "report_" + dateTime + ".json";
    }

}
