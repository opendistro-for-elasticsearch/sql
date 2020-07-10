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

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazon.opendistroforelasticsearch.sql.correctness.report.ErrorTestCase;
import com.amazon.opendistroforelasticsearch.sql.correctness.report.FailedTestCase;
import com.amazon.opendistroforelasticsearch.sql.correctness.report.SuccessTestCase;
import com.amazon.opendistroforelasticsearch.sql.correctness.report.TestReport;
import com.amazon.opendistroforelasticsearch.sql.correctness.runner.ComparisonTest;
import com.amazon.opendistroforelasticsearch.sql.correctness.runner.connection.DBConnection;
import com.amazon.opendistroforelasticsearch.sql.correctness.runner.resultset.DBResult;
import com.amazon.opendistroforelasticsearch.sql.correctness.runner.resultset.Row;
import com.amazon.opendistroforelasticsearch.sql.correctness.runner.resultset.Type;
import com.amazon.opendistroforelasticsearch.sql.correctness.testset.TestQuerySet;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Tests for {@link ComparisonTest}
 */
@RunWith(MockitoJUnitRunner.class)
public class ComparisonTestTest {

  @Mock
  private DBConnection esConnection;

  @Mock
  private DBConnection otherDbConnection;

  private ComparisonTest correctnessTest;

  @Before
  public void setUp() {
    when(esConnection.getDatabaseName()).thenReturn("ES");
    when(otherDbConnection.getDatabaseName()).thenReturn("Other");
    correctnessTest = new ComparisonTest(
        esConnection, new DBConnection[] {otherDbConnection}
    );
  }

  @Test
  public void testSuccess() {
    when(esConnection.select(anyString())).thenReturn(
        new DBResult("ES", asList(new Type("firstname", "text")), asList(new Row(asList("John"))))
    );
    when(otherDbConnection.select(anyString())).thenReturn(
        new DBResult("Other DB", asList(new Type("firstname", "text")),
            asList(new Row(asList("John"))))
    );

    TestReport expected = new TestReport();
    expected.addTestCase(new SuccessTestCase(1, "SELECT * FROM accounts"));
    TestReport actual = correctnessTest.verify(querySet("SELECT * FROM accounts"));
    assertEquals(expected, actual);
  }

  @Test
  public void testFailureDueToInconsistency() {
    DBResult esResult =
        new DBResult("ES", asList(new Type("firstname", "text")), asList(new Row(asList("John"))));
    DBResult otherDbResult = new DBResult("Other DB", asList(new Type("firstname", "text")),
        asList(new Row(asList("JOHN"))));
    when(esConnection.select(anyString())).thenReturn(esResult);
    when(otherDbConnection.select(anyString())).thenReturn(otherDbResult);

    TestReport expected = new TestReport();
    expected.addTestCase(
        new FailedTestCase(1, "SELECT * FROM accounts", asList(esResult, otherDbResult), ""));
    TestReport actual = correctnessTest.verify(querySet("SELECT * FROM accounts"));
    assertEquals(expected, actual);
  }

  @Test
  public void testSuccessFinally() {
    DBConnection anotherDbConnection = mock(DBConnection.class);
    when(anotherDbConnection.getDatabaseName()).thenReturn("Another");
    correctnessTest = new ComparisonTest(
        esConnection, new DBConnection[] {otherDbConnection, anotherDbConnection}
    );

    DBResult esResult =
        new DBResult("ES", asList(new Type("firstname", "text")), asList(new Row(asList("John"))));
    DBResult otherDbResult = new DBResult("Other DB", asList(new Type("firstname", "text")),
        asList(new Row(asList("JOHN"))));
    DBResult anotherDbResult = new DBResult("Another DB", asList(new Type("firstname", "text")),
        asList(new Row(asList("John"))));
    when(esConnection.select(anyString())).thenReturn(esResult);
    when(otherDbConnection.select(anyString())).thenReturn(otherDbResult);
    when(anotherDbConnection.select(anyString())).thenReturn(anotherDbResult);

    TestReport expected = new TestReport();
    expected.addTestCase(new SuccessTestCase(1, "SELECT * FROM accounts"));
    TestReport actual = correctnessTest.verify(querySet("SELECT * FROM accounts"));
    assertEquals(expected, actual);
  }

