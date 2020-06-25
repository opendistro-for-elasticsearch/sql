/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazon.opendistroforelasticsearch.sql.correctness.tests;

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.amazon.opendistroforelasticsearch.sql.correctness.runner.resultset.DBResult;
import com.amazon.opendistroforelasticsearch.sql.correctness.runner.resultset.Row;
import com.amazon.opendistroforelasticsearch.sql.correctness.runner.resultset.Type;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.HashSet;
import org.junit.Test;

/**
 * Unit tests for {@link DBResult}
 */
public class DBResultTest {

  @Test
  public void dbResultFromDifferentDbNameShouldEqual() {
    DBResult result1 =
        new DBResult("DB 1", Arrays.asList(new Type("name", "VARCHAR")), emptyList());
    DBResult result2 =
        new DBResult("DB 2", Arrays.asList(new Type("name", "VARCHAR")), emptyList());
    assertEquals(result1, result2);
  }

  @Test
  public void dbResultWithDifferentColumnShouldNotEqual() {
    DBResult result1 =
        new DBResult("DB 1", Arrays.asList(new Type("name", "VARCHAR")), emptyList());
    DBResult result2 = new DBResult("DB 2", Arrays.asList(new Type("age", "INT")), emptyList());
    assertNotEquals(result1, result2);
  }

  @Test
  public void dbResultWithDifferentColumnTypeShouldNotEqual() {
    DBResult result1 = new DBResult("DB 1", Arrays.asList(new Type("age", "FLOAT")), emptyList());
    DBResult result2 = new DBResult("DB 2", Arrays.asList(new Type("age", "INT")), emptyList());
    assertNotEquals(result1, result2);
  }

  @Test
  public void canExplainColumnTypeDifference() {
    DBResult result1 = new DBResult("DB 1",
        Arrays.asList(new Type("name", "VARCHAR"), new Type("age", "FLOAT")), emptyList());
    DBResult result2 = new DBResult("DB 2",
        Arrays.asList(new Type("name", "VARCHAR"), new Type("age", "INT")), emptyList());

    assertEquals(
        "Schema type at [1] is different: "
            + "thisType=[Type(name=age, type=FLOAT)], otherType=[Type(name=age, type=INT)]",
        result1.diff(result2)
    );
  }

  @Test
  public void canExplainDataRowsDifference() {
    DBResult result1 = new DBResult("DB 1", Arrays.asList(new Type("name", "VARCHAR")),
        Sets.newHashSet(
            new Row(Arrays.asList("hello")),
            new Row(Arrays.asList("world")),
            new Row(Lists.newArrayList((Object) null))));
    DBResult result2 = new DBResult("DB 2",Arrays.asList(new Type("name", "VARCHAR")),
        Sets.newHashSet(
            new Row(Lists.newArrayList((Object) null)),
            new Row(Arrays.asList("hello")),
            new Row(Arrays.asList("world123"))));

    assertEquals(
        "Data row at [1] is different: this=[Row(values=[world])], other=[Row(values=[world123])]",
        result1.diff(result2)
    );
  }

}
