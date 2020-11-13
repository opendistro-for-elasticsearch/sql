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
    // TODO: Add proper queries supported by all databases.
    queries.add("select * from lineitem;");
    queries.add("select l_orderkey, l_partkey, l_suppkey from lineitem;");
    queries
        .add("select count(*), max(l_discount), sum(l_tax), avg(l_extendedprice) from lineitem;");
  }
}
