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

import com.amazon.opendistroforelasticsearch.jdbc.internal.results.ColumnMetaData;
import com.amazon.opendistroforelasticsearch.jdbc.internal.results.Cursor;
import com.amazon.opendistroforelasticsearch.jdbc.internal.results.Row;
import com.amazon.opendistroforelasticsearch.jdbc.internal.results.Schema;
import com.amazon.opendistroforelasticsearch.jdbc.protocol.http.JsonQueryResponse;
import com.amazon.opendistroforelasticsearch.jdbc.types.ElasticsearchType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CursorTests {

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 7, 10, 11})
    void testCursorNext(final int rowCount) {
        Schema schema = new Schema(Arrays.asList(
                toColumnMetaData("rownum", ElasticsearchType.INTEGER.getTypeName())));

        List<Row> rows = new ArrayList<>();
        for (int rowNum = 1; rowNum <= rowCount; rowNum++) {
            rows.add(toRow(rowNum));
        }

        Cursor cursor = new Cursor(schema, rows);
        int cursorRowCount = 0;

        while (cursor.next()) {
            cursorRowCount++;
            assertEquals(1, cursor.getColumnCount(), "Unexpected column count. Row number: " + cursorRowCount);
            assertEquals(cursorRowCount, cursor.getColumn(0), "Unexpected row number indicator");
        }

        assertEquals(rowCount, cursorRowCount, "Unexpected number of rows retrieved from cursor.");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 7, 10, 11})
    void testCursorNextWithMultipleColumns(final int rowCount) {

        final String STRINGVAL = "string";
        final long LONGVAL = 12345678901234567L;
        final double DOUBLEVAL = 100.25;

        final List<ColumnMetaData> columnMetaDatas = Arrays.asList(
                toColumnMetaData("rownum", ElasticsearchType.INTEGER.getTypeName()),
                toColumnMetaData("stringval", ElasticsearchType.TEXT.getTypeName()),
                toColumnMetaData("longval", ElasticsearchType.LONG.getTypeName()),
                toColumnMetaData("doubleval", ElasticsearchType.DOUBLE.getTypeName())
        );

        Schema schema = new Schema(columnMetaDatas);

        List<Row> rows = new ArrayList<>();
        for (int rowNum = 1; rowNum <= rowCount; rowNum++) {
            rows.add(toRow(rowNum, STRINGVAL, LONGVAL, DOUBLEVAL));
        }

        Cursor cursor = new Cursor(schema, rows);
        int cursorRowCount = 0;

        assertEquals(Integer.valueOf(0), cursor.findColumn("rownum"), "Mismatch in locating column 'rownum'");
        assertEquals(Integer.valueOf(1), cursor.findColumn("stringval"), "Mismatch in locating column 'stringval'");
        assertEquals(Integer.valueOf(2), cursor.findColumn("longval"), "Mismatch in locating column 'longval'");
        assertEquals(Integer.valueOf(3), cursor.findColumn("doubleval"), "Mismatch in locating column 'doubleval'");
        assertNull(cursor.findColumn("unknown"), "Valid index for unknown column label");

        while (cursor.next()) {
            cursorRowCount++;
            assertThrows(IllegalArgumentException.class, () -> cursor.getColumn(-1));
            assertEquals(columnMetaDatas.size(), cursor.getColumnCount(), "Unexpected column count. Row number: " + cursorRowCount);
            assertEquals(cursorRowCount, cursor.getColumn(0), "Unexpected row number indicator");
            assertEquals(STRINGVAL, cursor.getColumn(1), "Unexpected column value. Row number: " + cursorRowCount);
            assertEquals(LONGVAL, cursor.getColumn(2), "Unexpected column value. Row number: " + cursorRowCount);
            assertEquals(DOUBLEVAL, cursor.getColumn(3), "Unexpected column value. Row number: " + cursorRowCount);
            assertThrows(IllegalArgumentException.class, () -> cursor.getColumn(4));
        }

        assertEquals(rowCount, cursorRowCount, "Unexpected number of rows retrieved from cursor.");
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 7, 10, 11})
    void testCursorFindColumn(final int rowCount) {
        final String STRINGVAL = "string";
        final long LONGVAL = 12345678901234567L;
        final double DOUBLEVAL = 100.25;

        final List<ColumnMetaData> columnMetaDatas = Arrays.asList(
                toColumnMetaData("rownum", ElasticsearchType.INTEGER.getTypeName()),
                toColumnMetaData("stringval", ElasticsearchType.TEXT.getTypeName(), "stringlabel"),
                toColumnMetaData("longval", ElasticsearchType.LONG.getTypeName()),
                toColumnMetaData("doubleval", ElasticsearchType.DOUBLE.getTypeName(), "doubleLabel")
        );

        Schema schema = new Schema(columnMetaDatas);

        List<Row> rows = new ArrayList<>();
        for (int rowNum = 1; rowNum <= rowCount; rowNum++) {
            rows.add(toRow(rowNum, STRINGVAL, LONGVAL, DOUBLEVAL));
        }

        Cursor cursor = new Cursor(schema, rows);
        int cursorRowCount = 0;

        assertEquals(Integer.valueOf(0), cursor.findColumn("rownum"), "Mismatch in locating column 'rownum'");
        assertNull(cursor.findColumn("stringval"), "column lookup succeeded by name - doubleval");
        assertEquals(Integer.valueOf(1), cursor.findColumn("stringlabel"), "Mismatch in locating column 'stringlabel'");
        assertEquals(Integer.valueOf(2), cursor.findColumn("longval"), "Mismatch in locating column 'longval'");
        assertEquals(Integer.valueOf(3), cursor.findColumn("doubleLabel"), "Mismatch in locating column 'doubleLabel'");
        assertNull(cursor.findColumn("doubleval"), "column lookup succeeded by name - doubleval");
        assertNull(cursor.findColumn("unknown"), "Valid index for unknown column label");

        while (cursor.next()) {
            cursorRowCount++;
            assertThrows(IllegalArgumentException.class, () -> cursor.getColumn(-1));
            assertEquals(columnMetaDatas.size(), cursor.getColumnCount(), "Unexpected column count. Row number: " + cursorRowCount);
            assertEquals(cursorRowCount, columnObject(cursor, "rownum"), "Unexpected row number indicator");
            assertEquals(STRINGVAL, columnObject(cursor, "stringlabel"), "Unexpected column value. Row number: " + cursorRowCount);
            assertEquals(LONGVAL, columnObject(cursor, "longval"), "Unexpected column value. Row number: " + cursorRowCount);
            assertEquals(DOUBLEVAL, columnObject(cursor, "doubleLabel"), "Unexpected column value. Row number: " + cursorRowCount);
            assertThrows(IllegalArgumentException.class, () -> cursor.getColumn(4));
        }

        assertEquals(rowCount, cursorRowCount, "Unexpected number of rows retrieved from cursor.");
    }

    private Object columnObject(Cursor cursor, String columnLabel) {
        return cursor.getColumn(cursor.findColumn(columnLabel));
    }

    private Row toRow(Object... values) {
        return new Row(Arrays.asList(values));
    }

    private ColumnMetaData toColumnMetaData(String name, String type) {
        return toColumnMetaData(name, type, null);

    }

    private ColumnMetaData toColumnMetaData(String name, String type, String label) {
        return new ColumnMetaData(new JsonQueryResponse.SchemaEntry(name, type, label));
    }
}
