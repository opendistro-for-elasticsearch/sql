/*
 *   Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazon.opendistroforelasticsearch.sql.benchmark.utils.load.cassandra;

import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.load.DataFormat;
import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.load.DataLoader;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Session;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Data loader for Cassandra database.
 */
public class CassandraDataLoader implements DataLoader {
  private static final int MAX_UPLOAD_COUNT = 8;
  private static final int THREAD_PAUSE_TIME_MILLISECONDS = 100;
  private static final String NODE = "127.0.0.1";
  private Cluster cluster;
  private Session session;

  /**
   * Constructor for CassandraDataLoader.
   */
  public CassandraDataLoader() {
    cluster = Cluster.builder()
            .addContactPoint(NODE)
            .build();
    session = cluster.connect();
  }

  private List<String> readFile(String file) throws FileNotFoundException {
    Scanner s = new Scanner(new File(file));
    ArrayList<String> list = new ArrayList<>();
    while (s.hasNextLine()) {
      list.add(s.nextLine());
    }
    s.close();
    return list;
  }

  private void prepareTable(
      final String keyspace, final String table, final String format) {
    final String createKeyspace = String.format("CREATE KEYSPACE IF NOT EXISTS %s "
            + "WITH REPLICATION = "
            + "{ 'class' : 'SimpleStrategy', 'replication_factor' : 1 };", keyspace);
    final String createTable = String.format("CREATE TABLE IF NOT EXISTS %s.%s "
            + "(%s);", keyspace, table, format);
    session.execute(createKeyspace);
    session.execute(createTable);
  }

  private String getFormattedInsert(
      final String keyspace, final String table, final String format) {
    String stringFormat = String.format(" INSERT INTO %s.%s (%s)", keyspace, table, format);
    stringFormat += " VALUES (%s);";
    return stringFormat;
  }

  private ResultSetFuture uploadBatch(String uploadFormat, List<String> dataUploads) {
    final StringBuilder uploadData = new StringBuilder();
    uploadData.append("BEGIN BATCH");
    dataUploads.forEach(d -> uploadData.append(String.format(uploadFormat, d)));
    uploadData.append(" APPLY BATCH");
    return session.executeAsync(uploadData.toString());
  }

  /**
   * Function to remove any complete ResultSetFuture Objects and throw an Exception if any failed.
   * @param resultSetFutures List of ResultSetFutures to iterate on.
   * @throws Exception if a failure occurred in upload.
   */
  private void removeAndCheck(final List<ResultSetFuture> resultSetFutures) throws Exception {
    final List<ResultSetFuture> completeFutures = resultSetFutures.stream().filter(Future::isDone)
        .collect(Collectors.toList());
    resultSetFutures.removeAll(completeFutures);
    final List<ResultSet> results = completeFutures.stream().map(f -> {
      try {
        return f.get();
      } catch (Exception e) {
        System.out.println("Exception occurred: " + e);
        e.printStackTrace();
      }
      return null;
    }).collect(Collectors.toList());

    if (results.stream().anyMatch(Objects::isNull)) {
      throw new Exception("Failed to upload results to Cassandra.");
    }
  }

  /**
   * Load function for Cassandra databases.
   *
   * @param data Data to load in CassandraDataFormat.
   * @throws Exception Throws an Exception if data does not match expected type.
   */
  @Override
  public void loadData(final DataFormat data) throws Exception {
    if (!(data instanceof CassandraDataFormat)) {
      throw new IllegalArgumentException("Wrong data format for Cassandra.");
    }

    final CassandraDataFormat dataFormat = (CassandraDataFormat)data;
    final List<String> files = dataFormat.getFiles();
    final List<ResultSetFuture> futures = new ArrayList<>();
    for (String file : files) {
      removeAndCheck(futures);

      if (futures.size() >= MAX_UPLOAD_COUNT) {
        Thread.sleep(THREAD_PAUSE_TIME_MILLISECONDS);
        continue;
      }

      final List<String> queries = readFile(file);

      // According to format we can get keyspace, table, and data format in the following way
      final String keyspace = queries.remove(0);
      final String table = queries.remove(0);
      final String columnTypeFormat = queries.remove(0);
      final String columnFormat = queries.remove(0);

      // Prepare table, get upload format, and upload data.
      prepareTable(keyspace, table, columnTypeFormat);
      final String format = getFormattedInsert(keyspace, table, columnFormat);
      futures.add(uploadBatch(format, queries));
    }

    // Wait for any pending uploads to complete.
    while (futures.size() > 0) {
      removeAndCheck(futures);

      if (futures.size() > 0) {
        Thread.sleep(THREAD_PAUSE_TIME_MILLISECONDS);
      }
    }

    cluster.close();
    session.close();
  }
}
