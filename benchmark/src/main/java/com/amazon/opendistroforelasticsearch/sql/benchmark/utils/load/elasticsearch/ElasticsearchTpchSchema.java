package com.amazon.opendistroforelasticsearch.sql.benchmark.utils.load.elasticsearch;

import java.util.LinkedHashMap;
import java.util.Map;

public class ElasticsearchTpchSchema {

  public static Map<String, Map<String, String>> schemaMap;

  static {
    schemaMap = new LinkedHashMap<>();

    LinkedHashMap customerArgs = new LinkedHashMap<>();
    customerArgs.put("c_custkey", "integer");
    customerArgs.put("c_name", "keyword");
    customerArgs.put("c_address", "keyword");
    customerArgs.put("c_nationkey", "integer");
    customerArgs.put("c_phone", "keyword");
    customerArgs.put("c_acctbal", "double");
    customerArgs.put("c_mktsegment", "keyword");
    customerArgs.put("c_comment", "keyword");
    schemaMap.put("customer", customerArgs);

    LinkedHashMap lineitemArgs = new LinkedHashMap<>();
    lineitemArgs.put("l_orderkey", "integer");
    lineitemArgs.put("l_partkey", "integer");
    lineitemArgs.put("l_suppkey", "integer");
    lineitemArgs.put("l_linenumber", "integer");
    lineitemArgs.put("l_quantity", "integer");
    lineitemArgs.put("l_extendedprice", "double");
    lineitemArgs.put("l_discount", "double");
    lineitemArgs.put("l_tax", "double");
    lineitemArgs.put("l_returnflag", "keyword");
    lineitemArgs.put("l_linestatus", "keyword");
    lineitemArgs.put("l_shipdate", "date");
    lineitemArgs.put("l_commitdate", "date");
    lineitemArgs.put("l_receiptdate", "date");
    lineitemArgs.put("l_shipinstruct", "keyword");
    lineitemArgs.put("l_shipmode", "keyword");
    lineitemArgs.put("l_comment", "keyword");
    schemaMap.put("lineitem", lineitemArgs);

    LinkedHashMap nationArgs = new LinkedHashMap<>();
    nationArgs.put("n_nationkey", "integer");
    nationArgs.put("n_name", "keyword");
    nationArgs.put("n_regionkey", "integer");
    nationArgs.put("n_comment", "keyword");
    schemaMap.put("nation", nationArgs);

    LinkedHashMap ordersArgs = new LinkedHashMap<>();
    ordersArgs.put("o_orderkey", "integer");
    ordersArgs.put("o_custkey", "integer");
    ordersArgs.put("o_orderstatus", "keyword");
    ordersArgs.put("o_totalprice", "double");
    ordersArgs.put("o_orderdate", "date");
    ordersArgs.put("o_orderpriority", "keyword");
    ordersArgs.put("o_clerk", "keyword");
    ordersArgs.put("o_shippriority", "integer");
    ordersArgs.put("o_comment", "keyword");
    schemaMap.put("orders", ordersArgs);

    LinkedHashMap partArgs = new LinkedHashMap<>();
    partArgs.put("p_partkey", "integer");
    partArgs.put("p_name", "keyword");
    partArgs.put("p_mfgr", "keyword");
    partArgs.put("p_brand", "keyword");
    partArgs.put("p_type", "keyword");
    partArgs.put("p_size", "integer");
    partArgs.put("p_container", "keyword");
    partArgs.put("p_retailprice", "double");
    partArgs.put("p_comment", "keyword");
    schemaMap.put("part", partArgs);

    LinkedHashMap partsuppArgs = new LinkedHashMap<>();
    partsuppArgs.put("ps_partkey", "integer");
    partsuppArgs.put("ps_suppkey", "integer");
    partsuppArgs.put("ps_availqty", "integer");
    partsuppArgs.put("ps_supplycost", "double");
    partsuppArgs.put("ps_comment", "keyword");
    schemaMap.put("partsupp", partsuppArgs);

    LinkedHashMap regionArgs = new LinkedHashMap<>();
    regionArgs.put("r_regionkey", "integer");
    regionArgs.put("r_name", "keyword");
    regionArgs.put("r_comment", "keyword");
    schemaMap.put("region", regionArgs);

    LinkedHashMap supplierArgs = new LinkedHashMap<>();
    supplierArgs.put("s_suppkey", "integer");
    supplierArgs.put("s_name", "keyword");
    supplierArgs.put("s_address", "keyword");
    supplierArgs.put("s_nationkey", "integer");
    supplierArgs.put("s_phone", "keyword");
    supplierArgs.put("s_acctbal", "double");
    supplierArgs.put("s_comment", "keyword");
    schemaMap.put("supplier", supplierArgs);
  }
}