  @Test
  public void testFailureDueToEventualInconsistency() {
    DBConnection anotherDbConnection = mock(DBConnection.class);
    when(anotherDbConnection.getDatabaseName())
        .thenReturn("ZZZ DB"); // Make sure this will be called after Other DB
    correctnessTest = new ComparisonTest(
        esConnection, new DBConnection[] {otherDbConnection, anotherDbConnection}
    );

    DBResult esResult =
        new DBResult("ES", asList(new Type("firstname", "text")), asList(new Row(asList("John"))));
    DBResult otherDbResult = new DBResult("Other DB", asList(new Type("firstname", "text")),
        asList(new Row(asList("JOHN"))));
    DBResult anotherDbResult = new DBResult("ZZZ DB", asList(new Type("firstname", "text")),
        asList(new Row(asList("Hank"))));
    when(esConnection.select(anyString())).thenReturn(esResult);
    when(otherDbConnection.select(anyString())).thenReturn(otherDbResult);
    when(anotherDbConnection.select(anyString())).thenReturn(anotherDbResult);

    TestReport expected = new TestReport();
    expected.addTestCase(new FailedTestCase(1, "SELECT * FROM accounts",
        asList(esResult, otherDbResult, anotherDbResult), ""));
    TestReport actual = correctnessTest.verify(querySet("SELECT * FROM accounts"));
    assertEquals(expected, actual);
  }

  @Test
  public void testErrorDueToESException() {
    when(esConnection.select(anyString())).thenThrow(new RuntimeException("All shards failure"));

    TestReport expected = new TestReport();
    expected.addTestCase(
        new ErrorTestCase(1, "SELECT * FROM accounts", "RuntimeException: All shards failure"));
    TestReport actual = correctnessTest.verify(querySet("SELECT * FROM accounts"));
    assertEquals(expected, actual);
  }

  @Test
  public void testErrorDueToNoOtherDBSupportThisQuery() {
    when(esConnection.select(anyString())).thenReturn(
        new DBResult("ES", asList(new Type("firstname", "text")), asList(new Row(asList("John"))))
    );
    when(otherDbConnection.select(anyString()))
        .thenThrow(new RuntimeException("Unsupported feature"));

    TestReport expected = new TestReport();
    expected.addTestCase(new ErrorTestCase(1, "SELECT * FROM accounts",
        "No other databases support this query: Unsupported feature;"));
    TestReport actual = correctnessTest.verify(querySet("SELECT * FROM accounts"));
    assertEquals(expected, actual);
  }

  @Test
  public void testSuccessWhenOneDBSupportThisQuery() {
    DBConnection anotherDbConnection = mock(DBConnection.class);
    when(anotherDbConnection.getDatabaseName()).thenReturn("Another");
    correctnessTest = new ComparisonTest(
        esConnection, new DBConnection[] {otherDbConnection, anotherDbConnection}
    );

    when(esConnection.select(anyString())).thenReturn(
        new DBResult("ES", asList(new Type("firstname", "text")), asList(new Row(asList("John"))))
    );
    when(otherDbConnection.select(anyString()))
        .thenThrow(new RuntimeException("Unsupported feature"));
    when(anotherDbConnection.select(anyString())).thenReturn(
        new DBResult("Another DB", asList(new Type("firstname", "text")),
            asList(new Row(asList("John"))))
    );

    TestReport expected = new TestReport();
    expected.addTestCase(new SuccessTestCase(1, "SELECT * FROM accounts"));
    TestReport actual = correctnessTest.verify(querySet("SELECT * FROM accounts"));
    assertEquals(expected, actual);
  }

  @Test
  public void testFailureDueToInconsistencyAndExceptionMixed() {
    DBConnection otherDBConnection2 = mock(DBConnection.class);
    when(otherDBConnection2.getDatabaseName()).thenReturn("ZZZ DB");
    correctnessTest = new ComparisonTest(
        esConnection, new DBConnection[] {otherDbConnection, otherDBConnection2}
    );

    DBResult esResult =
        new DBResult("ES", asList(new Type("firstname", "text")), asList(new Row(asList("John"))));
    DBResult otherResult =
        new DBResult("Other", asList(new Type("firstname", "text")), Collections.emptyList());

    when(esConnection.select(anyString())).thenReturn(esResult);
    when(otherDbConnection.select(anyString())).thenReturn(otherResult);
    when(otherDBConnection2.select(anyString()))
        .thenThrow(new RuntimeException("Unsupported feature"));

    TestReport expected = new TestReport();
    expected.addTestCase(new FailedTestCase(1, "SELECT * FROM accounts",
        asList(esResult, otherResult), "Unsupported feature;"));
    TestReport actual = correctnessTest.verify(querySet("SELECT * FROM accounts"));
    assertEquals(expected, actual);
  }

  private TestQuerySet querySet(String query) {
    return new TestQuerySet(new String[] {query});
  }

}
