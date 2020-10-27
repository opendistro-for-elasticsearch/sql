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

package com.amazon.opendistroforelasticsearch.sql.benchmark;

import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.launch.DatabaseLauncher;
import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.launch.DatabaseLauncherFactory;
import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.load.DataFormat;
import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.load.DataGenerator;
import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.load.DataLoader;
import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.load.DataTransformer;
import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.load.DataUtilHolderFactory;
import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.query.Queries;
import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.query.QueryGenerator;
import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.query.ResultGrabber;
import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.results.BenchmarkResults;
import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.results.BenchmarkResultsInterpreter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class to run benchmarking, includes entry-point for benchmarking.
 */
public class BenchmarkService {
  private List<String> types;
  private String outputFile;
  private String benchmarkPath;
  private String dataDirectoryPath;
  private List<Double> scaleFactors;
  private String systemPassword;

  private static final String TYPES = "types";
  private static final String OUTPUT_FILE = "outputFile";
  private static final String BENCHMARK_PATH = "benchmarkPath";
  private static final String SCALE_FACTORS = "scaleFactors";
  private static final String SYSTEM_PASSWORD = "systemPassword";
  private static final Set<String> EXPECTED_KEYS = ImmutableSet.of(
      TYPES, OUTPUT_FILE, BENCHMARK_PATH, SCALE_FACTORS, SYSTEM_PASSWORD);

  /**
   * Constructor for BenchmarkingService.
   * @param filePath Path to configuration file.
   * @throws Exception Thrown if file parsing fails.
   */
  private BenchmarkService(final String filePath) throws Exception {
    parseFile(filePath);
  }

  /**
   * Function to run all procedures related to benchmarking.
   * @throws Exception Thrown if benchmarking fails.
   */
  private void runBenchmarks() throws Exception {
    final List<BenchmarkResults> results = new ArrayList<>();
    for (Double sf: scaleFactors) {
      DataGenerator.generateData(benchmarkPath, sf);
      QueryGenerator.generateQueries(benchmarkPath, sf);
      for (final String type: types) {
        DatabaseLauncher launcher = DatabaseLauncherFactory.getDatabaseLauncher(type);
        launcher.launchDatabase(systemPassword);
        performDataLoad(type);
        results.add(performBenchmark(type, sf));
        launcher.shutdownDatabase(systemPassword);
      }
      DataGenerator.cleanupData(benchmarkPath);
      QueryGenerator.cleanupQueries(benchmarkPath);
    }
    interpretResults(results);
  }

  /**
   * Function to load data into a specified database.
   * @param type Type of database to load data into.
   * @throws Exception Thrown if data load fails.
   */
  private void performDataLoad(final String type) throws Exception {
    // Grab utility classes
    final DataUtilHolderFactory.DataUtilHolder dataUtilHolder =
        DataUtilHolderFactory.getDataUtilHolder(type);
    final DataLoader dataLoader = dataUtilHolder.getDataLoader();
    final DataTransformer dataTransformer = dataUtilHolder.getDataTransformer();

    // Generate data, transform, and load data.
    final DataFormat dataFormat = dataTransformer.transformData(dataDirectoryPath);
    dataLoader.loadData(dataFormat);
  }

  /**
   * Function to run benchmarking and get a result for a specified database.
   * @param type Database type to run benchmarking against.
   * @param scaleFactor Scale factor for data set.
   * @return BenchmarkResults for the query execution.
   * @throws Exception Thrown if benchmarking fails.
   */
  private BenchmarkResults performBenchmark(final String type, final Double scaleFactor)
      throws Exception {
    final ResultGrabber resultGrabber = new ResultGrabber(type, scaleFactor);
    return resultGrabber.runQueries(Queries.queries, benchmarkPath);
  }

  /**
   * Function to run result interpreter.
   * @param results List of benchmarking results to use.
   */
  private void interpretResults(final List<BenchmarkResults> results) throws Exception {
    final BenchmarkResultsInterpreter interpreter = new BenchmarkResultsInterpreter();
    interpreter.interpretResults(results);
  }

  /**
   * Function to parse the config file and get parameters needed for running benchmarking.
   * @param filePath Path to config file.
   * @throws Exception Thrown if config file parsing fails.
   */
  private void parseFile(final String filePath) throws Exception {
    final String jsonString = new String(Files.readAllBytes(Paths.get(filePath).toAbsolutePath()));
    final ObjectMapper mapper = new ObjectMapper();
    final Map map = mapper.readValue(jsonString, Map.class);
    if (!map.keySet().equals(EXPECTED_KEYS)) {
      throw new Exception(
          String.format("Expected JSON config file to contain the following keys: '%s'. "
              + "Instead it contained the following keys: '%s'.",
              EXPECTED_KEYS.toString(), map.keySet().toString()));
    }
    types = getValueCheckType(map, TYPES, ArrayList.class);
    outputFile = getValueCheckType(map, OUTPUT_FILE, String.class);
    final String basePath = getValueCheckType(map, BENCHMARK_PATH, String.class);
    benchmarkPath = Paths.get(basePath).toAbsolutePath().toString() + "/";
    dataDirectoryPath = Paths.get(basePath, "data").toString() + "/";
    scaleFactors = getValueCheckType(map, SCALE_FACTORS, ArrayList.class);
    systemPassword = getValueCheckType(map, SYSTEM_PASSWORD, String.class);
  }

  /**
   * Function to get value from map and check the type before returning it.
   * @param map Map to get result from.
   * @param key Key that Object exists in map under.
   * @param expectedClass Expected type of Object from map.
   * @param <T> Template type to cast Object to.
   * @return Object casted for specific type.
   * @throws Exception Throws an exception if the type does not match.
   */
  @SuppressWarnings("unchecked")
  private <T> T getValueCheckType(final Map map, final String key, final Class<?> expectedClass)
      throws Exception {
    final Object obj = map.get(key);
    if (!(obj.getClass().equals(expectedClass))) {
      throw new Exception(String.format("Expected %s key to have a value of type '%s'. "
          + "Instead it contained a value of type '%s'.",
          key, expectedClass.toString(), obj.getClass().toString()));
    }
    return (T)obj;
  }

  /**
   * Entry point to run benchmarking service.
   * @param args Should only specify a single configuration file.
   */
  public static void main(final String[] args) {
    if (args.length != 1) {
      System.out.println(
          String.format("Expected a single argument (path to config file), "
              + "but received %d arguments.", args.length));
      return;
    }

    try {
      final BenchmarkService benchmarkService = new BenchmarkService(args[0]);
      benchmarkService.runBenchmarks();
    } catch (Exception e) {
      System.out.println("Received exception when benchmarking: " + e);
    }
  }
}
