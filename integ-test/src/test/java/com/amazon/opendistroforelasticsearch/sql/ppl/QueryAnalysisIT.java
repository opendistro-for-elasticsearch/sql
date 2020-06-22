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

import static com.amazon.opendistroforelasticsearch.sql.legacy.TestsConstants.TEST_INDEX_ACCOUNT;

import com.amazon.opendistroforelasticsearch.sql.common.antlr.SyntaxCheckException;
import com.amazon.opendistroforelasticsearch.sql.exception.SemanticCheckException;
import java.io.IOException;
import org.elasticsearch.client.ResponseException;
import org.junit.Ignore;
import org.junit.Test;

public class QueryAnalysisIT extends PPLIntegTestCase {

  @Override
  public void init() throws IOException {
    loadIndex(Index.ACCOUNT);
  }

  /** Valid commands should pass both syntax analysis and semantic check. */
  @Test
  public void searchCommandShouldPassSemanticCheck() {
    String query = String.format("search source=%s age=20", TEST_INDEX_ACCOUNT);
    queryShouldPassSyntaxAndSemanticCheck(query);
  }

  @Test
  public void whereCommandShouldPassSemanticCheck() {
    String query = String.format("search source=%s | where age=20", TEST_INDEX_ACCOUNT);
    queryShouldPassSyntaxAndSemanticCheck(query);
  }

  @Test
  public void fieldsCommandShouldPassSemanticCheck() {
    String query = String.format("search source=%s | fields firstname", TEST_INDEX_ACCOUNT);
    queryShouldPassSyntaxAndSemanticCheck(query);
  }

  @Ignore("Can't resolve target field yet")
  @Test
  public void renameCommandShouldPassSemanticCheck() {
    String query =
        String.format("search source=%s | rename firstname as first", TEST_INDEX_ACCOUNT);
    queryShouldPassSyntaxAndSemanticCheck(query);
  }

  @Test
  public void statsCommandShouldPassSemanticCheck() {
    String query = String.format("search source=%s | stats avg(age)", TEST_INDEX_ACCOUNT);
    queryShouldPassSyntaxAndSemanticCheck(query);
  }

  @Test
  public void dedupCommandShouldPassSemanticCheck() {
    String query =
        String.format("search source=%s | dedup firstname, lastname", TEST_INDEX_ACCOUNT);
    queryShouldPassSyntaxAndSemanticCheck(query);
  }

  @Test
  public void sortCommandShouldPassSemanticCheck() {
    String query = String.format("search source=%s | sort age", TEST_INDEX_ACCOUNT);
    queryShouldPassSyntaxAndSemanticCheck(query);
  }

  @Test
  public void evalCommandShouldPassSemanticCheck() {
    String query = String.format("search source=%s | eval age=abs(age)", TEST_INDEX_ACCOUNT);
    queryShouldPassSyntaxAndSemanticCheck(query);
  }

  @Test
  public void queryShouldBeCaseInsensitiveInKeywords() {
    String query = String.format("SEARCH SourCE=%s", TEST_INDEX_ACCOUNT);
    queryShouldPassSyntaxAndSemanticCheck(query);
  }

  /**
   * Commands that fail syntax analysis should throw {@link SyntaxCheckException}.
   */
  @Test
  public void queryNotStartingWithSearchCommandShouldFailSyntaxCheck() {
    String query = "fields firstname";
    queryShouldThrowSyntaxException(query, "Failed to parse query due to offending symbol");
  }

  @Test
  public void queryWithIncorrectCommandShouldFailSyntaxCheck() {
    String query = String.format("search source=%s | field firstname", TEST_INDEX_ACCOUNT);
    queryShouldThrowSyntaxException(query, "Failed to parse query due to offending symbol");
  }

  @Test
  public void queryWithIncorrectKeywordsShouldFailSyntaxCheck() {
    String query = String.format("search sources=%s", TEST_INDEX_ACCOUNT);
    queryShouldThrowSyntaxException(query, "Failed to parse query due to offending symbol");
  }

  @Test
  public void unsupportedAggregationFunctionShouldFailSyntaxCheck() {
    String query = String.format("search source=%s | stats range(age)", TEST_INDEX_ACCOUNT);
    queryShouldThrowSyntaxException(query, "Failed to parse query due to offending symbol");
  }

  /**
   * Commands that fail semantic analysis should throw {@link SemanticCheckException}.
   */
  @Test
  public void nonexistentFieldShouldFailSemanticCheck() {
    String query = String.format("search source=%s | fields name", TEST_INDEX_ACCOUNT);
    queryShouldThrowSemanticException(query, "can't resolve expression name in type env");
  }

  private void queryShouldPassSyntaxAndSemanticCheck(String query) {
    try {
      executeQuery(query);
    } catch (SemanticCheckException e) {
      fail("Expected to pass semantic check but failed for query: " + query);
    } catch (IOException e) {
      throw new IllegalStateException("Unexpected IOException raised for query: " + query);
    }
  }

  private void queryShouldThrowSyntaxException(String query, String... messages) {
    try {
      executeQuery(query);
      fail("Expected to throw SyntaxCheckException, but none was thrown for query: " + query);
    } catch (ResponseException e) {
      String errorMsg = e.getMessage();
      assertTrue(errorMsg.contains("SyntaxCheckException"));
      for (String msg: messages) {
        assertTrue(errorMsg.contains(msg));
      }
    } catch (IOException e) {
      throw new IllegalStateException("Unexpected exception raised for query: " + query);
    }
  }

  private void queryShouldThrowSemanticException(String query, String... messages) {
    try {
      executeQuery(query);
      fail("Expected to throw SemanticCheckException, but none was thrown for query: " + query);
    } catch (ResponseException e) {
      String errorMsg = e.getMessage();
      assertTrue(errorMsg.contains("SemanticCheckException"));
      for (String msg : messages) {
        assertTrue(errorMsg.contains(msg));
      }
    } catch (IOException e) {
      throw new IllegalStateException("Unexpected exception raised for query: " + query);
    }
  }
}
