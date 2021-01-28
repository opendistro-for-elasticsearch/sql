/*
 *    Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License").
 *    You may not use this file except in compliance with the License.
 *    A copy of the License is located at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    or in the "license" file accompanying this file. This file is distributed
 *    on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *    express or implied. See the License for the specific language governing
 *    permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.sql.sql;

import static com.amazon.opendistroforelasticsearch.sql.legacy.TestsConstants.TEST_INDEX_BANK_CSV_SANITIZE;

import com.amazon.opendistroforelasticsearch.sql.legacy.SQLIntegTestCase;
import java.io.IOException;
import java.util.Locale;
import org.junit.Test;

public class CsvFormatIT extends SQLIntegTestCase {

  @Override
  public void init() throws IOException {
    loadIndex(Index.BANK_CSV_SANITIZE);
  }

  @Test
  public void sanitizeTest() {
    String result = executeQuery(
        String.format(Locale.ROOT, "SELECT firstname, lastname FROM %s", TEST_INDEX_BANK_CSV_SANITIZE), "csv");
    assertEquals(
        "firstname,lastname\n"
            + "'+Amber JOHnny,Duke Willmington+\n"
            + "'-Hattie,Bond-\n"
            + "'=Nanette,Bates=\n"
            + "'@Dale,Adams@\n"
            + "\",Elinor\",\"Ratliff,,,\"\n",
        result);
  }

  @Test
  public void escapeSanitizeTest() {
    String result = executeQuery(
        String.format(Locale.ROOT, "SELECT firstname, lastname FROM %s", TEST_INDEX_BANK_CSV_SANITIZE),
        "csv&sanitize=false");
    assertEquals(
        "firstname,lastname\n"
            + "+Amber JOHnny,Duke Willmington+\n"
            + "-Hattie,Bond-\n"
            + "=Nanette,Bates=\n"
            + "@Dale,Adams@\n"
            + ",Elinor,Ratliff,,,\n",
        result);
  }
}
