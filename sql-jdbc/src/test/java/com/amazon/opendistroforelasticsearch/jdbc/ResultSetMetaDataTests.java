/*
 * Copyright <2019> Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */
package com.amazon.opendistroforelasticsearch.jdbc;

import com.amazon.opendistroforelasticsearch.jdbc.DatabaseMetaDataImpl.ResultSetColumnDescriptor;
import com.amazon.opendistroforelasticsearch.jdbc.internal.results.ColumnMetaData;
import com.amazon.opendistroforelasticsearch.jdbc.internal.results.Schema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link ResultSetMetaDataImpl}
 */
public class ResultSetMetaDataTests {

    private ResultSetMetaDataImpl metaData;

    @BeforeEach
    public void setUp() {
        ResultSetImpl resultSet = mock(ResultSetImpl.class);
        Schema schema = new Schema(Arrays.asList(
            new ColumnMetaData(new ResultSetColumnDescriptor("name", "keyword", null)),
            new ColumnMetaData(new ResultSetColumnDescriptor("address", "text", null)),
            new ColumnMetaData(new ResultSetColumnDescriptor("age", "long", null)),
            new ColumnMetaData(new ResultSetColumnDescriptor("balance", "float", null)),
            new ColumnMetaData(new ResultSetColumnDescriptor("employer", "nested", null)),
            new ColumnMetaData(new ResultSetColumnDescriptor("birthday", "date", null))
        ));
        metaData = new ResultSetMetaDataImpl(resultSet, schema);
    }

    @Test
    public void getColumnTypeNameShouldReturnJDBCType() throws SQLException {
        assertEquals("VARCHAR", metaData.getColumnTypeName(1));
        assertEquals("VARCHAR", metaData.getColumnTypeName(2));
        assertEquals("BIGINT", metaData.getColumnTypeName(3));
        assertEquals("REAL", metaData.getColumnTypeName(4));
        assertEquals("STRUCT", metaData.getColumnTypeName(5));
        assertEquals("TIMESTAMP", metaData.getColumnTypeName(6));
    }

}
