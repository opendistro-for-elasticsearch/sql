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

package com.amazon.opendistroforelasticsearch.sql.benchmark.utils;

/**
 * Class to hold benchmarking constants.
 */
public class BenchmarkConstants {
  public static final String ELASTICSEARCH = "elasticsearch";
  public static final String CASSANDRA = "cassandra";
  public static final String MOCK1 = "mock1";
  public static final String MOCK2 = "mock2";
  public static final String MOCK3 = "mock3";
  public static final String SUMMARY_FILE_NAME = "summary.jpg";
  public static final String OUTPUT_HTML_FILE_NAME = "index.html";
  private static final String QUERY_FILE_NAME_FORMAT = "%s.jpg";

  public static String queryToFileName(String query) {
    return String.format(QUERY_FILE_NAME_FORMAT, query);
  }
}
