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
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazon.opendistroforelasticsearch.sql.correctness.runner.connection.JDBCConnection;
import com.amazon.opendistroforelasticsearch.sql.correctness.runner.resultset.DBResult;
import com.amazon.opendistroforelasticsearch.sql.correctness.runner.resultset.Type;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.OngoingStubbing;

/**
 * Tests for {@link JDBCConnection}
 */
@RunWith(MockitoJUnitRunner.class)
public class JDBCConnectionTest {

  @Mock
  private Connection connection;

  @Mock
  private Statement statement;

  private JDBCConnection conn;

  @Before
  public void setUp() throws SQLException {
    conn = new JDBCConnection("Test DB", "jdbc:testdb://localhost:12345");
    conn.setConnection(connection);

    when(connection.createStatement()).thenReturn(statement);
  }

  @Test
  public void testCreateTable() throws SQLException {
    conn.create("test",
        "{\"mappings\":{\"properties\":{\"name\":{\"type\":\"keyword\"},\"age\":{\"type\":\"INT\"}}}}");

    ArgumentCaptor<String> argCap = ArgumentCaptor.forClass(String.class);
    verify(statement).executeUpdate(argCap.capture());
    String actual = argCap.getValue();

    assertEquals("CREATE TABLE test(name VARCHAR,age INT)", actual);
  }

  @Test
  public void testDropTable() throws SQLException {
    conn.drop("test");

    ArgumentCaptor<String> argCap = ArgumentCaptor.forClass(String.class);
    verify(statement).executeUpdate(argCap.capture());
    String actual = argCap.getValue();

    assertEquals("DROP TABLE test", actual);
  }

  @Test
  public void testInsertData() throws SQLException {
    conn.insert("test", new String[] {"name", "age"},
        Arrays.asList(new String[] {"John", "25"}, new String[] {"Hank", "30"}));

    ArgumentCaptor<String> argCap = ArgumentCaptor.forClass(String.class);
    verify(statement, times(2)).addBatch(argCap.capture());
    List<String> actual = argCap.getAllValues();

    assertEquals(
        Arrays.asList(
            "INSERT INTO test(name,age) VALUES ('John','25')",
            "INSERT INTO test(name,age) VALUES ('Hank','30')"
        ), actual
    );
  }

  @Test
  public void testSelectQuery() throws SQLException {
    ResultSetMetaData metaData = mockMetaData(ImmutableMap.of("name", "VARCHAR", "age", "INT"));
    ResultSet resultSet = mockResultSet(new Object[] {"John", 25}, new Object[] {"Hank", 30});
    when(statement.executeQuery(anyString())).thenReturn(resultSet);
    when(resultSet.getMetaData()).thenReturn(metaData);

    DBResult result = conn.select("SELECT * FROM test");
    assertEquals("Test DB", result.getDatabaseName());
    assertEquals(
        Arrays.asList(
            new Type("NAME", "VARCHAR"),
            new Type("AGE", "INT")
        ),
        result.getSchema()
    );
    assertEquals(
        HashMultiset.create(ImmutableList.of(
            Arrays.asList("John", 25),
            Arrays.asList("Hank", 30)
        )),
        result.getDataRows()
    );
  }

  @Test
  public void testSelectQueryWithAlias() throws SQLException {
    ResultSetMetaData metaData =
        mockMetaData(ImmutableMap.of("name", "VARCHAR", "age", "INT"), "n", "a");
    ResultSet resultSet = mockResultSet(new Object[] {"John", 25}, new Object[] {"Hank", 30});
    when(statement.executeQuery(anyString())).thenReturn(resultSet);
    when(resultSet.getMetaData()).thenReturn(metaData);

    DBResult result = conn.select("SELECT * FROM test");
    assertEquals(
        Arrays.asList(
            new Type("N", "VARCHAR"),
            new Type("A", "INT")
        ),
        result.getSchema()
    );
  }

  @Test
  public void testSelectQueryWithFloatInResultSet() throws SQLException {
    ResultSetMetaData metaData =
        mockMetaData(ImmutableMap.of("name", "VARCHAR", "balance", "FLOAT"));
    ResultSet resultSet = mockResultSet(
        new Object[] {"John", 25.123},
        new Object[] {"Hank", 30.456},
        new Object[] {"Allen", 15.1}
    );
    when(statement.executeQuery(anyString())).thenReturn(resultSet);
    when(resultSet.getMetaData()).thenReturn(metaData);

    DBResult result = conn.select("SELECT * FROM test");
    assertEquals(
        Arrays.asList(
            new Type("NAME", "VARCHAR"),
            new Type("BALANCE", "[FLOAT, DOUBLE, REAL]")
        ),
        result.getSchema()
    );
    assertEquals(
        HashMultiset.create(ImmutableList.of(
            Arrays.asList("John", 25.13),
            Arrays.asList("Hank", 30.46),
            Arrays.asList("Allen", 15.1)
        )),
        result.getDataRows()
    );
  }

  private ResultSet mockResultSet(Object[]... rows) throws SQLException {
    ResultSet resultSet = mock(ResultSet.class);
    OngoingStubbing<Boolean> next = when(resultSet.next());
    for (int i = 0; i < rows.length; i++) {
      next = next.thenReturn(true);
    }
    next.thenReturn(false);

    OngoingStubbing<Object> getObject = when(resultSet.getObject(anyInt()));
    for (Object[] row : rows) {
      for (Object val : row) {
        getObject = getObject.thenReturn(val);
      }
    }
    return resultSet;
  }

  private ResultSetMetaData mockMetaData(Map<String, String> nameAndTypes, String... aliases)
      throws SQLException {
    ResultSetMetaData metaData = mock(ResultSetMetaData.class);

    OngoingStubbing<String> getColumnName = when(metaData.getColumnName(anyInt()));
    for (String name : nameAndTypes.keySet()) {
      getColumnName = getColumnName.thenReturn(name);
    }

    OngoingStubbing<String> getColumnTypeName = when(metaData.getColumnTypeName(anyInt()));
    for (String value : nameAndTypes.values()) {
      getColumnTypeName = getColumnTypeName.thenReturn(value);
    }

    if (aliases.length > 0) {
      OngoingStubbing<String> getColumnLabel = when(metaData.getColumnLabel(anyInt()));
      for (String alias : aliases) {
        getColumnLabel = getColumnLabel.thenReturn(alias);
      }
    }

    when(metaData.getColumnCount()).thenReturn(nameAndTypes.size());
    return metaData;
  }

}
