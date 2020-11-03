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

package com.amazon.opendistroforelasticsearch.sql.benchmark.utils.query;

import java.util.LinkedList;

public class Queries {

  public static int tpchQueriesCountMax = 22;
  public static LinkedList<String> queries = new LinkedList<>();

  static {
    queries.add("select l_returnflag, l_linestatus, sum(l_quantity) as sum_qty, "
        + "sum(l_extendedprice) as sum_base_price, sum(l_extendedprice * (1 - l_discount)) as "
        + "sum_disc_price, sum(l_extendedprice * (1 - l_discount) * (1 + l_tax)) as sum_charge, "
        + "avg(l_quantity) as avg_qty, avg(l_extendedprice) as avg_price, avg(l_discount) as "
        + "avg_disc, count(*) as count_order from lineitem;");
    queries.add("select c_custkey, c_name from customer where c_custkey in (1, 2, 3, 4, 5);");
    queries.add("select * from lineitem;");
    queries.add("select count(*), max(l_discount), l_comment from lineitem where l_shipmode = 'AIR'"
        + " ALLOW FILTERING");
  }
}
