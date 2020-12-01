/*
 *
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

package com.amazon.opendistroforelasticsearch.sql.ppl.utils;

import static org.junit.Assert.assertEquals;

import com.amazon.opendistroforelasticsearch.sql.ppl.antlr.PPLSyntaxParser;
import com.amazon.opendistroforelasticsearch.sql.ppl.parser.AstBuilder;
import com.amazon.opendistroforelasticsearch.sql.ppl.parser.AstExpressionBuilder;
import org.junit.Test;

public class PPLQueryDataAnonymizerTest {

  private PPLSyntaxParser parser = new PPLSyntaxParser();

  @Test
  public void testSearchCommand() {
    assertEquals("source=t | where a = ***",
        anonymize("search source=t a=1")
    );
  }

  @Test
  public void testWhereCommand() {
    assertEquals("source=t | where a = ***",
        anonymize("search source=t | where a=1")
    );
  }

  @Test
  public void testFieldsCommandWithoutArguments() {
    assertEquals("source=t | fields + f,g",
        anonymize("source=t | fields f,g"));
  }

  @Test
  public void testFieldsCommandWithIncludeArguments() {
    assertEquals("source=t | fields + f,g",
        anonymize("source=t | fields + f,g"));
  }

  @Test
  public void testFieldsCommandWithExcludeArguments() {
    assertEquals("source=t | fields - f,g",
        anonymize("source=t | fields - f,g"));
  }

  @Test
  public void testRenameCommandWithMultiFields() {
    assertEquals("source=t | rename f as g,h as i,j as k",
        anonymize("source=t | rename f as g,h as i,j as k"));
  }

  @Test
  public void testStatsCommandWithByClause() {
    assertEquals("source=t | stats count(a) by b",
        anonymize("source=t | stats count(a) by b"));
  }

  @Test
  public void testStatsCommandWithNestedFunctions() {
    assertEquals("source=t | stats sum(+(a,b))",
        anonymize("source=t | stats sum(a+b)"));
  }

  @Test
  public void testDedupCommand() {
    assertEquals("source=t | dedup f1,f2 1 keepempty=false consecutive=false",
        anonymize("source=t | dedup f1, f2"));
  }

  @Test
  public void testHeadCommandWithNumber() {
    assertEquals("source=t | head keeplast=true while(***) 3",
        anonymize("source=t | head 3"));
  }

  @Test
  public void testHeadCommandWithWhileExpr() {
    assertEquals("source=t | head keeplast=true while(a < ***) 5",
        anonymize("source=t | head while(a < 5) 5"));
  }

  //todo, sort order is ignored, it doesn't impact the log analysis.
  @Test
  public void testSortCommandWithOptions() {
    assertEquals("source=t | sort f1,f2",
        anonymize("source=t | sort - f1, + f2"));
  }

  @Test
  public void testEvalCommand() {
    assertEquals("source=t | eval r=abs(f)",
        anonymize("source=t | eval r=abs(f)"));
  }

  @Test
  public void testRareCommandWithGroupBy() {
    assertEquals("source=t | rare 10 a by b",
        anonymize("source=t | rare a by b"));
  }

  @Test
  public void testTopCommandWithNAndGroupBy() {
    assertEquals("source=t | top 1 a by b",
        anonymize("source=t | top 1 a by b"));
  }

  @Test
  public void testAndExpression() {
    assertEquals("source=t | where a = *** and b = ***",
        anonymize("source=t | where a=1 and b=2")
    );
  }

  @Test
  public void testOrExpression() {
    assertEquals("source=t | where a = *** or b = ***",
        anonymize("source=t | where a=1 or b=2")
    );
  }

  @Test
  public void testXorExpression() {
    assertEquals("source=t | where a = *** xor b = ***",
        anonymize("source=t | where a=1 xor b=2")
    );
  }

  @Test
  public void testNotExpression() {
    assertEquals("source=t | where not a = ***",
        anonymize("source=t | where not a=1 ")
    );
  }

  @Test
  public void testQualifiedName() {
    assertEquals("source=t | fields + field0.field1",
        anonymize("source=t | fields field0.field1")
    );
  }

  @Test
  public void testDateFunction() {
    assertEquals("source=t | eval date=DATE_ADD(DATE(***),INTERVAL *** HOUR)",
        anonymize("source=t | eval date=DATE_ADD(DATE('2020-08-26'),INTERVAL 1 HOUR)")
    );
  }

  private String anonymize(String query) {
    AstBuilder astBuilder = new AstBuilder(new AstExpressionBuilder(), query);
    final PPLQueryDataAnonymizer anonymizer = new PPLQueryDataAnonymizer();
    return anonymizer.anonymizeData(astBuilder.visit(parser.analyzeSyntax(query)));
  }
}
