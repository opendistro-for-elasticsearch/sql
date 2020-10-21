package com.amazon.opendistroforelasticsearch.sql.benchmark.utils.load;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TpchSchema {

  public static Map<String, ArrayList> schemaMap;

  static {
    schemaMap = new HashMap<>();
    schemaMap.put("customer", new ArrayList<>(
        Arrays.asList("custkey", "name", "address", "nationkey", "phone", "acctbal", "mktsegment",
            "comment")));
    schemaMap.put("lineitem", new ArrayList<>(
        Arrays.asList("orderkey", "partkey", "suppkey", "linenumber", "quantity", "extendedprice",
            "discount", "tax", "returnflag", "linestatus", "shipdate", "commitdate", "receiptdate",
            "shipinstruct", "shipmode", "comment")));
    schemaMap.put("nation", new ArrayList<>(
        Arrays.asList("nationkey", "name", "regionkey", " comment")));
    schemaMap.put("orders", new ArrayList<>(
        Arrays.asList("orderkey", " custkey", " orderstatus", "totalprice", "orderdate",
            "orderpriority", "clerk", "shippriority", "comment")));
    schemaMap.put("part", new ArrayList<>(
        Arrays.asList("partkey", "name", "mfgr", "brand", "type", "size", "container",
            "retailprice", "comment")));
    schemaMap.put("partsupp", new ArrayList<>(
        Arrays.asList("partkey", "suppkey", "availqty", "supplycost", "comment")));
    schemaMap.put("region", new ArrayList<>(
        Arrays.asList("regionkey", "name", " comment")));
    schemaMap.put("supplier", new ArrayList<>(
        Arrays.asList("suppkey", "name", "address", "nationkey", "phone", "acctbal", "comment")));
  }
}
