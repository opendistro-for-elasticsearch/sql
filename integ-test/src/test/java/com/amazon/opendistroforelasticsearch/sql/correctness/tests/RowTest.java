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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.amazon.opendistroforelasticsearch.sql.correctness.runner.resultset.Row;
import org.junit.Test;

/**
 * Unit test {@link Row}
 */
public class RowTest {

  @Test
  public void rowShouldEqualToOtherRowWithSimilarFloat() {
    Row row1 = new Row();
    Row row2 = new Row();
    row1.add(1.000001);
    row2.add(1.000002);
    assertEquals(row1, row2);
    assertEquals(row2, row1);
  }

  @Test
  public void rowShouldNotEqualToOtherRowWithDifferentString() {
    Row row1 = new Row();
    Row row2 = new Row();
    row1.add("hello");
    row2.add("hello1");
    assertNotEquals(row1, row2);
    assertNotEquals(row2, row1);
  }

  @Test
  public void shouldConsiderNullGreater() {
    Row row1 = new Row();
    Row row2 = new Row();
    row1.add("hello");
    row1.add(null);
    row2.add("hello");
    row2.add("world");
    assertEquals(1, row1.compareTo(row2));
  }

}
