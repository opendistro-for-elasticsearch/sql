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

package com.amazon.opendistroforelasticsearch.sql.protocol.response.format;

import com.amazon.opendistroforelasticsearch.sql.protocol.response.QueryResult;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CsvResponseFormatter implements ResponseFormatter<QueryResult> {
  private static final String INLINE_SEPARATOR = ",";
  private static final String INTERLINE_SEPARATOR = System.lineSeparator();
  private static final Set<String> SENSITIVE_CHAR = ImmutableSet.of("=", "+", "-", "@");

  private final boolean sanitize;

  public CsvResponseFormatter() {
    this.sanitize = true;
  }

  @Override
  public String format(QueryResult response) {
    CsvResult result = new CsvResult(response, sanitize);
    return result.getCsv();
  }

  @Override
  public String format(Throwable t) {
    return ErrorFormatter.prettyFormat(t);
  }


  /**
   * Sanitize methods are migrated from legacy CSV result.
   * To deal with special character in column name and to avoid csv injection.
   * Sanitize both headers and data lines by:
   *  1) First prepend single quote at the start if first char is sensitive (= - + @);
   *  2) Second double quote entire cell if any comma is found.
   */
  @Getter
  @RequiredArgsConstructor
  static class CsvResult {
    private final QueryResult response;
    private final boolean sanitize;

    public String getCsv() {
      List<String> headersAndData = new ArrayList<>();
      headersAndData.add(getHeaderLine(response, sanitize));
      headersAndData.addAll(getDataLines(response, sanitize));
      return String.join(INTERLINE_SEPARATOR, headersAndData);
    }

    private String getHeaderLine(QueryResult response, boolean sanitize) {
      List<String> headers = getHeaders(response, sanitize);
      return String.join(INLINE_SEPARATOR, headers);
    }

    private List<String> getDataLines(QueryResult response, boolean sanitize) {
      List<List<String>> data = getData(response, sanitize);
      return data.stream().map(v -> String.join(INLINE_SEPARATOR, v)).collect(Collectors.toList());
    }

    private List<String> getHeaders(QueryResult response, boolean sanitize) {
      ImmutableList.Builder<String> headers = ImmutableList.builder();
      response.columnNameTypes().forEach((column, type) -> headers.add(column));
      List<String> result = headers.build();
      return sanitize ? sanitizeHeaders(result) : result;
    }

    private List<List<String>> getData(QueryResult response, boolean sanitize) {
      ImmutableList.Builder<List<String>> dataLines = new ImmutableList.Builder<>();
      response.iterator().forEachRemaining(row -> {
        ImmutableList.Builder<String> line = new ImmutableList.Builder<>();
        // replace null values with empty string
        Arrays.asList(row).forEach(val -> line.add(val == null ? "" : val.toString()));
        dataLines.add(line.build());
      });
      List<List<String>> result = dataLines.build();
      return sanitize ? sanitizeData(result) : result;
    }

    /**
     * Sanitize CSV headers because Elasticsearch allows special character present in field names.
     */
    private List<String> sanitizeHeaders(List<String> headers) {
      return headers.stream()
          .map(this::sanitizeCell)
          .map(cell -> quoteIfRequired(INLINE_SEPARATOR, cell))
          .collect(Collectors.toList());
    }

    /**
     * Sanitize CSV lines in which each cell is sanitized to avoid CSV injection.
     */
    private List<List<String>> sanitizeData(List<List<String>> lines) {
      List<List<String>> result = new ArrayList<>();
      for (List<String> line : lines) {
        result.add(line.stream()
            .map(this::sanitizeCell)
            .map(cell -> quoteIfRequired(INLINE_SEPARATOR, cell))
            .collect(Collectors.toList()));
      }
      return result;
    }

    private String sanitizeCell(String cell) {
      if (isStartWithSensitiveChar(cell)) {
        return "'" + cell;
      }
      return cell;
    }

    private String quoteIfRequired(String separator, String cell) {
      final String quote = "\"";
      return cell.contains(separator)
          ? quote + cell.replaceAll("\"", "\"\"") + quote : cell;
    }

    private boolean isStartWithSensitiveChar(String cell) {
      return SENSITIVE_CHAR.stream().anyMatch(cell::startsWith);
    }
  }

}
