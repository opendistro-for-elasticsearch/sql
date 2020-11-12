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

import static com.amazon.opendistroforelasticsearch.sql.benchmark.utils.BenchmarkConstants.BIGINT;
import static com.amazon.opendistroforelasticsearch.sql.benchmark.utils.BenchmarkConstants.DATE;
import static com.amazon.opendistroforelasticsearch.sql.benchmark.utils.BenchmarkConstants.DECIMAL;
import static com.amazon.opendistroforelasticsearch.sql.benchmark.utils.BenchmarkConstants.INT;
import static com.amazon.opendistroforelasticsearch.sql.benchmark.utils.BenchmarkConstants.TEXT;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CassandraTpchSchema {

  public static Map<String, List<String>> primaryKeyMap;
  public static Map<String, Map<String, String>> schemaMap;
  public static String keyspaceName = "benchmark";

  static {
    primaryKeyMap = new LinkedHashMap<>();
    primaryKeyMap.put("customer", Arrays.asList("c_custkey"));
    primaryKeyMap.put("lineitem", Arrays.asList("l_orderkey", "l_linenumber"));
    primaryKeyMap.put("nation", Arrays.asList("n_nationkey"));
    primaryKeyMap.put("orders", Arrays.asList("o_orderkey"));
    primaryKeyMap.put("part", Arrays.asList("p_partkey"));
    primaryKeyMap.put("partsupp", Arrays.asList("ps_partkey", "ps_suppkey"));
    primaryKeyMap.put("region", Arrays.asList("r_regionkey"));
    primaryKeyMap.put("supplier", Arrays.asList("s_suppkey"));

    schemaMap = new LinkedHashMap<>();

    Map<String, String> customerArgs = new LinkedHashMap<>();
    customerArgs.put("c_custkey", BIGINT);
    customerArgs.put("c_name", TEXT);
    customerArgs.put("c_address", TEXT);
    customerArgs.put("c_nationkey", BIGINT);
    customerArgs.put("c_phone", TEXT);
    customerArgs.put("c_acctbal", DECIMAL);
    customerArgs.put("c_mktsegment", TEXT);
    customerArgs.put("c_comment", TEXT);
    schemaMap.put("customer", customerArgs);

    Map<String, String> lineitemArgs = new LinkedHashMap<>();
    lineitemArgs.put("l_orderkey", BIGINT);
    lineitemArgs.put("l_partkey", BIGINT);
    lineitemArgs.put("l_suppkey", BIGINT);
    lineitemArgs.put("l_linenumber", INT);
    lineitemArgs.put("l_quantity", DECIMAL);
    lineitemArgs.put("l_extendedprice", DECIMAL);
    lineitemArgs.put("l_discount", DECIMAL);
    lineitemArgs.put("l_tax", DECIMAL);
    lineitemArgs.put("l_returnflag", TEXT);
    lineitemArgs.put("l_linestatus", TEXT);
    lineitemArgs.put("l_shipdate", DATE);
    lineitemArgs.put("l_commitdate", DATE);
    lineitemArgs.put("l_receiptdate", DATE);
    lineitemArgs.put("l_shipinstruct", TEXT);
    lineitemArgs.put("l_shipmode", TEXT);
    lineitemArgs.put("l_comment", TEXT);
    schemaMap.put("lineitem", lineitemArgs);

    Map<String, String> nationArgs = new LinkedHashMap<>();
    nationArgs.put("n_nationkey", BIGINT);
    nationArgs.put("n_name", TEXT);
    nationArgs.put("n_regionkey", BIGINT);
    nationArgs.put("n_comment", TEXT);
    schemaMap.put("nation", nationArgs);

    Map<String, String> ordersArgs = new LinkedHashMap<>();
    ordersArgs.put("o_orderkey", BIGINT);
    ordersArgs.put("o_custkey", BIGINT);
    ordersArgs.put("o_orderstatus", TEXT);
    ordersArgs.put("o_totalprice", DECIMAL);
    ordersArgs.put("o_orderdate", DATE);
    ordersArgs.put("o_orderpriority", TEXT);
    ordersArgs.put("o_clerk", TEXT);
    ordersArgs.put("o_shippriority", INT);
    ordersArgs.put("o_comment", TEXT);
    schemaMap.put("orders", ordersArgs);

    Map<String, String> partArgs = new LinkedHashMap<>();
    partArgs.put("p_partkey", BIGINT);
    partArgs.put("p_name", TEXT);
    partArgs.put("p_mfgr", TEXT);
    partArgs.put("p_brand", TEXT);
    partArgs.put("p_type", TEXT);
    partArgs.put("p_size", INT);
    partArgs.put("p_container", TEXT);
    partArgs.put("p_retailprice", DECIMAL);
    partArgs.put("p_comment", TEXT);
    schemaMap.put("part", partArgs);

    Map<String, String> partsuppArgs = new LinkedHashMap<>();
    partsuppArgs.put("ps_partkey", BIGINT);
    partsuppArgs.put("ps_suppkey", BIGINT);
    partsuppArgs.put("ps_availqty", INT);
    partsuppArgs.put("ps_supplycost", DECIMAL);
    partsuppArgs.put("ps_comment", TEXT);
    schemaMap.put("partsupp", partsuppArgs);

    Map<String, String> regionArgs = new LinkedHashMap<>();
    regionArgs.put("r_regionkey", BIGINT);
    regionArgs.put("r_name", TEXT);
    regionArgs.put("r_comment", TEXT);
    schemaMap.put("region", regionArgs);

    Map<String, String> supplierArgs = new LinkedHashMap<>();
    supplierArgs.put("s_suppkey", BIGINT);
    supplierArgs.put("s_name", TEXT);
    supplierArgs.put("s_address", TEXT);
    supplierArgs.put("s_nationkey", BIGINT);
    supplierArgs.put("s_phone", TEXT);
    supplierArgs.put("s_acctbal", DECIMAL);
    supplierArgs.put("s_comment", TEXT);
    schemaMap.put("supplier", supplierArgs);
  }
}