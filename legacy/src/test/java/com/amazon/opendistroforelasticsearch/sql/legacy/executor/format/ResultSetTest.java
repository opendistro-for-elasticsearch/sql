/*
 *    Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License").
 *    You may not use this file except in compliance with the License.
 *    A copy of the License is located at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    or in the "license" file accompanying this file. This file is distributed
 *    on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *    express or implied. See the License for the specific language governing
 *    permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.sql.legacy.executor.format;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ResultSetTest {

  private final ResultSet resultSet = new ResultSet() {
    @Override
    public Schema getSchema() {
      return super.getSchema();
    }
  };

  /**
   * Case #1:
   * LIKE 'test%' is converted to:
   *  1. Regex pattern: test.*
   *  2. ES search pattern: test*
   * In this case, what ES returns is the final result.
   */
  @Test
  public void testWildcardForZeroOrMoreCharacters() {
    assertTrue(resultSet.matchesPatternIfRegex("test123", "test.*"));
  }

  /**
   * Case #2:
   * LIKE 'test_123' is converted to:
   *  1. Regex pattern: test.123
   *  2. ES search pattern: (all)
   * Because ES doesn't support single wildcard character, in this case, none is passed
   * as ES search pattern. So all index names are returned and need to be filtered by
   * regex pattern again.
   */
  @Test
  public void testWildcardForSingleCharacter() {
    assertFalse(resultSet.matchesPatternIfRegex("accounts", "test.23"));
    assertFalse(resultSet.matchesPatternIfRegex(".kibana", "test.23"));
    assertTrue(resultSet.matchesPatternIfRegex("test123", "test.23"));
  }

  /**
   * Case #3:
   * LIKE 'acc' has same regex and ES pattern.
   * In this case, only index name(s) aliased by 'acc' is returned.
   * So regex match is skipped to avoid wrong empty result.
   * The assumption here is ES won't return unrelated index names if
   * LIKE pattern doesn't include any wildcard.
   */
  @Test
  public void testIndexAlias() {
    assertTrue(resultSet.matchesPatternIfRegex("accounts", "acc"));
  }

  /**
   * Case #4:
   * LIKE 'test.2020.10' has same regex pattern. Because it includes dot (wildcard),
   * ES search pattern is all.
   * In this case, all index names are returned. Because the pattern includes dot,
   * it's treated as regex and regex match won't be skipped.
   */
  @Test
  public void testIndexNameWithDot() {
    assertFalse(resultSet.matchesPatternIfRegex("accounts", "test.2020.10"));
    assertFalse(resultSet.matchesPatternIfRegex(".kibana", "test.2020.10"));
    assertTrue(resultSet.matchesPatternIfRegex("test.2020.10", "test.2020.10"));
  }

}