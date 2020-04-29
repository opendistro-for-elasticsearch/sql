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

import com.google.common.collect.ImmutableSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Eliran on 27/12/2015.
 */
public class CSVResult {

    private static final Set<String> SENSITIVE_CHAR = ImmutableSet.of("=", "+", "-", "@");

    private final List<String> headers;
    private final List<String> lines;

    /**
     * Skip sanitizing if string line provided. This constructor is basically used by
     * assertion in test code.
     */
    public CSVResult(List<String> headers, List<String> lines) {
        this.headers = headers;
        this.lines = lines;
    }

    public CSVResult(String separator, List<String> headers, List<List<String>> lines) {
        this.headers = sanitizeHeaders(headers);
        this.lines = sanitizeLines(separator, lines);
    }

    /**
     * Return CSV header names which are sanitized because Elasticsearch allows
     * special character present in field name too.
     * @return  CSV header name list after sanitized
     */
    public List<String> getHeaders() {
        return headers;
    }

    /**
     * Return CSV lines in which each cell is sanitized to avoid CSV injection.
     * @return  CSV lines after sanitized
     */
    public List<String> getLines() {
        return lines;
    }

    private List<String> sanitizeHeaders(List<String> headers) {
        return headers.stream().
                       map(this::sanitizeCell).
                       collect(Collectors.toList());
    }

    private List<String> sanitizeLines(String separator, List<List<String>> lines) {
        List<String> result = new ArrayList<>();
        for (List<String> line : lines) {
            result.add(line.stream().
                            map(this::sanitizeCell).
                            collect(Collectors.joining(separator)));
        }
        return result;
    }

    private String sanitizeCell(String cell) {
        if (isStartWithSensitiveChar(cell)) {
            return "'" + cell;
        }
        return cell;
    }

    private boolean isStartWithSensitiveChar(String cell) {
        return SENSITIVE_CHAR.stream().
                              anyMatch(cell::startsWith);
    }

}
