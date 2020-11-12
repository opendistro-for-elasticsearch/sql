package com.amazon.opendistroforelasticsearch.sql.benchmark.utils.load.elasticsearch;

import static com.amazon.opendistroforelasticsearch.sql.benchmark.utils.BenchmarkConstants.DATE;
import static com.amazon.opendistroforelasticsearch.sql.benchmark.utils.BenchmarkConstants.DOUBLE;
import static com.amazon.opendistroforelasticsearch.sql.benchmark.utils.BenchmarkConstants.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.benchmark.utils.BenchmarkConstants.KEYWORD;

import java.util.LinkedHashMap;
import java.util.Map;

public class ElasticsearchTpchSchema {

  public static Map<String, Map<String, String>> schemaMap;

  static {
    schemaMap = new LinkedHashMap<>();

    Map<String, String> customerArgs = new LinkedHashMap<>();
    customerArgs.put("c_custkey", INTEGER);
    customerArgs.put("c_name", KEYWORD);
    customerArgs.put("c_address", KEYWORD);
    customerArgs.put("c_nationkey", INTEGER);
    customerArgs.put("c_phone", KEYWORD);
    customerArgs.put("c_acctbal", DOUBLE);
    customerArgs.put("c_mktsegment", KEYWORD);
    customerArgs.put("c_comment", KEYWORD);
    schemaMap.put("customer", customerArgs);

    Map<String, String> lineitemArgs = new LinkedHashMap<>();
    lineitemArgs.put("l_orderkey", INTEGER);
    lineitemArgs.put("l_partkey", INTEGER);
    lineitemArgs.put("l_suppkey", INTEGER);
    lineitemArgs.put("l_linenumber", INTEGER);
    lineitemArgs.put("l_quantity", DOUBLE);
    lineitemArgs.put("l_extendedprice", DOUBLE);
    lineitemArgs.put("l_discount", DOUBLE);
    lineitemArgs.put("l_tax", DOUBLE);
    lineitemArgs.put("l_returnflag", KEYWORD);
    lineitemArgs.put("l_linestatus", KEYWORD);
    lineitemArgs.put("l_shipdate", DATE);
    lineitemArgs.put("l_commitdate", DATE);
    lineitemArgs.put("l_receiptdate", DATE);
    lineitemArgs.put("l_shipinstruct", KEYWORD);
    lineitemArgs.put("l_shipmode", KEYWORD);
    lineitemArgs.put("l_comment", KEYWORD);
    schemaMap.put("lineitem", lineitemArgs);

    Map<String, String> nationArgs = new LinkedHashMap<>();
    nationArgs.put("n_nationkey", INTEGER);
    nationArgs.put("n_name", KEYWORD);
    nationArgs.put("n_regionkey", INTEGER);
    nationArgs.put("n_comment", KEYWORD);
    schemaMap.put("nation", nationArgs);

    Map<String, String> ordersArgs = new LinkedHashMap<>();
    ordersArgs.put("o_orderkey", INTEGER);
    ordersArgs.put("o_custkey", INTEGER);
    ordersArgs.put("o_orderstatus", KEYWORD);
    ordersArgs.put("o_totalprice", DOUBLE);
    ordersArgs.put("o_orderdate", DATE);
    ordersArgs.put("o_orderpriority", KEYWORD);
    ordersArgs.put("o_clerk", KEYWORD);
    ordersArgs.put("o_shippriority", INTEGER);
    ordersArgs.put("o_comment", KEYWORD);
    schemaMap.put("orders", ordersArgs);

    Map<String, String> partArgs = new LinkedHashMap<>();
    partArgs.put("p_partkey", INTEGER);
    partArgs.put("p_name", KEYWORD);
    partArgs.put("p_mfgr", KEYWORD);
    partArgs.put("p_brand", KEYWORD);
    partArgs.put("p_type", KEYWORD);
    partArgs.put("p_size", INTEGER);
    partArgs.put("p_container", KEYWORD);
    partArgs.put("p_retailprice", DOUBLE);
    partArgs.put("p_comment", KEYWORD);
    schemaMap.put("part", partArgs);

    Map<String, String> partsuppArgs = new LinkedHashMap<>();
    partsuppArgs.put("ps_partkey", INTEGER);
    partsuppArgs.put("ps_suppkey", INTEGER);
    partsuppArgs.put("ps_availqty", INTEGER);
    partsuppArgs.put("ps_supplycost", DOUBLE);
    partsuppArgs.put("ps_comment", KEYWORD);
    schemaMap.put("partsupp", partsuppArgs);

    Map<String, String> regionArgs = new LinkedHashMap<>();
    regionArgs.put("r_regionkey", INTEGER);
    regionArgs.put("r_name", KEYWORD);
    regionArgs.put("r_comment", KEYWORD);
    schemaMap.put("region", regionArgs);

    Map<String, String> supplierArgs = new LinkedHashMap<>();
    supplierArgs.put("s_suppkey", INTEGER);
    supplierArgs.put("s_name", KEYWORD);
    supplierArgs.put("s_address", KEYWORD);
    supplierArgs.put("s_nationkey", INTEGER);
    supplierArgs.put("s_phone", KEYWORD);
    supplierArgs.put("s_acctbal", DOUBLE);
    supplierArgs.put("s_comment", KEYWORD);
    schemaMap.put("supplier", supplierArgs);
  }
}
