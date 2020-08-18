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

package com.amazon.opendistroforelasticsearch.sql.ppl.antlr;

import static org.junit.Assert.assertNotEquals;

import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class PPLSyntaxParserTest {

  @Rule
  public ExpectedException exceptionRule = ExpectedException.none();

  @Test
  public void testSearchCommandShouldPass() {
    ParseTree tree = new PPLSyntaxParser().analyzeSyntax("search source=t a=1 b=2");
    assertNotEquals(null, tree);
  }

  @Test
  public void testSearchCommandIgnoreSearchKeywordShouldPass() {
    ParseTree tree = new PPLSyntaxParser().analyzeSyntax("source=t a=1 b=2");
    assertNotEquals(null, tree);
  }

  @Test
  public void testSearchFieldsCommandShouldPass() {
    ParseTree tree = new PPLSyntaxParser().analyzeSyntax("search source=t a=1 b=2 | fields a,b");
    assertNotEquals(null, tree);
  }

  @Test
  public void testSearchCommandWithoutSourceShouldFail() {
    exceptionRule.expect(RuntimeException.class);
    exceptionRule.expectMessage("Failed to parse query due to offending symbol");

    new PPLSyntaxParser().analyzeSyntax("search a=1");
  }

  @Test
  public void testRareCommandShouldPass() {
    ParseTree tree = new PPLSyntaxParser().analyzeSyntax("source=t a=1 | rare a");
    assertNotEquals(null, tree);
  }

  @Test
  public void testRareCommandWithGroupByShouldPass() {
    ParseTree tree = new PPLSyntaxParser().analyzeSyntax("source=t a=1 | rare a by b");
    assertNotEquals(null, tree);
  }

  @Test
  public void testTopCommandWithoutNShouldPass() {
    ParseTree tree = new PPLSyntaxParser().analyzeSyntax("source=t a=1 | top a");
    assertNotEquals(null, tree);
  }

  @Test
  public void testTopCommandWithNShouldPass() {
    ParseTree tree = new PPLSyntaxParser().analyzeSyntax("source=t a=1 | top 1 a");
    assertNotEquals(null, tree);
  }

  @Test
  public void testTopCommandWithNAndGroupByShouldPass() {
    ParseTree tree = new PPLSyntaxParser().analyzeSyntax("source=t a=1 | top 1 a by b");
    assertNotEquals(null, tree);
  }

  @Test
  public void testTopCommandWithoutNAndGroupByShouldPass() {
    ParseTree tree = new PPLSyntaxParser().analyzeSyntax("source=t a=1 | top a by b");
    assertNotEquals(null, tree);
  }
}