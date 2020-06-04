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

package com.amazon.opendistroforelasticsearch.sql.ppl;

import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static com.amazon.opendistroforelasticsearch.sql.sql.TestsConstants.TEST_INDEX_BANK;
import static com.amazon.opendistroforelasticsearch.sql.sql.TestsConstants.TEST_INDEX_BANK_WITH_NULL_VALUES;
import static com.amazon.opendistroforelasticsearch.sql.sql.TestsConstants.TEST_INDEX_DOG;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.rows;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.verifyOrder;

public class SortCommandIT extends PPLIntegTestCase {

  @Override
  public void init() throws IOException {
    loadIndex(Index.BANK);
    loadIndex(Index.BANK_WITH_NULL_VALUES);
    loadIndex(Index.DOG);
  }

  @Test
  public void testSortCommand() throws IOException {
    JSONObject result =
        executeQuery(String.format("source=%s | sort age | fields age", TEST_INDEX_BANK));
    verifyOrder(result, rows(28), rows(32), rows(33), rows(34), rows(36), rows(36), rows(39));
  }

  @Ignore("Order with duplicated value")
  @Test
  public void testSortWithNullValue() throws IOException {
    JSONObject result =
        executeQuery(
            String.format(
                "source=%s | sort balance | fields firstname, balance",
                TEST_INDEX_BANK_WITH_NULL_VALUES));
    verifyOrder(
        result,
        rows("Hattie"),
        rows("Elinor"),
        rows("Virginia"),
        rows("Dale", 4180),
        rows("Nanette", 32838),
        rows("Amber JOHnny", 39225),
        rows("Dillard", 48086));
  }

  @Test
  public void testSortStringField() throws IOException {
    JSONObject result =
        executeQuery(String.format("source=%s | sort lastname | fields lastname", TEST_INDEX_BANK));
    verifyOrder(
        result,
        rows("Adams"),
        rows("Ayala"),
        rows("Bates"),
        rows("Bond"),
        rows("Duke Willmington"),
        rows("Mcpherson"),
        rows("Ratliff"));
  }

  @Test
  public void testSortMultipleFields() throws IOException {
    JSONObject result =
        executeQuery(
            String.format("source=%s | sort dog_name, age | fields dog_name, age", TEST_INDEX_DOG));
    verifyOrder(result, rows("rex", 2), rows("snoopy", 4));
  }
}
