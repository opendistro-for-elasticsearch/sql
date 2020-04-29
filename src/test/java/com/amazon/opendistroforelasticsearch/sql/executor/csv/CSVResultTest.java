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

package com.amazon.opendistroforelasticsearch.sql.executor.csv;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link CSVResult}
 */
public class CSVResultTest {

    private static final String SEPARATOR = ",";

    @Test
    public void getHeadersShouldReturnHeadersSanitized() {
        CSVResult csv = csv(headers("name", "=age"), lines(line("John", "30")));
        assertEquals(
            headers("name", "'=age"),
            csv.getHeaders()
        );
    }

    @Test
    public void getLinesShouldReturnLinesSanitized() {
        CSVResult csv = csv(
            headers("name", "city"),
            lines(
                line("John", "Seattle"),
                line("John", "=Seattle"),
                line("John", "+Seattle"),
                line("-John", "Seattle"),
                line("@John", "Seattle"),
                line("John", "Seattle=")
            )
        );

        assertEquals(
            line(
                "John,Seattle",
                "John,'=Seattle",
                "John,'+Seattle",
                "'-John,Seattle",
                "'@John,Seattle",
                "John,Seattle="
            ),
            csv.getLines()
        );
    }

    private CSVResult csv(List<String> headers, List<List<String>> lines) {
        return new CSVResult(SEPARATOR, headers, lines);
    }

    private List<String> headers(String... headers) {
        return Arrays.stream(headers).collect(Collectors.toList());
    }

    private List<String> line(String... line) {
        return Arrays.stream(line).collect(Collectors.toList());
    }

    @SafeVarargs
    private final List<List<String>> lines(List<String>... lines) {
        return Arrays.stream(lines).collect(Collectors.toList());
    }

}