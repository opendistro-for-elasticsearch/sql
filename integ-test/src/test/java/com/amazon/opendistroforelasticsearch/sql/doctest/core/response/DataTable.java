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

package com.amazon.opendistroforelasticsearch.sql.doctest.core.response;

import com.amazon.opendistroforelasticsearch.sql.utils.StringUtils;
import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Data table that represent rows of data with a header.
 * For now the format is actually in ReST and may need to decouple later.
 */
public class DataTable {

    private final int[] maxWidths;
    private final Object[] header;
    private final List<Object[]> rows;

    public DataTable(Object[] header) {
        this.maxWidths = new int[header.length];
        this.header = header;
        this.rows = new ArrayList<>();
        updateMaxWidthForEachColumn(header);
    }

    public void addRow(Object[] row) {
        rows.add(row);
        updateMaxWidthForEachColumn(row);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        String format = format();
        String separateLine1 = separateLine("-");
        String separateLine2 = separateLine("=");

        str.append(separateLine1).
            append('\n').
            append(StringUtils.format(format, header)).
            append('\n').
            append(separateLine2).
            append('\n');

        for (Object[] row : rows) {
            str.append(StringUtils.format(format, row)).
                append('\n').
                append(separateLine("-")).
                append('\n');
        }
        return str.toString();
    }

    private void updateMaxWidthForEachColumn(Object[] row) {
        for (int i = 0; i < row.length; i++) {
            maxWidths[i] = Math.max(maxWidths[i], String.valueOf(row[i]).length());
        }
    }

    private String separateLine(String separator) {
        return Arrays.stream(maxWidths).
                      mapToObj(width -> Strings.repeat(separator, width)).
                      collect(Collectors.joining("+", "+", "+"));
    }

    /**
     * Format as Java String.format needs to make use of auto pad feature.
     * For example, to ensure width of 10 and pad spaces, we need to String.format("%10s", str);
     */
    private String format() {
        return Arrays.stream(maxWidths).
                      mapToObj(width -> "%" + width + "s").
                      collect(Collectors.joining("|", "|", "|"));
    }

}
