package com.amazon.opendistroforelasticsearch.sql.benchmark.utils.load;

import java.util.LinkedHashMap;
import java.util.Map;

public class TpchSchema {

  public static Map<String, Map<String, String>> schemaMap;

  static {
    schemaMap = new LinkedHashMap<>();

    LinkedHashMap customerArgs = new LinkedHashMap<>();
    customerArgs.put("custkey", "integer");
    customerArgs.put("name", "keyword");
    customerArgs.put("address", "keyword");
    customerArgs.put("nationkey", "integer");
    customerArgs.put("phone", "keyword");
    customerArgs.put("acctbal", "double");
    customerArgs.put("mktsegment", "keyword");
    customerArgs.put("comment", "keyword");
    schemaMap.put("customer", customerArgs);

    LinkedHashMap lineitemArgs = new LinkedHashMap<>();
    lineitemArgs.put("orderkey", "integer");
    lineitemArgs.put("partkey", "integer");
    lineitemArgs.put("suppkey", "integer");
    lineitemArgs.put("linenumber", "integer");
    lineitemArgs.put("quantity", "integer");
    lineitemArgs.put("extendedprice", "double");
    lineitemArgs.put("discount", "double");
    lineitemArgs.put("tax", "double");
    lineitemArgs.put("returnflag", "keyword");
    lineitemArgs.put("linestatus", "keyword");
    lineitemArgs.put("shipdate", "date");
    lineitemArgs.put("commitdate", "date");
    lineitemArgs.put("receiptdate", "date");
    lineitemArgs.put("shipinstruct", "keyword");
    lineitemArgs.put("shipmode", "keyword");
    lineitemArgs.put("comment", "keyword");
    schemaMap.put("lineitem", lineitemArgs);

    LinkedHashMap nationArgs = new LinkedHashMap<>();
    nationArgs.put("nationkey", "integer");
    nationArgs.put("name", "keyword");
    nationArgs.put("regionkey", "integer");
    nationArgs.put("comment", "keyword");
    schemaMap.put("nation", nationArgs);

    LinkedHashMap ordersArgs = new LinkedHashMap<>();
    ordersArgs.put("orderkey", "integer");
    ordersArgs.put("custkey", "integer");
    ordersArgs.put("orderstatus", "keyword");
    ordersArgs.put("totalprice", "double");
    ordersArgs.put("orderdate", "date");
    ordersArgs.put("orderpriority", "keyword");
    ordersArgs.put("clerk", "keyword");
    ordersArgs.put("shippriority", "integer");
    ordersArgs.put("comment", "keyword");
    schemaMap.put("orders", ordersArgs);

    LinkedHashMap partArgs = new LinkedHashMap<>();
    partArgs.put("partkey", "integer");
    partArgs.put("name", "keyword");
    partArgs.put("mfgr", "keyword");
    partArgs.put("brand", "keyword");
    partArgs.put("type", "keyword");
    partArgs.put("size", "integer");
    partArgs.put("container", "keyword");
    partArgs.put("retailprice", "double");
    partArgs.put("comment", "keyword");
    schemaMap.put("part", partArgs);

    LinkedHashMap partsuppArgs = new LinkedHashMap<>();
    partsuppArgs.put("partkey", "integer");
    partsuppArgs.put("suppkey", "integer");
    partsuppArgs.put("availqty", "integer");
    partsuppArgs.put("supplycost", "double");
    partsuppArgs.put("comment", "keyword");
    schemaMap.put("partsupp", partsuppArgs);

    LinkedHashMap regionArgs = new LinkedHashMap<>();
    regionArgs.put("regionkey", "integer");
    regionArgs.put("name", "keyword");
    regionArgs.put("comment", "keyword");
    schemaMap.put("region", regionArgs);

    LinkedHashMap supplierArgs = new LinkedHashMap<>();
    supplierArgs.put("suppkey", "integer");
    supplierArgs.put("name", "keyword");
    supplierArgs.put("address", "keyword");
    supplierArgs.put("nationkey", "integer");
    supplierArgs.put("phone", "keyword");
    supplierArgs.put("acctbal", "double");
    supplierArgs.put("comment", "keyword");
    schemaMap.put("supplier", supplierArgs);
  }
}
