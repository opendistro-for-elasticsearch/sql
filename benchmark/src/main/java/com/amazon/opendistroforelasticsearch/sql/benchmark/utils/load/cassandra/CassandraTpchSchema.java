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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CassandraTpchSchema {

  public static Map<String, List<String>> primaryKeyMap;
  public static Map<String, Map<String, String>> schemaMap;
  public static String keyspaceName = "benchmark";

  static {
    primaryKeyMap = new LinkedHashMap<>();
    primaryKeyMap.put("customer", new LinkedList<>(Arrays.asList("c_custkey")));
    primaryKeyMap.put("lineitem", new LinkedList<>(Arrays.asList("l_orderkey", "l_linenumber")));
    primaryKeyMap.put("nation", new LinkedList<>(Arrays.asList("n_nationkey")));
    primaryKeyMap.put("orders", new LinkedList<>(Arrays.asList("o_orderkey")));
    primaryKeyMap.put("part", new LinkedList<>(Arrays.asList("p_partkey")));
    primaryKeyMap.put("partsupp", new LinkedList<>(Arrays.asList("ps_partkey", "ps_suppkey")));
    primaryKeyMap.put("region", new LinkedList<>(Arrays.asList("r_regionkey")));
    primaryKeyMap.put("supplier", new LinkedList<>(Arrays.asList("s_suppkey")));

    schemaMap = new LinkedHashMap<>();

    LinkedHashMap customerArgs = new LinkedHashMap<>();
    customerArgs.put("c_custkey", "bigint");
    customerArgs.put("c_name", "text");
    customerArgs.put("c_address", "text");
    customerArgs.put("c_nationkey", "bigint");
    customerArgs.put("c_phone", "text");
    customerArgs.put("c_acctbal", "decimal");
    customerArgs.put("c_mktsegment", "text");
    customerArgs.put("c_comment", "text");
    schemaMap.put("customer", customerArgs);

    LinkedHashMap lineitemArgs = new LinkedHashMap<>();
    lineitemArgs.put("l_orderkey", "bigint");
    lineitemArgs.put("l_partkey", "bigint");
    lineitemArgs.put("l_suppkey", "bigint");
    lineitemArgs.put("l_linenumber", "int");
    lineitemArgs.put("l_quantity", "decimal");
    lineitemArgs.put("l_extendedprice", "decimal");
    lineitemArgs.put("l_discount", "decimal");
    lineitemArgs.put("l_tax", "decimal");
    lineitemArgs.put("l_returnflag", "text");
    lineitemArgs.put("l_linestatus", "text");
    lineitemArgs.put("l_shipdate", "date");
    lineitemArgs.put("l_commitdate", "date");
    lineitemArgs.put("l_receiptdate", "date");
    lineitemArgs.put("l_shipinstruct", "text");
    lineitemArgs.put("l_shipmode", "text");
    lineitemArgs.put("l_comment", "text");
    schemaMap.put("lineitem", lineitemArgs);

    LinkedHashMap nationArgs = new LinkedHashMap<>();
    nationArgs.put("n_nationkey", "bigint");
    nationArgs.put("n_name", "text");
    nationArgs.put("n_regionkey", "bigint");
    nationArgs.put("n_comment", "text");
    schemaMap.put("nation", nationArgs);

    LinkedHashMap ordersArgs = new LinkedHashMap<>();
    ordersArgs.put("o_orderkey", "bigint");
    ordersArgs.put("o_custkey", "bigint");
    ordersArgs.put("o_orderstatus", "text");
    ordersArgs.put("o_totalprice", "decimal");
    ordersArgs.put("o_orderdate", "date");
    ordersArgs.put("o_orderpriority", "text");
    ordersArgs.put("o_clerk", "text");
    ordersArgs.put("o_shippriority", "int");
    ordersArgs.put("o_comment", "text");
    schemaMap.put("orders", ordersArgs);

    LinkedHashMap partArgs = new LinkedHashMap<>();
    partArgs.put("p_partkey", "bigint");
    partArgs.put("p_name", "text");
    partArgs.put("p_mfgr", "text");
    partArgs.put("p_brand", "text");
    partArgs.put("p_type", "text");
    partArgs.put("p_size", "int");
    partArgs.put("p_container", "text");
    partArgs.put("p_retailprice", "decimal");
    partArgs.put("p_comment", "text");
    schemaMap.put("part", partArgs);

    LinkedHashMap partsuppArgs = new LinkedHashMap<>();
    partsuppArgs.put("ps_partkey", "bigint");
    partsuppArgs.put("ps_suppkey", "bigint");
    partsuppArgs.put("ps_availqty", "int");
    partsuppArgs.put("ps_supplycost", "decimal");
    partsuppArgs.put("ps_comment", "text");
    schemaMap.put("partsupp", partsuppArgs);

    LinkedHashMap regionArgs = new LinkedHashMap<>();
    regionArgs.put("r_regionkey", "bigint");
    regionArgs.put("r_name", "text");
    regionArgs.put("r_comment", "text");
    schemaMap.put("region", regionArgs);

    LinkedHashMap supplierArgs = new LinkedHashMap<>();
    supplierArgs.put("s_suppkey", "bigint");
    supplierArgs.put("s_name", "text");
    supplierArgs.put("s_address", "text");
    supplierArgs.put("s_nationkey", "bigint");
    supplierArgs.put("s_phone", "text");
    supplierArgs.put("s_acctbal", "decimal");
    supplierArgs.put("s_comment", "text");
    schemaMap.put("supplier", supplierArgs);
  }
}